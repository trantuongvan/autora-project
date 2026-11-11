package entity;

import java.time.LocalDate;
import java.util.Objects;

/** Doanh thu đã thu tiền trong một ngày, tổng hợp bằng SQL. Chỉ đọc, không có setter. */
public class DoanhThuNgay {
    private final LocalDate ngay;
    private final int soHoaDon;
    private final double tienCong;
    private final double tienPhuTung;
    private final double giamGia;
    private final double tienVAT;
    private final double tongThanhToan;

    public DoanhThuNgay(LocalDate ngay, int soHoaDon, double tienCong, double tienPhuTung,
                        double giamGia, double tienVAT, double tongThanhToan) {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày thống kê không được để trống.");
        }
        if (soHoaDon < 0 || tienCong < 0 || tienPhuTung < 0 || giamGia < 0 || tienVAT < 0 || tongThanhToan < 0) {
            throw new IllegalArgumentException("Số hóa đơn và số tiền không được âm.");
        }
        this.ngay = ngay;
        this.soHoaDon = soHoaDon;
        this.tienCong = tienCong;
        this.tienPhuTung = tienPhuTung;
        this.giamGia = giamGia;
        this.tienVAT = tienVAT;
        this.tongThanhToan = tongThanhToan;
    }

    public LocalDate getNgay() { return ngay; }
    public int getSoHoaDon() { return soHoaDon; }
    public double getTienCong() { return tienCong; }
    public double getTienPhuTung() { return tienPhuTung; }
    public double getGiamGia() { return giamGia; }
    public double getTienVAT() { return tienVAT; }
    public double getTongThanhToan() { return tongThanhToan; }

    @Override
    public boolean equals(Object o) {
        return o instanceof DoanhThuNgay && Objects.equals(ngay, ((DoanhThuNgay) o).ngay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngay);
    }

    @Override
    public String toString() {
        return ngay + ": " + soHoaDon + " hóa đơn, " + tongThanhToan;
    }
}
