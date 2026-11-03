package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Phiếu bàn giao gắn với một phiếu sửa chữa. */
public class PhieuBanGiao {

    private String maPhieuBG;
    private String maPhieuTNSC;
    private String maNVBanGiao;
    private String chuKyKhachHang;
    private LocalDateTime ngayBanGiao;
    private boolean xacNhanHoanTat;
    private boolean xacNhanThanhToan;

    public PhieuBanGiao() {
    }

    public PhieuBanGiao(String maPhieuBG) {
        setMaPhieuBG(maPhieuBG);
    }

    public PhieuBanGiao(String maPhieuBG, String maPhieuTNSC, String maNVBanGiao,
                        LocalDateTime ngayBanGiao, boolean xacNhanHoanTat,
                        boolean xacNhanThanhToan, String chuKyKhachHang) {
        setMaPhieuBG(maPhieuBG);
        setMaPhieuTNSC(maPhieuTNSC);
        setMaNVBanGiao(maNVBanGiao);
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

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }

    public void setMaPhieuTNSC(String maPhieuTNSC) {
        if (maPhieuTNSC == null || maPhieuTNSC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu sửa chữa không được để trống.");
        }
        this.maPhieuTNSC = maPhieuTNSC.trim();
    }

    public String getMaNVBanGiao() {
        return maNVBanGiao;
    }

    public void setMaNVBanGiao(String maNVBanGiao) {
        if (maNVBanGiao == null || maNVBanGiao.trim().isEmpty()) {
            throw new IllegalArgumentException("Nhân viên bàn giao không được để trống.");
        }
        this.maNVBanGiao = maNVBanGiao.trim();
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
        return maPhieuBG + " - " + maPhieuTNSC;
    }
}
