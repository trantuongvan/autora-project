package entity;

import java.time.LocalDate;
import java.util.Objects;

/** Thực thể thống kê số lượng phụ tùng nhập và xuất theo ngày. */
public class NhapXuatNgay {
    private LocalDate ngay;
    private int soLuongNhap;
    private int soLuongXuat;

    public NhapXuatNgay(LocalDate ngay, int soLuongNhap, int soLuongXuat) {
        setNgay(ngay);
        setSoLuongNhap(soLuongNhap);
        setSoLuongXuat(soLuongXuat);
    }

    public LocalDate getNgay() {
        return ngay;
    }

    private void setNgay(LocalDate ngay) {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày thống kê không được để trống.");
        }
        this.ngay = ngay;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhapXuatNgay)) return false;
        NhapXuatNgay that = (NhapXuatNgay) o;
        return Objects.equals(ngay, that.ngay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngay);
    }

    @Override
    public String toString() {
        return ngay + " (Nhập: " + soLuongNhap + ", Xuất: " + soLuongXuat + ")";
    }
}