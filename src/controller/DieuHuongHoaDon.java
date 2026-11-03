package controller;

/** Các màn hình hóa đơn dùng giao diện này để chuyển màn hình mà không phụ thuộc trực tiếp vào khung chính. */
public interface DieuHuongHoaDon {
    /** Mở hóa đơn đã có. */
    void moChiTietHoaDon(String maHD);

    /** Mở trang lập hóa đơn cho phiếu đã hoàn thành. */
    void moLapHoaDon(String maPhieu);

    void moDanhSachHoaDon();
}
