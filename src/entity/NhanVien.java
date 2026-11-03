package entity;

import java.util.Objects;

/** Nhân viên và thông tin tài khoản liên kết lấy từ NhanVien/TaiKhoan. */
public class NhanVien {

    private String maNV;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String viTriLamViec;
    private String tenDangNhap;

    public NhanVien() {
    }

    public NhanVien(String maNV) {
        setMaNV(maNV);
    }

    public NhanVien(String maNV, String hoTen, String soDienThoai, String diaChi, String viTriLamViec) {
        setMaNV(maNV);
        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setDiaChi(diaChi);
        setViTriLamViec(viTriLamViec);
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống.");
        }
        this.maNV = maNV.trim();
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        this.hoTen = hoTen.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || !soDienThoai.trim().matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException(
                    "SĐT phải gồm 10 chữ số và bắt đầu bằng 0.");
        }
        this.soDienThoai = soDienThoai.trim();
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi == null ? "" : diaChi.trim();
    }

    public String getViTriLamViec() {
        return viTriLamViec;
    }

    public void setViTriLamViec(String viTriLamViec) {
        if (viTriLamViec == null || viTriLamViec.trim().isEmpty()) {
            throw new IllegalArgumentException("Vị trí làm việc không được để trống.");
        }
        this.viTriLamViec = viTriLamViec.trim();
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NhanVien)) {
            return false;
        }
        NhanVien that = (NhanVien) obj;
        return Objects.equals(maNV, that.maNV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNV);
    }

    @Override
    public String toString() {
        return maNV + " - " + hoTen;
    }
}
