package entity;

import java.util.Arrays;
import java.util.Objects;

public class ChiTietSuaChua_DichVu {
    public static final String[] TIEN_DO = {
        "Chờ sửa", "Đang sửa", "Hoàn thành"
    };

    private String maPhieuTNSC;
    private String maDichVu;
    private String tenDichVu;
    private String maNVKyThuat;
    private String tenKyThuatVien;
    private String trangThaiTienDo;
    private double tienCongThucTe;

    public ChiTietSuaChua_DichVu() {
    }

    public ChiTietSuaChua_DichVu(String maPhieuTNSC, String maDichVu) {
        setMaPhieuTNSC(maPhieuTNSC);
        setMaDichVu(maDichVu);
    }

    public ChiTietSuaChua_DichVu(String maPhieuTNSC, String maDichVu,
            String maNVKyThuat, String trangThaiTienDo, double tienCongThucTe) {
        this(maPhieuTNSC, maDichVu, null, maNVKyThuat, null,
                trangThaiTienDo, tienCongThucTe);
    }

    public ChiTietSuaChua_DichVu(String maPhieuTNSC, String maDichVu,
            String tenDichVu, String maNVKyThuat, String tenKyThuatVien,
            String trangThaiTienDo, double tienCongThucTe) {
        setMaPhieuTNSC(maPhieuTNSC);
        setMaDichVu(maDichVu);
        setTenDichVu(tenDichVu);
        setMaNVKyThuat(maNVKyThuat);
        setTenKyThuatVien(tenKyThuatVien);
        setTrangThaiTienDo(trangThaiTienDo);
        setTienCongThucTe(tienCongThucTe);
    }

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }

    public void setMaPhieuTNSC(String maPhieuTNSC) {
        if (maPhieuTNSC == null || maPhieuTNSC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được để trống.");
        }
        this.maPhieuTNSC = maPhieuTNSC.trim();
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        }
        this.maDichVu = maDichVu.trim();
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getMaNVKyThuat() {
        return maNVKyThuat;
    }

    public void setMaNVKyThuat(String maNVKyThuat) {
        if (maNVKyThuat != null && maNVKyThuat.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã kỹ thuật viên phải có nội dung hoặc là null.");
        }
        this.maNVKyThuat = maNVKyThuat == null ? null : maNVKyThuat.trim();
    }

    public String getTenKyThuatVien() {
        return tenKyThuatVien;
    }

    public void setTenKyThuatVien(String tenKyThuatVien) {
        this.tenKyThuatVien = tenKyThuatVien;
    }

    public String getTrangThaiTienDo() {
        return trangThaiTienDo;
    }

    public void setTrangThaiTienDo(String trangThaiTienDo) {
        if (!Arrays.asList(TIEN_DO).contains(trangThaiTienDo)) {
            throw new IllegalArgumentException("Trạng thái tiến độ dịch vụ không hợp lệ.");
        }
        this.trangThaiTienDo = trangThaiTienDo;
    }

    public double getTienCongThucTe() {
        return tienCongThucTe;
    }

    public void setTienCongThucTe(double tienCongThucTe) {
        if (!Double.isFinite(tienCongThucTe) || tienCongThucTe < 0) {
            throw new IllegalArgumentException("Tiền công thực tế phải là số hữu hạn không âm.");
        }
        this.tienCongThucTe = tienCongThucTe;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChiTietSuaChua_DichVu other = (ChiTietSuaChua_DichVu) obj;
        return Objects.equals(maPhieuTNSC, other.maPhieuTNSC)
                && Objects.equals(maDichVu, other.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuTNSC, maDichVu);
    }

    @Override
    public String toString() {
        return maPhieuTNSC + " - " + maDichVu;
    }
}