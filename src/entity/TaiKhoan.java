package entity;

import java.util.Arrays;
import java.util.Objects;

/** Tài khoản đăng nhập. Không giữ mật khẩu dạng rõ: mật khẩu chỉ đi qua kiemTraMatKhau rồi được DAO băm. */
public class TaiKhoan {
    public static final String[] VAI_TRO = {PhienDangNhap.VAI_TRO_CHU_CUA_HANG, PhienDangNhap.VAI_TRO_LE_TAN,
        PhienDangNhap.VAI_TRO_KY_THUAT_VIEN, PhienDangNhap.VAI_TRO_THU_KHO};
    public static final String DANG_HOAT_DONG = "Đang hoạt động";
    public static final String BI_KHOA = "Bị khóa";

    private String tenDangNhap;
    private String vaiTro;
    private boolean trangThai = true;
    private String maNV;
    private String hoTen;

    public TaiKhoan() {
    }

    public TaiKhoan(String tenDangNhap) {
        setTenDangNhap(tenDangNhap);
    }

    public TaiKhoan(String tenDangNhap, String vaiTro, boolean trangThai, String maNV, String hoTen) {
        setTenDangNhap(tenDangNhap);
        setVaiTro(vaiTro);
        setTrangThai(trangThai);
        setMaNV(maNV);
        setHoTen(hoTen);
    }

    /** Ném IllegalArgumentException nếu mật khẩu dạng rõ không đạt quy tắc. */
    public static void kiemTraMatKhau(String matKhau) {
        if (matKhau == null || matKhau.length() < 6 || matKhau.matches("(?s).*[\\s\\p{Z}].*")) {
            throw new IllegalArgumentException("Mật khẩu phải từ 6 ký tự trở lên và không chứa khoảng trắng");
        }
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        // Giới hạn 50 theo cột VARCHAR(50) của bảng TaiKhoan.
        if (tenDangNhap == null || !tenDangNhap.matches("^[A-Za-z0-9._]{3,50}$")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập không hợp lệ: 3-50 ký tự gồm chữ, số, dấu chấm, gạch dưới");
        }
        this.tenDangNhap = tenDangNhap;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        if (!Arrays.asList(VAI_TRO).contains(vaiTro)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        }
        this.vaiTro = vaiTro;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThaiHienThi() {
        return trangThai ? DANG_HOAT_DONG : BI_KHOA;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên");
        }
        this.maNV = maNV.trim();
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof TaiKhoan && Objects.equals(tenDangNhap, ((TaiKhoan) o).tenDangNhap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenDangNhap);
    }

    @Override
    public String toString() {
        return tenDangNhap + " - " + vaiTro + " (" + getTrangThaiHienThi() + ")";
    }
}
