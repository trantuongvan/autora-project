package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Phiếu bàn giao gắn với một phiếu sửa chữa. */
public class PhieuBanGiao {
    private String maPhieuBG, maPhieuTNSC, maNVBanGiao, chuKyKhachHang;
    private LocalDateTime ngayBanGiao;
    private boolean xacNhanHoanTat, xacNhanThanhToan;
    public PhieuBanGiao() {}
    public PhieuBanGiao(String maPhieuBG) { setMaPhieuBG(maPhieuBG); }
    public PhieuBanGiao(String maPhieuBG, String maPhieuTNSC, String maNVBanGiao,
                        LocalDateTime ngayBanGiao, boolean xacNhanHoanTat,
                        boolean xacNhanThanhToan, String chuKyKhachHang) {
        setMaPhieuBG(maPhieuBG); setMaPhieuTNSC(maPhieuTNSC); setMaNVBanGiao(maNVBanGiao);
        setNgayBanGiao(ngayBanGiao); setXacNhanHoanTat(xacNhanHoanTat);
        setXacNhanThanhToan(xacNhanThanhToan); setChuKyKhachHang(chuKyKhachHang);
    }
    public String getMaPhieuBG() { return maPhieuBG; }
    public void setMaPhieuBG(String v) { maPhieuBG = required(v, "Mã phiếu bàn giao"); }
    public String getMaPhieuTNSC() { return maPhieuTNSC; }
    public void setMaPhieuTNSC(String v) { maPhieuTNSC = required(v, "Mã phiếu sửa chữa"); }
    public String getMaNVBanGiao() { return maNVBanGiao; }
    public void setMaNVBanGiao(String v) { maNVBanGiao = required(v, "Nhân viên bàn giao"); }
    public String getChuKyKhachHang() { return chuKyKhachHang; }
    public void setChuKyKhachHang(String v) { chuKyKhachHang = v == null ? "" : v.trim(); }
    public LocalDateTime getNgayBanGiao() { return ngayBanGiao; }
    public void setNgayBanGiao(LocalDateTime v) { ngayBanGiao = v; }
    public boolean isXacNhanHoanTat() { return xacNhanHoanTat; }
    public void setXacNhanHoanTat(boolean v) { xacNhanHoanTat = v; }
    public boolean isXacNhanThanhToan() { return xacNhanThanhToan; }
    public void setXacNhanThanhToan(boolean v) { xacNhanThanhToan = v; }
    private static String required(String v, String l) { if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(l + " không được để trống."); return v.trim(); }
    @Override public boolean equals(Object obj) { return this==obj || obj instanceof PhieuBanGiao && Objects.equals(maPhieuBG,((PhieuBanGiao)obj).maPhieuBG); }
    @Override public int hashCode() { return Objects.hash(maPhieuBG); }
    @Override public String toString() { return maPhieuBG+" - "+maPhieuTNSC; }
}
