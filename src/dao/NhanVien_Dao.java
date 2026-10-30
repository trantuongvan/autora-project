package dao;

import connection.ConnectDB;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn nhân viên và tài khoản liên kết theo schema AUTORA. */
public class NhanVien_Dao {

    private static final String SELECT_COLUMNS =
            "SELECT n.maNV, n.hoTen, n.soDienThoai, n.diaChi, n.viTriLamViec, t.tenDangNhap ";
    private static final String FROM =
            "FROM NhanVien n LEFT JOIN TaiKhoan t ON t.maNV = n.maNV ";

    public List<NhanVien> findAll() throws SQLException {
        return search("", "");
    }

    public List<NhanVien> search(String keyword, String viTri) throws SQLException {
        String searchText = keyword == null ? "" : keyword.trim();
        String position = viTri == null ? "" : viTri;
        String sql = SELECT_COLUMNS + FROM
                + "WHERE (? = '' OR n.maNV LIKE ? OR n.hoTen LIKE ? OR n.soDienThoai LIKE ?) "
                + "AND (? = '' OR n.viTriLamViec = ?) ORDER BY n.maNV";
        List<NhanVien> employees = new ArrayList<>();

        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchText);
            statement.setString(2, "%" + searchText + "%");
            statement.setString(3, "%" + searchText + "%");
            statement.setString(4, "%" + searchText + "%");
            statement.setString(5, position);
            statement.setString(6, position);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    employees.add(mapNhanVien(result));
                }
            }
        }
        return employees;
    }

    /** Nhân viên theo vị trí để chọn người phụ trách ở các nghiệp vụ khác. */
    public List<NhanVien> findByViTri(String viTri) throws SQLException {
        String sql = SELECT_COLUMNS + FROM + "WHERE n.viTriLamViec = ? ORDER BY n.hoTen";
        List<NhanVien> employees = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, viTri);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    employees.add(mapNhanVien(result));
                }
            }
        }
        return employees;
    }

    public boolean existsSoDienThoai(String soDienThoai, String exceptMaNV) throws SQLException {
        String sql = "SELECT 1 FROM NhanVien WHERE soDienThoai = ? AND maNV <> ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, soDienThoai);
            statement.setString(2, exceptMaNV == null ? "" : exceptMaNV);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public NhanVien findById(String id) throws SQLException {
        String sql = SELECT_COLUMNS + FROM + "WHERE n.maNV = ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapNhanVien(result) : null;
            }
        }
    }

    public String nextId() throws SQLException {
        String sql = "SELECT 'NV' + RIGHT('0000' + CAST(COALESCE(MAX("
                + "TRY_CONVERT(INT, SUBSTRING(maNV, 3, 18))), 0) + 1 AS VARCHAR(18)), 4) "
                + "FROM NhanVien";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getString(1);
        }
    }

    public boolean insert(NhanVien employee) throws SQLException {
        String sql = "INSERT INTO NhanVien(maNV, hoTen, soDienThoai, diaChi, viTriLamViec) "
                + "VALUES(?, ?, ?, ?, ?)";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getMaNV());
            statement.setString(2, employee.getHoTen());
            statement.setString(3, employee.getSoDienThoai());
            statement.setString(4, employee.getDiaChi());
            statement.setString(5, employee.getViTriLamViec());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(NhanVien employee) throws SQLException {
        String sql = "UPDATE NhanVien SET hoTen = ?, soDienThoai = ?, diaChi = ?, "
                + "viTriLamViec = ? WHERE maNV = ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getHoTen());
            statement.setString(2, employee.getSoDienThoai());
            statement.setString(3, employee.getDiaChi());
            statement.setString(4, employee.getViTriLamViec());
            statement.setString(5, employee.getMaNV());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM NhanVien WHERE maNV = ?";
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private NhanVien mapNhanVien(ResultSet result) throws SQLException {
        NhanVien employee = new NhanVien(
                result.getString("maNV"),
                result.getString("hoTen"),
                result.getString("soDienThoai"),
                result.getString("diaChi"),
                result.getString("viTriLamViec"));
        employee.setTenDangNhap(result.getString("tenDangNhap"));
        return employee;
    }
}
