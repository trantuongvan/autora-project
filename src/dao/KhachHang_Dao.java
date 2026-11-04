package dao;

import connection.ConnectDB;
import entity.KhachHang;
import entity.LichHen;
import entity.LoaiKhachHang;
import entity.Xe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** CRUD khách hàng, tìm kiếm theo tên/SĐT/biển số; xe liên kết và lịch sử sửa chữa chỉ đọc. */
public class KhachHang_Dao {
    public static final String TRUNG_SO_DIEN_THOAI = "Số điện thoại này đã thuộc về một khách hàng khác.";
    private static final int LOI_VI_PHAM_KHOA_NGOAI = 547;
    /** Các cột khách hàng (bảng đặt bí danh k) mà docKhachHang đọc theo tên. */
    static final String COT_KHACH_HANG =
            "k.maKH, k.hoTen, k.soDienThoai, k.diaChi, k.email, k.maSoThue, k.loaiKhachHang, k.duocGhiNo";

    static KhachHang docKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang(rs.getString("maKH"));
        kh.setHoTen(rs.getString("hoTen"));
        kh.setSoDienThoai(rs.getString("soDienThoai"));
        String diaChi = rs.getString("diaChi");
        // Cột diaChi cho phép NULL còn setDiaChi thì không.
        if (diaChi != null && !diaChi.trim().isEmpty()) {
            kh.setDiaChi(diaChi);
        }
        kh.setEmail(rs.getString("email"));
        kh.setMaSoThue(rs.getString("maSoThue"));
        kh.setLoaiKhachHang(LoaiKhachHang.fromDb(rs.getString("loaiKhachHang")));
        kh.setDuocGhiNo(rs.getBoolean("duocGhiNo"));
        return kh;
    }

    /**
     * duocGhiNo null là không lọc. getDanhSachXe() của mỗi khách chứa mọi xe của khách, chỉ có biển số.
     */
    public List<KhachHang> search(String tuKhoa, LoaiKhachHang loaiKhachHang, Boolean duocGhiNo) throws SQLException {
        return search(tuKhoa, loaiKhachHang, duocGhiNo, null);
    }

    /**
     * coLichHen null là không lọc; true chỉ lấy khách có lịch Đang hẹn từ hôm nay trở đi, false là khách không có.
     * getLichHenGanNhat() của mỗi khách là lịch Đang hẹn gần nhất đó (chỉ có thời gian), null nếu không có.
     */
    public List<KhachHang> search(String tuKhoa, LoaiKhachHang loaiKhachHang, Boolean duocGhiNo, Boolean coLichHen)
            throws SQLException {
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        String loai = loaiKhachHang == null ? "" : loaiKhachHang.getText();
        String sql = "SELECT " + COT_KHACH_HANG + ", xe.bienSo, lh.gan "
                + "FROM KhachHang k LEFT JOIN Xe xe ON xe.maKH = k.maKH "
                + "LEFT JOIN (SELECT maKH, MIN(thoiGianHen) AS gan FROM LichHen WHERE trangThai = N'Đang hẹn' "
                + "AND thoiGianHen >= " + LichHen_Dao.DAU_NGAY + " GROUP BY maKH) lh ON lh.maKH = k.maKH "
                + "WHERE (? = N'' OR k.hoTen LIKE ? OR k.soDienThoai LIKE ? "
                + "OR EXISTS (SELECT 1 FROM Xe x WHERE x.maKH = k.maKH AND x.bienSo LIKE ?)) "
                + "AND (? = N'' OR k.loaiKhachHang = ?) AND (? = -1 OR k.duocGhiNo = ?) "
                + "AND (? = -1 OR (? = 1 AND lh.gan IS NOT NULL) OR (? = 0 AND lh.gan IS NULL)) "
                + "ORDER BY k.maKH, xe.bienSo";
        int ghiNo = duocGhiNo == null ? -1 : duocGhiNo ? 1 : 0;
        int lich = coLichHen == null ? -1 : coLichHen ? 1 : 0;
        List<KhachHang> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, "%" + key + "%");
            statement.setString(3, "%" + key + "%");
            statement.setString(4, "%" + key + "%");
            statement.setString(5, loai);
            statement.setString(6, loai);
            statement.setInt(7, ghiNo);
            statement.setInt(8, ghiNo);
            statement.setInt(9, lich);
            statement.setInt(10, lich);
            statement.setInt(11, lich);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String maKH = rs.getString("maKH");
                    if (ketQua.isEmpty() || !ketQua.get(ketQua.size() - 1).getMaKH().equals(maKH)) {
                        KhachHang kh = docKhachHang(rs);
                        Timestamp gan = rs.getTimestamp("gan");
                        if (gan != null) {
                            LichHen lh = new LichHen();
                            lh.setThoiGianHen(gan.toLocalDateTime());
                            kh.setLichHenGanNhat(lh);
                        }
                        ketQua.add(kh);
                    }
                    String bienSo = rs.getString("bienSo");
                    if (bienSo != null) {
                        ketQua.get(ketQua.size() - 1).getDanhSachXe().add(new Xe(bienSo));
                    }
                }
            }
        }
        return ketQua;
    }

    /** Mã do DAO cấp trong transaction rồi gán lại vào kh. Ném SQLIntegrityConstraintViolationException khi trùng SĐT. */
    public void insert(KhachHang kh) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                chanTrungSoDienThoai(connection, kh.getSoDienThoai(), "");
                String ma = nextId(connection);
                String sql = "INSERT INTO KhachHang (maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, "
                        + "duocGhiNo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, ma);
                    statement.setString(2, kh.getHoTen());
                    statement.setString(3, kh.getSoDienThoai());
                    statement.setString(4, kh.getDiaChi());
                    statement.setString(5, kh.getEmail());
                    statement.setString(6, kh.getMaSoThue());
                    statement.setString(7, kh.getLoaiKhachHang().getText());
                    statement.setBoolean(8, kh.isDuocGhiNo());
                    statement.executeUpdate();
                }
                connection.commit();
                kh.setMaKH(ma);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Trả về false khi khách hàng không còn tồn tại. Ném SQLIntegrityConstraintViolationException khi trùng SĐT. */
    public boolean update(KhachHang kh) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                chanTrungSoDienThoai(connection, kh.getSoDienThoai(), kh.getMaKH());
                String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, diaChi = ?, email = ?, maSoThue = ?, "
                        + "loaiKhachHang = ?, duocGhiNo = ? WHERE maKH = ?";
                boolean thanhCong;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, kh.getHoTen());
                    statement.setString(2, kh.getSoDienThoai());
                    statement.setString(3, kh.getDiaChi());
                    statement.setString(4, kh.getEmail());
                    statement.setString(5, kh.getMaSoThue());
                    statement.setString(6, kh.getLoaiKhachHang().getText());
                    statement.setBoolean(7, kh.isDuocGhiNo());
                    statement.setString(8, kh.getMaKH());
                    thanhCong = statement.executeUpdate() == 1;
                }
                connection.commit();
                return thanhCong;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Trả về false khi khách hàng còn xe liên kết hoặc không còn tồn tại. */
    public boolean delete(String maKH) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE maKH = ? AND NOT EXISTS (SELECT 1 FROM Xe WHERE maKH = ?)";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKH);
            statement.setString(2, maKH);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            // Xe vừa được thêm ngay giữa lúc xóa.
            if (ex.getErrorCode() == LOI_VI_PHAM_KHOA_NGOAI) {
                return false;
            }
            throw ex;
        }
    }

    public List<Xe> xeLienKet(String maKH) throws SQLException {
        String sql = "SELECT bienSo, soVIN, hangXe, dongXe, doiXe, mauSon FROM Xe WHERE maKH = ? ORDER BY bienSo";
        List<Xe> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKH);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int doiXe = rs.getInt(5);
                    ketQua.add(new Xe(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.wasNull() ? null : doiXe, rs.getString(6), new KhachHang(maKH)));
                }
            }
        }
        return ketQua;
    }

    /**
     * Mỗi dòng: {maPhieu, LocalDateTime tiepNhan, hangMuc, Double tongTien}, mới nhất trước.
     * tongTien là null khi phiếu chưa có hóa đơn còn hiệu lực.
     */
    public List<Object[]> lichSuSuaChua(String maKH) throws SQLException {
        // FOR XML PATH thay cho STRING_AGG để chạy được trên SQL Server cũ; TYPE/value giữ nguyên ký tự & trong tên dịch vụ.
        String sql = "SELECT p.maPhieuTNSC, p.thoiGianTiepNhan, "
                + "STUFF((SELECT N', ' + d.tenDichVu FROM ChiTietSuaChua_DichVu ct "
                + "JOIN DichVu d ON d.maDichVu = ct.maDichVu WHERE ct.maPhieuTNSC = p.maPhieuTNSC "
                + "ORDER BY d.tenDichVu FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS hangMuc, "
                + "hd.tongThanhToan "
                + "FROM PhieuTiepNhanVaSuaChua p JOIN Xe x ON x.bienSo = p.bienSo "
                + "LEFT JOIN HoaDon hd ON hd.maPhieuTNSC = p.maPhieuTNSC AND hd.trangThaiThanhToan <> N'Đã hủy' "
                + "WHERE x.maKH = ? ORDER BY p.thoiGianTiepNhan DESC, p.maPhieuTNSC DESC";
        List<Object[]> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKH);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Timestamp tiepNhan = rs.getTimestamp(2);
                    ketQua.add(new Object[] {rs.getString(1), tiepNhan == null ? null : tiepNhan.toLocalDateTime(),
                        rs.getString(3), rs.getObject(4)});
                }
            }
        }
        return ketQua;
    }

    private void chanTrungSoDienThoai(Connection connection, String soDienThoai, String maTru) throws SQLException {
        String sql = "SELECT 1 FROM KhachHang WITH (UPDLOCK, HOLDLOCK) WHERE soDienThoai = ? AND maKH <> ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, soDienThoai);
            statement.setString(2, maTru);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    throw new SQLIntegrityConstraintViolationException(TRUNG_SO_DIEN_THOAI);
                }
            }
        }
    }

    private String nextId(Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maKH, 3, 18))), 0) + 1 "
                + "FROM KhachHang WITH (UPDLOCK, HOLDLOCK)";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return String.format("KH%04d", rs.getInt(1));
        }
    }
}
