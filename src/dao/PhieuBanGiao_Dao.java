package dao;

import connection.ConnectDB;
import entity.HoaDon;
import entity.NhanVien;
import entity.PhieuBanGiao;
import entity.PhieuTiepNhanVaSuaChua;
import entity.Thue;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn danh sách bàn giao, kiểm tra điều kiện và ghi nhận bàn giao trong transaction. */
public class PhieuBanGiao_Dao {

    private static final String LIST_SQL = "SELECT p.maPhieuTNSC, " + PhieuTiepNhanVaSuaChua_Dao.COT_XE + ", "
            + "COALESCE(ti.leHoanTat, 0) AS tienDo, ti.soHangMuc, ti.soHangMucXong, "
            + "COALESCE(h.trangThaiThanhToan, N'Chưa lập hóa đơn') AS thanhToan, "
            + "COALESCE(h.tongThanhToan, 0) AS tong, COALESCE(h.maHD, '') AS maHD, "
            + "COALESCE(h.tongTienCong, 0) AS tienCong, "
            + "COALESCE(h.tongTienPhuTung, 0) AS tienPhuTung, "
            + "COALESCE(h.tongTienGiam, 0) AS giam, COALESCE(h.tongTienVAT, 0) AS vat, "
            + "COALESCE(th.thueSuat, 0) AS thueSuat, p.trangThai, "
            + "COALESCE(bg.maPhieuBG, '') AS maPhieuBG, bg.ngayBanGiao, "
            + "bg.maNV_BanGiao, nv.hoTen AS tenNVBanGiao "
            + "FROM PhieuTiepNhanVaSuaChua p "
            + "JOIN Xe x ON x.bienSo = p.bienSo "
            + "JOIN KhachHang k ON k.maKH = p.maKH "
            + "LEFT JOIN HoaDon h ON h.maPhieuTNSC = p.maPhieuTNSC "
            + "AND h.trangThaiThanhToan <> N'Đã hủy' "
            + "LEFT JOIN Thue th ON th.maThue = h.maThue "
            + "LEFT JOIN PhieuBanGiao bg ON bg.maPhieuTNSC = p.maPhieuTNSC "
            + "LEFT JOIN NhanVien nv ON nv.maNV = bg.maNV_BanGiao "
            + "OUTER APPLY (SELECT CASE WHEN COUNT(*) = 0 THEN 0 "
            + "ELSE CONVERT(INT, FLOOR(100.0 * SUM(CASE "
            + "WHEN d.trangThaiTienDo = N'Hoàn thành' THEN 1 ELSE 0 END) / COUNT(*))) "
            + "END AS leHoanTat, COUNT(*) AS soHangMuc, "
            + "COALESCE(SUM(CASE WHEN d.trangThaiTienDo = N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS soHangMucXong "
            + "FROM ChiTietSuaChua_DichVu d "
            + "WHERE d.maPhieuTNSC = p.maPhieuTNSC) ti";

