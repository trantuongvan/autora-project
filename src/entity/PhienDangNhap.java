package entity;

/** Phiên làm việc của người dùng đang đăng nhập, dùng chung toàn ứng dụng. */
public final class PhienDangNhap {
    public static final String VAI_TRO_CHU_CUA_HANG = "Chủ cửa hàng";
    public static final String VAI_TRO_LE_TAN = "Lễ tân";
    public static final String VAI_TRO_KY_THUAT_VIEN = "Kỹ thuật viên";
    public static final String VAI_TRO_THU_KHO = "Thủ kho";

    private static PhienDangNhap hienTai;

    private final String tenDangNhap;
    private final String maNV;
    private final String hoTen;
    private final String vaiTro;

    public PhienDangNhap(String tenDangNhap, String maNV, String hoTen, String vaiTro) {
        this.tenDangNhap = tenDangNhap;
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
    }

    public static void batDau(PhienDangNhap phien) {
        hienTai = phien;
    }

    public static void ketThuc() {
        hienTai = null;
    }

    /** Trả về null khi chưa đăng nhập. */
    public static PhienDangNhap getHienTai() {
        return hienTai;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMaNV() {
        return maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getVaiTro() {
        return vaiTro;
    }
}
