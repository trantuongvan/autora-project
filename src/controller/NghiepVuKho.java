package controller;

import entity.ChiTietNhapKho;
import entity.PhuTung;

/** Nghiệp vụ kho: đủ tồn, cảnh báo tồn, giá nhập hợp lệ và số lượng nhập đề xuất. */
public final class NghiepVuKho {

    private NghiepVuKho() {
    }

    public static boolean duTon(PhuTung pt, int soLuong) {
        return pt.getSoLuongTon() >= soLuong;
    }

    public static boolean duoiNguongCanhBao(PhuTung pt) {
        return pt.getSoLuongTon() <= pt.getNguongCanhBao();
    }

    /** Giá bán lẻ không được thấp hơn giá nhập, kể cả khi giá nhập tăng lúc nhập kho. */
    public static boolean giaNhapHopLe(PhuTung pt, double giaNhapMoi) {
        return giaNhapMoi <= pt.getGiaBanLe();
    }

    /** Đưa tồn kho lên gấp đôi ngưỡng cảnh báo, tối thiểu bằng ngưỡng và không dưới 1. */
    public static int soLuongNhapDeXuat(PhuTung pt) {
        return Math.max(1, Math.max(pt.getNguongCanhBao() * 2 - pt.getSoLuongTon(), pt.getNguongCanhBao()));
    }

    public static double thanhTien(ChiTietNhapKho ct) {
        return ct.getSoLuongNhap() * ct.getGiaNhapThucTe();
    }

    /** Giá trị tồn theo giá nhập hiện tại của phụ tùng. */
    public static double giaTriTon(PhuTung pt) {
        return pt.getSoLuongTon() * pt.getGiaNhap();
    }
}