    public PhieuBanGiao findById(String maPhieuBG) throws SQLException {
        String sql = "SELECT maPhieuBG, maPhieuTNSC, maNV_BanGiao, ngayBanGiao, "
                + "xacNhanHoanTat, xacNhanThanhToan, chuKyKhachHang "
                + "FROM PhieuBanGiao WHERE maPhieuBG = ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuBG);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPhieuBanGiao(result) : null;
            }
        }
    }

    public PhieuBanGiao findByMaPhieuSuaChua(String maPhieuTNSC) throws SQLException {
        String sql = "SELECT maPhieuBG, maPhieuTNSC, maNV_BanGiao, ngayBanGiao, "
                + "xacNhanHoanTat, xacNhanThanhToan, chuKyKhachHang "
                + "FROM PhieuBanGiao WHERE maPhieuTNSC = ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuTNSC);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPhieuBanGiao(result) : null;
            }
        }
    }

    public List<PhieuTiepNhanVaSuaChua> findDanhSach(String tuKhoa, String loc) throws SQLException {
        String sql = LIST_SQL
                + " WHERE p.trangThai <> N'Đã hủy' "
                + "AND (? = '' OR p.maPhieuTNSC LIKE ? OR x.bienSo LIKE ? OR k.hoTen LIKE ?)"
                + " AND (? = N'Tất cả'"
                + " OR (? = N'Chờ bàn giao' AND NOT EXISTS (SELECT 1 FROM PhieuBanGiao bg"
                + " WHERE bg.maPhieuTNSC = p.maPhieuTNSC))"
                + " OR (? = N'Đã bàn giao' AND EXISTS (SELECT 1 FROM PhieuBanGiao bg"
                + " WHERE bg.maPhieuTNSC = p.maPhieuTNSC)))"
                + " ORDER BY p.thoiGianTiepNhan DESC";
        String query = tuKhoa == null ? "" : tuKhoa.trim();
        String filter = loc == null ? "Tất cả" : loc;
        List<PhieuTiepNhanVaSuaChua> rows = new ArrayList<>();

        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, query);
            statement.setString(2, "%" + query + "%");
            statement.setString(3, "%" + query + "%");
            statement.setString(4, "%" + query + "%");
            statement.setString(5, filter);
            statement.setString(6, filter);
            statement.setString(7, filter);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(docPhieu(result));
                }
            }
        }
        return rows;
    }

    public boolean xacNhan(String maPhieu, String maNhanVien, String chuKy) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String checkSql = "SELECT p.maPhieuTNSC, COUNT(d.maDichVu) AS tongDV, "
                        + "SUM(CASE WHEN d.trangThaiTienDo = N'Hoàn thành' THEN 1 ELSE 0 END) AS xong, "
                        + "MAX(CASE WHEN h.trangThaiThanhToan = N'Đã thanh toán' "
                        + "OR (h.trangThaiThanhToan = N'Công nợ' AND k.duocGhiNo = 1) "
                        + "THEN 1 ELSE 0 END) AS daXuLy, "
                        + "MAX(CASE WHEN bg.maPhieuBG IS NULL THEN 0 ELSE 1 END) AS daBanGiao "
                        + "FROM PhieuTiepNhanVaSuaChua p "
                        + "JOIN Xe x ON x.bienSo = p.bienSo "
                        + "JOIN KhachHang k ON k.maKH = p.maKH "
                        + "LEFT JOIN ChiTietSuaChua_DichVu d ON d.maPhieuTNSC = p.maPhieuTNSC "
                        + "LEFT JOIN HoaDon h ON h.maPhieuTNSC = p.maPhieuTNSC "
                        + "AND h.trangThaiThanhToan <> N'Đã hủy' "
                        + "LEFT JOIN PhieuBanGiao bg ON bg.maPhieuTNSC = p.maPhieuTNSC "
                        + "WHERE p.maPhieuTNSC = ? AND p.trangThai <> N'Đã hủy' "
                        + "GROUP BY p.maPhieuTNSC";
                try (PreparedStatement statement = connection.prepareStatement(checkSql)) {
                    statement.setString(1, maPhieu);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()
                                || result.getInt("tongDV") == 0
                                || result.getInt("xong") != result.getInt("tongDV")
                                || result.getInt("daXuLy") != 1
                                || result.getInt("daBanGiao") != 0) {
                            throw new SQLException(
                                    "Phiếu chưa hoàn tất, chưa thanh toán hợp lệ hoặc đã được bàn giao.");
                        }
                    }
                }

                String idSql = "SELECT 'PBG' + RIGHT('00000' + CAST(COALESCE(MAX("
                        + "TRY_CONVERT(INT, SUBSTRING(maPhieuBG, 4, 18))), 0) + 1 AS VARCHAR(18)), 5) "
                        + "FROM PhieuBanGiao";
                String id;
                try (PreparedStatement statement = connection.prepareStatement(idSql);
                     ResultSet result = statement.executeQuery()) {
                    result.next();
                    id = result.getString(1);
                }

                String insertSql = "INSERT INTO PhieuBanGiao(maPhieuBG, maPhieuTNSC, "
                        + "maNV_BanGiao, ngayBanGiao, xacNhanHoanTat, xacNhanThanhToan, chuKyKhachHang) "
                        + "VALUES(?, ?, ?, GETDATE(), 1, 1, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setString(1, id);
                    statement.setString(2, maPhieu);
                    statement.setString(3, maNhanVien);
                    statement.setString(4, chuKy);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public PhieuTiepNhanVaSuaChua findChiTiet(String maPhieu) throws SQLException {
        String sql = LIST_SQL + " WHERE p.maPhieuTNSC = ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? docPhieu(result) : null;
            }
        }
    }

    public List<PhieuTiepNhanVaSuaChua> findDashboardJobs() throws SQLException {
        String sql = LIST_SQL.replaceFirst("SELECT ", "SELECT TOP 7 ")
                + " WHERE p.trangThai <> N'Đã hủy' AND bg.maPhieuBG IS NULL "
                + "ORDER BY p.thoiGianTiepNhan DESC";
        List<PhieuTiepNhanVaSuaChua> rows = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(docPhieu(result));
            }
        }
        return rows;
    }

    /** Thống kê toàn gara; tách khỏi 7 dòng gần nhất đang hiển thị ở dashboard. */
    public int[] findDashboardStats() throws SQLException {
        String sql = "SELECT SUM(CASE WHEN trangThai = N'Chờ kiểm tra' THEN 1 ELSE 0 END) AS choTiepNhan, "
                + "SUM(CASE WHEN trangThai <> N'Chờ kiểm tra' AND tienDo < 100 THEN 1 ELSE 0 END) AS dangSua, "
                + "SUM(CASE WHEN tienDo = 100 AND (thanhToan = N'Đã thanh toán' "
                + "OR (thanhToan = N'Công nợ' AND duocGhiNo = 1)) THEN 1 ELSE 0 END) AS choBanGiao "
                + "FROM (" + LIST_SQL + ") q WHERE maPhieuBG = '' "
                + "AND trangThai <> N'Đã hủy'";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new int[]{result.getInt("choTiepNhan"), result.getInt("dangSua"),
                    result.getInt("choBanGiao"), countDeliveredToday()};
        }
    }

    public int countDeliveredToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuBanGiao "
                + "WHERE CAST(ngayBanGiao AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    /** Một dòng của LIST_SQL thành phiếu kèm xe, chủ xe, hóa đơn còn hiệu lực và phiếu bàn giao. */
    private static PhieuTiepNhanVaSuaChua docPhieu(ResultSet result) throws SQLException {
        PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(result.getString("maPhieuTNSC"));
        phieu.setXe(PhieuTiepNhanVaSuaChua_Dao.docXe(result));
        phieu.setTrangThai(TrangThaiPhieu.fromDb(result.getString("trangThai")));
        phieu.setTongSoHangMuc(result.getInt("soHangMuc"));
        phieu.setSoHangMucHoanThanh(result.getInt("soHangMucXong"));
        String maHD = result.getString("maHD");
        if (!maHD.isEmpty()) {
            Thue thue = new Thue();
            thue.setThueSuat(result.getDouble("thueSuat"));
            phieu.setHoaDon(new HoaDon(maHD, TrangThaiHoaDon.fromDb(result.getString("thanhToan")),
                    result.getDouble("tienCong"), result.getDouble("tienPhuTung"), result.getDouble("giam"),
                    result.getDouble("vat"), result.getDouble("tong"), thue));
        }
        String maBanGiao = result.getString("maPhieuBG");
        if (!maBanGiao.isEmpty()) {
            PhieuBanGiao banGiao = new PhieuBanGiao(maBanGiao);
            Timestamp ngay = result.getTimestamp("ngayBanGiao");
            banGiao.setNgayBanGiao(ngay == null ? null : ngay.toLocalDateTime());
            NhanVien nhanVien = new NhanVien(result.getString("maNV_BanGiao"));
            nhanVien.setHoTen(result.getString("tenNVBanGiao"));
            banGiao.setNhanVienBanGiao(nhanVien);
            phieu.setBanGiao(banGiao);
        }
        return phieu;
    }

    private PhieuBanGiao mapPhieuBanGiao(ResultSet result) throws SQLException {
        Timestamp time = result.getTimestamp("ngayBanGiao");
        return new PhieuBanGiao(
                result.getString("maPhieuBG"),
                new PhieuTiepNhanVaSuaChua(result.getString("maPhieuTNSC")),
                new NhanVien(result.getString("maNV_BanGiao")),
                time == null ? null : time.toLocalDateTime(),
                result.getBoolean("xacNhanHoanTat"),
                result.getBoolean("xacNhanThanhToan"),
                result.getString("chuKyKhachHang"));
    }
}
