package entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class PhieuTiepNhanVaSuaChua {
    public static final String[] UU_TIEN = {"Thường", "Khẩn"};
    public static final String[] TRANG_THAI = {
        "Chờ kiểm tra", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"
    };
    /** Không nằm trong TRANG_THAI vì chỉ đạt được qua thao tác Hủy phiếu. */
    public static final String DA_HUY = "Đã hủy";

    private String maPhieuTNSC;
    private String yeuCauBanDau;
    private int soKm;
    private double mucNhienLieu;
    private String trayXuoc;
    private String taiSanTrenXe;
    private LocalDateTime thoiGianTiepNhan;
    private LocalDateTime thoiGianDuKien;
    private String trangThai;
    private String bienSo;
    private String maNVLapPhieu;
    private String tenKhachHang;
    private String thongTinXe;
    private String tenNVLapPhieu;
    private String danhGiaNoiThat = "";
    private String ghiChu = "";
    private String phuTungDuKien = "";
    private String ghiChuKyThuat = "";
    private String uuTien = UU_TIEN[0];
    private String maNVPhuTrach;
    private String sdtKhachHang;

    public PhieuTiepNhanVaSuaChua() {
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC) {
        setMaPhieuTNSC(maPhieuTNSC);
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC, String yeuCauBanDau,
            int soKm, double mucNhienLieu, String trayXuoc, String taiSanTrenXe,
            LocalDateTime thoiGianTiepNhan, LocalDateTime thoiGianDuKien,
            String trangThai, String bienSo, String maNVLapPhieu) {
        this(maPhieuTNSC, yeuCauBanDau, soKm, mucNhienLieu, trayXuoc,
                taiSanTrenXe, thoiGianTiepNhan, thoiGianDuKien,
                trangThai, bienSo, maNVLapPhieu, null, null, null);
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC, String yeuCauBanDau,
            int soKm, double mucNhienLieu, String trayXuoc, String taiSanTrenXe,
            LocalDateTime thoiGianTiepNhan, LocalDateTime thoiGianDuKien,
            String trangThai, String bienSo, String maNVLapPhieu,
            String tenKhachHang, String thongTinXe, String tenNVLapPhieu) {
        setMaPhieuTNSC(maPhieuTNSC);
        setYeuCauBanDau(yeuCauBanDau);
        setSoKm(soKm);
        setMucNhienLieu(mucNhienLieu);
        setTrayXuoc(trayXuoc);
        setTaiSanTrenXe(taiSanTrenXe);
        // Gán thẳng vì dữ liệu đọc từ CSDL không được phép làm hỏng danh sách khi đồng hồ máy bị lệch.
        this.thoiGianTiepNhan = thoiGianTiepNhan;
        setThoiGianDuKien(thoiGianDuKien);
        setTrangThai(trangThai);
        setBienSo(bienSo);
        setMaNVLapPhieu(maNVLapPhieu);
        setTenKhachHang(tenKhachHang);
        setThongTinXe(thongTinXe);
        setTenNVLapPhieu(tenNVLapPhieu);
        kiemTraHopLe();
    }

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }

    public void setMaPhieuTNSC(String maPhieuTNSC) {
        if (maPhieuTNSC == null || maPhieuTNSC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được để trống.");
        }
        this.maPhieuTNSC = maPhieuTNSC.trim();
    }

    public String getYeuCauBanDau() {
        return yeuCauBanDau;
    }

    public void setYeuCauBanDau(String yeuCauBanDau) {
        this.yeuCauBanDau = yeuCauBanDau;
    }

    public int getSoKm() {
        return soKm;
    }

    public void setSoKm(int soKm) {
        if (soKm < 0) {
            throw new IllegalArgumentException("Số kilômét không được âm.");
        }
        this.soKm = soKm;
    }

    public double getMucNhienLieu() {
        return mucNhienLieu;
    }

    public void setMucNhienLieu(double mucNhienLieu) {
        if (!Double.isFinite(mucNhienLieu)
                || mucNhienLieu < 0 || mucNhienLieu > 100) {
            throw new IllegalArgumentException("Mức nhiên liệu phải nằm trong khoảng từ 0 đến 100.");
        }
        this.mucNhienLieu = mucNhienLieu;
    }

    public String getTrayXuoc() {
        return trayXuoc;
    }

    public void setTrayXuoc(String trayXuoc) {
        this.trayXuoc = trayXuoc;
    }

    public String getTaiSanTrenXe() {
        return taiSanTrenXe;
    }

    public void setTaiSanTrenXe(String taiSanTrenXe) {
        this.taiSanTrenXe = taiSanTrenXe;
    }

    public LocalDateTime getThoiGianTiepNhan() {
        return thoiGianTiepNhan;
    }

    public void setThoiGianTiepNhan(LocalDateTime thoiGianTiepNhan) {
        if (thoiGianTiepNhan != null && thoiGianTiepNhan.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian tiếp nhận không thể ở tương lai.");
        }
        this.thoiGianTiepNhan = thoiGianTiepNhan;
    }

    public LocalDateTime getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    public void setThoiGianDuKien(LocalDateTime thoiGianDuKien) {
        this.thoiGianDuKien = thoiGianDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (!DA_HUY.equals(trangThai) && !Arrays.asList(TRANG_THAI).contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái phiếu tiếp nhận không hợp lệ.");
        }
        this.trangThai = trangThai;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) {
            throw new IllegalArgumentException("Biển số xe không được để trống.");
        }
        this.bienSo = bienSo.trim();
    }

    public String getMaNVLapPhieu() {
        return maNVLapPhieu;
    }

    public void setMaNVLapPhieu(String maNVLapPhieu) {
        if (maNVLapPhieu == null || maNVLapPhieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên lập phiếu không được để trống.");
        }
        this.maNVLapPhieu = maNVLapPhieu.trim();
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getThongTinXe() {
        return thongTinXe;
    }

    public void setThongTinXe(String thongTinXe) {
        this.thongTinXe = thongTinXe;
    }

    public String getTenNVLapPhieu() {
        return tenNVLapPhieu;
    }

    public void setTenNVLapPhieu(String tenNVLapPhieu) {
        this.tenNVLapPhieu = tenNVLapPhieu;
    }

    /** Luồng trạng thái hợp lệ (ràng buộc nghiệp vụ, UC005); phiếu Hoàn thành hoặc Đã hủy không đổi được nữa. */
    public static boolean coTheChuyen(String cu, String moi) {
        switch (String.valueOf(cu)) {
            case "Chờ kiểm tra":
                return "Đang sửa".equals(moi) || "Chờ phụ tùng".equals(moi);
            case "Đang sửa":
                return "Chờ phụ tùng".equals(moi) || "Hoàn thành".equals(moi);
            case "Chờ phụ tùng":
                return "Đang sửa".equals(moi);
            default:
                return false;
        }
    }

    public String getDanhGiaNoiThat() {
        return danhGiaNoiThat;
    }

    public void setDanhGiaNoiThat(String danhGiaNoiThat) {
        this.danhGiaNoiThat = danhGiaNoiThat == null ? "" : danhGiaNoiThat.trim();
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu == null ? "" : ghiChu.trim();
    }

    public String getPhuTungDuKien() {
        return phuTungDuKien;
    }

    public void setPhuTungDuKien(String phuTungDuKien) {
        this.phuTungDuKien = phuTungDuKien == null ? "" : phuTungDuKien.trim();
    }

    public String getGhiChuKyThuat() {
        return ghiChuKyThuat;
    }

    public void setGhiChuKyThuat(String ghiChuKyThuat) {
        this.ghiChuKyThuat = ghiChuKyThuat == null ? "" : ghiChuKyThuat.trim();
    }

    public String getUuTien() {
        return uuTien;
    }

    public void setUuTien(String uuTien) {
        if (!java.util.Arrays.asList(UU_TIEN).contains(uuTien)) {
            throw new IllegalArgumentException("Mức ưu tiên không hợp lệ.");
        }
        this.uuTien = uuTien;
    }

    /** Trả về null khi chưa phân công. */
    public String getMaNVPhuTrach() {
        return maNVPhuTrach;
    }

    public void setMaNVPhuTrach(String maNVPhuTrach) {
        this.maNVPhuTrach = maNVPhuTrach;
    }

    public String getSdtKhachHang() {
        return sdtKhachHang;
    }

    public void setSdtKhachHang(String sdtKhachHang) {
        this.sdtKhachHang = sdtKhachHang;
    }

    public void kiemTraHopLe() throws IllegalArgumentException {
        if (thoiGianTiepNhan != null && thoiGianDuKien != null
                && thoiGianDuKien.isBefore(thoiGianTiepNhan)) {
            throw new IllegalArgumentException(
                    "Thời gian dự kiến không được trước thời gian tiếp nhận.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PhieuTiepNhanVaSuaChua other = (PhieuTiepNhanVaSuaChua) obj;
        return Objects.equals(maPhieuTNSC, other.maPhieuTNSC);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuTNSC);
    }

    @Override
    public String toString() {
        return maPhieuTNSC;
    }
}
