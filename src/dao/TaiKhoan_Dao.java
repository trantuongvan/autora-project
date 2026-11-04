package dao;

import connection.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.VaiTro;
import util.MatKhauUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/** Tài khoản và nhật ký đi cùng. Không bao giờ ghi giá trị mật khẩu vào nhật ký. */
public class TaiKhoan_Dao {
    public static final String TRUNG_TEN_DANG_NHAP = "Tên tài khoản đã tồn tại";
    public static final String NHAN_VIEN_DA_CO_TAI_KHOAN = "Nhân viên này đã có tài khoản";
    private static final String CHU_CUOI = "Không thể thực hiện: đây là tài khoản Chủ cửa hàng đang hoạt động cuối cùng.";

    public List<TaiKhoan> search(String tuKhoa) throws SQLException {
        return search(tuKhoa, null, null);
    }

    /** vaiTro null và trangThai null nghĩa là không lọc theo tiêu chí đó. */
    public List<TaiKhoan> search(String tuKhoa, VaiTro vaiTro, Boolean trangThai) throws SQLException {
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        String sql = "SELECT t.tenDangNhap, t.vaiTro, t.trangThai, t.maNV, n.hoTen "
                + "FROM TaiKhoan t JOIN NhanVien n ON n.maNV = t.maNV "
                + "WHERE (? = N'' OR t.tenDangNhap LIKE ? OR n.hoTen LIKE ? OR t.maNV LIKE ? OR t.vaiTro LIKE ?) "
                + "AND (? = N'' OR t.vaiTro = ?) "
                + (trangThai == null ? "" : "AND t.trangThai = " + (trangThai ? 1 : 0) + " ")
                + "ORDER BY t.maNV";
        List<TaiKhoan> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            for (int i = 2; i <= 5; i++) {
                statement.setString(i, "%" + key + "%");
            }
            String chuoiVaiTro = vaiTro == null ? "" : vaiTro.getText();
            statement.setString(6, chuoiVaiTro);
            statement.setString(7, chuoiVaiTro);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new TaiKhoan(rs.getString(1), VaiTro.fromDb(rs.getString(2)), rs.getBoolean(3), rs.getString(4),
                            rs.getString(5)));
                }
            }
        }
        return ketQua;
    }

    public List<NhanVien> nhanVienChuaCoTaiKhoan() throws SQLException {
        String sql = "SELECT n.maNV, n.hoTen, n.soDienThoai, n.diaChi, n.viTriLamViec FROM NhanVien n "
                + "WHERE NOT EXISTS (SELECT 1 FROM TaiKhoan t WHERE t.maNV = n.maNV) ORDER BY n.maNV";
        List<NhanVien> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ketQua.add(new NhanVien(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        VaiTro.fromDb(rs.getString(5))));
            }
        }
        return ketQua;
    }

    public boolean exists(String tenDangNhap) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return tonTai(connection, "tenDangNhap", tenDangNhap);
        }
    }

    /** Ném SQLIntegrityConstraintViolationException khi trùng tên đăng nhập hoặc nhân viên đã có tài khoản. */
    public void insert(TaiKhoan taiKhoan, String matKhau, String nguoiThucHien) throws SQLException {
        TaiKhoan.kiemTraMatKhau(matKhau);
        String giaTriBam = MatKhauUtil.bam(matKhau);
        String sql = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThai, maNV) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (tonTai(connection, "tenDangNhap", taiKhoan.getTenDangNhap())) {
                    throw new SQLIntegrityConstraintViolationException(TRUNG_TEN_DANG_NHAP);
                }
                if (tonTai(connection, "maNV", taiKhoan.getMaNV())) {
                    throw new SQLIntegrityConstraintViolationException(NHAN_VIEN_DA_CO_TAI_KHOAN);
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, taiKhoan.getTenDangNhap());
                    statement.setString(2, giaTriBam);
                    statement.setString(3, taiKhoan.getVaiTro().getText());
                    statement.setBoolean(4, taiKhoan.isTrangThai());
                    statement.setString(5, taiKhoan.getMaNV());
                    statement.executeUpdate();
                }
                new NhatKy_Dao().ghi(connection, nguoiThucHien, "Tạo tài khoản", taiKhoan.getTenDangNhap(), null,
                        taiKhoan.getVaiTro() + ", " + taiKhoan.getTrangThaiHienThi());
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public boolean doiVaiTro(String tenDangNhap, VaiTro vaiTroMoi, String nguoiThucHien) throws SQLException {
        return capNhat(tenDangNhap, vaiTroMoi, null, null, nguoiThucHien);
    }

    public boolean doiMatKhau(String tenDangNhap, String matKhauMoi, String nguoiThucHien) throws SQLException {
        return capNhat(tenDangNhap, null, matKhauMoi, null, nguoiThucHien);
    }

    public boolean datTrangThai(String tenDangNhap, boolean hoatDong, String nguoiThucHien) throws SQLException {
        return capNhat(tenDangNhap, null, null, hoatDong, nguoiThucHien);
    }

    /**
     * Cập nhật các giá trị khác null trong một transaction; giá trị không đổi thì không ghi nhật ký.
     * Trả về false khi không có tài khoản này.
     * Ném IllegalStateException khi việc đổi làm mất Chủ cửa hàng đang hoạt động cuối cùng.
     */
    public boolean capNhat(String tenDangNhap, VaiTro vaiTroMoi, String matKhauMoi, Boolean trangThaiMoi,
            String nguoiThucHien) throws SQLException {
        String giaTriBam = null;
        if (matKhauMoi != null) {
            TaiKhoan.kiemTraMatKhau(matKhauMoi);
            giaTriBam = MatKhauUtil.bam(matKhauMoi);
        }
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int soChu = demChuDangHoatDong(connection);
                TaiKhoan cu = docTaiKhoan(connection, tenDangNhap);
                if (cu == null) {
                    connection.rollback();
                    return false;
                }
                boolean doiVaiTro = vaiTroMoi != null && vaiTroMoi != cu.getVaiTro();
                boolean doiTrangThai = trangThaiMoi != null && trangThaiMoi != cu.isTrangThai();
                if (doiVaiTro || (doiTrangThai && !trangThaiMoi)) {
                    chanChuCuoi(cu, soChu);
                }
                NhatKy_Dao nhatKy = new NhatKy_Dao();
                if (doiVaiTro) {
                    capNhatCot(connection, "vaiTro", vaiTroMoi.getText(), tenDangNhap);
                    nhatKy.ghi(connection, nguoiThucHien, "Đổi vai trò", tenDangNhap, cu.getVaiTro().getText(), vaiTroMoi.getText());
                }
                if (giaTriBam != null) {
                    capNhatCot(connection, "matKhau", giaTriBam, tenDangNhap);
                    nhatKy.ghi(connection, nguoiThucHien, "Đổi mật khẩu", tenDangNhap, null, null);
                }
                if (doiTrangThai) {
                    capNhatCot(connection, "trangThai", trangThaiMoi, tenDangNhap);
                    nhatKy.ghi(connection, nguoiThucHien, trangThaiMoi ? "Mở khóa tài khoản" : "Khóa tài khoản",
                            tenDangNhap, cu.getTrangThaiHienThi(),
                            trangThaiMoi ? TaiKhoan.DANG_HOAT_DONG : TaiKhoan.BI_KHOA);
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Trả về false khi không có tài khoản này. Ném IllegalStateException nếu là Chủ cửa hàng đang hoạt động cuối cùng. */
    public boolean delete(String tenDangNhap, String nguoiThucHien) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int soChu = demChuDangHoatDong(connection);
                TaiKhoan cu = docTaiKhoan(connection, tenDangNhap);
                if (cu == null) {
                    connection.rollback();
                    return false;
                }
                chanChuCuoi(cu, soChu);
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM TaiKhoan WHERE tenDangNhap = ?")) {
                    statement.setString(1, tenDangNhap);
                    statement.executeUpdate();
                }
                new NhatKy_Dao().ghi(connection, nguoiThucHien, "Xóa tài khoản", tenDangNhap,
                        cu.getVaiTro() + ", " + cu.getTrangThaiHienThi(), null);
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Luôn gọi đầu tiên trong transaction: khóa giữ tới commit nên hai thao tác đồng thời không cùng đếm được số cũ. */
    private int demChuDangHoatDong(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WITH (UPDLOCK, HOLDLOCK) WHERE vaiTro = ? AND trangThai = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, VaiTro.CHU_CUA_HANG.getText());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void chanChuCuoi(TaiKhoan taiKhoan, int soChuDangHoatDong) {
        if (taiKhoan.getVaiTro() == VaiTro.CHU_CUA_HANG && taiKhoan.isTrangThai()
                && soChuDangHoatDong <= 1) {
            throw new IllegalStateException(CHU_CUOI);
        }
    }

    private TaiKhoan docTaiKhoan(Connection connection, String tenDangNhap) throws SQLException {
        String sql = "SELECT vaiTro, trangThai FROM TaiKhoan WITH (UPDLOCK, HOLDLOCK) WHERE tenDangNhap = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tenDangNhap);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TaiKhoan taiKhoan = new TaiKhoan();
                taiKhoan.setVaiTro(VaiTro.fromDb(rs.getString(1)));
                taiKhoan.setTrangThai(rs.getBoolean(2));
                return taiKhoan;
            }
        }
    }

    /** cot chỉ nhận tên cột cố định trong lớp này, không nhận dữ liệu người dùng. */
    private boolean tonTai(Connection connection, String cot, String giaTri) throws SQLException {
        String sql = "SELECT 1 FROM TaiKhoan WITH (UPDLOCK, HOLDLOCK) WHERE " + cot + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, giaTri);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** cot chỉ nhận tên cột cố định trong lớp này, không nhận dữ liệu người dùng. */
    private void capNhatCot(Connection connection, String cot, Object giaTri, String tenDangNhap) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE TaiKhoan SET " + cot + " = ? WHERE tenDangNhap = ?")) {
            statement.setObject(1, giaTri);
            statement.setString(2, tenDangNhap);
            statement.executeUpdate();
        }
    }
}
