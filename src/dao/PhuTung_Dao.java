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

    /** Không ghi tồn kho: tồn chỉ đổi qua nhập kho và duyệt xuất kho, màn hình cũ không được ghi đè số mới. */
    /** Sửa thông tin phụ tùng; đổi giá nhập hoặc giá bán lẻ thì ghi nhật ký giá cũ và giá mới. */
    public boolean update(PhuTung pt, String tenDangNhap) throws SQLException {
        String sql = "UPDATE PhuTung SET tenPhuTung = ?, hangSanXuat = ?, viTriLuuKho = ?, giaNhap = ?, "
                + "giaBanLe = ?, nguongCanhBao = ? WHERE maSKU = ?";
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(sql)) {
                PhuTung cu = docDeDoiTon(c, pt.getMaSKU());
                p.setString(1, pt.getTenPhuTung());
                p.setString(2, pt.getHangSanXuat());
                p.setString(3, pt.getViTriLuuKho());
                p.setDouble(4, pt.getGiaNhap());
                p.setDouble(5, pt.getGiaBanLe());
                p.setInt(6, pt.getNguongCanhBao());
                p.setString(7, pt.getMaSKU());
                if (p.executeUpdate() != 1) {
                    c.rollback();
                    return false;
                }
                if (cu.getGiaNhap() != pt.getGiaNhap() || cu.getGiaBanLe() != pt.getGiaBanLe()) {
                    new NhatKy_Dao().ghi(c, tenDangNhap, "Sửa giá phụ tùng", pt.getMaSKU(),
                            moTaGia(cu), moTaGia(pt));
                }
                c.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static String moTaGia(PhuTung pt) {
        return "Nhập " + util.FormatUtil.formatCurrency(pt.getGiaNhap()) + " / Bán " + util.FormatUtil.formatCurrency(pt.getGiaBanLe());
    }

    public boolean delete(String maSKU) throws SQLException {
        String sql = "DELETE FROM PhuTung WHERE maSKU = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSKU);
            return p.executeUpdate() == 1;
        }
    }

    /** Đọc tồn và giá nhập trong transaction của người gọi. */
    static PhuTung docDeDoiTon(Connection c, String maSKU) throws SQLException {
        String sql = "SELECT soLuongTon, giaNhap, giaBanLe FROM PhuTung WHERE maSKU = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSKU);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new SQLException("Phụ tùng " + maSKU + " không tồn tại.");
                }
                PhuTung pt = new PhuTung(maSKU);
                pt.setSoLuongTon(r.getInt("soLuongTon"));
                pt.setGiaBanLe(r.getDouble("giaBanLe"));
                pt.setGiaNhap(r.getDouble("giaNhap"));
                return pt;
            }
        }
    }

    static void luuTon(Connection c, PhuTung pt) throws SQLException {
        String sql = "UPDATE PhuTung SET soLuongTon = ?, giaNhap = ? WHERE maSKU = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, pt.getSoLuongTon());
            p.setDouble(2, pt.getGiaNhap());
            p.setString(3, pt.getMaSKU());
            p.executeUpdate();
        }
    }

    /**
     * Đặt giá bán trước giá nhập để dòng cũ có giá nhập vượt giá bán vẫn đọc được:
     * một dòng như vậy không được làm hỏng cả danh sách, và thủ kho cần thấy nó để sửa.
     */
    private PhuTung mapPhuTung(ResultSet r) throws SQLException {
        PhuTung pt = new PhuTung(r.getString("maSKU"));
        pt.setTenPhuTung(r.getString("tenPhuTung"));
        pt.setHangSanXuat(r.getString("hangSanXuat"));
        pt.setViTriLuuKho(r.getString("viTriLuuKho"));
        pt.setGiaBanLe(r.getDouble("giaBanLe"));
        pt.setGiaNhap(r.getDouble("giaNhap"));
        pt.setSoLuongTon(r.getInt("soLuongTon"));
        pt.setNguongCanhBao(r.getInt("nguongCanhBao"));
        return pt;
    }
}