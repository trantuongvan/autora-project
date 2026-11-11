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

    /** Hóa đơn mới chưa có mã, với các khoản tiền đã được tính ở tầng nghiệp vụ. */
    public HoaDon(PhieuTiepNhanVaSuaChua phieu, KhuyenMai khuyenMai, Thue thue, HinhThucThanhToan hinhThucThanhToan,
            TrangThaiHoaDon trangThaiThanhToan, double tongTienCong, double tongTienPhuTung, double tongTienGiam,
            double tongTienVAT, double tongThanhToan) {
        setPhieu(phieu);
        this.khuyenMai = khuyenMai;
        this.thue = thue;
        setHinhThucThanhToan(hinhThucThanhToan);
        setTrangThaiThanhToan(trangThaiThanhToan);
        ghiSoTien(tongTienCong, tongTienPhuTung, tongTienGiam, tongTienVAT, tongThanhToan);
    }

    /** Hóa đơn đã lưu, đọc kèm phiếu bàn giao. */
    public HoaDon(String maHD, TrangThaiHoaDon trangThaiThanhToan, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, Thue thue) {
        setMaHD(maHD);
        setTrangThaiThanhToan(trangThaiThanhToan);
        ghiSoTien(tongTienCong, tongTienPhuTung, tongTienGiam, tongTienVAT, tongThanhToan);
        this.thue = thue;
    }

    /** Hóa đơn đã lưu, đọc kèm lịch sử sửa chữa: chỉ cần trạng thái và tổng tiền. */
    public HoaDon(TrangThaiHoaDon trangThaiThanhToan, double tongThanhToan) {
        setTrangThaiThanhToan(trangThaiThanhToan);
        this.tongThanhToan = khongAm(tongThanhToan, "Tổng thanh toán không hợp lệ");
    }

    public HoaDon(String maHD, PhieuTiepNhanVaSuaChua phieu, LocalDateTime ngayLap, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, HinhThucThanhToan hinhThucThanhToan,
            TrangThaiHoaDon trangThaiThanhToan, String lyDoHuy, KhuyenMai khuyenMai, Thue thue) {
        setMaHD(maHD);
        setPhieu(phieu);
        setNgayLap(ngayLap);
        ghiSoTien(tongTienCong, tongTienPhuTung, tongTienGiam, tongTienVAT, tongThanhToan);
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

    // Không chặn ngày tương lai: ngày do máy chủ CSDL sinh, đồng hồ máy trạm lệch không được làm hỏng việc đọc danh sách.
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

    /** Số tiền chỉ vào hóa đơn qua constructor, không có setter. */
    private void ghiSoTien(double cong, double phuTung, double giam, double vat, double tong) {
        this.tongTienCong = khongAm(cong, "Tổng tiền công không được âm");
        this.tongTienPhuTung = khongAm(phuTung, "Tổng tiền phụ tùng không được âm");
        this.tongTienGiam = khongAm(giam, "Tổng tiền giảm không được âm");
        this.tongTienVAT = khongAm(vat, "Tổng tiền VAT không được âm");
        this.tongThanhToan = khongAm(tong, "Tổng thanh toán không hợp lệ");
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

    /** Null khi hóa đơn không áp dụng khuyến mãi. */
    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    /** Null khi hóa đơn không có loại thuế. */
    public Thue getThue() {
        return thue;
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
