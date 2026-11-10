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

    public ThongKePhuTung(PhuTung phuTung, int soLuongNhap, int soLuongXuat,
                          int tonHienTai, double giaTriTon, boolean duoiNguongCanhBao) {
        setPhuTung(phuTung);
        setSoLuongNhap(soLuongNhap);
        setSoLuongXuat(soLuongXuat);
        setTonHienTai(tonHienTai);
        setGiaTriTon(giaTriTon);
        setDuoiNguongCanhBao(duoiNguongCanhBao);
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        if (phuTung == null) {
            throw new IllegalArgumentException("Phụ tùng không được để trống.");
        }
        this.phuTung = phuTung;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        if (soLuongNhap < 0) {
            throw new IllegalArgumentException("Số lượng nhập không được âm.");
        }
        this.soLuongNhap = soLuongNhap;
    }

    public int getSoLuongXuat() {
        return soLuongXuat;
    }

    public void setSoLuongXuat(int soLuongXuat) {
        if (soLuongXuat < 0) {
            throw new IllegalArgumentException("Số lượng xuất không được âm.");
        }
        this.soLuongXuat = soLuongXuat;
    }

    public int getTonHienTai() {
        return tonHienTai;
    }

    public void setTonHienTai(int tonHienTai) {
        if (tonHienTai < 0) {
            throw new IllegalArgumentException("Số lượng tồn không được âm.");
        }
        this.tonHienTai = tonHienTai;
    }

    public double getGiaTriTon() {
        return giaTriTon;
    }

    public void setGiaTriTon(double giaTriTon) {
        if (giaTriTon < 0) {
            throw new IllegalArgumentException("Giá trị tồn kho không được âm.");
        }
        this.giaTriTon = giaTriTon;
    }

    public boolean isDuoiNguongCanhBao() {
        return duoiNguongCanhBao;
    }

    public void setDuoiNguongCanhBao(boolean duoiNguongCanhBao) {
        this.duoiNguongCanhBao = duoiNguongCanhBao;
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