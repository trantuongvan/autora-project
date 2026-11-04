package entity;

import java.time.LocalDate;
import java.util.Objects;

public class KhuyenMai {
    private String maKM;
    private String tenChuongTrinh;
    private HinhThucKhuyenMai hinhThuc;
    private double giaTri;
    private LocalDate thoiGianBatDau;
    private LocalDate thoiGianKetThuc;
    private boolean trangThai;
    private PhamViKhuyenMai phamViApDung = PhamViKhuyenMai.TONG_HOA_DON;
    private Integer soLuotToiDa;
    private int soLuotDaDung;
    private String ghiChu = "";

    public KhuyenMai() {
    }

    public KhuyenMai(String maKM) {
        setMaKM(maKM);
    }

    public KhuyenMai(String maKM, String tenChuongTrinh, HinhThucKhuyenMai hinhThuc,
            double giaTri, LocalDate thoiGianBatDau,
            LocalDate thoiGianKetThuc, boolean trangThai) {
        setMaKM(maKM);
        setTenChuongTrinh(tenChuongTrinh);
        setHinhThuc(hinhThuc);
        setGiaTri(giaTri);
        setThoiGianBatDau(thoiGianBatDau);
        setThoiGianKetThuc(thoiGianKetThuc);
        setTrangThai(trangThai);
        kiemTraHopLe();
    }

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        if (maKM == null || maKM.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống.");
        }
        this.maKM = maKM.trim();
    }

    public String getTenChuongTrinh() {
        return tenChuongTrinh;
    }

    public void setTenChuongTrinh(String tenChuongTrinh) {
        if (tenChuongTrinh == null || tenChuongTrinh.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chương trình không được để trống.");
        }
        this.tenChuongTrinh = tenChuongTrinh.trim();
    }

    public HinhThucKhuyenMai getHinhThuc() {
        return hinhThuc;
    }

    public void setHinhThuc(HinhThucKhuyenMai hinhThuc) {
        if (hinhThuc == null) {
            throw new IllegalArgumentException("Hình thức khuyến mãi không hợp lệ.");
        }
        this.hinhThuc = hinhThuc;
    }

    public double getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(double giaTri) {
        if (!Double.isFinite(giaTri) || giaTri <= 0) {
            throw new IllegalArgumentException("Giá trị khuyến mãi phải là số hữu hạn lớn hơn 0.");
        }
        this.giaTri = giaTri;
    }

    public LocalDate getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDate thoiGianBatDau) {
        if (thoiGianBatDau == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống.");
        }
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDate getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDate thoiGianKetThuc) {
        if (thoiGianKetThuc == null) {
            throw new IllegalArgumentException("Ngày kết thúc không được để trống.");
        }
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public PhamViKhuyenMai getPhamViApDung() {
        return phamViApDung;
    }

    public void setPhamViApDung(PhamViKhuyenMai phamViApDung) {
        if (phamViApDung == null) {
            throw new IllegalArgumentException("Phạm vi áp dụng không hợp lệ.");
        }
        this.phamViApDung = phamViApDung;
    }

    /** Trả về null khi không giới hạn số lượt. */
    public Integer getSoLuotToiDa() {
        return soLuotToiDa;
    }

    public void setSoLuotToiDa(Integer soLuotToiDa) {
        if (soLuotToiDa != null && soLuotToiDa <= 0) {
            throw new IllegalArgumentException("Số lượt tối đa phải lớn hơn 0.");
        }
        this.soLuotToiDa = soLuotToiDa;
    }

    /** Đếm từ hóa đơn chưa hủy nên chỉ đọc, không lưu xuống CSDL. */
    public int getSoLuotDaDung() {
        return soLuotDaDung;
    }

    public void setSoLuotDaDung(int soLuotDaDung) {
        this.soLuotDaDung = soLuotDaDung;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu == null ? "" : ghiChu.trim();
    }

    public void kiemTraHopLe() throws IllegalArgumentException {
        if (hinhThuc == null) {
            throw new IllegalArgumentException("Hình thức khuyến mãi không hợp lệ.");
        }
        if (!Double.isFinite(giaTri) || giaTri <= 0) {
            throw new IllegalArgumentException("Giá trị khuyến mãi phải là số hữu hạn lớn hơn 0.");
        }
        if (hinhThuc == HinhThucKhuyenMai.PHAN_TRAM && giaTri > 100) {
            throw new IllegalArgumentException("Phần trăm giảm giá không được vượt quá 100.");
        }
        if (thoiGianBatDau == null || thoiGianKetThuc == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (thoiGianKetThuc.isBefore(thoiGianBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
    }

    public String getTinhTrang(LocalDate homNay) {
        if (!trangThai) {
            return "Ngừng áp dụng";
        }
        if (homNay == null) {
            throw new IllegalArgumentException("Ngày xét tình trạng không được để trống.");
        }
        kiemTraHopLe();
        if (homNay.isBefore(thoiGianBatDau)) {
            return "Sắp diễn ra";
        }
        if (homNay.isAfter(thoiGianKetThuc)) {
            return "Hết hạn";
        }
        return "Đang áp dụng";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KhuyenMai other = (KhuyenMai) obj;
        return Objects.equals(maKM, other.maKM);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maKM);
    }

    @Override
    public String toString() {
        return tenChuongTrinh;
    }
}
