package entity;

import java.util.Objects;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String diaChi;

    public NhaCungCap() {}

    public NhaCungCap(String maNCC) {
        setMaNCC(maNCC);
    }

    public NhaCungCap(String maNCC, String tenNCC, String soDienThoai, String diaChi) {
        setMaNCC(maNCC);
        setTenNCC(tenNCC);
        setSoDienThoai(soDienThoai);
        setDiaChi(diaChi);
    }

    public String getMaNCC() {
        return maNCC;
    }
    public void setMaNCC(String maNCC) {
        if (maNCC == null || maNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được rỗng.");
        }
        this.maNCC = maNCC.trim();
    }

    public String getTenNCC() {
        return tenNCC;
    }
    public void setTenNCC(String tenNCC) {
        if (tenNCC == null || tenNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được rỗng.");
        }
        this.tenNCC = tenNCC.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }
    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai != null && !soDienThoai.trim().isEmpty() && !soDienThoai.trim().matches("^0[0-9]{9,10}$")) {
            throw new IllegalArgumentException("Số điện thoại nhà cung cấp không hợp lệ.");
        }
        this.soDienThoai = soDienThoai == null ? "" : soDienThoai.trim();
    }

    public String getDiaChi() {
        return diaChi;
    }
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi == null ? "" : diaChi.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhaCungCap)) return false;
        NhaCungCap that = (NhaCungCap) o;
        return Objects.equals(maNCC, that.maNCC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNCC);
    }

    @Override
    public String toString() {
        return tenNCC;
    }
}