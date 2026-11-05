package entity;

import java.util.Objects;

public class PhuTung {
    private String maSKU;
    private String tenPhuTung;
    private String hangSanXuat;
    private String viTriLuuKho;
    private double giaNhap;
    private double giaBanLe;
    private int soLuongTon;
    private int nguongCanhBao;

    public PhuTung() {}

    public PhuTung(String maSKU) {
        setMaSKU(maSKU);
    }

    public PhuTung(String maSKU, String tenPhuTung, String hangSanXuat, String viTriLuuKho,
                   double giaNhap, double giaBanLe, int soLuongTon, int nguongCanhBao) {
        setMaSKU(maSKU);
        setTenPhuTung(tenPhuTung);
        setHangSanXuat(hangSanXuat);
        setViTriLuuKho(viTriLuuKho);
        setGiaNhap(giaNhap);
        setGiaBanLe(giaBanLe);
        setSoLuongTon(soLuongTon);
        setNguongCanhBao(nguongCanhBao);
    }

    public String getMaSKU() {
        return maSKU;
    }
    public void setMaSKU(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được để trống.");
        }
        this.maSKU = maSKU.trim();
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }
    public void setTenPhuTung(String tenPhuTung) {
        if (tenPhuTung == null || tenPhuTung.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phụ tùng không được rỗng.");
        }
        this.tenPhuTung = tenPhuTung.trim();
    }

    public String getHangSanXuat() {
        return hangSanXuat;
    }
    public void setHangSanXuat(String hangSanXuat) {
        this.hangSanXuat = hangSanXuat == null ? "" : hangSanXuat.trim();
    }

    public String getViTriLuuKho() {
        return viTriLuuKho;
    }
    public void setViTriLuuKho(String viTriLuuKho) {
        this.viTriLuuKho = viTriLuuKho == null ? "" : viTriLuuKho.trim();
    }

    public double getGiaNhap() {
        return giaNhap;
    }
    public void setGiaNhap(double giaNhap) {
        if (giaNhap < 0) {
            throw new IllegalArgumentException("Giá nhập phải > 0.");
        }
        this.giaNhap = giaNhap;
    }

    public double getGiaBanLe() {
        return giaBanLe;
    }
    public void setGiaBanLe(double giaBanLe) {
        if (giaBanLe < this.giaNhap) {
            throw new IllegalArgumentException("Giá bán lẻ không được thấp hơn giá nhập.");
        }
        this.giaBanLe = giaBanLe;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }
    public void setSoLuongTon(int soLuongTon) {
        if (soLuongTon < 0) {
            throw new IllegalArgumentException("Số lượng tồn không được là số âm.");
        }
        this.soLuongTon = soLuongTon;
    }

    public int getNguongCanhBao() {
        return nguongCanhBao;
    }
    public void setNguongCanhBao(int nguongCanhBao) {
        if (nguongCanhBao < 0) {
            throw new IllegalArgumentException("Ngưỡng cảnh báo không hợp lệ.");
        }
        this.nguongCanhBao = nguongCanhBao;
    }

    public boolean duTon(int soLuong) {
        return soLuongTon >= soLuong;
    }

    /** Trừ tồn khi duyệt xuất kho. */
    public void xuatKho(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0.");
        }
        if (!duTon(soLuong)) {
            throw new IllegalArgumentException("Tồn kho trên hệ thống không đủ cho mã " + maSKU);
        }
        soLuongTon -= soLuong;
    }

    /** Cộng tồn khi nhập kho; giá nhập của phụ tùng lấy theo lần nhập này. */
    public void nhapKho(int soLuong, double giaNhapMoi) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        }
        setGiaNhap(giaNhapMoi);
        soLuongTon += soLuong;
    }

    /** Đưa tồn kho lên gấp đôi ngưỡng cảnh báo, tối thiểu bằng ngưỡng và không dưới 1. */
    public int soLuongNhapDeXuat() {
        return Math.max(1, Math.max(nguongCanhBao * 2 - soLuongTon, nguongCanhBao));
    }

    public boolean kiemTraCanhBaoTon() {
        return soLuongTon <= nguongCanhBao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhuTung)) return false;
        PhuTung that = (PhuTung) o;
        return Objects.equals(maSKU, that.maSKU);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSKU);
    }


    @Override
    public String toString() {
        return maSKU + " - " + tenPhuTung;
    }
}
