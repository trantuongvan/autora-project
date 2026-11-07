package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Thue {
    private String maThue;
    private String tenLoaiThue;
    private double thueSuat;

    public Thue() {
    }

    public Thue(String maThue) {
        setMaThue(maThue);
    }

    public Thue(String maThue, String tenLoaiThue, double thueSuat) {
        setMaThue(maThue);
        setTenLoaiThue(tenLoaiThue);
        setThueSuat(thueSuat);
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

    /** Tiền thuế trên số tiền cơ sở theo thuế suất của loại thuế. */
    public double tinhTienThue(double tongTienCoSo) {
        return phanTram(tongTienCoSo, thueSuat);
    }

    /** Dùng BigDecimal để số kiểu 80,5 không bị double làm tròn xuống. */
    static double phanTram(double coSo, double tyLe) {
        return BigDecimal.valueOf(coSo).multiply(BigDecimal.valueOf(tyLe))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).doubleValue();
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
