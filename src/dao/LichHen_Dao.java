package dao;

import connection.ConnectDB;
import entity.KhachHang;
import entity.LichHen;
import entity.TrangThaiLichHen;
import entity.Xe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Lịch hẹn của khách: tạo, xem theo khách, danh sách hôm nay và hủy. Tự đóng lịch khi lập phiếu nằm ở taoTiepNhan. */
public class LichHen_Dao {
    public static final String THOI_GIAN_QUA_KHU = "Thời gian hẹn không được ở quá khứ.";
    /** 0 giờ hôm nay theo đồng hồ máy chủ CSDL, cùng nguồn giờ với cột GETDATE() của các bảng khác. */
    static final String DAU_NGAY = "CAST(CAST(GETDATE() AS DATE) AS DATETIME)";
    private static final String COT_LICH_HEN = "l.maLichHen, l.thoiGianHen, l.ghiChu, l.trangThai, x.bienSo, x.hangXe, x.dongXe";

    /** Gán mã cho lh. Ném IllegalArgumentException khi thiếu khách, thiếu giờ hoặc giờ hẹn đã qua (tính theo phút). */
    public void insert(LichHen lh) throws SQLException {
        if (lh.getKhachHang() == null || lh.getThoiGianHen() == null) {
            throw new IllegalArgumentException("Lịch hẹn cần có khách hàng và thời gian hẹn.");
        }
        if (lh.getThoiGianHen().isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
            throw new IllegalArgumentException(THOI_GIAN_QUA_KHU);
        }
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String ma = nextId(connection);
                String sql = "INSERT INTO LichHen (maLichHen, thoiGianHen, ghiChu, trangThai, maKH, bienSo) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, ma);
                    statement.setTimestamp(2, Timestamp.valueOf(lh.getThoiGianHen()));
                    statement.setString(3, lh.getGhiChu().isEmpty() ? null : lh.getGhiChu());
                    statement.setString(4, lh.getTrangThai().getText());
                    statement.setString(5, lh.getKhachHang().getMaKH());
                    if (lh.getXe() == null) {
                        statement.setNull(6, Types.VARCHAR);
                    } else {
                        statement.setString(6, lh.getXe().getBienSo());
                    }
                    statement.executeUpdate();
                }
                connection.commit();
                lh.setMaLichHen(ma);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Mọi lịch hẹn của khách, mới nhất trước. Khách chỉ có mã. */
    public List<LichHen> findByKhachHang(String maKH) throws SQLException {
        String sql = "SELECT " + COT_LICH_HEN + " FROM LichHen l LEFT JOIN Xe x ON x.bienSo = l.bienSo "
                + "WHERE l.maKH = ? ORDER BY l.thoiGianHen DESC, l.maLichHen DESC";
        List<LichHen> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKH);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(docLichHen(rs, new KhachHang(maKH)));
                }
            }
        }
        return ketQua;
    }

    /** Lịch Đang hẹn trong ngày hôm nay, giờ sớm trước; khách và xe lấy bằng JOIN. */
    public List<LichHen> findHomNay() throws SQLException {
        String sql = "SELECT " + COT_LICH_HEN + ", " + KhachHang_Dao.COT_KHACH_HANG
                + " FROM LichHen l JOIN KhachHang k ON k.maKH = l.maKH LEFT JOIN Xe x ON x.bienSo = l.bienSo "
                + "WHERE l.trangThai = N'Đang hẹn' AND l.thoiGianHen >= " + DAU_NGAY
                + " AND l.thoiGianHen < DATEADD(DAY, 1, " + DAU_NGAY + ") ORDER BY l.thoiGianHen, l.maLichHen";
        List<LichHen> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ketQua.add(docLichHen(rs, KhachHang_Dao.docKhachHang(rs)));
            }
        }
        return ketQua;
    }

    /** Chỉ hủy được lịch đang ở Đang hẹn; trả về false khi lịch không còn ở trạng thái đó hoặc không tồn tại. */
    public boolean huy(String maLichHen) throws SQLException {
        String sql = "UPDATE LichHen SET trangThai = N'Đã hủy' WHERE maLichHen = ? AND trangThai = N'Đang hẹn'";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maLichHen);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Gọi trong transaction lập phiếu: lịch Đang hẹn của xe này, hoặc của chủ xe mà chưa gắn xe, nếu đã tới hạn
     * (hôm nay hoặc trước đó) chuyển sang Đã đến. Lịch những ngày sau để nguyên.
     */
    static int danhDauDaDen(Connection connection, String bienSo) throws SQLException {
        String sql = "UPDATE LichHen SET trangThai = N'Đã đến' WHERE trangThai = N'Đang hẹn' "
                + "AND thoiGianHen < DATEADD(DAY, 1, " + DAU_NGAY + ") "
                + "AND (bienSo = ? OR (bienSo IS NULL AND maKH = (SELECT maKH FROM Xe WHERE bienSo = ?)))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bienSo);
            statement.setString(2, bienSo);
            return statement.executeUpdate();
        }
    }

    private LichHen docLichHen(ResultSet rs, KhachHang khachHang) throws SQLException {
        LichHen lh = new LichHen(rs.getString("maLichHen"));
        lh.setKhachHang(khachHang);
        String bienSo = rs.getString("bienSo");
        if (bienSo != null) {
            Xe xe = new Xe(bienSo);
            xe.setHangXe(rs.getString("hangXe"));
            xe.setDongXe(rs.getString("dongXe"));
            lh.setXe(xe);
        }
        lh.setThoiGianHen(rs.getTimestamp("thoiGianHen").toLocalDateTime());
        lh.setGhiChu(rs.getString("ghiChu"));
        lh.setTrangThai(TrangThaiLichHen.fromDb(rs.getString("trangThai")));
        return lh;
    }

    private String nextId(Connection connection) throws SQLException {
        String sql = "SELECT 'LH' + RIGHT('00000' + CAST(COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maLichHen, 3, 18))), 0) + 1 "
                + "AS VARCHAR(18)), 5) FROM LichHen WITH (UPDLOCK, HOLDLOCK)";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }
}
