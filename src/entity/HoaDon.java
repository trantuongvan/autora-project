package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class HoaDon {
    public static final String TIEN_MAT = "Tiền mặt";
    public static final String CHUYEN_KHOAN = "Chuyển khoản";
    public static final String CONG_NO = "Công nợ";
    public static final String[] HINH_THUC = {TIEN_MAT, CHUYEN_KHOAN, CONG_NO};
    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final String DA_HUY = "Đã hủy";
    public static final String[] TRANG_THAI = {DA_THANH_TOAN, CONG_NO, DA_HUY};

    private String maHD;
    private String maPhieuTNSC;
    private LocalDateTime ngayLap;
    private double tongTienCong;
    private double tongTienPhuTung;
    private double tongTienGiam;
    private double tongTienVAT;
    private double tongThanhToan;
    private String hinhThucThanhToan = TIEN_MAT;
    private String trangThaiThanhToan = DA_THANH_TOAN;
    private String lyDoHuy;
    private String maNVLap;
    private String maKM;
    private String maThue;
    // Trường hiển thị lấy từ bảng khác.
    private String tenKhachHang;
    private String bienSo;
    private double thueSuat;

    public HoaDon() {
    }

    public HoaDon(String maHD) {
        setMaHD(maHD);
    }

    public HoaDon(String maHD, String maPhieuTNSC, LocalDateTime ngayLap, double tongTienCong, double tongTienPhuTung,
            double tongTienGiam, double tongTienVAT, double tongThanhToan, String hinhThucThanhToan,
            String trangThaiThanhToan, String lyDoHuy, String maNVLap, String maKM, String maThue) {
        setMaHD(maHD);
        setMaPhieuTNSC(maPhieuTNSC);
        setNgayLap(ngayLap);
        setTongTienCong(tongTienCong);
        setTongTienPhuTung(tongTienPhuTung);
        setTongTienGiam(tongTienGiam);
        setTongTienVAT(tongTienVAT);
        setTongThanhToan(tongThanhToan);
        setHinhThucThanhToan(hinhThucThanhToan);
        setTrangThaiThanhToan(trangThaiThanhToan);
        setLyDoHuy(lyDoHuy);
        this.maNVLap = maNVLap;
        this.maKM = maKM;
        this.maThue = maThue;
    }

    /** Các khoản tiền đã làm tròn tới đồng. */
    public static final class TienHoaDon {
        public final double cong;
        public final double phuTung;
        public final double giam;
        public final double vat;
        public final double tong;

        private TienHoaDon(double cong, double phuTung, double giam, double vat, double tong) {
            this.cong = cong;
            this.phuTung = phuTung;
            this.giam = giam;
            this.vat = vat;
            this.tong = tong;
        }
    }

    /** Thông tin khách và xe của phiếu để hiển thị và in hóa đơn. */
    public static final class ThongTinPhieu {
        public final String maPhieu;
        public final String trangThaiPhieu;
        public final String maKH;
        public final String hoTen;
        public final String loaiKhachHang;
        public final String soDienThoai;
        public final String diaChi;
        public final String email;
        public final boolean duocGhiNo;
        public final String bienSo;
        public final String xe;
        public final String soVIN;

        public ThongTinPhieu(String maPhieu, String trangThaiPhieu, String maKH, String hoTen, String loaiKhachHang,
                String soDienThoai, String diaChi, String email, boolean duocGhiNo, String bienSo, String xe, String soVIN) {
            this.maPhieu = maPhieu;
            this.trangThaiPhieu = trangThaiPhieu;
            this.maKH = maKH;
            this.hoTen = hoTen;
            this.loaiKhachHang = loaiKhachHang;
            this.soDienThoai = soDienThoai;
            this.diaChi = diaChi;
            this.email = email;
            this.duocGhiNo = duocGhiNo;
            this.bienSo = bienSo;
            this.xe = xe;
            this.soVIN = soVIN;
        }
    }

    /** Hàm thuần để màn hình xem trước và lúc lưu dùng cùng một công thức. hinhThucKM null là không có khuyến mãi. */
    public static TienHoaDon tinhTien(double tienCong, double tienPhuTung, String hinhThucKM, double giaTriKM,
            String phamViKM, double thueSuat) {
        if (tienCong < 0 || tienPhuTung < 0 || giaTriKM < 0 || thueSuat < 0) {
            throw new IllegalArgumentException("Số tiền và thuế suất không được âm.");
        }
        double cong = Math.round(tienCong);
        double phuTung = Math.round(tienPhuTung);
        double giam = hinhThucKM == null ? 0 : tinhGiam(cong, phuTung, hinhThucKM, giaTriKM, phamViKM);
        double sauGiam = cong + phuTung - giam;
        double vat = phanTram(sauGiam, thueSuat);
        return new TienHoaDon(cong, phuTung, giam, vat, sauGiam + vat);
    }

    private static double tinhGiam(double cong, double phuTung, String hinhThucKM, double giaTriKM, String phamViKM) {
        double coSo;
        if (KhuyenMai.PHAM_VI[0].equals(phamViKM)) {
            coSo = cong + phuTung;
        } else if (KhuyenMai.PHAM_VI[1].equals(phamViKM)) {
            coSo = cong;
        } else if (KhuyenMai.PHAM_VI[2].equals(phamViKM)) {
            coSo = phuTung;
        } else {
            throw new IllegalArgumentException("Phạm vi khuyến mãi không hợp lệ.");
        }
        if (KhuyenMai.HINH_THUC_PHAN_TRAM.equals(hinhThucKM)) {
            return Math.min(coSo, phanTram(coSo, giaTriKM));
        }
        if (KhuyenMai.HINH_THUC_TIEN_MAT.equals(hinhThucKM)) {
            return Math.min(coSo, Math.round(giaTriKM));
        }
        throw new IllegalArgumentException("Hình thức khuyến mãi không hợp lệ.");
    }

    /** Dùng BigDecimal để số kiểu 80,5 không bị double làm tròn xuống. */
    public static double phanTram(double coSo, double tyLe) {
        return BigDecimal.valueOf(coSo).multiply(BigDecimal.valueOf(tyLe))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).doubleValue();
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

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }

    public void setMaPhieuTNSC(String maPhieuTNSC) {
        if (maPhieuTNSC == null || maPhieuTNSC.trim().isEmpty()) {
            throw new IllegalArgumentException("Phiếu sửa chữa không tồn tại hoặc chưa xong");
        }
        this.maPhieuTNSC = maPhieuTNSC.trim();
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

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        if (!Arrays.asList(HINH_THUC).contains(hinhThucThanhToan)) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        if (!Arrays.asList(TRANG_THAI).contains(trangThaiThanhToan)) {
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

    public String getMaNVLap() {
        return maNVLap;
    }

    public void setMaNVLap(String maNVLap) {
        this.maNVLap = maNVLap;
    }

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getMaThue() {
        return maThue;
    }

    public void setMaThue(String maThue) {
        this.maThue = maThue;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public double getThueSuat() {
        return thueSuat;
    }

    public void setThueSuat(double thueSuat) {
        this.thueSuat = thueSuat;
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
        return "HoaDon{" + maHD + ", phieu=" + maPhieuTNSC + ", tong=" + tongThanhToan
                + ", " + hinhThucThanhToan + ", " + trangThaiThanhToan + "}";
    }
}
