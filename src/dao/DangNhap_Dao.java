package dao;

import connection.ConnectDB;
import util.PhienDangNhap;
import entity.VaiTro;
import util.MatKhauUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DangNhap_Dao {

    /**
     * Trả về phiên khi đúng tài khoản, mật khẩu và tài khoản đang hoạt động; ngược lại trả về null.
     */
    public PhienDangNhap xacThuc(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT t.tenDangNhap, t.maNV, n.hoTen, t.vaiTro, t.matKhau "
                + "FROM TaiKhoan t JOIN NhanVien n ON n.maNV = t.maNV "
                + "WHERE t.tenDangNhap = ? AND t.trangThai = 1";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tenDangNhap);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next() || !MatKhauUtil.kiemTra(matKhau, rs.getString("matKhau"))) {
                    return null;
                }
                return new PhienDangNhap(
                        rs.getString("tenDangNhap"),
                        rs.getString("maNV"),
                        rs.getString("hoTen"),
                        VaiTro.fromDb(rs.getString("vaiTro")));
            }
        }
    }

    /** Trả về false khi không có tài khoản này. Controller phải xác thực chủ cửa hàng trước khi gọi. */
    public boolean datLaiMatKhau(String tenDangNhap, String matKhauMoi, String nguoiXacNhan) throws SQLException {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, MatKhauUtil.bam(matKhauMoi));
                statement.setString(2, tenDangNhap);
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                // Không ghi giá trị mật khẩu vào nhật ký.
                new NhatKy_Dao().ghi(connection, nguoiXacNhan, "Đặt lại mật khẩu", tenDangNhap, null, null);
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
