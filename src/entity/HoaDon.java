package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class HoaDon {
    private String maHD;
    private String maPhieuTNSC;
    private LocalDateTime ngayLap;
    private double tongTienCong;
    private double tongTienPhuTung;
    private double tongTienGiam;
    private double tongTienVAT;
    private double tongThanhToan;
    private HinhThucThanhToan hinhThucThanhToan = HinhThucThanhToan.TIEN_MAT;
    private TrangThaiHoaDon trangThaiThanhToan = TrangThaiHoaDon.DA_THANH_TOAN;
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
            double tongTienGiam, double tongTienVAT, double tongThanhToan, HinhThucThanhToan hinhThucThanhToan,
            TrangThaiHoaDon trangThaiThanhToan, String lyDoHuy, String maNVLap, String maKM, String maThue) {
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

    /** Thông tin khách và xe của phiếu để hiển thị và in hóa đơn. */
    public static final class ThongTinPhieu {
        public final String maPhieu;
        public final TrangThaiPhieu trangThaiPhieu;
        public final String maKH;
        public final String hoTen;
        public final LoaiKhachHang loaiKhachHang;
        public final String soDienThoai;
        public final String diaChi;
        public final String email;
        public final boolean duocGhiNo;
        public final String bienSo;
        public final String xe;
        public final String soVIN;

        public ThongTinPhieu(String maPhieu, TrangThaiPhieu trangThaiPhieu, String maKH, String hoTen, LoaiKhachHang loaiKhachHang,
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
