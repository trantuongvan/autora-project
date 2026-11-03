package controller;

/** Các màn hình phiếu dùng giao diện này để chuyển màn hình mà không phụ thuộc trực tiếp vào khung chính. */
public interface DieuHuongPhieu {
    void moChiTiet(String maPhieu);

    void moDanhSach();

    void moTiepNhanMoi();

    void suaPhieu(String maPhieu);

    void moBaoCao();
}
