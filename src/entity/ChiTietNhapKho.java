package entity;

import java.util.Objects;

public class ChiTietNhapKho {
    private String maPhieuNhap;
    private String maSKU;
    private int soLuongNhap;
    private double giaNhapThucTe;

    // Thuộc tính phụ hiển thị JOIN
    private String tenPhuTung;

    public ChiTietNhapKho() {}

    public ChiTietNhapKho(String maPhieuNhap, String maSKU, int soLuongNhap, double giaNhapThucTe) {
        this.maPhieuNhap = maPhieuNhap;
        this.maSKU = maSKU;
        setSoLuongNhap(soLuongNhap);
        setGiaNhapThucTe(giaNhapThucTe);
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }
    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap;
    }

    public String getMaSKU() {
        return maSKU;
    }
    public void setMaSKU(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được rỗng.");
        }
        this.maSKU = maSKU.trim();
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }
    public void setSoLuongNhap(int soLuongNhap) {
        if (soLuongNhap <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải là số nguyên dương");
        }
        this.soLuongNhap = soLuongNhap;
    }

    public double getGiaNhapThucTe() {
        return giaNhapThucTe;
    }
    public void setGiaNhapThucTe(double giaNhapThucTe) {
        if (giaNhapThucTe < 0) {
            throw new IllegalArgumentException("Giá nhập thực tế không được là số âm");
        }
        this.giaNhapThucTe = giaNhapThucTe;
    }

    public double getThanhTien() {
        return soLuongNhap * giaNhapThucTe;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }
    public void setTenPhuTung(String tenPhuTung) {
        this.tenPhuTung = tenPhuTung;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietNhapKho)) return false;
        ChiTietNhapKho that = (ChiTietNhapKho) o;
        return Objects.equals(maPhieuNhap, that.maPhieuNhap) && Objects.equals(maSKU, that.maSKU);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuNhap, maSKU);
    }

    @Override
    public String toString() {
        return maSKU + " x" + soLuongNhap;
    }
}