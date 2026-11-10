package entity;

import java.time.LocalDate;
import java.util.Objects;

/** Thực thể thống kê doanh thu tổng hợp theo từng ngày. */
public class DoanhThuNgay {
    private LocalDate ngay;
    private int soHoaDon;
    private double tienCong;
    private double tienPhuTung;
    private double giamGia;
    private double tienVAT;
    private double tongThanhToan;

    public DoanhThuNgay() {
    }

    public DoanhThuNgay(LocalDate ngay, int soHoaDon, double tienCong, double tienPhuTung,
                        double giamGia, double tienVAT, double tongThanhToan) {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày thống kê không được để trống.");
        }
        if (soHoaDon < 0) {
            throw new IllegalArgumentException("Số hóa đơn không được âm.");
        }
        if (tienCong < 0) {
            throw new IllegalArgumentException("Tiền công không được âm.");
        }
        if (tienPhuTung < 0) {
            throw new IllegalArgumentException("Tiền phụ tùng không được âm.");
        }
        if (giamGia < 0) {
            throw new IllegalArgumentException("Tiền giảm giá không được âm.");
        }
        if (tienVAT < 0) {
            throw new IllegalArgumentException("Tiền VAT không được âm.");
        }
        if (tongThanhToan < 0) {
            throw new IllegalArgumentException("Tổng thanh toán không được âm.");
        }
        this.ngay = ngay;
        this.soHoaDon = soHoaDon;
        this.tienCong = tienCong;
        this.tienPhuTung = tienPhuTung;
        this.giamGia = giamGia;
        this.tienVAT = tienVAT;
        this.tongThanhToan = tongThanhToan;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public int getSoHoaDon() {
        return soHoaDon;
    }

    public double getTienCong() {
        return tienCong;
    }

    public double getTienPhuTung() {
        return tienPhuTung;
    }

    public double getGiamGia() {
        return giamGia;
    }

    public double getTienVAT() {
        return tienVAT;
    }

    public double getTongThanhToan() {
        return tongThanhToan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoanhThuNgay)) return false;
        DoanhThuNgay that = (DoanhThuNgay) o;
        return Objects.equals(ngay, that.ngay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngay);
    }

    @Override
    public String toString() {
        return ngay + " - " + tongThanhToan;
    }
}
