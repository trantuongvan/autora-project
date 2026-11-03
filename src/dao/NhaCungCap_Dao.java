package dao;

import connection.ConnectDB;
import entity.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCap_Dao {

    public List<NhaCungCap> findAll() throws SQLException {
        String sql = "SELECT maNCC, tenNCC, soDienThoai, diaChi FROM NhaCungCap ORDER BY maNCC ASC";
        List<NhaCungCap> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new NhaCungCap(
                        r.getString("maNCC"),
                        r.getString("tenNCC"),
                        r.getString("soDienThoai"),
                        r.getString("diaChi")
                ));
            }
        }
        return list;
    }

    public NhaCungCap findById(String maNCC) throws SQLException {
        String sql = "SELECT maNCC, tenNCC, soDienThoai, diaChi FROM NhaCungCap WHERE maNCC = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maNCC);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return new NhaCungCap(
                            r.getString("maNCC"),
                            r.getString("tenNCC"),
                            r.getString("soDienThoai"),
                            r.getString("diaChi")
                    );
                }
            }
        }
        return null;
    }
}