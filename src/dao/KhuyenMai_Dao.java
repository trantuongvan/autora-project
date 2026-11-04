package dao;

import connection.ConnectDB;
import entity.HinhThucKhuyenMai;
import entity.KhuyenMai;
import entity.PhamViKhuyenMai;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMai_Dao {
    private static final String SELECT_COLUMNS =
            "SELECT maKM, tenChuongTrinh, hinhThuc, giaTri, "
            + "thoiGianBatDau, thoiGianKetThuc, trangThai, phamViApDung, soLuotToiDa, ghiChu, "
            + "(SELECT COUNT(*) FROM HoaDon h WHERE h.maKM = KhuyenMai.maKM "
            + "AND h.trangThaiThanhToan <> N'Đã hủy') AS soLuotDaDung FROM KhuyenMai ";

    public List<KhuyenMai> search(String keyword, HinhThucKhuyenMai hinhThuc) throws SQLException {
        String key = keyword == null ? "" : keyword.trim();
        String type = hinhThuc == null ? "" : hinhThuc.getText();
        String sql = SELECT_COLUMNS
                + "WHERE (? = N'' OR maKM LIKE ? OR tenChuongTrinh LIKE ?) "
                + "AND (? = N'' OR hinhThuc = ?) "
                + "ORDER BY thoiGianKetThuc DESC";
        List<KhuyenMai> result = new ArrayList<>();

        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, "%" + key + "%");
            statement.setString(3, "%" + key + "%");
            statement.setString(4, type);
            statement.setString(5, type);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapKhuyenMai(rs));
                }
            }
        }
        return result;
    }

    public KhuyenMai findById(String maKM) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE maKM = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKM);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapKhuyenMai(rs);
                }
                return null;
            }
        }
    }

    public String nextId() throws SQLException {
        String sql = "SELECT 'KM' + RIGHT('0000' + CAST("
                + "COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maKM, 3, 18))), 0) + 1 "
                + "AS VARCHAR(18)), 4) FROM KhuyenMai";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    public boolean insert(KhuyenMai khuyenMai) throws SQLException {
        khuyenMai.kiemTraHopLe();
        String sql = "INSERT INTO KhuyenMai "
                + "(maKM, tenChuongTrinh, hinhThuc, giaTri, "
                + "thoiGianBatDau, thoiGianKetThuc, trangThai, phamViApDung, soLuotToiDa, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, khuyenMai.getMaKM());
            statement.setString(2, khuyenMai.getTenChuongTrinh());
            statement.setString(3, khuyenMai.getHinhThuc().getText());
            statement.setDouble(4, khuyenMai.getGiaTri());
            statement.setDate(5, Date.valueOf(khuyenMai.getThoiGianBatDau()));
            statement.setDate(6, Date.valueOf(khuyenMai.getThoiGianKetThuc()));
            statement.setBoolean(7, khuyenMai.isTrangThai());
            statement.setString(8, khuyenMai.getPhamViApDung().getText());
            statement.setObject(9, khuyenMai.getSoLuotToiDa(), java.sql.Types.INTEGER);
            statement.setString(10, khuyenMai.getGhiChu());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(KhuyenMai khuyenMai) throws SQLException {
        khuyenMai.kiemTraHopLe();
        String sql = "UPDATE KhuyenMai SET tenChuongTrinh = ?, hinhThuc = ?, "
                + "giaTri = ?, thoiGianBatDau = ?, thoiGianKetThuc = ?, "
                + "trangThai = ?, phamViApDung = ?, soLuotToiDa = ?, ghiChu = ? WHERE maKM = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, khuyenMai.getTenChuongTrinh());
            statement.setString(2, khuyenMai.getHinhThuc().getText());
            statement.setDouble(3, khuyenMai.getGiaTri());
            statement.setDate(4, Date.valueOf(khuyenMai.getThoiGianBatDau()));
            statement.setDate(5, Date.valueOf(khuyenMai.getThoiGianKetThuc()));
            statement.setBoolean(6, khuyenMai.isTrangThai());
            statement.setString(7, khuyenMai.getPhamViApDung().getText());
            statement.setObject(8, khuyenMai.getSoLuotToiDa(), java.sql.Types.INTEGER);
            statement.setString(9, khuyenMai.getGhiChu());
            statement.setString(10, khuyenMai.getMaKM());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String maKM) throws SQLException {
        String sql = "DELETE FROM KhuyenMai WHERE maKM = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKM);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean isDangDuocDung(String maKM) throws SQLException {
        String sql = "SELECT 1 FROM HoaDon WHERE maKM = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKM);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private KhuyenMai mapKhuyenMai(ResultSet rs) throws SQLException {
        KhuyenMai khuyenMai = new KhuyenMai(
                rs.getString("maKM"),
                rs.getString("tenChuongTrinh"),
                HinhThucKhuyenMai.fromDb(rs.getString("hinhThuc")),
                rs.getDouble("giaTri"),
                rs.getTimestamp("thoiGianBatDau").toLocalDateTime().toLocalDate(),
                rs.getTimestamp("thoiGianKetThuc").toLocalDateTime().toLocalDate(),
                rs.getBoolean("trangThai"));
        khuyenMai.setPhamViApDung(PhamViKhuyenMai.fromDb(rs.getString("phamViApDung")));
        khuyenMai.setSoLuotToiDa((Integer) rs.getObject("soLuotToiDa"));
        khuyenMai.setGhiChu(rs.getString("ghiChu"));
        khuyenMai.setSoLuotDaDung(rs.getInt("soLuotDaDung"));
        return khuyenMai;
    }
}
