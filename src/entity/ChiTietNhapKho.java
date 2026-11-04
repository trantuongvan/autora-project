package entity;

import java.util.Objects;

public class ChiTietNhapKho {
    private PhieuNhapKho phieuNhapKho;
    private PhuTung phuTung;
    private int soLuongNhap;
    private double giaNhapThucTe;

    public ChiTietNhapKho() {}

    public ChiTietNhapKho(PhieuNhapKho phieuNhapKho, PhuTung phuTung, int soLuongNhap, double giaNhapThucTe) {
        this.phieuNhapKho = phieuNhapKho;
        this.phuTung = phuTung;
        setSoLuongNhap(soLuongNhap);
        setGiaNhapThucTe(giaNhapThucTe);
    }

    public PhieuNhapKho getPhieuNhapKho() {
        return phieuNhapKho;
    }
    public void setPhieuNhapKho(PhieuNhapKho phieuNhapKho) {
        this.phieuNhapKho = phieuNhapKho;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }
    public void setPhuTung(PhuTung phuTung) {
        if (phuTung == null) {
            throw new IllegalArgumentException("Mã SKU không được rỗng.");
        }
        this.phuTung = phuTung;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietNhapKho)) return false;
        ChiTietNhapKho that = (ChiTietNhapKho) o;
        return Objects.equals(phieuNhapKho, that.phieuNhapKho) && Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuNhapKho, phuTung);
    }

    @Override
    public String toString() {
        return (phuTung == null ? null : phuTung.getMaSKU()) + " x" + soLuongNhap;
    }
}