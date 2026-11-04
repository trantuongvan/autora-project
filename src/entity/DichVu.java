package entity;

import java.util.Objects;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private double tienCong;
    private int thoiGianChuan = 60;

    public DichVu() {
    }

    public DichVu(String maDichVu) {
        setMaDichVu(maDichVu);
    }

    public DichVu(String maDichVu, String tenDichVu, double tienCong) {
        setMaDichVu(maDichVu);
        setTenDichVu(tenDichVu);
        setTienCong(tienCong);
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        }
        this.maDichVu = maDichVu.trim();
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        if (tenDichVu == null || tenDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        this.tenDichVu = tenDichVu.trim();
    }

    public double getTienCong() {
        return tienCong;
    }

    public void setTienCong(double tienCong) {
        if (!Double.isFinite(tienCong) || tienCong < 0) {
            throw new IllegalArgumentException("Tiền công phải là số hữu hạn không âm.");
        }
        this.tienCong = tienCong;
    }

    /** Số phút chuẩn để làm xong dịch vụ. */
    public int getThoiGianChuan() {
        return thoiGianChuan;
    }

    public void setThoiGianChuan(int thoiGianChuan) {
        if (thoiGianChuan <= 0) {
            throw new IllegalArgumentException("Thời gian chuẩn phải lớn hơn 0 phút.");
        }
        this.thoiGianChuan = thoiGianChuan;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DichVu other = (DichVu) obj;
        return Objects.equals(maDichVu, other.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maDichVu);
    }

    @Override
    public String toString() {
        return tenDichVu;
    }
}
