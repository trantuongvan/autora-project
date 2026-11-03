package dao;

import connection.ConnectDB;
import entity.Xe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD hồ sơ xe; truy vấn danh sách ghép thông tin chủ xe. */
public class Xe_Dao {

    public List<Object[]> findAll() throws SQLException {
        return search("");
    }

    public List<Object[]> search(String keyword) throws SQLException {
        String q = keyword == null ? "" : keyword.trim();
        List<Object[]> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection()) {
            String vin = column(c, "soVIN", "soKhung", "soKhungXe");
            String brand = column(c, "hangXe", "hang", "thuongHieu");
            String model = column(c, "dongXe", "dong", "tenDongXe", "model");
            String year = column(c, "doiXe", "namSanXuat", "namSX", "nam");
            String owner = column(c, "maKH", "maKhachHang", "maChuXe");
            String sql = "SELECT x.bienSo, x.[" + brand + "] AS hangXe, x.[" + model + "] AS dongXe, "
                    + "x.[" + year + "] AS doiXe, x.[" + vin + "] AS soVIN, "
                    + "k.maKH, k.hoTen, k.soDienThoai "
                    + "FROM Xe x JOIN KhachHang k ON k.maKH = x.[" + owner + "] "
                    + "WHERE (? = '' OR x.bienSo LIKE ? OR x.[" + vin + "] LIKE ? "
                    + "OR k.hoTen LIKE ? OR k.soDienThoai LIKE ?) "
                    + "ORDER BY x.bienSo";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, q);
                for (int i = 2; i <= 5; i++) {
                    p.setString(i, "%" + q + "%");
                }
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) {
                        ds.add(new Object[]{
                                r.getString("bienSo"),
                                r.getString("hangXe") + " / " + r.getString("dongXe"),
                                r.getObject("doiXe"),
                                r.getString("soVIN"),
                                r.getString("hoTen"),
                                r.getString("soDienThoai"),
                                ""
                        });
                    }
                }
            }
        }
        return ds;
    }

    public Xe findById(String bienSo) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            String color = optionalColumn(c, "mauSon", "mauSac", "mauXe");
            String colorExpression = color == null
                    ? "CAST('' AS NVARCHAR(50))" : "[" + color + "]";
            String sql = "SELECT bienSo, ["
                    + column(c, "soVIN", "soKhung", "soKhungXe") + "] AS soVIN, ["
                    + column(c, "hangXe", "hang", "thuongHieu") + "] AS hangXe, ["
                    + column(c, "dongXe", "dong", "tenDongXe", "model") + "] AS dongXe, ["
                    + column(c, "doiXe", "namSanXuat", "namSX", "nam") + "] AS doiXe, "
                    + colorExpression + " AS mauSon, ["
                    + column(c, "maKH", "maKhachHang", "maChuXe")
                    + "] AS maKH FROM Xe WHERE bienSo = ?";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, bienSo);
                try (ResultSet r = p.executeQuery()) {
                    return r.next() ? mapXe(r) : null;
                }
            }
        }
    }

    public boolean insert(Xe xe) throws SQLException {
        return write(xe, false);
    }

    public boolean update(Xe xe) throws SQLException {
        return write(xe, true);
    }

    private boolean write(Xe xe, boolean update) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            String vin = column(c, "soVIN", "soKhung", "soKhungXe");
            String brand = column(c, "hangXe", "hang", "thuongHieu");
            String model = column(c, "dongXe", "dong", "tenDongXe", "model");
            String year = column(c, "doiXe", "namSanXuat", "namSX", "nam");
            String owner = column(c, "maKH", "maKhachHang", "maChuXe");
            String color = optionalColumn(c, "mauSon", "mauSac", "mauXe");
            String colorColumn = color == null ? "" : ",[" + color + "]";
            String colorValue = color == null ? "" : ",?";
            String sql;
            if (update) {
                sql = "UPDATE Xe SET [" + vin + "] = ?, [" + brand + "] = ?, ["
                        + model + "] = ?, [" + year + "] = ?";
                if (color != null) {
                    sql += ", [" + color + "] = ?";
                }
                sql += ", [" + owner + "] = ? WHERE bienSo = ?";
            } else {
                sql = "INSERT INTO Xe([" + vin + "], [" + brand + "], [" + model + "], ["
                        + year + "]" + colorColumn + ", [" + owner + "], bienSo) "
                        + "VALUES(?, ?, ?, ?" + colorValue + ", ?, ?)";
            }
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, xe.getSoVIN());
                p.setString(2, xe.getHangXe());
                p.setString(3, xe.getDongXe());
                if (xe.getDoiXe() == null) {
                    p.setNull(4, Types.INTEGER);
                } else {
                    p.setInt(4, xe.getDoiXe());
                }
                int next = 5;
                if (color != null) {
                    p.setString(next++, xe.getMauSon());
                }
                p.setString(next++, xe.getMaKH());
                p.setString(next, xe.getBienSo());
                return p.executeUpdate() == 1;
            }
        }
    }

    public boolean delete(String bienSo) throws SQLException {
        String sql = "DELETE FROM Xe WHERE bienSo = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, bienSo);
            return p.executeUpdate() == 1;
        }
    }

    private Xe mapXe(ResultSet r) throws SQLException {
        Object nam = r.getObject("doiXe");
        Integer doiXe = null;
        if (nam != null && !nam.toString().trim().isEmpty()) {
            try {
                doiXe = Integer.valueOf(nam.toString().trim());
            } catch (NumberFormatException ex) {
                throw new SQLException("Năm sản xuất trong hồ sơ xe không hợp lệ: " + nam, ex);
            }
        }
        return new Xe(
                r.getString("bienSo"),
                r.getString("soVIN"),
                r.getString("hangXe"),
                r.getString("dongXe"),
                doiXe,
                r.getString("mauSon"),
                r.getString("maKH"));
    }

    /** Hỗ trợ tên cột từ schema hiện tại và các bản CSDL cũ. */
    private String column(Connection c, String... candidates) throws SQLException {
        String column = optionalColumn(c, candidates);
        if (column != null) {
            return column;
        }
        throw new SQLException("Bảng Xe thiếu cột cần thiết ("
                + String.join(" / ", candidates) + "). Kiểm tra CSDL GaraAUTORA.");
    }

    private String optionalColumn(Connection c, String... candidates) throws SQLException {
        StringBuilder placeholders = new StringBuilder();
        StringBuilder orderBy = new StringBuilder("CASE name");
        for (int i = 0; i < candidates.length; i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append('?');
            orderBy.append(" WHEN ? THEN ").append(i);
        }
        orderBy.append(" ELSE 999 END");
        String sql = "SELECT name FROM sys.columns WHERE object_id = OBJECT_ID(?) "
                + "AND name IN (" + placeholders + ") ORDER BY " + orderBy;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int parameter = 1;
            ps.setString(parameter++, "dbo.Xe");
            for (String candidate : candidates) {
                ps.setString(parameter++, candidate);
            }
            for (String candidate : candidates) {
                ps.setString(parameter++, candidate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return null;
    }

    public List<String> findChuSoHuu() throws SQLException {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT maKH, hoTen, soDienThoai FROM KhachHang ORDER BY hoTen";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString("maKH") + " - " + rs.getString("hoTen")
                        + " (" + rs.getString("soDienThoai") + ")");
            }
        }
        return ds;
    }

    public List<Object[]> findLichSu(String bienSo) throws SQLException {
        String sql = "SELECT p.maPhieuTNSC, "
                + "CONVERT(VARCHAR(10), p.thoiGianTiepNhan, 103) AS ngay, "
                + "p.yeuCauBanDau, p.trangThai, COALESCE(h.tongThanhToan, 0) AS tong, "
                + "h.trangThaiThanhToan, COALESCE(ti.hoanTat, 0) AS hoanTat "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "LEFT JOIN HoaDon h ON h.maPhieuTNSC = p.maPhieuTNSC "
                + "OUTER APPLY (SELECT SUM(CASE WHEN d.trangThaiTienDo = N'Hoàn thành' "
                + "THEN 1 ELSE 0 END) * 100 / NULLIF(COUNT(*), 0) AS hoanTat "
                + "FROM ChiTietSuaChua_DichVu d WHERE d.maPhieuTNSC = p.maPhieuTNSC) ti "
                + "WHERE p.bienSo = ? ORDER BY p.thoiGianTiepNhan DESC";
        List<Object[]> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, bienSo);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String paid = r.getString("trangThaiThanhToan");
                    String status = "Đã thanh toán".equals(paid)
                            ? "Hoàn tất " + r.getInt("hoanTat") + "% • Đã thanh toán"
                            : r.getString("trangThai");
                    ds.add(new Object[]{
                            r.getString("maPhieuTNSC"),
                            r.getString("ngay"),
                            r.getString("yeuCauBanDau"),
                            status,
                            money(r.getDouble("tong")) + " đ"
                    });
                }
            }
        }
        return ds;
    }

    private static String money(double amount) {
        return String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0f", amount)
                .replace(',', '.');
    }
}
