package dao;

import connection.ConnectDB;
import entity.PhuTung;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhuTung_Dao {

    public List<PhuTung> findAll() throws SQLException {
        return search("", "Tất cả", "Tất cả", "Tất cả");
    }

    public List<PhuTung> search(String keyword) throws SQLException {
        return search(keyword, "Tất cả", "Tất cả", "Tất cả");
    }

    public List<PhuTung> search(String keyword, String hangSanXuat, String khoangGia, String tinhTrangTon) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String hang = (hangSanXuat == null || hangSanXuat.startsWith("Hãng sản xuất:") || "Tất cả".equals(hangSanXuat))
                ? "" : hangSanXuat.trim();

        StringBuilder sql = new StringBuilder(
                "SELECT maSKU, tenPhuTung, hangSanXuat, viTriLuuKho, giaNhap, giaBanLe, soLuongTon, nguongCanhBao "
                        + "FROM PhuTung WHERE (? = '' OR maSKU LIKE ? OR tenPhuTung LIKE ? OR hangSanXuat LIKE ?) "
                        + "AND (? = '' OR hangSanXuat = ?) "
        );

        if (khoangGia != null) {
            if (khoangGia.contains("Dưới 200.000")) {
                sql.append("AND giaBanLe < 200000 ");
            } else if (khoangGia.contains("200.000đ - 500.000")) {
                sql.append("AND giaBanLe >= 200000 AND giaBanLe <= 500000 ");
            } else if (khoangGia.contains("Trên 500.000")) {
                sql.append("AND giaBanLe > 500000 ");
            }
        }

        if (tinhTrangTon != null) {
            if (tinhTrangTon.contains("Dưới ngưỡng")) {
                sql.append("AND soLuongTon <= nguongCanhBao ");
            } else if (tinhTrangTon.contains("Còn hàng")) {
                sql.append("AND soLuongTon > nguongCanhBao ");
            } else if (tinhTrangTon.contains("Hết hàng")) {
                sql.append("AND soLuongTon = 0 ");
            }
        }

        sql.append("ORDER BY maSKU ASC");

        List<PhuTung> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql.toString())) {
            p.setString(1, k);
            p.setString(2, "%" + k + "%");
            p.setString(3, "%" + k + "%");
            p.setString(4, "%" + k + "%");
            p.setString(5, hang);
            p.setString(6, hang);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(mapPhuTung(r));
                }
            }
        }
        return list;
    }

    public List<PhuTung> findDuoiNguongCanhBao(String keyword) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String sql = "SELECT maSKU, tenPhuTung, hangSanXuat, viTriLuuKho, giaNhap, giaBanLe, soLuongTon, nguongCanhBao "
                + "FROM PhuTung WHERE soLuongTon <= nguongCanhBao "
                + "AND (? = '' OR maSKU LIKE ? OR tenPhuTung LIKE ?) "
                + "ORDER BY soLuongTon ASC, maSKU ASC";
        List<PhuTung> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k);
            p.setString(2, "%" + k + "%");
            p.setString(3, "%" + k + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(mapPhuTung(r));
            }
        }
        return list;
    }

    public int countDuoiNguongCanhBao() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhuTung WHERE soLuongTon <= nguongCanhBao";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    public List<String> findAllHangSanXuat() throws SQLException {
        String sql = "SELECT DISTINCT hangSanXuat FROM PhuTung WHERE hangSanXuat IS NOT NULL AND hangSanXuat <> '' ORDER BY hangSanXuat";
        List<String> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(r.getString(1));
        }
        return list;
    }

    public List<String> findAllViTriKho() throws SQLException {
        String sql = "SELECT DISTINCT viTriLuuKho FROM PhuTung WHERE viTriLuuKho IS NOT NULL AND viTriLuuKho <> '' ORDER BY viTriLuuKho";
        List<String> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(r.getString(1));
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

    public boolean existsMaSKU(String maSKU) throws SQLException {
        String sql = "SELECT 1 FROM PhuTung WHERE maSKU = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSKU == null ? "" : maSKU.trim());
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean isUsedInTransactions(String maSKU) throws SQLException {
        String sql = "SELECT 1 WHERE EXISTS (SELECT 1 FROM ChiTietSuaChua_PhuTung WHERE maSKU = ?) "
                + "OR EXISTS (SELECT 1 FROM ChiTietYeuCauXuatKho WHERE maSKU = ?) "
                + "OR EXISTS (SELECT 1 FROM ChiTietNhapKho WHERE maSKU = ?)";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSKU);
            p.setString(2, maSKU);
            p.setString(3, maSKU);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean insert(PhuTung pt) throws SQLException {
        String sql = "INSERT INTO PhuTung(maSKU, tenPhuTung, hangSanXuat, viTriLuuKho, giaNhap, giaBanLe, soLuongTon, nguongCanhBao) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, pt.getMaSKU());
            p.setString(2, pt.getTenPhuTung());
            p.setString(3, pt.getHangSanXuat());
            p.setString(4, pt.getViTriLuuKho());
            p.setDouble(5, pt.getGiaNhap());
            p.setDouble(6, pt.getGiaBanLe());
            p.setInt(7, pt.getSoLuongTon());
            p.setInt(8, pt.getNguongCanhBao());
            return p.executeUpdate() == 1;
        }
    }

    public boolean update(PhuTung pt) throws SQLException {
        String sql = "UPDATE PhuTung SET tenPhuTung = ?, hangSanXuat = ?, viTriLuuKho = ?, giaNhap = ?, "
                + "giaBanLe = ?, soLuongTon = ?, nguongCanhBao = ? WHERE maSKU = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, pt.getTenPhuTung());
            p.setString(2, pt.getHangSanXuat());
            p.setString(3, pt.getViTriLuuKho());
            p.setDouble(4, pt.getGiaNhap());
            p.setDouble(5, pt.getGiaBanLe());
            p.setInt(6, pt.getSoLuongTon());
            p.setInt(7, pt.getNguongCanhBao());
            p.setString(8, pt.getMaSKU());
            return p.executeUpdate() == 1;
        }
    }

    public boolean delete(String maSKU) throws SQLException {
        String sql = "DELETE FROM PhuTung WHERE maSKU = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSKU);
            return p.executeUpdate() == 1;
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