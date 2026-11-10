package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể đóng gói dữ liệu tổng hợp báo cáo thống kê (UC014).
 * Chỉ khởi tạo các chỉ số tổng hợp qua Constructor và cung cấp getter.
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

    public DuLieuThongKe(
            double tongDoanhThu, double tongTienCong, double tongTienPhuTung,
            double tongGiamGia, double tongVAT, double tongLoiNhuan,
            int tongSoHoaDon, double tyTrongTienCong, double tyTrongPhuTung,
            List<DoanhThuNgay> danhSachDoanhThuNgay,
            double giaTriTonKhoHienTai, int soMatHangQuanLy,
            int tongSoLuongNhap, double tongGiaTriNhap,
            int tongSoLuongXuat, double tongGiaTriXuat,
            int soPhieuNhapTrongKy,
            List<ThongKePhuTung> danhSachPhuTung, List<NhapXuatNgay> danhSachNhapXuatNgay,
            int tongLuotXeVaoXuong, int tongXeSuaXong, double tyLeXeSuaXong,
            int tongHangMucHoanThanh, int tongHangMucDangLam,
            double trungBinhHangMucTrenXe, int soKyThuatVienHoatDong, double tyLeHoanThanhChung,
            List<NangSuatKTV> danhSachNangSuatKTV, List<LuotXeNgay> danhSachLuotXeNgay) {
        this.tongDoanhThu = tongDoanhThu;
        this.tongTienCong = tongTienCong;
        this.tongTienPhuTung = tongTienPhuTung;
        this.tongGiamGia = tongGiamGia;
        this.tongVAT = tongVAT;
        this.tongLoiNhuan = tongLoiNhuan;
        this.tongSoHoaDon = tongSoHoaDon;
        this.tyTrongTienCong = tyTrongTienCong;
        this.tyTrongPhuTung = tyTrongPhuTung;
        this.danhSachDoanhThuNgay = danhSachDoanhThuNgay != null ? danhSachDoanhThuNgay : new ArrayList<>();
        this.giaTriTonKhoHienTai = giaTriTonKhoHienTai;
        this.soMatHangQuanLy = soMatHangQuanLy;
        this.tongSoLuongNhap = tongSoLuongNhap;
        this.tongGiaTriNhap = tongGiaTriNhap;
        this.tongSoLuongXuat = tongSoLuongXuat;
        this.tongGiaTriXuat = tongGiaTriXuat;
        this.soPhieuNhapTrongKy = soPhieuNhapTrongKy;
        this.danhSachPhuTung = danhSachPhuTung != null ? danhSachPhuTung : new ArrayList<>();
        this.danhSachNhapXuatNgay = danhSachNhapXuatNgay != null ? danhSachNhapXuatNgay : new ArrayList<>();
        this.tongLuotXeVaoXuong = tongLuotXeVaoXuong;
        this.tongXeSuaXong = tongXeSuaXong;
        this.tyLeXeSuaXong = tyLeXeSuaXong;
        this.tongHangMucHoanThanh = tongHangMucHoanThanh;
        this.tongHangMucDangLam = tongHangMucDangLam;
        this.trungBinhHangMucTrenXe = trungBinhHangMucTrenXe;
        this.soKyThuatVienHoatDong = soKyThuatVienHoatDong;
        this.tyLeHoanThanhChung = tyLeHoanThanhChung;
        this.danhSachNangSuatKTV = danhSachNangSuatKTV != null ? danhSachNangSuatKTV : new ArrayList<>();
        this.danhSachLuotXeNgay = danhSachLuotXeNgay != null ? danhSachLuotXeNgay : new ArrayList<>();
    }

    public double getTongDoanhThu() { return tongDoanhThu; }
    public double getTongTienCong() { return tongTienCong; }
    public double getTongTienPhuTung() { return tongTienPhuTung; }
    public double getTongGiamGia() { return tongGiamGia; }
    public double getTongVAT() { return tongVAT; }
    public double getTongLoiNhuan() { return tongLoiNhuan; }
    public int getTongSoHoaDon() { return tongSoHoaDon; }
    public double getTyTrongTienCong() { return tyTrongTienCong; }
    public double getTyTrongPhuTung() { return tyTrongPhuTung; }
    public List<DoanhThuNgay> getDanhSachDoanhThuNgay() { return danhSachDoanhThuNgay; }

    public double getGiaTriTonKhoHienTai() { return giaTriTonKhoHienTai; }
    public int getSoMatHangQuanLy() { return soMatHangQuanLy; }
    public int getTongSoLuongNhap() { return tongSoLuongNhap; }
    public double getTongGiaTriNhap() { return tongGiaTriNhap; }
    public int getTongSoLuongXuat() { return tongSoLuongXuat; }
    public double getTongGiaTriXuat() { return tongGiaTriXuat; }
    public int getSoPhieuNhapTrongKy() { return soPhieuNhapTrongKy; }
    public List<ThongKePhuTung> getDanhSachPhuTung() { return danhSachPhuTung; }
    public List<NhapXuatNgay> getDanhSachNhapXuatNgay() { return danhSachNhapXuatNgay; }

    public int getTongLuotXeVaoXuong() { return tongLuotXeVaoXuong; }
    public int getTongXeSuaXong() { return tongXeSuaXong; }
    public double getTyLeXeSuaXong() { return tyLeXeSuaXong; }
    public int getTongHangMucHoanThanh() { return tongHangMucHoanThanh; }
    public int getTongHangMucDangLam() { return tongHangMucDangLam; }
    public double getTrungBinhHangMucTrenXe() { return trungBinhHangMucTrenXe; }
    public int getSoKyThuatVienHoatDong() { return soKyThuatVienHoatDong; }
    public double getTyLeHoanThanhChung() { return tyLeHoanThanhChung; }
    public List<NangSuatKTV> getDanhSachNangSuatKTV() { return danhSachNangSuatKTV; }
    public List<LuotXeNgay> getDanhSachLuotXeNgay() { return danhSachLuotXeNgay; }
}
