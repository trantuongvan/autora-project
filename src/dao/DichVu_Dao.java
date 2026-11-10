package dao;

import connection.ConnectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVu_Dao {
    private static final int LOI_VI_PHAM_KHOA_NGOAI = 547;

    /** Tìm theo mã hoặc tên; từ khóa rỗng trả về tất cả. */
    public List<DichVu> search(String tuKhoa) throws SQLException {
        String k = tuKhoa == null ? "" : tuKhoa.trim();
        String sql = "SELECT maDichVu, tenDichVu, tienCong, thoiGianChuan FROM DichVu "
                + "WHERE ? = '' OR maDichVu LIKE ? OR tenDichVu LIKE ? ORDER BY maDichVu";
        List<DichVu> ketQua = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k);
            p.setString(2, "%" + k + "%");
            p.setString(3, "%" + k + "%");
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(doc(rs));
                }
            }
        }
        return ketQua;
    }

    /** Thêm mới khi mã rỗng (mã do CSDL sinh trong transaction), ngược lại cập nhật. Trả về mã đã lưu. */
    public String luu(DichVu dv, String tenDangNhap) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                boolean moi = dv.getMaDichVu() == null;
                String ma = moi ? maMoi(c) : dv.getMaDichVu();
                String sql = moi
                        ? "INSERT INTO DichVu (tenDichVu, tienCong, thoiGianChuan, maDichVu) VALUES (?, ?, ?, ?)"
                        : "UPDATE DichVu SET tenDichVu = ?, tienCong = ?, thoiGianChuan = ? WHERE maDichVu = ?";
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, dv.getTenDichVu());
                    p.setDouble(2, dv.getTienCong());
                    p.setInt(3, dv.getThoiGianChuan());
                    p.setString(4, ma);
                    if (p.executeUpdate() != 1) {
                        throw new SQLException("Không tìm thấy dịch vụ " + ma + ".");
                    }
                }
                new NhatKy_Dao().ghi(c, tenDangNhap, moi ? "Thêm dịch vụ" : "Sửa dịch vụ", ma, null,
                        dv.getTenDichVu() + " • " + util.FormatUtil.formatCurrency(dv.getTienCong()) + " • " + dv.getThoiGianChuan() + " phút");
                c.commit();
                return ma;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** False khi dịch vụ đã dùng trên phiếu hoặc là kỹ năng của kỹ thuật viên. */
    public boolean xoa(String maDichVu, String tenDangNhap) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("DELETE FROM DichVu WHERE maDichVu = ?")) {
                p.setString(1, maDichVu);
                if (p.executeUpdate() != 1) {
                    c.rollback();
                    return false;
                }
                new NhatKy_Dao().ghi(c, tenDangNhap, "Xóa dịch vụ", maDichVu, null, null);
                c.commit();
                return true;
            } catch (SQLException ex) {
                c.rollback();
                if (ex.getErrorCode() == LOI_VI_PHAM_KHOA_NGOAI) {
                    return false;
                }
                throw ex;
            }
        }
    }

    private static String maMoi(Connection c) throws SQLException {
        String sql = "SELECT 'DV' + RIGHT('0000' + CAST(COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maDichVu, 3, 18))), 0) + 1 "
                + "AS VARCHAR(18)), 4) FROM DichVu";
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    private static DichVu doc(ResultSet rs) throws SQLException {
        DichVu dv = new DichVu(rs.getString("maDichVu"), rs.getString("tenDichVu"), rs.getDouble("tienCong"));
        dv.setThoiGianChuan(rs.getInt("thoiGianChuan"));
        return dv;
    }

    public List<DichVu> findAll() throws SQLException {
        String sql = "SELECT maDichVu, tenDichVu, tienCong, thoiGianChuan "
                + "FROM DichVu ORDER BY maDichVu";
        List<DichVu> result = new ArrayList<>();

        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                DichVu dv = new DichVu(
                        rs.getString("maDichVu"),
                        rs.getString("tenDichVu"),
                        rs.getDouble("tienCong"));
                dv.setThoiGianChuan(rs.getInt("thoiGianChuan"));
                result.add(dv);
            }
        }
        return result;
    }
}
