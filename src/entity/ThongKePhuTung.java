package entity;

import java.util.Objects;

/** Thực thể thống kê nhập - xuất - tồn của một mặt hàng phụ tùng. */
public class ThongKePhuTung {
    private PhuTung phuTung;
    private int soLuongNhap;
    private int soLuongXuat;
    private final double giaTriNhap;
    private final double giaTriXuat;

    /** giaTriNhap, giaTriXuat: tổng tiền nhập và xuất của mặt hàng trong kỳ, tính bằng SQL. */
    public ThongKePhuTung(PhuTung phuTung, int soLuongNhap, int soLuongXuat, double giaTriNhap, double giaTriXuat) {
        setPhuTung(phuTung);
        setSoLuongNhap(soLuongNhap);
        setSoLuongXuat(soLuongXuat);
        if (giaTriNhap < 0 || giaTriXuat < 0) {
            throw new IllegalArgumentException("Giá trị nhập, xuất không được âm.");
        }
        this.giaTriNhap = giaTriNhap;
        this.giaTriXuat = giaTriXuat;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    private void setPhuTung(PhuTung phuTung) {
        if (phuTung == null) {
            throw new IllegalArgumentException("Phụ tùng không được để trống.");
        }
        this.phuTung = phuTung;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    private void setSoLuongNhap(int soLuongNhap) {
        if (soLuongNhap < 0) {
            throw new IllegalArgumentException("Số lượng nhập không được âm.");
        }
        this.soLuongNhap = soLuongNhap;
    }

    public int getSoLuongXuat() {
        return soLuongXuat;
    }

    private void setSoLuongXuat(int soLuongXuat) {
        if (soLuongXuat < 0) {
            throw new IllegalArgumentException("Số lượng xuất không được âm.");
        }
        this.soLuongXuat = soLuongXuat;
    }

    public double getGiaTriNhap() {
        return giaTriNhap;
    }

    public double getGiaTriXuat() {
        return giaTriXuat;
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