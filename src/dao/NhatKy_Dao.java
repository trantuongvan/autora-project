package dao;

import connection.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Ghi và đọc nhật ký thao tác nhạy cảm (Audit Trail - NFR-04). */
public class NhatKy_Dao {

    /** Ghi trong transaction của thao tác gốc để không có thay đổi nào thiếu nhật ký. */
    public void ghi(Connection connection, String tenDangNhap, String hanhDong, String doiTuong,
            String giaTriCu, String giaTriMoi) throws SQLException {
        String sql = "INSERT INTO NhatKyHoatDong (tenDangNhap, hanhDong, doiTuong, giaTriCu, giaTriMoi) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tenDangNhap);
            statement.setString(2, hanhDong);
            statement.setString(3, doiTuong);
            statement.setString(4, giaTriCu);
            statement.setString(5, giaTriMoi);
            statement.executeUpdate();
        }
    }

    /** Mỗi dòng: {thoiGian, tenDangNhap, hanhDong, doiTuong, giaTriCu, giaTriMoi}, mới nhất trước. */
    public List<Object[]> search(String tuKhoa) throws SQLException {
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        String sql = "SELECT TOP (500) thoiGian, tenDangNhap, hanhDong, doiTuong, giaTriCu, giaTriMoi "
                + "FROM NhatKyHoatDong WHERE (? = N'' OR tenDangNhap LIKE ? OR hanhDong LIKE ? OR doiTuong LIKE ?) "
                + "ORDER BY thoiGian DESC, maNhatKy DESC";
        List<Object[]> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, "%" + key + "%");
            statement.setString(3, "%" + key + "%");
            statement.setString(4, "%" + key + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new Object[] {rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)});
                }
            }
        }
        return ketQua;
    }
}
