package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private NhanVien nhanVienLap;
    private KhuyenMai khuyenMai;
    private Thue thue;

    public HoaDon() {
    }

    public HoaDon(String maHD) {
        setMaHD(maHD);
    }

    public HoaDon(String maHD, PhieuTiepNhanVaSuaChua phieu, LocalDateTime ngayLap, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, HinhThucThanhToan hinhThucThanhToan,
            TrangThaiHoaDon trangThaiThanhToan, String lyDoHuy, NhanVien nhanVienLap, KhuyenMai khuyenMai, Thue thue) {
        setMaHD(maHD);
        setPhieu(phieu);
        setNgayLap(ngayLap);
        setTongTienCong(tongTienCong);
        setTongTienPhuTung(tongTienPhuTung);
        setTongTienGiam(tongTienGiam);
        setTongTienVAT(tongTienVAT);
        setTongThanhToan(tongThanhToan);
        setHinhThucThanhToan(hinhThucThanhToan);
        setTrangThaiThanhToan(trangThaiThanhToan);
        setLyDoHuy(lyDoHuy);
        this.nhanVienLap = nhanVienLap;
        this.khuyenMai = khuyenMai;
        this.thue = thue;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        if (maHD == null || !maHD.matches("^HD\\d{6}$")) {
            throw new IllegalArgumentException("Mã hóa đơn phải là HD và 6 chữ số.");
        }
        this.maHD = maHD;
    }

    public PhieuTiepNhanVaSuaChua getPhieu() {
        return phieu;
    }

    public void setPhieu(PhieuTiepNhanVaSuaChua phieu) {
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu sửa chữa không tồn tại hoặc chưa xong");
        }
        this.phieu = phieu;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    // Không chặn ngày tương lai: ngày do máy chủ CSDL sinh, đồng hồ máy trạm lệch không được làm hỏng việc đọc danh sách.
    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTongTienCong() {
        return tongTienCong;
    }

    public void setTongTienCong(double tongTienCong) {
        this.tongTienCong = khongAm(tongTienCong, "Tổng tiền công không được âm");
    }

    public double getTongTienPhuTung() {
        return tongTienPhuTung;
    }

    public void setTongTienPhuTung(double tongTienPhuTung) {
        this.tongTienPhuTung = khongAm(tongTienPhuTung, "Tổng tiền phụ tùng không được âm");
    }

    public double getTongTienGiam() {
        return tongTienGiam;
    }

    public void setTongTienGiam(double tongTienGiam) {
        this.tongTienGiam = khongAm(tongTienGiam, "Tổng tiền giảm không được âm");
    }

    public double getTongTienVAT() {
        return tongTienVAT;
    }

    public void setTongTienVAT(double tongTienVAT) {
        this.tongTienVAT = khongAm(tongTienVAT, "Tổng tiền VAT không được âm");
    }

    public double getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(double tongThanhToan) {
        this.tongThanhToan = khongAm(tongThanhToan, "Tổng thanh toán không hợp lệ");
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

    public void setTrangThaiThanhToan(TrangThaiHoaDon trangThaiThanhToan) {
        if (trangThaiThanhToan == null) {
            throw new IllegalArgumentException("Trạng thái thanh toán không hợp lệ");
        }
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getLyDoHuy() {
        return lyDoHuy;
    }

    public void setLyDoHuy(String lyDoHuy) {
        if (lyDoHuy != null && lyDoHuy.trim().length() > 255) {
            throw new IllegalArgumentException("Lý do hủy không được quá 255 ký tự.");
        }
        this.lyDoHuy = lyDoHuy == null ? null : lyDoHuy.trim();
    }

    public NhanVien getNhanVienLap() {
        return nhanVienLap;
    }

    public void setNhanVienLap(NhanVien nhanVienLap) {
        this.nhanVienLap = nhanVienLap;
    }

    /** Null khi hóa đơn không áp dụng khuyến mãi. */
    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    /** Null khi hóa đơn không có loại thuế. */
    public Thue getThue() {
        return thue;
    }

    public void setThue(Thue thue) {
        this.thue = thue;
    }

    /**
     * Tính lại mọi khoản từ tiền công, tiền phụ tùng, khuyến mãi và thuế của hóa đơn, làm tròn tới đồng.
     * Chưa có khuyến mãi hoặc thuế thì khoản đó bằng 0. Màn hình xem trước và lúc lưu dùng cùng công thức này.
     */
    public void tinhTongThanhToan(double tienCong, double tienPhuTung) {
        if (tienCong < 0 || tienPhuTung < 0) {
            throw new IllegalArgumentException("Số tiền không được âm.");
        }
        double cong = Math.round(tienCong);
        double phuTung = Math.round(tienPhuTung);
        double giam = khuyenMai == null ? 0 : khuyenMai.tinhTienGiam(cong, phuTung);
        double sauGiam = cong + phuTung - giam;
        double vat = thue == null ? 0 : thue.tinhTienThue(sauGiam);
        setTongTienCong(cong);
        setTongTienPhuTung(phuTung);
        setTongTienGiam(giam);
        setTongTienVAT(vat);
        setTongThanhToan(sauGiam + vat);
    }

    /** Dùng BigDecimal để số kiểu 80,5 không bị double làm tròn xuống. */
    static double phanTram(double coSo, double tyLe) {
        return BigDecimal.valueOf(coSo).multiply(BigDecimal.valueOf(tyLe))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).doubleValue();
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
