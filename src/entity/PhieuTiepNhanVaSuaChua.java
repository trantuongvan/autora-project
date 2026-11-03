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
    private String trangThai;
    private String bienSo;
    private String maNVLapPhieu;

    // Các thuộc tính phụ lấy từ JOIN Xe, KhachHang, ChiTietSuaChua_DichVu
    private String hangXe;
    private String dongXe;
    private int doiXe;
    private String mauSon;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String tomTatHangMuc;
    private int tongSoHangMuc;
    private int soHangMucHoanThanh;
    private List<ChiTietSuaChua_DichVu> danhSachDichVu = new ArrayList<>();

    public PhieuTiepNhanVaSuaChua() {
        this.thoiGianTiepNhan = LocalDateTime.now();
        this.trangThai = "Chờ kiểm tra";
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC) {
        this();
        setMaPhieuTNSC(maPhieuTNSC);
    }

    public PhieuTiepNhanVaSuaChua(String maPhieuTNSC, String yeuCauBanDau, int soKm, double mucNhienLieu,
                                  String trayXuoc, String taiSanTrenXe, LocalDateTime thoiGianTiepNhan,
                                  LocalDateTime thoiGianDuKien, String trangThai, String bienSo, String maNVLapPhieu) {
        setMaPhieuTNSC(maPhieuTNSC);
        setYeuCauBanDau(yeuCauBanDau);
        setSoKm(soKm);
        setMucNhienLieu(mucNhienLieu);
        setTrayXuoc(trayXuoc);
        setTaiSanTrenXe(taiSanTrenXe);
        setThoiGianTiepNhan(thoiGianTiepNhan);
        setThoiGianDuKien(thoiGianDuKien);
        setTrangThai(trangThai);
        setBienSo(bienSo);
        setMaNVLapPhieu(maNVLapPhieu);
    }

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }
    public void setMaPhieuTNSC(String maPhieuTNSC) {
        if (maPhieuTNSC == null || maPhieuTNSC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu không được để trống.");
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
        if (soKm < 0) throw new IllegalArgumentException("Số Km không được âm.");
        this.soKm = soKm;
    }

    public double getMucNhienLieu() {
        return mucNhienLieu;
    }
    public void setMucNhienLieu(double mucNhienLieu) {
        if (mucNhienLieu < 0.0 || mucNhienLieu > 100.0) {
            throw new IllegalArgumentException("Mức nhiên liệu không hợp lệ (0-100).");
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
        this.thoiGianTiepNhan = thoiGianTiepNhan;
    }

    public LocalDateTime getThoiGianDuKien() {
        return thoiGianDuKien;
    }
    public void setThoiGianDuKien(LocalDateTime thoiGianDuKien) {
        if (thoiGianDuKien != null && thoiGianTiepNhan != null && thoiGianDuKien.isBefore(thoiGianTiepNhan)) {
            throw new IllegalArgumentException("Thời gian dự kiến phải sau thời gian tiếp nhận.");
        }
        this.thoiGianDuKien = thoiGianDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }
    public void setTrangThai(String trangThai) {
        if (trangThai == null || (!trangThai.equals("Chờ kiểm tra")
                && !trangThai.equals("Chờ sửa")
                && !trangThai.equals("Đang sửa")
                && !trangThai.equals("Chờ phụ tùng")
                && !trangThai.equals("Hoàn thành"))) {
            throw new IllegalArgumentException("Trạng thái phiếu không hợp lệ.");
        }
        this.trangThai = trangThai;
    }

    public String getBienSo() {
        return bienSo;
    }
    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public String getMaNVLapPhieu() {
        return maNVLapPhieu;
    }
    public void setMaNVLapPhieu(String maNVLapPhieu) {
        this.maNVLapPhieu = maNVLapPhieu;
    }

    public String getHangXe() {
        return hangXe;
    }
    public void setHangXe(String hangXe) {
        this.hangXe = hangXe;
    }

    public String getDongXe() {
        return dongXe;
    }
    public void setDongXe(String dongXe) {
        this.dongXe = dongXe;
    }

    public int getDoiXe() {
        return doiXe;
    }
    public void setDoiXe(int doiXe) {
        this.doiXe = doiXe;
    }

    public String getMauSon() {
        return mauSon;
    }
    public void setMauSon(String mauSon) {
        this.mauSon = mauSon;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }
    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSdtKhachHang() {
        return sdtKhachHang;
    }
    public void setSdtKhachHang(String sdtKhachHang) {
        this.sdtKhachHang = sdtKhachHang;
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
        this.danhSachDichVu = danhSachDichVu;
    }

    /** Chuỗi hiển thị thông tin xe: "Honda Civic • 2021" */
    public String getThongTinXeNgan() {
        String h = hangXe == null ? "" : hangXe;
        String d = dongXe == null ? "" : dongXe;
        String tenXe = (h + " " + d).trim();
        return doiXe > 0 ? tenXe + " • " + doiXe : tenXe;
    }

    /** Chuỗi hiển thị đầy đủ: "Honda Civic • 2021 • 51H-123.45" */
    public String getThongTinXeDayDu() {
        return getThongTinXeNgan() + " • " + (bienSo == null ? "" : bienSo);
    }

    /** Tính % tiến độ hoàn thành của phiếu dựa trên các hạng mục dịch vụ */
    public int getPhanTramTienDo() {
        if ("Hoàn thành".equalsIgnoreCase(trangThai)) return 100;
        if ("Chờ kiểm tra".equalsIgnoreCase(trangThai) || "Chờ sửa".equalsIgnoreCase(trangThai)) {
            if (tongSoHangMuc > 0 && soHangMucHoanThanh > 0) {
                return Math.round((soHangMucHoanThanh * 100f) / tongSoHangMuc);
            }
            return 0;
        }
        if (tongSoHangMuc > 0) {
            int pct = Math.round((soHangMucHoanThanh * 100f) / tongSoHangMuc);
            if ("Đang sửa".equalsIgnoreCase(trangThai) && pct == 0) return 50;
            return pct;
        }
        return "Đang sửa".equalsIgnoreCase(trangThai) ? 50 : 0;
    }

    /** Hiển thị nhãn trạng thái đồng nhất ("Chờ kiểm tra" hoặc "Chờ sửa" hiển thị là "Chờ sửa" cho KTV) */
    public String getTrangThaiHienThiKTV() {
        if ("Chờ kiểm tra".equalsIgnoreCase(trangThai)) return "Chờ sửa";
        return trangThai == null ? "Chờ sửa" : trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhieuTiepNhanVaSuaChua)) return false;
        PhieuTiepNhanVaSuaChua that = (PhieuTiepNhanVaSuaChua) o;
        return Objects.equals(maPhieuTNSC, that.maPhieuTNSC);
    }

    @Override
    public int hashCode() { return Objects.hash(maPhieuTNSC); }

    @Override
    public String toString() { return maPhieuTNSC + " - " + bienSo; }
}