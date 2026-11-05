package entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Ca làm việc cố định trong ngày của nhân viên. */
public class CaLamViec {
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;

    public CaLamViec() {
    }

    public CaLamViec(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        setMaCa(maCa);
        setTenCa(tenCa);
        setKhungGio(gioBatDau, gioKetThuc);
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        if (maCa == null || maCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ca không được để trống.");
        }
        this.maCa = maCa.trim();
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        if (tenCa == null || tenCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ca không được để trống.");
        }
        this.tenCa = tenCa.trim();
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    /** Đặt cả hai giờ cùng lúc vì từng giờ riêng lẻ không kiểm tra được thứ tự. */
    public void setKhungGio(LocalTime gioBatDau, LocalTime gioKetThuc) {
        if (gioBatDau == null || gioKetThuc == null) {
            throw new IllegalArgumentException("Giờ bắt đầu và giờ kết thúc không được để trống.");
        }
        if (!gioBatDau.isBefore(gioKetThuc)) {
            throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc.");
        }
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public boolean dangTrongCa(LocalTime gio) {
        return !gio.isBefore(gioBatDau) && gio.isBefore(gioKetThuc);
    }

    /**
     * Giờ xong khi bắt đầu lúc {@code tu} và chỉ tính thời gian nằm trong ca.
     * Trước giờ vào ca thì bắt đầu từ giờ vào ca; từ giờ tan ca trở đi thì bắt đầu ca ngày hôm sau.
     */
    public LocalDateTime congPhut(LocalDateTime tu, int phut) {
        if (phut <= 0) {
            return tu;
        }
        long can = phut * 60L;
        LocalDateTime t = tu;
        while (true) {
            LocalDateTime vao = t.toLocalDate().atTime(gioBatDau);
            LocalDateTime ra = t.toLocalDate().atTime(gioKetThuc);
            if (t.isBefore(vao)) {
                t = vao;
            } else if (!t.isBefore(ra)) {
                t = vao.plusDays(1);
                continue;
            }
            long con = Duration.between(t, ra).getSeconds();
            if (can <= con) {
                return t.plusSeconds(can);
            }
            can -= con;
            t = vao.plusDays(1);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(maCa, ((CaLamViec) obj).maCa);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maCa);
    }

    /** Ví dụ "Ca sáng (08:00 - 12:00)" để dùng thẳng trong JComboBox. */
    @Override
    public String toString() {
        return tenCa + " (" + gioBatDau.format(GIO) + " - " + gioKetThuc.format(GIO) + ")";
    }
}
