package entity;

import java.util.Objects;

/** POJO khách hàng theo mẫu trong tài liệu kiến trúc, dùng khi tiếp nhận khách mới. */
public class KhachHang {
    public static final String[] LOAI_KHACH_HANG = {"Cá nhân", "Doanh nghiệp"};

    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String email;
    private String maSoThue;
    private String loaiKhachHang = LOAI_KHACH_HANG[0];
    private boolean duocGhiNo;

    public KhachHang() {
    }

    public KhachHang(String maKH) {
        setMaKH(maKH);
    }

    public KhachHang(String maKH, String hoTen, String soDienThoai, String diaChi, String email,
            String maSoThue, String loaiKhachHang, boolean duocGhiNo) {
        setMaKH(maKH);
        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setDiaChi(diaChi);
        setEmail(email);
        setMaSoThue(maSoThue);
        setLoaiKhachHang(loaiKhachHang);
        setDuocGhiNo(duocGhiNo);
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được để trống!");
        }
        this.maKH = maKH.trim();
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên khách hàng không được để trống!");
        }
        this.hoTen = hoTen.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || !soDienThoai.trim().matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        this.soDienThoai = soDienThoai.trim();
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        if (diaChi == null || diaChi.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống!");
        }
        this.diaChi = diaChi.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String giaTri = email == null ? "" : email.trim();
        if (!giaTri.isEmpty() && !giaTri.matches("^[\\w.+-]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ!");
        }
        this.email = giaTri;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        String giaTri = maSoThue == null ? "" : maSoThue.trim();
        if (!giaTri.isEmpty() && !giaTri.matches("^[0-9]{10}(-[0-9]{3})?$")) {
            throw new IllegalArgumentException("Mã số thuế phải gồm 10 chữ số, có thể thêm -xxx cho chi nhánh!");
        }
        this.maSoThue = giaTri;
    }

    public String getLoaiKhachHang() {
        return loaiKhachHang;
    }

    public void setLoaiKhachHang(String loaiKhachHang) {
        if (!java.util.Arrays.asList(LOAI_KHACH_HANG).contains(loaiKhachHang)) {
            throw new IllegalArgumentException("Phân loại khách hàng không hợp lệ!");
        }
        this.loaiKhachHang = loaiKhachHang;
    }

    public boolean isDuocGhiNo() {
        return duocGhiNo;
    }

    public void setDuocGhiNo(boolean duocGhiNo) {
        this.duocGhiNo = duocGhiNo;
    }

    /** Khách cá nhân bắt buộc có email, khách doanh nghiệp bắt buộc có mã số thuế (FR-04). */
    public void kiemTraHopLe() {
        boolean doanhNghiep = LOAI_KHACH_HANG[1].equals(loaiKhachHang);
        if (doanhNghiep && (maSoThue == null || maSoThue.isEmpty())) {
            throw new IllegalArgumentException("Khách doanh nghiệp phải có mã số thuế!");
        }
        if (!doanhNghiep && (email == null || email.isEmpty())) {
            throw new IllegalArgumentException("Khách cá nhân phải có email!");
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof KhachHang && Objects.equals(maKH, ((KhachHang) o).maKH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKH);
    }

    @Override
    public String toString() {
        return maKH + " - " + hoTen + " (" + soDienThoai + ")";
    }
}
