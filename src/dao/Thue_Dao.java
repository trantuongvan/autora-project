package dao;

import connection.ConnectDB;
import entity.Thue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Thue_Dao {
    public List<Thue> findAll() throws SQLException {
        String sql = "SELECT maThue, tenLoaiThue, thueSuat FROM Thue ORDER BY thueSuat, maThue";
        List<Thue> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ketQua.add(new Thue(rs.getString("maThue"), rs.getString("tenLoaiThue"), rs.getDouble("thueSuat")));
            }
        }
        return ketQua;
    }
}
