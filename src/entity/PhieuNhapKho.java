package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhieuNhapKho {
    private String maPhieuNhap;
    private LocalDateTime ngayNhap;
    private double tongTienNhap;
    private NhaCungCap nhaCungCap;
    private NhanVien thuKho;

    // Thuộc tính phụ phục vụ hiển thị JOIN
    private int soMatHang;
    private List<ChiTietNhapKho> danhSachChiTiet = new ArrayList<>();

    public PhieuNhapKho() {
        this.ngayNhap = LocalDateTime.now();
    }

    public PhieuNhapKho(String maPhieuNhap) {
        this();
        setMaPhieuNhap(maPhieuNhap);
    }

    public PhieuNhapKho(String maPhieuNhap, LocalDateTime ngayNhap, double tongTienNhap, NhaCungCap nhaCungCap, NhanVien thuKho) {
        setMaPhieuNhap(maPhieuNhap);
        setNgayNhap(ngayNhap);
        if (tongTienNhap < 0) {
            throw new IllegalArgumentException("Tổng tiền nhập không được là số âm.");
        }
        this.tongTienNhap = tongTienNhap;
        setNhaCungCap(nhaCungCap);
        setThuKho(thuKho);
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

    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }
    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        if (nhaCungCap == null) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp.");
        }
        this.nhaCungCap = nhaCungCap;
    }

    public NhanVien getThuKho() {
        return thuKho;
    }
    public void setThuKho(NhanVien thuKho) {
        if (thuKho == null) {
            throw new IllegalArgumentException("Thủ kho lập phiếu không được rỗng.");
        }
        this.thuKho = thuKho;
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
        if (nhaCungCap == null) {
            return maPhieuNhap + " - " + null;
        }
        return maPhieuNhap + " - " + (nhaCungCap.getTenNCC() != null ? nhaCungCap.getTenNCC() : nhaCungCap.getMaNCC());
    }
}