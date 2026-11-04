package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Phiếu bàn giao gắn với một phiếu sửa chữa. */
public class PhieuBanGiao {

    private String maPhieuBG;
    private PhieuTiepNhanVaSuaChua phieu;
    private NhanVien nhanVienBanGiao;
    private String chuKyKhachHang;
    private LocalDateTime ngayBanGiao;
    private boolean xacNhanHoanTat;
    private boolean xacNhanThanhToan;

    public PhieuBanGiao() {
    }

    public PhieuBanGiao(String maPhieuBG) {
        setMaPhieuBG(maPhieuBG);
    }

    public PhieuBanGiao(String maPhieuBG, PhieuTiepNhanVaSuaChua phieu, NhanVien nhanVienBanGiao,
                        LocalDateTime ngayBanGiao, boolean xacNhanHoanTat,
                        boolean xacNhanThanhToan, String chuKyKhachHang) {
        setMaPhieuBG(maPhieuBG);
        setPhieu(phieu);
        setNhanVienBanGiao(nhanVienBanGiao);
        setNgayBanGiao(ngayBanGiao);
        setXacNhanHoanTat(xacNhanHoanTat);
        setXacNhanThanhToan(xacNhanThanhToan);
        setChuKyKhachHang(chuKyKhachHang);
    }

    public String getMaPhieuBG() {
        return maPhieuBG;
    }

    public void setMaPhieuBG(String maPhieuBG) {
        if (maPhieuBG == null || maPhieuBG.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu bàn giao không được để trống.");
        }
        this.maPhieuBG = maPhieuBG.trim();
    }

    public PhieuTiepNhanVaSuaChua getPhieu() {
        return phieu;
    }

    public void setPhieu(PhieuTiepNhanVaSuaChua phieu) {
        if (phieu == null) {
            throw new IllegalArgumentException("Mã phiếu sửa chữa không được để trống.");
        }
        this.phieu = phieu;
    }

    public NhanVien getNhanVienBanGiao() {
        return nhanVienBanGiao;
    }

    public void setNhanVienBanGiao(NhanVien nhanVienBanGiao) {
        if (nhanVienBanGiao == null) {
            throw new IllegalArgumentException("Nhân viên bàn giao không được để trống.");
        }
        this.nhanVienBanGiao = nhanVienBanGiao;
    }

    public String getChuKyKhachHang() {
        return chuKyKhachHang;
    }

    public void setChuKyKhachHang(String chuKyKhachHang) {
        this.chuKyKhachHang = chuKyKhachHang == null ? "" : chuKyKhachHang.trim();
    }

    public LocalDateTime getNgayBanGiao() {
        return ngayBanGiao;
    }

    public void setNgayBanGiao(LocalDateTime ngayBanGiao) {
        this.ngayBanGiao = ngayBanGiao;
    }

    public boolean isXacNhanHoanTat() {
        return xacNhanHoanTat;
    }

    public void setXacNhanHoanTat(boolean xacNhanHoanTat) {
        this.xacNhanHoanTat = xacNhanHoanTat;
    }

    public boolean isXacNhanThanhToan() {
        return xacNhanThanhToan;
    }

    public void setXacNhanThanhToan(boolean xacNhanThanhToan) {
        this.xacNhanThanhToan = xacNhanThanhToan;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PhieuBanGiao)) {
            return false;
        }
        PhieuBanGiao that = (PhieuBanGiao) obj;
        return Objects.equals(maPhieuBG, that.maPhieuBG);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuBG);
    }

    @Override
    public String toString() {
        return maPhieuBG + " - " + (phieu == null ? null : phieu.getMaPhieuTNSC());
    }
}
