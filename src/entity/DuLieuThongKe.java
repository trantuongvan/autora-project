package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể đóng gói dữ liệu tổng hợp báo cáo thống kê (UC014).
 * Tuân thủ chuẩn OOP: Chỉ chứa thuộc tính private, constructor, getter/setter.
 * Các thực thể chi tiết đã được tách thành các class độc lập trong package entity.
 */
public class DuLieuThongKe {
    // 1. Dữ liệu tổng hợp Tab Doanh thu
    private double tongDoanhThu;
    private double tongTienCong;
    private double tongTienPhuTung;
    private double tongGiamGia;
    private double tongVAT;
    private int tongSoHoaDon;
    private double tongGiaVonPhuTung;
    private List<DoanhThuNgay> danhSachDoanhThuNgay = new ArrayList<>();

    // 2. Dữ liệu tổng hợp Tab Phụ tùng
    private int soMatHangQuanLy;
    private int tongSoLuongNhap;
    private double tongGiaTriNhap;
    private int tongSoLuongXuat;
    private double tongGiaTriXuat;
    private int soPhieuNhapTrongKy;
    private List<ThongKePhuTung> danhSachPhuTung = new ArrayList<>();
    private List<NhapXuatNgay> danhSachNhapXuatNgay = new ArrayList<>();

    // 3. Dữ liệu tổng hợp Tab Hiệu suất Kỹ thuật viên
    private int tongLuotXeVaoXuong;
    private int tongXeSuaXong;
    private List<NangSuatKTV> danhSachNangSuatKTV = new ArrayList<>();
    private List<LuotXeNgay> danhSachLuotXeNgay = new ArrayList<>();

    public double getTongGiaVonPhuTung() { return tongGiaVonPhuTung; }
    public void setTongGiaVonPhuTung(double tongGiaVonPhuTung) { this.tongGiaVonPhuTung = tongGiaVonPhuTung; }

    // Các giá trị dưới đây do báo cáo tự tính từ số liệu đã đọc, nên không có setter.

    /** Lợi nhuận gộp = doanh thu trước thuế sau giảm giá - giá vốn phụ tùng theo giá nhập hiện tại; có thể âm. */
    public double getTongLoiNhuan() {
        return tongTienCong + tongTienPhuTung - tongGiamGia - tongGiaVonPhuTung;
    }

    public double getTyTrongTienCong() {
        return phanTram(tongTienCong, tongTienCong + tongTienPhuTung);
    }

    public double getTyTrongPhuTung() {
        return tongTienCong + tongTienPhuTung > 0 ? Math.round((100.0 - getTyTrongTienCong()) * 10.0) / 10.0 : 0;
    }

    public double getGiaTriTonKhoHienTai() {
        double tong = 0;
        for (ThongKePhuTung pt : danhSachPhuTung) {
            tong += pt.getGiaTriTon();
        }
        return tong;
    }

    public double getTyLeXeSuaXong() {
        return phanTram(tongXeSuaXong, tongLuotXeVaoXuong);
    }

    public int getTongHangMucHoanThanh() {
        int tong = 0;
        for (NangSuatKTV k : danhSachNangSuatKTV) {
            tong += k.getHangMucHoanThanh();
        }
        return tong;
    }

    public int getTongHangMucDangLam() {
        int tong = 0;
        for (NangSuatKTV k : danhSachNangSuatKTV) {
            tong += k.getHangMucDangThucHien();
        }
        return tong;
    }

    public double getTrungBinhHangMucTrenXe() {
        return tongLuotXeVaoXuong > 0 ? Math.round(getTongHangMucHoanThanh() * 10.0 / tongLuotXeVaoXuong) / 10.0 : 0;
    }

    /** Kỹ thuật viên có ít nhất một xe trong kỳ. */
    public int getSoKyThuatVienHoatDong() {
        int dem = 0;
        for (NangSuatKTV k : danhSachNangSuatKTV) {
            if (k.getSoXeThamGia() > 0) {
                dem++;
            }
        }
        return dem;
    }

    public double getTyLeHoanThanhChung() {
        return phanTram(getTongHangMucHoanThanh(), getTongHangMucHoanThanh() + getTongHangMucDangLam());
    }

    /** Phần trăm làm tròn 1 chữ số; mẫu bằng 0 thì 0. */
    static double phanTram(double tu, double mau) {
        return mau > 0 ? Math.round(tu * 1000.0 / mau) / 10.0 : 0;
    }

    public DuLieuThongKe() {
    }

