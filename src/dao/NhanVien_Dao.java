package dao;

import connection.ConnectDB;
import entity.NhanVien;
import entity.VaiTro;

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
        return search("", null);
    }

    public List<NhanVien> search(String keyword, VaiTro viTri) throws SQLException {
        String searchText = keyword == null ? "" : keyword.trim();
        String position = viTri == null ? "" : viTri.getText();
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
    public List<NhanVien> findByViTri(VaiTro viTri) throws SQLException {
        String sql = SELECT_COLUMNS + FROM + "WHERE n.viTriLamViec = ? ORDER BY n.hoTen";
        List<NhanVien> employees = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, viTri.getText());
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
        return insert(employee, "system");
    }

    public boolean insert(NhanVien employee, String tenDangNhap) throws SQLException {
        String sql = "INSERT INTO NhanVien(maNV, hoTen, soDienThoai, diaChi, viTriLamViec) "
                + "VALUES(?, ?, ?, ?, ?)";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, employee.getMaNV());
                statement.setString(2, employee.getHoTen());
                statement.setString(3, employee.getSoDienThoai());
                statement.setString(4, employee.getDiaChi());
                statement.setString(5, employee.getViTriLamViec().getText());
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                new NhatKy_Dao().ghi(connection, tenDangNhap, "Thêm nhân viên",
                        employee.getMaNV(), null, giaTri(employee));
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public boolean update(NhanVien employee) throws SQLException {
        return update(employee, "system");
    }

    public boolean update(NhanVien employee, String tenDangNhap) throws SQLException {
        String sqlCu = "SELECT hoTen, soDienThoai, diaChi, viTriLamViec "
                + "FROM NhanVien WITH (UPDLOCK, HOLDLOCK) WHERE maNV = ?";
        String sql = "UPDATE NhanVien SET hoTen = ?, soDienThoai = ?, diaChi = ?, "
                + "viTriLamViec = ? WHERE maNV = ?";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String oldValue;
                try (PreparedStatement statement = connection.prepareStatement(sqlCu)) {
                    statement.setString(1, employee.getMaNV());
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        oldValue = giaTri(
                                result.getString("hoTen"),
                                result.getString("soDienThoai"),
                                result.getString("diaChi"),
                                result.getString("viTriLamViec"));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, employee.getHoTen());
                    statement.setString(2, employee.getSoDienThoai());
                    statement.setString(3, employee.getDiaChi());
                    statement.setString(4, employee.getViTriLamViec().getText());
                    statement.setString(5, employee.getMaNV());
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                new NhatKy_Dao().ghi(connection, tenDangNhap, "Cập nhật nhân viên",
                        employee.getMaNV(), oldValue, giaTri(employee));
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private String giaTri(NhanVien employee) {
        return giaTri(employee.getHoTen(), employee.getSoDienThoai(),
                employee.getDiaChi(), employee.getViTriLamViec().getText());
    }

    private String giaTri(String hoTen, String soDienThoai, String diaChi, String viTri) {
        return "Họ tên=" + hoTen + "; SĐT=" + soDienThoai + "; Địa chỉ="
                + (diaChi == null ? "" : diaChi) + "; Vị trí=" + viTri;
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
                VaiTro.fromDb(result.getString("viTriLamViec")));
        employee.setTenDangNhap(result.getString("tenDangNhap"));
        return employee;
    }
}
