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
        setNgay(ngay);
        setSoHoaDon(soHoaDon);
        setTienCong(tienCong);
        setTienPhuTung(tienPhuTung);
        setGiamGia(giamGia);
        setTienVAT(tienVAT);
        setTongThanhToan(tongThanhToan);
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày thống kê không được để trống.");
        }
        this.ngay = ngay;
    }

    public int getSoHoaDon() {
        return soHoaDon;
    }

    public void setSoHoaDon(int soHoaDon) {
        if (soHoaDon < 0) {
            throw new IllegalArgumentException("Số hóa đơn không được âm.");
        }
        this.soHoaDon = soHoaDon;
    }

    public double getTienCong() {
        return tienCong;
    }

    public void setTienCong(double tienCong) {
        if (tienCong < 0) {
            throw new IllegalArgumentException("Tiền công không được âm.");
        }
        this.tienCong = tienCong;
    }

    public double getTienPhuTung() {
        return tienPhuTung;
    }

    public void setTienPhuTung(double tienPhuTung) {
        if (tienPhuTung < 0) {
            throw new IllegalArgumentException("Tiền phụ tùng không được âm.");
        }
        this.tienPhuTung = tienPhuTung;
    }

    public double getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(double giamGia) {
        if (giamGia < 0) {
            throw new IllegalArgumentException("Tiền giảm giá không được âm.");
        }
        this.giamGia = giamGia;
    }

    public double getTienVAT() {
        return tienVAT;
    }

    public void setTienVAT(double tienVAT) {
        if (tienVAT < 0) {
            throw new IllegalArgumentException("Tiền VAT không được âm.");
        }
        this.tienVAT = tienVAT;
    }

    public double getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(double tongThanhToan) {
        if (tongThanhToan < 0) {
            throw new IllegalArgumentException("Tổng thanh toán không được âm.");
        }
        this.tongThanhToan = tongThanhToan;
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