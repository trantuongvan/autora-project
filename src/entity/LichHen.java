package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Lịch hẹn của khách; xe có thể để trống khi khách chưa nói xe nào. Việc kiểm tra giờ hẹn nằm ở LichHen_Dao. */
public class LichHen {
    private String maLichHen;
    private KhachHang khachHang;
    private Xe xe;
    private LocalDateTime thoiGianHen;
    private String ghiChu = "";
    private TrangThaiLichHen trangThai = TrangThaiLichHen.DANG_HEN;

    public LichHen() {
    }

    public LichHen(String maLichHen) {
        setMaLichHen(maLichHen);
    }

    public LichHen(String maLichHen, KhachHang khachHang, Xe xe, LocalDateTime thoiGianHen, String ghiChu,
            TrangThaiLichHen trangThai) {
        setMaLichHen(maLichHen);
        setKhachHang(khachHang);
        setXe(xe);
        setThoiGianHen(thoiGianHen);
        setGhiChu(ghiChu);
        setTrangThai(trangThai);
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        if (maLichHen == null || !maLichHen.matches("^LH\\d{5}$")) {
            throw new IllegalArgumentException("Mã lịch hẹn phải là LH và 5 chữ số.");
        }
        this.maLichHen = maLichHen;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        if (khachHang == null) {
            throw new IllegalArgumentException("Lịch hẹn phải thuộc về một khách hàng!");
        }
        this.khachHang = khachHang;
    }

    public Xe getXe() {
        return xe;
    }

    /** Cho phép null: lịch hẹn chưa gắn xe. */
    public void setXe(Xe xe) {
        this.xe = xe;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        if (thoiGianHen == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày giờ hẹn!");
        }
        this.thoiGianHen = thoiGianHen;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        String giaTri = ghiChu == null ? "" : ghiChu.trim();
        if (giaTri.length() > 255) {
            throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự!");
        }
        this.ghiChu = giaTri;
    }

    public TrangThaiLichHen getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiLichHen trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ!");
        }
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof LichHen && maLichHen != null && maLichHen.equals(((LichHen) o).maLichHen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maLichHen);
    }
}
