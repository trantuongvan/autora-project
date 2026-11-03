package dao;

import connection.ConnectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVu_Dao {
    public List<DichVu> findAll() throws SQLException {
        String sql = "SELECT maDichVu, tenDichVu, tienCong "
                + "FROM DichVu ORDER BY maDichVu";
        List<DichVu> result = new ArrayList<>();

        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(new DichVu(
                        rs.getString("maDichVu"),
                        rs.getString("tenDichVu"),
                        rs.getDouble("tienCong")));
            }
        }
        return result;
    }
}
