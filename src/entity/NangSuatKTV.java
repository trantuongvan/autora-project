package entity;

import java.util.Objects;

/** Thực thể thống kê năng suất làm việc của một Kỹ thuật viên trong kỳ. */
public class NangSuatKTV {
    private NhanVien kyThuatVien;
    private int soXeThamGia;
    private int hangMucHoanThanh;
    private int hangMucDangThucHien;
    private double tienCongMangLai;
    private double tyLeHoanThanh;

    public NangSuatKTV() {
    }

    public NangSuatKTV(NhanVien kyThuatVien, int soXeThamGia, int hangMucHoanThanh,
                       int hangMucDangThucHien, double tienCongMangLai) {
        this(kyThuatVien, soXeThamGia, hangMucHoanThanh, hangMucDangThucHien, tienCongMangLai, 0.0);
    }

    public NangSuatKTV(NhanVien kyThuatVien, int soXeThamGia, int hangMucHoanThanh,
                       int hangMucDangThucHien, double tienCongMangLai, double tyLeHoanThanh) {
        if (kyThuatVien == null) {
            throw new IllegalArgumentException("Kỹ thuật viên không được để trống.");
        }
        if (soXeThamGia < 0) {
            throw new IllegalArgumentException("Số xe tham gia không được âm.");
        }
        if (hangMucHoanThanh < 0) {
            throw new IllegalArgumentException("Số hạng mục hoàn thành không được âm.");
        }
        if (hangMucDangThucHien < 0) {
            throw new IllegalArgumentException("Số hạng mục đang thực hiện không được âm.");
        }
        if (tienCongMangLai < 0) {
            throw new IllegalArgumentException("Tiền công mang lại không được âm.");
        }
        if (tyLeHoanThanh < 0) {
            throw new IllegalArgumentException("Tỷ lệ hoàn thành không được âm.");
        }
        this.kyThuatVien = kyThuatVien;
        this.soXeThamGia = soXeThamGia;
        this.hangMucHoanThanh = hangMucHoanThanh;
        this.hangMucDangThucHien = hangMucDangThucHien;
        this.tienCongMangLai = tienCongMangLai;
        this.tyLeHoanThanh = tyLeHoanThanh;
    }

    public NhanVien getKyThuatVien() {
        return kyThuatVien;
    }

    public int getSoXeThamGia() {
        return soXeThamGia;
    }

    public int getHangMucHoanThanh() {
        return hangMucHoanThanh;
    }

    public int getHangMucDangThucHien() {
        return hangMucDangThucHien;
    }

    public double getTienCongMangLai() {
        return tienCongMangLai;
    }

    public double getTyLeHoanThanh() {
        return tyLeHoanThanh;
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
