package entity;

import java.util.Objects;

public class Thue {
    private String maThue;
    private String tenLoaiThue;
    private double thueSuat;
    // Không có cột trong CSDL: chỉ giữ số tiền thuế vừa tính.
    private double giaTriThue;

    public Thue() {
    }

    public Thue(String maThue) {
        setMaThue(maThue);
    }

    public Thue(String maThue, String tenLoaiThue, double thueSuat, double giaTriThue) {
        setMaThue(maThue);
        setTenLoaiThue(tenLoaiThue);
        setThueSuat(thueSuat);
        setGiaTriThue(giaTriThue);
    }

    public String getMaThue() {
        return maThue;
    }

    public void setMaThue(String maThue) {
        if (maThue == null || maThue.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thuế không được rỗng");
        }
        this.maThue = maThue.trim();
    }

    public String getTenLoaiThue() {
        return tenLoaiThue;
    }

    public void setTenLoaiThue(String tenLoaiThue) {
        if (tenLoaiThue == null || tenLoaiThue.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại thuế không được rỗng");
        }
        this.tenLoaiThue = tenLoaiThue.trim();
    }

    public double getThueSuat() {
        return thueSuat;
    }

    public void setThueSuat(double thueSuat) {
        if (thueSuat < 0 || thueSuat > 100) {
            throw new IllegalArgumentException("Thuế suất phải từ 0% đến 100%");
        }
        this.thueSuat = thueSuat;
    }

    public double getGiaTriThue() {
        return giaTriThue;
    }

    public void setGiaTriThue(double giaTriThue) {
        if (giaTriThue < 0) {
            throw new IllegalArgumentException("Giá trị thuế không được âm");
        }
        this.giaTriThue = giaTriThue;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Thue && Objects.equals(maThue, ((Thue) obj).maThue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maThue);
    }

    @Override
    public String toString() {
        return tenLoaiThue == null ? String.valueOf(maThue) : tenLoaiThue;
    }
}
