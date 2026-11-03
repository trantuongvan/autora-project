package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class PhieuYeuCauXuatKho {
    private String maPhieuYC;
    private LocalDateTime ngayYeuCau;
    private LocalDateTime ngayXuat;
    private String trangThai;
    private String maPhieuTNSC;
    private String maNVKyThuat;
    private String maNVThuKho;

    public PhieuYeuCauXuatKho() {
        this.ngayYeuCau = LocalDateTime.now();
        this.trangThai = "Chờ xuất";
    }

    public PhieuYeuCauXuatKho(String maPhieuYC) {
        this();
        setMaPhieuYC(maPhieuYC);
    }

    public PhieuYeuCauXuatKho(String maPhieuYC, LocalDateTime ngayYeuCau, LocalDateTime ngayXuat,
                              String trangThai, String maPhieuTNSC, String maNVKyThuat, String maNVThuKho) {
        setMaPhieuYC(maPhieuYC);
        setNgayYeuCau(ngayYeuCau);
        setNgayXuat(ngayXuat);
        setTrangThai(trangThai);
        setMaPhieuTNSC(maPhieuTNSC);
        setMaNVKyThuat(maNVKyThuat);
        this.maNVThuKho = maNVThuKho;
    }

    public String getMaPhieuYC() {
        return maPhieuYC;
    }
    public void setMaPhieuYC(String maPhieuYC) {
        if (maPhieuYC == null || !maPhieuYC.trim().matches("^PYC\\d{5}$")) {
            throw new IllegalArgumentException("Mã phiếu yêu cầu phải có định dạng PYC + 5 chữ số (VD: PYC00001).");
        }
        this.maPhieuYC = maPhieuYC.trim();
    }

    public LocalDateTime getNgayYeuCau() {
        return ngayYeuCau;
    }
    public void setNgayYeuCau(LocalDateTime ngayYeuCau) {
        if (ngayYeuCau == null || ngayYeuCau.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalArgumentException("Ngày yêu cầu không hợp lệ.");
        }
        this.ngayYeuCau = ngayYeuCau;
    }

    public LocalDateTime getNgayXuat() {
        return ngayXuat;
    }
    public void setNgayXuat(LocalDateTime ngayXuat) {
        if (ngayXuat != null && ngayYeuCau != null && ngayXuat.isBefore(ngayYeuCau)) {
            throw new IllegalArgumentException("Ngày xuất không hợp lệ.");
        }
        this.ngayXuat = ngayXuat;
    }

    public String getTrangThai() {
        return trangThai;
    }
    public void setTrangThai(String trangThai) {
        if (trangThai == null || (!trangThai.equals("Chờ xuất") && !trangThai.equals("Đã xuất") && !trangThai.equals("Từ chối"))) {
            throw new IllegalArgumentException("Trạng thái phiếu yêu cầu không hợp lệ.");
        }
        this.trangThai = trangThai;
    }

    public String getMaPhieuTNSC() {
        return maPhieuTNSC;
    }
    public void setMaPhieuTNSC(String maPhieuTNSC) {
        if (maPhieuTNSC == null || maPhieuTNSC.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận sửa chữa không được để trống.");
        }
        this.maPhieuTNSC = maPhieuTNSC.trim();
    }

    public String getMaNVKyThuat() {
        return maNVKyThuat;
    }
    public void setMaNVKyThuat(String maNVKyThuat) {
        if (maNVKyThuat == null || maNVKyThuat.trim().isEmpty()) {
            throw new IllegalArgumentException("Kỹ thuật viên yêu cầu không được để trống.");
        }
        this.maNVKyThuat = maNVKyThuat.trim();
    }

    public String getMaNVThuKho() {
        return maNVThuKho;
    }
    public void setMaNVThuKho(String maNVThuKho) {
        this.maNVThuKho = maNVThuKho;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhieuYeuCauXuatKho)) return false;
        PhieuYeuCauXuatKho that = (PhieuYeuCauXuatKho) o;
        return Objects.equals(maPhieuYC, that.maPhieuYC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuYC);
    }

    @Override
    public String toString() {
        return maPhieuYC + " - " + maPhieuTNSC + " (" + trangThai + ")";
    }
}