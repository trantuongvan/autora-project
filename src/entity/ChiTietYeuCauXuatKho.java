package entity;

import java.util.Objects;

public class ChiTietYeuCauXuatKho {
    private PhieuYeuCauXuatKho phieuYeuCau;
    private PhuTung phuTung;
    private int soLuong;

    public ChiTietYeuCauXuatKho() {}

    public ChiTietYeuCauXuatKho(int soLuong) {
        setSoLuong(soLuong);
    }

    public ChiTietYeuCauXuatKho(PhieuYeuCauXuatKho phieuYeuCau, PhuTung phuTung, int soLuong) {
        this.phieuYeuCau = phieuYeuCau;
        this.phuTung = phuTung;
        setSoLuong(soLuong);
    }

    public PhieuYeuCauXuatKho getPhieuYeuCau() {
        return phieuYeuCau;
    }
    public void setPhieuYeuCau(PhieuYeuCauXuatKho phieuYeuCau) {
        this.phieuYeuCau = phieuYeuCau;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }
    public void setPhuTung(PhuTung phuTung) {
        if (phuTung == null) {
            throw new IllegalArgumentException("Mã SKU không được để trống.");
        }
        this.phuTung = phuTung;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietYeuCauXuatKho)) return false;
        ChiTietYeuCauXuatKho that = (ChiTietYeuCauXuatKho) o;
        return Objects.equals(phieuYeuCau, that.phieuYeuCau) && Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuYeuCau, phuTung);
    }

    @Override
    public String toString() {
        return (phieuYeuCau == null ? null : phieuYeuCau.getMaPhieuYC()) + " - "
                + (phuTung == null ? null : phuTung.getMaSKU()) + " (SL: " + soLuong + ")";
    }
}