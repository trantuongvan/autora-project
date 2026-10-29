package entity;

import java.util.Objects;

/** Nhân viên và thông tin tài khoản liên kết lấy từ NhanVien/TaiKhoan. */
public class NhanVien {
    private String maNV, hoTen, soDienThoai, diaChi, viTriLamViec, tenDangNhap;

    public NhanVien() {}
    public NhanVien(String maNV) { setMaNV(maNV); }
    public NhanVien(String maNV, String hoTen, String soDienThoai, String diaChi, String viTriLamViec) {
        setMaNV(maNV); setHoTen(hoTen); setSoDienThoai(soDienThoai);
        this.diaChi = diaChi == null ? "" : diaChi.trim();
        setViTriLamViec(viTriLamViec);
    }
    public String getMaNV() { return maNV; }
    public void setMaNV(String v) { maNV = required(v, "Mã nhân viên"); }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String v) { hoTen = required(v, "Họ tên"); }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String v) {
        if (v == null || !v.trim().matches("0[0-9]{9}")) throw new IllegalArgumentException("SĐT phải gồm 10 chữ số và bắt đầu bằng 0.");
        soDienThoai = v.trim();
    }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String v) { diaChi = v == null ? "" : v.trim(); }
    public String getViTriLamViec() { return viTriLamViec; }
    public void setViTriLamViec(String v) { viTriLamViec = required(v, "Vị trí làm việc"); }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String v) { tenDangNhap = v; }
    private static String required(String v, String label) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(label + " không được để trống.");
        return v.trim();
    }
    @Override public boolean equals(Object obj) { return this==obj || obj instanceof NhanVien && Objects.equals(maNV,((NhanVien)obj).maNV); }
    @Override public int hashCode() { return Objects.hash(maNV); }
    @Override public String toString() { return maNV+" - "+hoTen; }
}
