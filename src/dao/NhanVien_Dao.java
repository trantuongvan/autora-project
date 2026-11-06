package dao;

import connection.ConnectDB;
import entity.CaLamViec;
import entity.DichVu;
import entity.KyNang;
import entity.NhanVien;
import entity.TaiKhoan;
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
            "SELECT n.maNV, n.hoTen, n.soDienThoai, n.diaChi, n.viTriLamViec, t.tenDangNhap, "
            + "ca.maCa, ca.tenCa, ca.gioBatDau, ca.gioKetThuc ";
    private static final String FROM =
            "FROM NhanVien n LEFT JOIN TaiKhoan t ON t.maNV = n.maNV "
            + "LEFT JOIN CaLamViec ca ON ca.maCa = n.maCa ";

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

    public List<CaLamViec> findAllCa() throws SQLException {
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc FROM CaLamViec ORDER BY gioBatDau, maCa";
        List<CaLamViec> result = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(docCa(rs));
            }
        }
        return result;
    }

    /** Kỹ năng của một nhân viên; không nạp cùng nhân viên để các màn hình khác khỏi tốn truy vấn. */
    public List<KyNang> findKyNang(String maNV) throws SQLException {
        String sql = "SELECT k.maDichVu, d.tenDichVu, d.tienCong, d.thoiGianChuan, k.thoiGianPhut "
                + "FROM NhanVien_DichVu k JOIN DichVu d ON d.maDichVu = k.maDichVu "
                + "WHERE k.maNV = ? ORDER BY k.maDichVu";
        List<KyNang> result = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNV);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DichVu dv = new DichVu(rs.getString("maDichVu"), rs.getString("tenDichVu"), rs.getDouble("tienCong"));
                    dv.setThoiGianChuan(rs.getInt("thoiGianChuan"));
                    result.add(new KyNang(new NhanVien(maNV), dv, rs.getInt("thoiGianPhut")));
                }
            }
        }
        return result;
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
        String sql = "INSERT INTO NhanVien(maNV, hoTen, soDienThoai, diaChi, viTriLamViec, maCa) "
                + "VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, employee.getMaNV());
                statement.setString(2, employee.getHoTen());
                statement.setString(3, employee.getSoDienThoai());
                statement.setString(4, employee.getDiaChi());
                statement.setString(5, employee.getViTriLamViec().getText());
                statement.setString(6, maCa(employee));
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                luuKyNang(connection, employee);
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
                + "FROM NhanVien WHERE maNV = ?";
        String sql = "UPDATE NhanVien SET hoTen = ?, soDienThoai = ?, diaChi = ?, "
                + "viTriLamViec = ?, maCa = ? WHERE maNV = ?";
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
                    statement.setString(5, maCa(employee));
                    statement.setString(6, employee.getMaNV());
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                luuKyNang(connection, employee);
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

    private static String maCa(NhanVien employee) {
        return employee.getCaLamViec() == null ? null : employee.getCaLamViec().getMaCa();
    }

    /** Thay toàn bộ kỹ năng của nhân viên bằng danh sách trên entity, trong transaction của lệnh lưu nhân viên. */
    private void luuKyNang(Connection connection, NhanVien employee) throws SQLException {
        try (PreparedStatement xoa = connection.prepareStatement("DELETE FROM NhanVien_DichVu WHERE maNV = ?");
             PreparedStatement them = connection.prepareStatement(
                     "INSERT INTO NhanVien_DichVu(maNV, maDichVu, thoiGianPhut) VALUES(?, ?, ?)")) {
            xoa.setString(1, employee.getMaNV());
            xoa.executeUpdate();
            for (KyNang kyNang : employee.getDanhSachKyNang()) {
                them.setString(1, employee.getMaNV());
                them.setString(2, kyNang.getDichVu().getMaDichVu());
                them.setInt(3, kyNang.getThoiGianPhut());
                them.executeUpdate();
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

    /** Tài khoản của nhân viên bị xóa theo; ném IllegalStateException nếu đó là Chủ cửa hàng đang hoạt động cuối cùng. */
    public boolean delete(String id, String tenDangNhap) throws SQLException {
        String sqlCu = "SELECT hoTen, soDienThoai, diaChi, viTriLamViec "
                + "FROM NhanVien WHERE maNV = ?";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                new TaiKhoan_Dao().chanChuCuoiCuaNhanVien(connection, id);
                String oldValue;
                try (PreparedStatement statement = connection.prepareStatement(sqlCu)) {
                    statement.setString(1, id);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        oldValue = giaTri(result.getString("hoTen"), result.getString("soDienThoai"),
                                result.getString("diaChi"), result.getString("viTriLamViec"));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM NhanVien WHERE maNV = ?")) {
                    statement.setString(1, id);
                    statement.executeUpdate();
                }
                new NhatKy_Dao().ghi(connection, tenDangNhap, "Xóa nhân viên", id, oldValue, null);
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Các cột nhân viên của bảng có bí danh {@code bd}, đặt tên có tiền tố để một câu SQL đọc được nhiều nhân viên. */
    static String cotNhanVien(String bd, String tienTo) {
        return bd + ".maNV AS " + tienTo + "maNV, " + bd + ".hoTen AS " + tienTo + "hoTen, "
                + bd + ".soDienThoai AS " + tienTo + "soDienThoai, " + bd + ".diaChi AS " + tienTo + "diaChi, "
                + bd + ".viTriLamViec AS " + tienTo + "viTriLamViec";
    }

    /** Trả về null khi dòng không có nhân viên (LEFT JOIN không khớp). */
    static NhanVien docNhanVien(ResultSet rs, String tienTo) throws SQLException {
        String ma = rs.getString(tienTo + "maNV");
        if (ma == null) {
            return null;
        }
        return new NhanVien(ma, rs.getString(tienTo + "hoTen"), rs.getString(tienTo + "soDienThoai"),
                rs.getString(tienTo + "diaChi"), VaiTro.fromDb(rs.getString(tienTo + "viTriLamViec")));
    }

    /** Trả về null khi dòng không có ca (LEFT JOIN không khớp). */
    static CaLamViec docCa(ResultSet rs) throws SQLException {
        if (rs.getString("maCa") == null) {
            return null;
        }
        return new CaLamViec(rs.getString("maCa"), rs.getString("tenCa"),
                rs.getTime("gioBatDau").toLocalTime(), rs.getTime("gioKetThuc").toLocalTime());
    }

    private NhanVien mapNhanVien(ResultSet result) throws SQLException {
        NhanVien employee = new NhanVien(
                result.getString("maNV"),
                result.getString("hoTen"),
                result.getString("soDienThoai"),
                result.getString("diaChi"),
                VaiTro.fromDb(result.getString("viTriLamViec")));
        employee.setCaLamViec(docCa(result));
        String tenDangNhap = result.getString("tenDangNhap");
        if (tenDangNhap != null) {
            TaiKhoan taiKhoan = new TaiKhoan(tenDangNhap);
            taiKhoan.setNhanVien(employee);
            employee.setTaiKhoan(taiKhoan);
        }
        return employee;
    }
}
