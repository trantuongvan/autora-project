package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhieuNhapKho {
    private String maPhieuNhap;
    private LocalDateTime ngayNhap;
    private double tongTienNhap;
    private String maNCC;
    private String maNVThuKho;

    // Thuộc tính phụ phục vụ hiển thị JOIN
    private String tenNCC;
    private String tenThuKho;
    private int soMatHang;
    private List<ChiTietNhapKho> danhSachChiTiet = new ArrayList<>();

    public PhieuNhapKho() {
        this.ngayNhap = LocalDateTime.now();
    }

    public PhieuNhapKho(String maPhieuNhap) {
        this();
        setMaPhieuNhap(maPhieuNhap);
    }

    public PhieuNhapKho(String maPhieuNhap, LocalDateTime ngayNhap, double tongTienNhap, String maNCC, String maNVThuKho) {
        setMaPhieuNhap(maPhieuNhap);
        setNgayNhap(ngayNhap);
        setTongTienNhap(tongTienNhap);
        setMaNCC(maNCC);
        setMaNVThuKho(maNVThuKho);
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }
    public void setMaPhieuNhap(String maPhieuNhap) {
        if (maPhieuNhap == null || !maPhieuNhap.trim().startsWith("PN")) {
            throw new IllegalArgumentException("Mã phiếu nhập phải bắt đầu bằng 'PN'.");
        }
        this.maPhieuNhap = maPhieuNhap.trim();
    }

    public LocalDateTime getNgayNhap() {
        return ngayNhap;
    }
    public void setNgayNhap(LocalDateTime ngayNhap) {
        if (ngayNhap == null || ngayNhap.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalArgumentException("Ngày nhập không được vượt quá thời gian hiện tại.");
        }
        this.ngayNhap = ngayNhap;
    }

    public double getTongTienNhap() {
        return tongTienNhap;
    }
    public void setTongTienNhap(double tongTienNhap) {
        if (tongTienNhap < 0) {
            throw new IllegalArgumentException("Tổng tiền nhập không được là số âm.");
        }
        this.tongTienNhap = tongTienNhap;
    }

    public String getMaNCC() {
        return maNCC;
    }
    public void setMaNCC(String maNCC) {
        if (maNCC == null || maNCC.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp.");
        }
        this.maNCC = maNCC.trim();
    }

    public String getMaNVThuKho() {
        return maNVThuKho;
    }
    public void setMaNVThuKho(String maNVThuKho) {
        if (maNVThuKho == null || maNVThuKho.trim().isEmpty()) {
            throw new IllegalArgumentException("Thủ kho lập phiếu không được rỗng.");
        }
        this.maNVThuKho = maNVThuKho.trim();
    }

    public String getTenNCC() {
        return tenNCC;
    }
    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getTenThuKho() {
        return tenThuKho;
    }
    public void setTenThuKho(String tenThuKho) {
        this.tenThuKho = tenThuKho;
    }

    public int getSoMatHang() {
        return soMatHang;
    }
    public void setSoMatHang(int soMatHang) {
        this.soMatHang = soMatHang;
    }

    public List<ChiTietNhapKho> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }
    public void setDanhSachChiTiet(List<ChiTietNhapKho> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhieuNhapKho)) return false;
        PhieuNhapKho that = (PhieuNhapKho) o;
        return Objects.equals(maPhieuNhap, that.maPhieuNhap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuNhap);
    }

    @Override
    public String toString() {
        return maPhieuNhap + " - " + (tenNCC != null ? tenNCC : maNCC);
    }
}