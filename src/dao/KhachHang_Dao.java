package dao;

import connection.ConnectDB;
import entity.KhachHang;
import entity.LoaiKhachHang;

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

    /**
     * duocGhiNo null là không lọc.
     * Mỗi dòng: {maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, Boolean duocGhiNo, danhSachXe}.
     */
    public List<Object[]> search(String tuKhoa, LoaiKhachHang loaiKhachHang, Boolean duocGhiNo) throws SQLException {
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        String loai = loaiKhachHang == null ? "" : loaiKhachHang.getText();
        String sql = "SELECT k.maKH, k.hoTen, k.soDienThoai, k.diaChi, k.email, k.maSoThue, k.loaiKhachHang, k.duocGhiNo, "
                + "STUFF((SELECT ', ' + x.bienSo FROM Xe x WHERE x.maKH = k.maKH ORDER BY x.bienSo "
                + "FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS danhSachXe "
                + "FROM KhachHang k "
                + "WHERE (? = N'' OR k.hoTen LIKE ? OR k.soDienThoai LIKE ? "
                + "OR EXISTS (SELECT 1 FROM Xe x WHERE x.maKH = k.maKH AND x.bienSo LIKE ?)) "
                + "AND (? = N'' OR k.loaiKhachHang = ?) AND (? = -1 OR k.duocGhiNo = ?) "
                + "ORDER BY k.maKH";
        int ghiNo = duocGhiNo == null ? -1 : duocGhiNo ? 1 : 0;
        List<Object[]> ketQua = new ArrayList<>();
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
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new Object[] {rs.getString(1), rs.getString(2), rs.getString(3), rong(rs.getString(4)),
                        rong(rs.getString(5)), rong(rs.getString(6)), LoaiKhachHang.fromDb(rs.getString(7)), rs.getBoolean(8),
                        rong(rs.getString(9))});
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

    /** Mỗi dòng: {bienSo, soVIN, hangXe, dongXe, doiXe, mauSon}. */
    public List<Object[]> xeLienKet(String maKH) throws SQLException {
        String sql = "SELECT bienSo, soVIN, hangXe, dongXe, doiXe, mauSon FROM Xe WHERE maKH = ? ORDER BY bienSo";
        List<Object[]> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKH);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new Object[] {rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getObject(5), rs.getString(6)});
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

    private static String rong(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }
}
