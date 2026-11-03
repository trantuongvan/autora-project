package entity;

import java.util.Objects;

public class ChiTietSuaChua_DichVu {
    private String maPhieuTNSC;
    private String maDichVu;
    private String maNVKyThuat;
    private String trangThaiTienDo;
    private double tienCongThucTe;

    // Thuộc tính hiển thị khi JOIN với bảng DichVu và NhanVien
    private String tenDichVu;
    private String tenKyThuatVien;

    public ChiTietSuaChua_DichVu() {
        this.trangThaiTienDo = "Chờ sửa";
    }

    public ChiTietSuaChua_DichVu(String maPhieuTNSC, String maDichVu) {
        this();
        this.maPhieuTNSC = maPhieuTNSC;
        this.maDichVu = maDichVu;
    }

    public ChiTietSuaChua_DichVu(String maPhieuTNSC, String maDichVu, String maNVKyThuat,
                                 String trangThaiTienDo, double tienCongThucTe) {
        this.maPhieuTNSC = maPhieuTNSC;
        this.maDichVu = maDichVu;
        this.maNVKyThuat = maNVKyThuat;
        setTrangThaiTienDo(trangThaiTienDo);
        setTienCongThucTe(tienCongThucTe);
    }

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }
    public void setMaPhieuTNSC(String maPhieuTNSC) {
        this.maPhieuTNSC = maPhieuTNSC;
    }

    public String getMaDichVu() {
        return maDichVu;
    }
    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getMaNVKyThuat() {
        return maNVKyThuat;
    }
    public void setMaNVKyThuat(String maNVKyThuat) {
        this.maNVKyThuat = maNVKyThuat;
    }

    public String getTrangThaiTienDo() {
        return trangThaiTienDo;
    }
    public void setTrangThaiTienDo(String trangThaiTienDo) {
        if (trangThaiTienDo == null || (!trangThaiTienDo.equals("Chờ sửa")
                && !trangThaiTienDo.equals("Đang sửa")
                && !trangThaiTienDo.equals("Hoàn thành"))) {
            throw new IllegalArgumentException("Trạng thái tiến độ hạng mục không hợp lệ.");
        }
        this.trangThaiTienDo = trangThaiTienDo;
    }

    public double getTienCongThucTe() {
        return tienCongThucTe;
    }
    public void setTienCongThucTe(double tienCongThucTe) {
        if (tienCongThucTe < 0) {
            throw new IllegalArgumentException("Tiền công thực tế không được là số âm.");
        }
        this.tienCongThucTe = tienCongThucTe;
    }

    public void capNhatTienDo(String trangThaiMoi) {
        setTrangThaiTienDo(trangThaiMoi);
    }

    public String getTenDichVu() {
        return tenDichVu;
    }
    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getTenKyThuatVien() {
        return tenKyThuatVien;
    }
    public void setTenKyThuatVien(String tenKyThuatVien) {
        this.tenKyThuatVien = tenKyThuatVien;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietSuaChua_DichVu)) return false;
        ChiTietSuaChua_DichVu that = (ChiTietSuaChua_DichVu) o;
        return Objects.equals(maPhieuTNSC, that.maPhieuTNSC) && Objects.equals(maDichVu, that.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuTNSC, maDichVu);
    }

    @Override
    public String toString() {
        return maPhieuTNSC + " - " + (tenDichVu != null ? tenDichVu : maDichVu) + " (" + trangThaiTienDo + ")";
    }
}