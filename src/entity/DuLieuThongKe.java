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
    private double tongLoiNhuan;
    private int tongSoHoaDon;
    private double tyTrongTienCong;
    private double tyTrongPhuTung;
    private List<DoanhThuNgay> danhSachDoanhThuNgay = new ArrayList<>();

    // 2. Dữ liệu tổng hợp Tab Phụ tùng
    private double giaTriTonKhoHienTai;
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
    private double tyLeXeSuaXong;
    private int tongHangMucHoanThanh;
    private int tongHangMucDangLam;
    private double trungBinhHangMucTrenXe;
    private int soKyThuatVienHoatDong;
    private double tyLeHoanThanhChung;
    private List<NangSuatKTV> danhSachNangSuatKTV = new ArrayList<>();
    private List<LuotXeNgay> danhSachLuotXeNgay = new ArrayList<>();

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

    public double getTongLoiNhuan() { return tongLoiNhuan; }
    public void setTongLoiNhuan(double tongLoiNhuan) { this.tongLoiNhuan = tongLoiNhuan; }

    public int getTongSoHoaDon() { return tongSoHoaDon; }
    public void setTongSoHoaDon(int tongSoHoaDon) { this.tongSoHoaDon = tongSoHoaDon; }

    public double getTyTrongTienCong() { return tyTrongTienCong; }
    public void setTyTrongTienCong(double tyTrongTienCong) { this.tyTrongTienCong = tyTrongTienCong; }

    public double getTyTrongPhuTung() { return tyTrongPhuTung; }
    public void setTyTrongPhuTung(double tyTrongPhuTung) { this.tyTrongPhuTung = tyTrongPhuTung; }

    public List<DoanhThuNgay> getDanhSachDoanhThuNgay() { return danhSachDoanhThuNgay; }
    public void setDanhSachDoanhThuNgay(List<DoanhThuNgay> danhSachDoanhThuNgay) {
        this.danhSachDoanhThuNgay = danhSachDoanhThuNgay != null ? danhSachDoanhThuNgay : new ArrayList<>();
    }

    public double getGiaTriTonKhoHienTai() { return giaTriTonKhoHienTai; }
    public void setGiaTriTonKhoHienTai(double giaTriTonKhoHienTai) { this.giaTriTonKhoHienTai = giaTriTonKhoHienTai; }

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

    public double getTyLeXeSuaXong() { return tyLeXeSuaXong; }
    public void setTyLeXeSuaXong(double tyLeXeSuaXong) { this.tyLeXeSuaXong = tyLeXeSuaXong; }

    public int getTongHangMucHoanThanh() { return tongHangMucHoanThanh; }
    public void setTongHangMucHoanThanh(int tongHangMucHoanThanh) { this.tongHangMucHoanThanh = tongHangMucHoanThanh; }

    public int getTongHangMucDangLam() { return tongHangMucDangLam; }
    public void setTongHangMucDangLam(int tongHangMucDangLam) { this.tongHangMucDangLam = tongHangMucDangLam; }

    public double getTrungBinhHangMucTrenXe() { return trungBinhHangMucTrenXe; }
    public void setTrungBinhHangMucTrenXe(double trungBinhHangMucTrenXe) { this.trungBinhHangMucTrenXe = trungBinhHangMucTrenXe; }

    public int getSoKyThuatVienHoatDong() { return soKyThuatVienHoatDong; }
    public void setSoKyThuatVienHoatDong(int soKyThuatVienHoatDong) { this.soKyThuatVienHoatDong = soKyThuatVienHoatDong; }

    public double getTyLeHoanThanhChung() { return tyLeHoanThanhChung; }
    public void setTyLeHoanThanhChung(double tyLeHoanThanhChung) { this.tyLeHoanThanhChung = tyLeHoanThanhChung; }

    public List<NangSuatKTV> getDanhSachNangSuatKTV() { return danhSachNangSuatKTV; }
    public void setDanhSachNangSuatKTV(List<NangSuatKTV> danhSachNangSuatKTV) {
        this.danhSachNangSuatKTV = danhSachNangSuatKTV != null ? danhSachNangSuatKTV : new ArrayList<>();
    }

    public List<LuotXeNgay> getDanhSachLuotXeNgay() { return danhSachLuotXeNgay; }
    public void setDanhSachLuotXeNgay(List<LuotXeNgay> danhSachLuotXeNgay) {
        this.danhSachLuotXeNgay = danhSachLuotXeNgay != null ? danhSachLuotXeNgay : new ArrayList<>();
    }
}