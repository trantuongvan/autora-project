package entity;

import java.util.Objects;

/** Thực thể thống kê nhập - xuất - tồn của một mặt hàng phụ tùng. */
public class ThongKePhuTung {
    private PhuTung phuTung;
    private int soLuongNhap;
    private int soLuongXuat;

    public ThongKePhuTung() {
    }

    public ThongKePhuTung(PhuTung phuTung, int soLuongNhap, int soLuongXuat) {
        setPhuTung(phuTung);
        setSoLuongNhap(soLuongNhap);
        setSoLuongXuat(soLuongXuat);
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

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        if (soLuongNhap < 0) {
            throw new IllegalArgumentException("Số lượng nhập không được âm.");
        }
        this.soLuongNhap = soLuongNhap;
    }

    public int getSoLuongXuat() {
        return soLuongXuat;
    }

    public void setSoLuongXuat(int soLuongXuat) {
        if (soLuongXuat < 0) {
            throw new IllegalArgumentException("Số lượng xuất không được âm.");
        }
        this.soLuongXuat = soLuongXuat;
    }

    public int getTonHienTai() {
        return phuTung.getSoLuongTon();
    }

    /** Giá trị tồn theo giá nhập hiện tại của phụ tùng. */
    public double getGiaTriTon() {
        return phuTung.getSoLuongTon() * phuTung.getGiaNhap();
    }

    public boolean isDuoiNguongCanhBao() {
        return phuTung.kiemTraCanhBaoTon();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThongKePhuTung)) return false;
        ThongKePhuTung that = (ThongKePhuTung) o;
        return Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phuTung);
    }

    @Override
    public String toString() {
        return phuTung == null ? "" : phuTung.toString();
    }
}