package util;

import entity.VaiTro;

/** Phiên làm việc của người dùng đang đăng nhập, dùng chung toàn ứng dụng. */
public final class PhienDangNhap {
    private static PhienDangNhap hienTai;

    private final String tenDangNhap;
    private final String maNV;
    private final String hoTen;
    private final VaiTro vaiTro;

    public PhienDangNhap(String tenDangNhap, String maNV, String hoTen, VaiTro vaiTro) {
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

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    /** Mã KTV để lọc phiếu: kỹ thuật viên chỉ thấy phiếu của mình, chủ cửa hàng thấy tất cả (chuỗi rỗng). */
    public static String maKyThuatVienDeLoc() {
        return hienTai != null && hienTai.vaiTro == VaiTro.KY_THUAT_VIEN && hienTai.maNV != null ? hienTai.maNV : "";
    }
}
