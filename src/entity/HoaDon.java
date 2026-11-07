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
        this.tongTienCong = tongTienCong;
        this.tongTienPhuTung = tongTienPhuTung;
        this.tongTienGiam = tongTienGiam;
        this.tongTienVAT = tongTienVAT;
        this.tongThanhToan = tongThanhToan;
        this.thue = thue;
    }

    /** Hóa đơn đã lưu, đọc kèm lịch sử sửa chữa: chỉ cần trạng thái và tổng tiền. */
    public HoaDon(TrangThaiHoaDon trangThaiThanhToan, double tongThanhToan) {
        setTrangThaiThanhToan(trangThaiThanhToan);
        // Gán thẳng không qua setter
        this.tongThanhToan = tongThanhToan;
    }

    public HoaDon(String maHD, PhieuTiepNhanVaSuaChua phieu, LocalDateTime ngayLap, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, HinhThucThanhToan hinhThucThanhToan,
            TrangThaiHoaDon trangThaiThanhToan, String lyDoHuy, KhuyenMai khuyenMai, Thue thue) {
        setMaHD(maHD);
        setPhieu(phieu);
        setNgayLap(ngayLap);
        // Gán thẳng các giá trị đã tính, không qua setter
        this.tongTienCong = tongTienCong;
        this.tongTienPhuTung = tongTienPhuTung;
        this.tongTienGiam = tongTienGiam;
        this.tongTienVAT = tongTienVAT;
        this.tongThanhToan = tongThanhToan;
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

    public boolean daThanhToan() {
        return trangThaiThanhToan == TrangThaiHoaDon.DA_THANH_TOAN;
    }

    /** Đã trả đủ, hoặc còn nợ nhưng khách được phép ghi nợ. */
    public boolean daThanhToanHopLe(KhachHang khachHang) {
        return daThanhToan() || laCongNoHopLe(khachHang);
    }

    public boolean laCongNoHopLe(KhachHang khachHang) {
        return trangThaiThanhToan == TrangThaiHoaDon.CONG_NO && khachHang.duocThanhToanBang(HinhThucThanhToan.CONG_NO);
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

    /**
     * Kết quả tính toán hóa đơn từ tiền công, phụ tùng, khuyến mãi và thuế.
     * Dùng để tính trước khi lưu hoặc xem trước.
     */
    public static class KetQuaTinhToan {
        public final double tongTienCong;
        public final double tongTienPhuTung;
        public final double tongTienGiam;
        public final double tongTienVAT;
        public final double tongThanhToan;

        public KetQuaTinhToan(double tongTienCong, double tongTienPhuTung, double tongTienGiam,
                              double tongTienVAT, double tongThanhToan) {
            this.tongTienCong = tongTienCong;
            this.tongTienPhuTung = tongTienPhuTung;
            this.tongTienGiam = tongTienGiam;
            this.tongTienVAT = tongTienVAT;
            this.tongThanhToan = tongThanhToan;
        }
    }

    /**
     * Tính toán tất cả các khoản tiền của hóa đơn từ tiền công, tiền phụ tùng, khuyến mãi và thuế.
     * Làm tròn tới đồng. Khuyến mãi hoặc thuế null thì khoản đó bằng 0.
     *
     * @param tienCong Tổng tiền công (trước làm tròn)
     * @param tienPhuTung Tổng tiền phụ tùng (trước làm tròn)
     * @param khuyenMai Khuyến mãi áp dụng (null nếu không có)
     * @param thue Loại thuế áp dụng (null nếu không có)
     * @return Kết quả tính toán đầy đủ
     */
    public static KetQuaTinhToan tinhToan(double tienCong, double tienPhuTung, KhuyenMai khuyenMai, Thue thue) {
        if (tienCong < 0 || tienPhuTung < 0) {
            throw new IllegalArgumentException("Số tiền không được âm.");
        }
        double cong = Math.round(tienCong);
        double phuTung = Math.round(tienPhuTung);
        double giam = khuyenMai == null ? 0 : khuyenMai.tinhTienGiam(cong, phuTung);
        double sauGiam = cong + phuTung - giam;
        double vat = thue == null ? 0 : thue.tinhTienThue(sauGiam);
        return new KetQuaTinhToan(cong, phuTung, giam, vat, sauGiam + vat);
    }
}
