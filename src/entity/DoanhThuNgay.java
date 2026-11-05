package entity;

import java.time.LocalDate;

/** Doanh thu đã thu trong một ngày, tổng hợp từ hóa đơn nên chỉ đọc. */
public class DoanhThuNgay {
    private final LocalDate ngay;
    private final int soPhieu;
    private final double tienCong;
    private final double tienPhuTung;
    private final double tienVAT;
    private final double tongTien;

    public DoanhThuNgay(LocalDate ngay, int soPhieu, double tienCong, double tienPhuTung, double tienVAT, double tongTien) {
        this.ngay = ngay;
        this.soPhieu = soPhieu;
        this.tienCong = tienCong;
        this.tienPhuTung = tienPhuTung;
        this.tienVAT = tienVAT;
        this.tongTien = tongTien;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public int getSoPhieu() {
        return soPhieu;
    }

    public double getTienCong() {
        return tienCong;
    }

    public double getTienPhuTung() {
        return tienPhuTung;
    }

    public double getTienVAT() {
        return tienVAT;
    }

    public double getTongTien() {
        return tongTien;
    }
}
