package entity;

import java.time.LocalDate;
import java.util.Objects;

/** Thực thể thống kê số lượt xe tiếp nhận vào xưởng theo ngày. */
public class LuotXeNgay {
    private LocalDate ngay;
    private int soLuotXe;

    public LuotXeNgay() {
    }

    public LuotXeNgay(LocalDate ngay, int soLuotXe) {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày thống kê không được để trống.");
        }
        if (soLuotXe < 0) {
            throw new IllegalArgumentException("Số lượt xe không được âm.");
        }
        this.ngay = ngay;
        this.soLuotXe = soLuotXe;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public int getSoLuotXe() {
        return soLuotXe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LuotXeNgay)) return false;
        LuotXeNgay that = (LuotXeNgay) o;
        return Objects.equals(ngay, that.ngay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngay);
    }

    @Override
    public String toString() {
        return ngay + " - " + soLuotXe + " xe";
    }
}