    public double getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(double tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

    public double getTongTienCong() { return tongTienCong; }
    public void setTongTienCong(double tongTienCong) { this.tongTienCong = tongTienCong; }

    public double getTongTienPhuTung() { return tongTienPhuTung; }
    public void setTongTienPhuTung(double tongTienPhuTung) { this.tongTienPhuTung = tongTienPhuTung; }

    public double getTongGiamGia() { return tongGiamGia; }
    public void setTongGiamGia(double tongGiamGia) { this.tongGiamGia = tongGiamGia; }

    public double getTongVAT() { return tongVAT; }
    public void setTongVAT(double tongVAT) { this.tongVAT = tongVAT; }

    public int getTongSoHoaDon() { return tongSoHoaDon; }
    public void setTongSoHoaDon(int tongSoHoaDon) { this.tongSoHoaDon = tongSoHoaDon; }

    public List<DoanhThuNgay> getDanhSachDoanhThuNgay() { return danhSachDoanhThuNgay; }
    public void setDanhSachDoanhThuNgay(List<DoanhThuNgay> danhSachDoanhThuNgay) {
        this.danhSachDoanhThuNgay = danhSachDoanhThuNgay != null ? danhSachDoanhThuNgay : new ArrayList<>();
    }

    public int getSoMatHangQuanLy() { return soMatHangQuanLy; }
    public void setSoMatHangQuanLy(int soMatHangQuanLy) { this.soMatHangQuanLy = soMatHangQuanLy; }

    public int getTongSoLuongNhap() { return tongSoLuongNhap; }
    public void setTongSoLuongNhap(int tongSoLuongNhap) { this.tongSoLuongNhap = tongSoLuongNhap; }

    public double getTongGiaTriNhap() { return tongGiaTriNhap; }
    public void setTongGiaTriNhap(double tongGiaTriNhap) { this.tongGiaTriNhap = tongGiaTriNhap; }

    public int getTongSoLuongXuat() { return tongSoLuongXuat; }
    public void setTongSoLuongXuat(int tongSoLuongXuat) { this.tongSoLuongXuat = tongSoLuongXuat; }

    public double getTongGiaTriXuat() { return tongGiaTriXuat; }
    public void setTongGiaTriXuat(double tongGiaTriXuat) { this.tongGiaTriXuat = tongGiaTriXuat; }

    public int getSoPhieuNhapTrongKy() { return soPhieuNhapTrongKy; }
    public void setSoPhieuNhapTrongKy(int soPhieuNhapTrongKy) { this.soPhieuNhapTrongKy = soPhieuNhapTrongKy; }

    public List<ThongKePhuTung> getDanhSachPhuTung() { return danhSachPhuTung; }
    public void setDanhSachPhuTung(List<ThongKePhuTung> danhSachPhuTung) {
        this.danhSachPhuTung = danhSachPhuTung != null ? danhSachPhuTung : new ArrayList<>();
    }

    public List<NhapXuatNgay> getDanhSachNhapXuatNgay() { return danhSachNhapXuatNgay; }
    public void setDanhSachNhapXuatNgay(List<NhapXuatNgay> danhSachNhapXuatNgay) {
        this.danhSachNhapXuatNgay = danhSachNhapXuatNgay != null ? danhSachNhapXuatNgay : new ArrayList<>();
    }

    public int getTongLuotXeVaoXuong() { return tongLuotXeVaoXuong; }
    public void setTongLuotXeVaoXuong(int tongLuotXeVaoXuong) { this.tongLuotXeVaoXuong = tongLuotXeVaoXuong; }

    public int getTongXeSuaXong() { return tongXeSuaXong; }
    public void setTongXeSuaXong(int tongXeSuaXong) { this.tongXeSuaXong = tongXeSuaXong; }

    public List<NangSuatKTV> getDanhSachNangSuatKTV() { return danhSachNangSuatKTV; }
    public void setDanhSachNangSuatKTV(List<NangSuatKTV> danhSachNangSuatKTV) {
        this.danhSachNangSuatKTV = danhSachNangSuatKTV != null ? danhSachNangSuatKTV : new ArrayList<>();
    }

    public List<LuotXeNgay> getDanhSachLuotXeNgay() { return danhSachLuotXeNgay; }
    public void setDanhSachLuotXeNgay(List<LuotXeNgay> danhSachLuotXeNgay) {
        this.danhSachLuotXeNgay = danhSachLuotXeNgay != null ? danhSachLuotXeNgay : new ArrayList<>();
    }
}