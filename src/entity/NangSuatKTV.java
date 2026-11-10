package entity;

import java.util.Objects;

/** Thực thể thống kê năng suất làm việc của một Kỹ thuật viên trong kỳ. */
public class NangSuatKTV {
    private NhanVien kyThuatVien;
    private int soXeThamGia;
    private int hangMucHoanThanh;
    private int hangMucDangThucHien;
    private double tienCongMangLai;

    public NangSuatKTV() {
    }

    public NangSuatKTV(NhanVien kyThuatVien, int soXeThamGia, int hangMucHoanThanh,
                       int hangMucDangThucHien, double tienCongMangLai) {
        setKyThuatVien(kyThuatVien);
        setSoXeThamGia(soXeThamGia);
        setHangMucHoanThanh(hangMucHoanThanh);
        setHangMucDangThucHien(hangMucDangThucHien);
        setTienCongMangLai(tienCongMangLai);
    }

    public NhanVien getKyThuatVien() {
        return kyThuatVien;
    }

    public void setKyThuatVien(NhanVien kyThuatVien) {
        if (kyThuatVien == null) {
            throw new IllegalArgumentException("Kỹ thuật viên không được để trống.");
        }
        this.kyThuatVien = kyThuatVien;
    }

    public int getSoXeThamGia() {
        return soXeThamGia;
    }

    public void setSoXeThamGia(int soXeThamGia) {
        if (soXeThamGia < 0) {
            throw new IllegalArgumentException("Số xe tham gia không được âm.");
        }
        this.soXeThamGia = soXeThamGia;
    }

    public int getHangMucHoanThanh() {
        return hangMucHoanThanh;
    }

    public void setHangMucHoanThanh(int hangMucHoanThanh) {
        if (hangMucHoanThanh < 0) {
            throw new IllegalArgumentException("Số hạng mục hoàn thành không được âm.");
        }
        this.hangMucHoanThanh = hangMucHoanThanh;
    }

    public int getHangMucDangThucHien() {
        return hangMucDangThucHien;
    }

    public void setHangMucDangThucHien(int hangMucDangThucHien) {
        if (hangMucDangThucHien < 0) {
            throw new IllegalArgumentException("Số hạng mục đang thực hiện không được âm.");
        }
        this.hangMucDangThucHien = hangMucDangThucHien;
    }

    public double getTienCongMangLai() {
        return tienCongMangLai;
    }

    public void setTienCongMangLai(double tienCongMangLai) {
        if (tienCongMangLai < 0) {
            throw new IllegalArgumentException("Tiền công mang lại không được âm.");
        }
        this.tienCongMangLai = tienCongMangLai;
    }

    /** Phần trăm hạng mục đã xong trên tổng hạng mục được giao, làm tròn 1 chữ số. */
    public double getTyLeHoanThanh() {
        return DuLieuThongKe.phanTram(hangMucHoanThanh, hangMucHoanThanh + hangMucDangThucHien);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NangSuatKTV)) return false;
        NangSuatKTV that = (NangSuatKTV) o;
        return Objects.equals(kyThuatVien, that.kyThuatVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kyThuatVien);
    }

    @Override
    public String toString() {
        return kyThuatVien == null ? "" : kyThuatVien.toString();
    }
}