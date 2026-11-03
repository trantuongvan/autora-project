package dao;

import connection.ConnectDB;
import entity.PhuTung;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhuTung_Dao {

    public List<PhuTung> findAll() throws SQLException {
        return search("");
    }

    public List<PhuTung> search(String keyword) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String sql = "SELECT maSKU, tenPhuTung, hangSanXuat, viTriLuuKho, giaNhap, giaBanLe, soLuongTon, nguongCanhBao "
                + "FROM PhuTung "
                + "WHERE (? = '' OR maSKU LIKE ? OR tenPhuTung LIKE ? OR hangSanXuat LIKE ?) "
                + "ORDER BY maSKU ASC";
        List<PhuTung> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k);
            p.setString(2, "%" + k + "%");
            p.setString(3, "%" + k + "%");
            p.setString(4, "%" + k + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(mapPhuTung(r));
                }
            }
        }
        return list;
    }

    public PhuTung findById(String maSKU) throws SQLException {
        String sql = "SELECT maSKU, tenPhuTung, hangSanXuat, viTriLuuKho, giaNhap, giaBanLe, soLuongTon, nguongCanhBao "
                + "FROM PhuTung WHERE maSKU = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSKU);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? mapPhuTung(r) : null;
            }
        }
    }

    private PhuTung mapPhuTung(ResultSet r) throws SQLException {
        return new PhuTung(
                r.getString("maSKU"),
                r.getString("tenPhuTung"),
                r.getString("hangSanXuat"),
                r.getString("viTriLuuKho"),
                r.getDouble("giaNhap"),
                r.getDouble("giaBanLe"),
                r.getInt("soLuongTon"),
                r.getInt("nguongCanhBao")
        );
    }
}
