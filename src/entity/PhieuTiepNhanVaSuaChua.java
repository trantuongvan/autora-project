package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhieuTiepNhanVaSuaChua {
    private String maPhieuTNSC;
    private String yeuCauBanDau;
    private int soKm;
    private double mucNhienLieu;
    private String trayXuoc;
    private String taiSanTrenXe;
    private LocalDateTime thoiGianTiepNhan;
    private LocalDateTime thoiGianDuKien;
    private TrangThaiPhieu trangThai;
    private Xe xe;
    private NhanVien nhanVienLap;
    private String danhGiaNoiThat = "";
    private String ghiChu = "";
    private String phuTungDuKien = "";
    private String ghiChuKyThuat = "";
    private MucUuTien uuTien = MucUuTien.THUONG;
    private NhanVien nhanVienPhuTrach;

    // Thuộc tính bổ sung cho phân hệ Kỹ thuật viên & Thủ kho
    private String tomTatHangMuc;
    private int tongSoHangMuc;
    private int soHangMucHoanThanh;
    private List<ChiTietSuaChua_DichVu> danhSachDichVu = new ArrayList<>();

    public PhieuTiepNhanVaSuaChua() {
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC) {
        setMaPhieuTNSC(maPhieuTNSC);
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC, String yeuCauBanDau,
                                  int soKm, double mucNhienLieu, String trayXuoc, String taiSanTrenXe,
                                  LocalDateTime thoiGianTiepNhan, LocalDateTime thoiGianDuKien,
                                  TrangThaiPhieu trangThai, Xe xe, NhanVien nhanVienLap) {
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
        setXe(xe);
        setNhanVienLap(nhanVienLap);
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
        this.yeuCauBanDau = yeuCauBanDau == null ? "" : yeuCauBanDau.trim();
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
        this.trayXuoc = trayXuoc == null ? "" : trayXuoc.trim();
    }

    public String getTaiSanTrenXe() {
        return taiSanTrenXe;
    }

    public void setTaiSanTrenXe(String taiSanTrenXe) {
        this.taiSanTrenXe = taiSanTrenXe == null ? "" : taiSanTrenXe.trim();
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

    public TrangThaiPhieu getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiPhieu trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException("Trạng thái phiếu tiếp nhận không hợp lệ.");
        }
        this.trangThai = trangThai;
    }

    public Xe getXe() {
        return xe;
    }

    public void setXe(Xe xe) {
        if (xe == null) {
            throw new IllegalArgumentException("Biển số xe không được để trống.");
        }
        this.xe = xe;
    }

    public NhanVien getNhanVienLap() {
        return nhanVienLap;
    }

    public void setNhanVienLap(NhanVien nhanVienLap) {
        if (nhanVienLap == null) {
            throw new IllegalArgumentException("Mã nhân viên lập phiếu không được để trống.");
        }
        this.nhanVienLap = nhanVienLap;
    }

    public static boolean coTheChuyen(TrangThaiPhieu cu, TrangThaiPhieu moi) {
        if (cu == null) {
            return false;
        }
        switch (cu) {
            case CHO_KIEM_TRA:
                return moi == TrangThaiPhieu.DANG_SUA || moi == TrangThaiPhieu.CHO_PHU_TUNG;
            case DANG_SUA:
                return moi == TrangThaiPhieu.CHO_PHU_TUNG || moi == TrangThaiPhieu.HOAN_THANH;
            case CHO_PHU_TUNG:
                return moi == TrangThaiPhieu.DANG_SUA;
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

    public MucUuTien getUuTien() {
        return uuTien;
    }

    public void setUuTien(MucUuTien uuTien) {
        if (uuTien == null) {
            throw new IllegalArgumentException("Mức ưu tiên không hợp lệ.");
        }
        this.uuTien = uuTien;
    }

    public NhanVien getNhanVienPhuTrach() {
        return nhanVienPhuTrach;
    }

    public void setNhanVienPhuTrach(NhanVien nhanVienPhuTrach) {
        this.nhanVienPhuTrach = nhanVienPhuTrach;
    }

    public String getTomTatHangMuc() {
        return tomTatHangMuc;
    }

    public void setTomTatHangMuc(String tomTatHangMuc) {
        this.tomTatHangMuc = tomTatHangMuc;
    }

    public int getTongSoHangMuc() {
        return tongSoHangMuc;
    }

    public void setTongSoHangMuc(int tongSoHangMuc) {
        this.tongSoHangMuc = tongSoHangMuc;
    }

    public int getSoHangMucHoanThanh() {
        return soHangMucHoanThanh;
    }

    public void setSoHangMucHoanThanh(int soHangMucHoanThanh) {
        this.soHangMucHoanThanh = soHangMucHoanThanh;
    }

    public List<ChiTietSuaChua_DichVu> getDanhSachDichVu() {
        return danhSachDichVu;
    }

    public void setDanhSachDichVu(List<ChiTietSuaChua_DichVu> danhSachDichVu) {
        this.danhSachDichVu = danhSachDichVu != null ? danhSachDichVu : new ArrayList<>();
    }

    public void kiemTraHopLe() throws IllegalArgumentException {
        if (thoiGianTiepNhan != null && thoiGianDuKien != null
                && thoiGianDuKien.isBefore(thoiGianTiepNhan)) {
            throw new IllegalArgumentException(
                    "Thời gian dự kiến không được trước thời gian tiếp nhận.");
        }
    }

    /** Hãng và dòng xe: "Honda Civic" */
    public String getTenXe() {
        if (xe == null) {
            return "";
        }
        String h = xe.getHangXe() == null ? "" : xe.getHangXe();
        String d = xe.getDongXe() == null ? "" : xe.getDongXe();
        return (h + " " + d).trim();
    }

    /** Chuỗi hiển thị thông tin xe gọn gàng: "Honda Civic • 2021" */
    public String getThongTinXeNgan() {
        Integer doiXe = xe == null ? null : xe.getDoiXe();
        return doiXe != null && doiXe > 0 ? getTenXe() + " • " + doiXe : getTenXe();
    }

    public String getThongTinXeDayDu() {
        return getThongTinXeNgan() + " • " + (xe == null ? "" : xe.getBienSo());
    }

    /** Hiển thị nhãn trạng thái đồng nhất cho riêng phân hệ Kỹ thuật viên */
    public String getTrangThaiHienThiKTV() {
        return trangThai == null || trangThai == TrangThaiPhieu.CHO_KIEM_TRA ? "Chờ sửa" : trangThai.getText();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
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