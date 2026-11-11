package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Số liệu báo cáo thống kê (UC014) đọc từ CSDL cho một kỳ: danh sách theo ngày, theo phụ tùng, theo kỹ thuật viên
 * và vài con số đếm trực tiếp bằng SQL. Chỉ đọc, không có setter; mọi tổng, tỷ lệ và lợi nhuận do controller tính.
 */
public class DuLieuThongKe {
    private final List<DoanhThuNgay> danhSachDoanhThuNgay;
    private final double tongGiaVonPhuTung;
    private final List<ThongKePhuTung> danhSachPhuTung;
    private final int soPhieuNhapTrongKy;
    private final List<NhapXuatNgay> danhSachNhapXuatNgay;
    private final int tongLuotXeVaoXuong;
    private final int tongXeSuaXong;
    private final List<NangSuatKTV> danhSachNangSuatKTV;
    private final List<LuotXeNgay> danhSachLuotXeNgay;

    public DuLieuThongKe(List<DoanhThuNgay> danhSachDoanhThuNgay, double tongGiaVonPhuTung,
            List<ThongKePhuTung> danhSachPhuTung, int soPhieuNhapTrongKy, List<NhapXuatNgay> danhSachNhapXuatNgay,
            int tongLuotXeVaoXuong, int tongXeSuaXong, List<NangSuatKTV> danhSachNangSuatKTV,
            List<LuotXeNgay> danhSachLuotXeNgay) {
        if (tongGiaVonPhuTung < 0 || soPhieuNhapTrongKy < 0 || tongLuotXeVaoXuong < 0 || tongXeSuaXong < 0) {
            throw new IllegalArgumentException("Số liệu thống kê không được âm.");
        }
        this.danhSachDoanhThuNgay = danhSach(danhSachDoanhThuNgay);
        this.tongGiaVonPhuTung = tongGiaVonPhuTung;
        this.danhSachPhuTung = danhSach(danhSachPhuTung);
        this.soPhieuNhapTrongKy = soPhieuNhapTrongKy;
        this.danhSachNhapXuatNgay = danhSach(danhSachNhapXuatNgay);
        this.tongLuotXeVaoXuong = tongLuotXeVaoXuong;
        this.tongXeSuaXong = tongXeSuaXong;
        this.danhSachNangSuatKTV = danhSach(danhSachNangSuatKTV);
        this.danhSachLuotXeNgay = danhSach(danhSachLuotXeNgay);
    }

    private static <T> List<T> danhSach(List<T> ds) {
        return ds == null ? new ArrayList<>() : ds;
    }

    public List<DoanhThuNgay> getDanhSachDoanhThuNgay() { return danhSachDoanhThuNgay; }
    /** Giá vốn phụ tùng của các hóa đơn đã thu tiền trong kỳ, theo giá nhập hiện tại. */
    public double getTongGiaVonPhuTung() { return tongGiaVonPhuTung; }
    public List<ThongKePhuTung> getDanhSachPhuTung() { return danhSachPhuTung; }
    public int getSoPhieuNhapTrongKy() { return soPhieuNhapTrongKy; }
    public List<NhapXuatNgay> getDanhSachNhapXuatNgay() { return danhSachNhapXuatNgay; }
    public int getTongLuotXeVaoXuong() { return tongLuotXeVaoXuong; }
    public int getTongXeSuaXong() { return tongXeSuaXong; }
    public List<NangSuatKTV> getDanhSachNangSuatKTV() { return danhSachNangSuatKTV; }
    public List<LuotXeNgay> getDanhSachLuotXeNgay() { return danhSachLuotXeNgay; }

    @Override
    public String toString() {
        return "DuLieuThongKe{" + danhSachDoanhThuNgay.size() + " ngày, " + danhSachPhuTung.size() + " phụ tùng, "
                + danhSachNangSuatKTV.size() + " KTV}";
    }
}
