package dao;

import connection.ConnectDB;
import entity.ChiTietSuaChua_DichVu;
import entity.TrangThaiTienDo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ChiTietSuaChua_DichVu_Dao {
    private static final String NOT_LOCKED =
            "NOT EXISTS (SELECT 1 FROM HoaDon h "
            + "WHERE h.maPhieuTNSC = c.maPhieuTNSC "
            + "AND h.trangThaiThanhToan <> N'Đã hủy') "
            + "AND NOT EXISTS (SELECT 1 FROM PhieuTiepNhanVaSuaChua p "
            + "WHERE p.maPhieuTNSC = c.maPhieuTNSC AND p.trangThai = N'Đã hủy')";

    public List<ChiTietSuaChua_DichVu> findByPhieu(String maPhieu) throws SQLException {
        String sql = "SELECT c.maPhieuTNSC, c.maDichVu, d.tenDichVu, "
                + "c.maNV_KyThuat, n.hoTen AS tenKyThuatVien, "
                + "c.trangThaiTienDo, c.tienCongThucTe "
                + "FROM ChiTietSuaChua_DichVu c "
                + "JOIN DichVu d ON d.maDichVu = c.maDichVu "
                + "LEFT JOIN NhanVien n ON n.maNV = c.maNV_KyThuat "
                + "WHERE c.maPhieuTNSC = ? ORDER BY c.maDichVu";
        List<ChiTietSuaChua_DichVu> result = new ArrayList<>();

        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new ChiTietSuaChua_DichVu(
                            rs.getString("maPhieuTNSC"),
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getString("maNV_KyThuat"),
                            rs.getString("tenKyThuatVien"),
                            TrangThaiTienDo.fromDb(rs.getString("trangThaiTienDo")),
                            rs.getDouble("tienCongThucTe")));
                }
            }
        }
        return result;
    }

    public boolean insert(ChiTietSuaChua_DichVu chiTiet) throws SQLException {
        String sql = "INSERT INTO ChiTietSuaChua_DichVu "
                + "(maPhieuTNSC, maDichVu, maNV_KyThuat, trangThaiTienDo, tienCongThucTe) "
                + "SELECT ?, ?, ?, ?, ? "
                + "WHERE NOT EXISTS (SELECT 1 FROM HoaDon h "
                + "WHERE h.maPhieuTNSC = ? "
                + "AND h.trangThaiThanhToan <> N'Đã hủy') "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuTiepNhanVaSuaChua p "
                + "WHERE p.maPhieuTNSC = ? AND p.trangThai = N'Đã hủy')";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chiTiet.getMaPhieuTNSC());
            statement.setString(2, chiTiet.getMaDichVu());
            if (chiTiet.getMaNVKyThuat() == null) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, chiTiet.getMaNVKyThuat());
            }
            statement.setString(4, chiTiet.getTrangThaiTienDo().getText());
            statement.setDouble(5, chiTiet.getTienCongThucTe());
            statement.setString(6, chiTiet.getMaPhieuTNSC());
            statement.setString(7, chiTiet.getMaPhieuTNSC());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(ChiTietSuaChua_DichVu chiTiet) throws SQLException {
        String sql = "UPDATE c SET maNV_KyThuat = ?, tienCongThucTe = ? "
                + "FROM ChiTietSuaChua_DichVu c "
                + "WHERE c.maPhieuTNSC = ? AND c.maDichVu = ? AND " + NOT_LOCKED;
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (chiTiet.getMaNVKyThuat() == null) {
                statement.setNull(1, Types.VARCHAR);
            } else {
                statement.setString(1, chiTiet.getMaNVKyThuat());
            }
            statement.setDouble(2, chiTiet.getTienCongThucTe());
            statement.setString(3, chiTiet.getMaPhieuTNSC());
            statement.setString(4, chiTiet.getMaDichVu());
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Lưu thợ, tiền công từng dòng và KTV phụ trách trong một transaction; tiền công đổi thì ghi nhật ký (NFR-04).
     * Trả về false và không lưu gì khi phiếu đã khóa hoặc dòng không còn tồn tại.
     */
    public boolean luuPhanCong(String maPhieu, List<ChiTietSuaChua_DichVu> dong, String maNVPhuTrach,
            String ghiChuKyThuat, String tenDangNhap) throws SQLException {
        String sqlCu = "SELECT tienCongThucTe FROM ChiTietSuaChua_DichVu WHERE maPhieuTNSC = ? AND maDichVu = ?";
        String sql = "UPDATE c SET maNV_KyThuat = ?, tienCongThucTe = ? FROM ChiTietSuaChua_DichVu c "
                + "WHERE c.maPhieuTNSC = ? AND c.maDichVu = ? AND " + NOT_LOCKED;
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement doc = connection.prepareStatement(sqlCu);
                    PreparedStatement ghi = connection.prepareStatement(sql)) {
                NhatKy_Dao nhatKy = new NhatKy_Dao();
                for (ChiTietSuaChua_DichVu d : dong) {
                    doc.setString(1, maPhieu);
                    doc.setString(2, d.getMaDichVu());
                    double cu;
                    try (ResultSet rs = doc.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }
                        cu = rs.getDouble(1);
                    }
                    ghi.setString(1, d.getMaNVKyThuat());
                    ghi.setDouble(2, d.getTienCongThucTe());
                    ghi.setString(3, maPhieu);
                    ghi.setString(4, d.getMaDichVu());
                    if (ghi.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                    if (cu != d.getTienCongThucTe()) {
                        nhatKy.ghi(connection, tenDangNhap, "Sửa tiền công", maPhieu + " / " + d.getMaDichVu(),
                                String.valueOf((long) cu), String.valueOf((long) d.getTienCongThucTe()));
                    }
                }
                if (!new PhieuTiepNhanVaSuaChua_Dao().capNhatPhanCong(connection, maPhieu, maNVPhuTrach, ghiChuKyThuat)) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public boolean delete(String maPhieu, String maDichVu) throws SQLException {
        String sql = "DELETE c FROM ChiTietSuaChua_DichVu c "
                + "WHERE c.maPhieuTNSC = ? AND c.maDichVu = ? "
                + "AND c.trangThaiTienDo <> N'Hoàn thành' AND " + NOT_LOCKED;
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            statement.setString(2, maDichVu);
            return statement.executeUpdate() == 1;
        }
    }
}
