package dao;

import connection.ConnectDB;
import entity.NhaCungCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCap_Dao {
    private static final int LOI_VI_PHAM_KHOA_NGOAI = 547;

    /** Tìm theo mã, tên hoặc số điện thoại; từ khóa rỗng trả về tất cả. */
    public List<NhaCungCap> search(String tuKhoa) throws SQLException {
        String k = tuKhoa == null ? "" : tuKhoa.trim();
        String sql = "SELECT maNCC, tenNCC, soDienThoai, diaChi FROM NhaCungCap "
                + "WHERE ? = '' OR maNCC LIKE ? OR tenNCC LIKE ? OR soDienThoai LIKE ? ORDER BY maNCC";
        List<NhaCungCap> ketQua = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k);
            for (int i = 2; i <= 4; i++) {
                p.setString(i, "%" + k + "%");
            }
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ketQua.add(new NhaCungCap(r.getString("maNCC"), r.getString("tenNCC"), r.getString("soDienThoai"), r.getString("diaChi")));
                }
            }
        }
        return ketQua;
    }

    /** Thêm mới khi mã rỗng (mã do CSDL sinh trong transaction), ngược lại cập nhật. Trả về mã đã lưu. */
    public String luu(NhaCungCap ncc, String tenDangNhap) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                boolean moi = ncc.getMaNCC() == null;
                String ma = moi ? maMoi(c) : ncc.getMaNCC();
                String sql = moi
                        ? "INSERT INTO NhaCungCap (tenNCC, soDienThoai, diaChi, maNCC) VALUES (?, ?, ?, ?)"
                        : "UPDATE NhaCungCap SET tenNCC = ?, soDienThoai = ?, diaChi = ? WHERE maNCC = ?";
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, ncc.getTenNCC());
                    p.setString(2, ncc.getSoDienThoai());
                    p.setString(3, ncc.getDiaChi());
                    p.setString(4, ma);
                    if (p.executeUpdate() != 1) {
                        throw new SQLException("Không tìm thấy nhà cung cấp " + ma + ".");
                    }
                }
                new NhatKy_Dao().ghi(c, tenDangNhap, moi ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp", ma, null, ncc.getTenNCC());
                c.commit();
                return ma;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** False khi nhà cung cấp đã có phiếu nhập kho. */
    public boolean xoa(String maNCC, String tenDangNhap) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("DELETE FROM NhaCungCap WHERE maNCC = ?")) {
                p.setString(1, maNCC);
                if (p.executeUpdate() != 1) {
                    c.rollback();
                    return false;
                }
                new NhatKy_Dao().ghi(c, tenDangNhap, "Xóa nhà cung cấp", maNCC, null, null);
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
        String sql = "SELECT 'NCC' + RIGHT('0000' + CAST(COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maNCC, 4, 17))), 0) + 1 "
                + "AS VARCHAR(17)), 4) FROM NhaCungCap";
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }


    public List<NhaCungCap> findAll() throws SQLException {
        String sql = "SELECT maNCC, tenNCC, soDienThoai, diaChi FROM NhaCungCap ORDER BY maNCC ASC";
        List<NhaCungCap> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new NhaCungCap(
                        r.getString("maNCC"),
                        r.getString("tenNCC"),
                        r.getString("soDienThoai"),
                        r.getString("diaChi")
                ));
            }
        }
        return list;
    }

    public NhaCungCap findById(String maNCC) throws SQLException {
        String sql = "SELECT maNCC, tenNCC, soDienThoai, diaChi FROM NhaCungCap WHERE maNCC = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maNCC);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return new NhaCungCap(
                            r.getString("maNCC"),
                            r.getString("tenNCC"),
                            r.getString("soDienThoai"),
                            r.getString("diaChi")
                    );
                }
            }
        }
        return null;
    }
}