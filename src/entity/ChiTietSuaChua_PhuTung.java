package entity;

import java.util.Objects;

/** Dòng phụ tùng đã dùng trên một phiếu sửa chữa, theo bảng ChiTietSuaChua_PhuTung. */
public class ChiTietSuaChua_PhuTung {
    private PhieuTiepNhanVaSuaChua phieu;
    private PhuTung phuTung;
    private int soLuongSuDung;
    private double giaBanThucTe;

    public ChiTietSuaChua_PhuTung() {
    }

    public ChiTietSuaChua_PhuTung(PhieuTiepNhanVaSuaChua phieu, PhuTung phuTung, int soLuongSuDung, double giaBanThucTe) {
        setPhieu(phieu);
        setPhuTung(phuTung);
        setSoLuongSuDung(soLuongSuDung);
        setGiaBanThucTe(giaBanThucTe);
    }

    public PhieuTiepNhanVaSuaChua getPhieu() {
        return phieu;
    }

    public void setPhieu(PhieuTiepNhanVaSuaChua phieu) {
        if (phieu == null) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được để trống.");
        }
        this.phieu = phieu;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        if (phuTung == null) {
            throw new IllegalArgumentException("Phụ tùng không được để trống.");
        }
        this.phuTung = phuTung;
    }

    public int getSoLuongSuDung() {
        return soLuongSuDung;
    }

    public void setSoLuongSuDung(int soLuongSuDung) {
        if (soLuongSuDung < 0) {
            throw new IllegalArgumentException("Số lượng sử dụng không được âm.");
        }
        this.soLuongSuDung = soLuongSuDung;
    }

    public double getGiaBanThucTe() {
        return giaBanThucTe;
    }

    public void setGiaBanThucTe(double giaBanThucTe) {
        if (!Double.isFinite(giaBanThucTe) || giaBanThucTe < 0) {
            throw new IllegalArgumentException("Giá bán thực tế phải là số hữu hạn không âm.");
        }
        this.giaBanThucTe = giaBanThucTe;
    }

    public double thanhTien() {
        return soLuongSuDung * giaBanThucTe;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChiTietSuaChua_PhuTung)) {
            return false;
        }
        ChiTietSuaChua_PhuTung other = (ChiTietSuaChua_PhuTung) obj;
        return Objects.equals(maPhieu(), other.maPhieu()) && Objects.equals(maSKU(), other.maSKU());
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieu(), maSKU());
    }

    @Override
    public String toString() {
        return maPhieu() + " - " + maSKU();
    }

    private String maPhieu() {
        return phieu == null ? null : phieu.getMaPhieuTNSC();
    }

    private String maSKU() {
        return phuTung == null ? null : phuTung.getMaSKU();
    }
}
