package entity;

import java.util.Objects;

public class ChiTietYeuCauXuatKho {
    private String maPhieuYC;
    private String maSKU;
    private int soLuong;

    // Thuộc tính phụ hỗ trợ hiển thị JOIN từ bảng PhuTung
    private String tenPhuTung;
    private String hangSanXuat;
    private int soLuongTon;
    private int nguongCanhBao;

    public ChiTietYeuCauXuatKho() {}

    public ChiTietYeuCauXuatKho(int soLuong) {
        setSoLuong(soLuong);
    }

    public ChiTietYeuCauXuatKho(String maPhieuYC, String maSKU, int soLuong) {
        this.maPhieuYC = maPhieuYC;
        this.maSKU = maSKU;
        setSoLuong(soLuong);
    }

    public String getMaPhieuYC() {
        return maPhieuYC;
    }
    public void setMaPhieuYC(String maPhieuYC) {
        this.maPhieuYC = maPhieuYC;
    }

    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được để trống.");
        }
        this.maSKU = maSKU.trim();
    }

    public int getSoLuong() {
        return soLuong;
    }
    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên dương");
        }
        this.soLuong = soLuong;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }
    public void setTenPhuTung(String tenPhuTung) {
        this.tenPhuTung = tenPhuTung;
    }

    public String getHangSanXuat() {
        return hangSanXuat;
    }
    public void setHangSanXuat(String hangSanXuat) {
        this.hangSanXuat = hangSanXuat;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }
    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public int getNguongCanhBao() {
        return nguongCanhBao;
    }
    public void setNguongCanhBao(int nguongCanhBao) {
        this.nguongCanhBao = nguongCanhBao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietYeuCauXuatKho)) return false;
        ChiTietYeuCauXuatKho that = (ChiTietYeuCauXuatKho) o;
        return Objects.equals(maPhieuYC, that.maPhieuYC) && Objects.equals(maSKU, that.maSKU);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuYC, maSKU);
    }

    @Override
    public String toString() {
        return maPhieuYC + " - " + maSKU + " (SL: " + soLuong + ")";
    }
}