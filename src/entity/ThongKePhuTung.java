package entity;

import java.util.Objects;

/** Thực thể thống kê nhập - xuất - tồn của một mặt hàng phụ tùng. */
public class ThongKePhuTung {
    private PhuTung phuTung;
    private int soLuongNhap;
    private int soLuongXuat;
    private int tonHienTai;
    private double giaTriTon;
    private boolean duoiNguongCanhBao;

    public ThongKePhuTung() {
    }

    public ThongKePhuTung(PhuTung phuTung, int soLuongNhap, int soLuongXuat, int tonHienTai) {
        this(phuTung, soLuongNhap, soLuongXuat, tonHienTai, 0.0, false);
    }

    public ThongKePhuTung(PhuTung phuTung, int soLuongNhap, int soLuongXuat,
                          int tonHienTai, double giaTriTon, boolean duoiNguongCanhBao) {
        if (phuTung == null) {
            throw new IllegalArgumentException("Phụ tùng không được để trống.");
        }
        if (soLuongNhap < 0) {
            throw new IllegalArgumentException("Số lượng nhập không được âm.");
        }
        if (soLuongXuat < 0) {
            throw new IllegalArgumentException("Số lượng xuất không được âm.");
        }
        if (tonHienTai < 0) {
            throw new IllegalArgumentException("Số lượng tồn không được âm.");
        }
        if (giaTriTon < 0) {
            throw new IllegalArgumentException("Giá trị tồn không được âm.");
        }
        this.phuTung = phuTung;
        this.soLuongNhap = soLuongNhap;
        this.soLuongXuat = soLuongXuat;
        this.tonHienTai = tonHienTai;
        this.giaTriTon = giaTriTon;
        this.duoiNguongCanhBao = duoiNguongCanhBao;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public int getSoLuongXuat() {
        return soLuongXuat;
    }

    public int getTonHienTai() {
        return tonHienTai;
    }

    public double getGiaTriTon() {
        return giaTriTon;
    }

    public boolean isDuoiNguongCanhBao() {
        return duoiNguongCanhBao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThongKePhuTung)) return false;
        ThongKePhuTung that = (ThongKePhuTung) o;
        return Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phuTung);
    }

    @Override
    public String toString() {
        return phuTung == null ? "" : phuTung.toString();
    }
}
