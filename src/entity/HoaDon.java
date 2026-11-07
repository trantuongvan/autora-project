package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class HoaDon {
    private String maHD;
    private PhieuTiepNhanVaSuaChua phieu;
    private LocalDateTime ngayLap;
    private double tongTienCong;
    private double tongTienPhuTung;
    private double tongTienGiam;
    private double tongTienVAT;
    private double tongThanhToan;
    private HinhThucThanhToan hinhThucThanhToan = HinhThucThanhToan.TIEN_MAT;
    private TrangThaiHoaDon trangThaiThanhToan = TrangThaiHoaDon.DA_THANH_TOAN;
    private String lyDoHuy;
    private KhuyenMai khuyenMai;
    private Thue thue;

    public HoaDon() {
    }

    public HoaDon(String maHD) {
        setMaHD(maHD);
    }

    /** Hóa đơn đã lưu, đọc kèm phiếu bàn giao. */
    public HoaDon(String maHD, TrangThaiHoaDon trangThaiThanhToan, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, Thue thue) {
        setMaHD(maHD);
        setTrangThaiThanhToan(trangThaiThanhToan);
        // Gán thẳng các giá trị đã tính, không qua setter
        this.tongTienCong = khongAm(tongTienCong, "Tổng tiền công không được âm");
        this.tongTienPhuTung = khongAm(tongTienPhuTung, "Tổng tiền phụ tùng không được âm");
        this.tongTienGiam = khongAm(tongTienGiam, "Tổng tiền giảm không được âm");
        this.tongTienVAT = khongAm(tongTienVAT, "Tổng tiền VAT không được âm");
        this.tongThanhToan = khongAm(tongThanhToan, "Tổng thanh toán không hợp lệ");
        this.thue = thue;
    }

    /** Hóa đơn đã lưu, đọc kèm lịch sử sửa chữa: chỉ cần trạng thái và tổng tiền. */
    public HoaDon(TrangThaiHoaDon trangThaiThanhToan, double tongThanhToan) {
        setTrangThaiThanhToan(trangThaiThanhToan);
        // Gán thẳng không qua setter
        this.tongThanhToan = khongAm(tongThanhToan, "Tổng thanh toán không hợp lệ");
    }

    public HoaDon(String maHD, PhieuTiepNhanVaSuaChua phieu, LocalDateTime ngayLap, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, HinhThucThanhToan hinhThucThanhToan,
            TrangThaiHoaDon trangThaiThanhToan, String lyDoHuy, KhuyenMai khuyenMai, Thue thue) {
        setMaHD(maHD);
        setPhieu(phieu);
        setNgayLap(ngayLap);
        // Gán thẳng các giá trị đã tính, không qua setter
        this.tongTienCong = khongAm(tongTienCong, "Tổng tiền công không được âm");
        this.tongTienPhuTung = khongAm(tongTienPhuTung, "Tổng tiền phụ tùng không được âm");
        this.tongTienGiam = khongAm(tongTienGiam, "Tổng tiền giảm không được âm");
        this.tongTienVAT = khongAm(tongTienVAT, "Tổng tiền VAT không được âm");
        this.tongThanhToan = khongAm(tongThanhToan, "Tổng thanh toán không hợp lệ");
        setHinhThucThanhToan(hinhThucThanhToan);
        setTrangThaiThanhToan(trangThaiThanhToan);
        setLyDoHuy(lyDoHuy);
        this.khuyenMai = khuyenMai;
        this.thue = thue;
    }

    public String getMaHD() {
        return maHD;
    }

    private void setMaHD(String maHD) {
        if (maHD == null || !maHD.matches("^HD\\d{6}$")) {
            throw new IllegalArgumentException("Mã hóa đơn phải là HD và 6 chữ số.");
        }
        this.maHD = maHD;
    }

    public PhieuTiepNhanVaSuaChua getPhieu() {
        return phieu;
    }

    private void setPhieu(PhieuTiepNhanVaSuaChua phieu) {
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu sửa chữa không tồn tại hoặc chưa xong");
        }
        this.phieu = phieu;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    private void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTongTienCong() {
        return tongTienCong;
    }

    public double getTongTienPhuTung() {
        return tongTienPhuTung;
    }

    public double getTongTienGiam() {
        return tongTienGiam;
    }

    public double getTongTienVAT() {
        return tongTienVAT;
    }

    public double getTongThanhToan() {
        return tongThanhToan;
    }

    private static double khongAm(double giaTri, String loi) {
        if (giaTri < 0) {
            throw new IllegalArgumentException(loi);
        }
        return giaTri;
    }

    public HinhThucThanhToan getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(HinhThucThanhToan hinhThucThanhToan) {
        if (hinhThucThanhToan == null) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public TrangThaiHoaDon getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    private void setTrangThaiThanhToan(TrangThaiHoaDon trangThaiThanhToan) {
        if (trangThaiThanhToan == null) {
            throw new IllegalArgumentException("Trạng thái thanh toán không hợp lệ");
        }
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getLyDoHuy() {
        return lyDoHuy;
    }

    private void setLyDoHuy(String lyDoHuy) {
        this.lyDoHuy = chuanHoaLyDoHuy(lyDoHuy);
    }

    public static String chuanHoaLyDoHuy(String lyDoHuy) {
        if (lyDoHuy != null && lyDoHuy.trim().length() > 255) {
            throw new IllegalArgumentException("Lý do hủy không được quá 255 ký tự.");
        }
        return lyDoHuy == null ? null : lyDoHuy.trim();
    }

    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public Thue getThue() {
        return thue;
    }

    public void setThue(Thue thue) {
        this.thue = thue;
    }

    public boolean daThanhToan() {
        return trangThaiThanhToan == TrangThaiHoaDon.DA_THANH_TOAN;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HoaDon && Objects.equals(maHD, ((HoaDon) obj).maHD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHD);
    }

    @Override
    public String toString() {
        return "HoaDon{" + maHD + ", phieu=" + (phieu == null ? null : phieu.getMaPhieuTNSC()) + ", tong=" + tongThanhToan
                + ", " + hinhThucThanhToan + ", " + trangThaiThanhToan + "}";
    }
}
