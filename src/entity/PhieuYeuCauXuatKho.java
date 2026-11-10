package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class PhieuYeuCauXuatKho {
    private String maPhieuYC;
    private LocalDateTime ngayYeuCau;
    private LocalDateTime ngayXuat;
    private TrangThaiYeuCauXuat trangThai;
    private PhieuTiepNhanVaSuaChua phieu;
    private NhanVien kyThuatVien;
    private NhanVien thuKho;

    public PhieuYeuCauXuatKho() {
        this.ngayYeuCau = LocalDateTime.now();
        this.trangThai = TrangThaiYeuCauXuat.CHO_XUAT;
    }

    public PhieuYeuCauXuatKho(String maPhieuYC) {
        this();
        setMaPhieuYC(maPhieuYC);
    }

    public PhieuYeuCauXuatKho(String maPhieuYC, LocalDateTime ngayYeuCau, LocalDateTime ngayXuat,
                              TrangThaiYeuCauXuat trangThai, PhieuTiepNhanVaSuaChua phieu, NhanVien kyThuatVien, NhanVien thuKho) {
        setMaPhieuYC(maPhieuYC);
        setNgayYeuCau(ngayYeuCau);
        setNgayXuat(ngayXuat);
        setTrangThai(trangThai);
        setPhieu(phieu);
        setKyThuatVien(kyThuatVien);
        this.thuKho = thuKho;
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

    public TrangThaiYeuCauXuat getTrangThai() {
        return trangThai;
    }
    public void setTrangThai(TrangThaiYeuCauXuat trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException("Trạng thái phiếu yêu cầu không hợp lệ.");
        }
        this.trangThai = trangThai;
    }

    public PhieuTiepNhanVaSuaChua getPhieu() {
        return phieu;
    }
    public void setPhieu(PhieuTiepNhanVaSuaChua phieu) {
        if (phieu == null) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận sửa chữa không được để trống.");
        }
        this.phieu = phieu;
    }

    /** Tên KTV để hiển thị; còn mã khi chưa đọc được tên. */
    public String getTenKyThuatVien() {
        return kyThuatVien == null ? "" : kyThuatVien.getHoTen() != null ? kyThuatVien.getHoTen() : kyThuatVien.getMaNV();
    }

    public NhanVien getKyThuatVien() {
        return kyThuatVien;
    }
    public void setKyThuatVien(NhanVien kyThuatVien) {
        if (kyThuatVien == null) {
            throw new IllegalArgumentException("Kỹ thuật viên yêu cầu không được để trống.");
        }
        this.kyThuatVien = kyThuatVien;
    }

    /** Null khi chưa có thủ kho duyệt. */
    public NhanVien getThuKho() {
        return thuKho;
    }
    public void setThuKho(NhanVien thuKho) {
        this.thuKho = thuKho;
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
        return maPhieuYC + " - " + (phieu == null ? null : phieu.getMaPhieuTNSC()) + " (" + trangThai + ")";
    }
}