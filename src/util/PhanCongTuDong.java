package util;

import entity.CaLamViec;
import entity.DichVu;
import entity.NhanVien;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tự chọn kỹ thuật viên cho từng dịch vụ: người có kỹ năng, ưu tiên người đang rảnh, rồi đến người xong sớm nhất.
 * Thời điểm hiện tại luôn là tham số để kết quả tính lại được.
 */
public class PhanCongTuDong {
    // shortcut: ca chạy mọi ngày nên KTV nghỉ phép vẫn được giao việc, thêm bảng lịch trực/nghỉ khi chủ cửa hàng cần.
    // shortcut: việc đang sửa dở tính đủ thời lượng vào hàng đợi, trừ phần đã làm khi có giờ bắt đầu thực tế.

    /** Kết quả chọn: KTV từng dịch vụ (null khi không ai làm được), ghi chú cần chú ý, người phụ trách và giờ dự kiến xong. */
    public static class KetQua {
        private final Map<DichVu, NhanVien> phanCong = new LinkedHashMap<>();
        private final List<String> ghiChu = new ArrayList<>();
        private NhanVien phuTrach;
        private LocalDateTime duKien;

        public Map<DichVu, NhanVien> getPhanCong() { return phanCong; }
        public List<String> getGhiChu() { return ghiChu; }
        /** Người có nhiều dịch vụ nhất trên phiếu; null khi chưa giao được dịch vụ nào. */
        public NhanVien getPhuTrach() { return phuTrach; }
        /** Giờ xong muộn nhất trong các dịch vụ đã giao; null khi chưa giao được dịch vụ nào. */
        public LocalDateTime getDuKien() { return duKien; }
    }

    /** Người làm được một dịch vụ, kèm số phút của riêng họ cho dịch vụ đó. */
    public static class UngVien {
        private final NhanVien nhanVien;
        private final int phut;

        public UngVien(NhanVien nhanVien, int phut) {
            this.nhanVien = nhanVien;
            this.phut = phut;
        }
    }

    /** Việc chưa xong đang xếp hàng của một kỹ thuật viên. */
    public static class HangDoi {
        private int phut;
        private int soViec;

        public HangDoi(int phut, int soViec) {
            this.phut = phut;
            this.soViec = soViec;
        }
    }

    private static class Tong {
        NhanVien nhanVien;
        int soViec;
        LocalDateTime xongCuoi;
    }

    private final Map<String, List<UngVien>> ungVienTheoDichVu;
    private final Map<String, HangDoi> hangDoiTheoNhanVien;

    /** Ứng viên theo mã dịch vụ và hàng đợi theo mã nhân viên. */
    public PhanCongTuDong(Map<String, List<UngVien>> ungVienTheoDichVu, Map<String, HangDoi> hangDoiTheoNhanVien) {
        this.ungVienTheoDichVu = ungVienTheoDichVu;
        this.hangDoiTheoNhanVien = hangDoiTheoNhanVien;
    }

    /** Chọn lần lượt theo thứ tự dịch vụ; việc vừa giao trong phiếu này tính vào hàng đợi cho dịch vụ kế tiếp. */
    public KetQua chon(LocalDateTime hienTai, List<DichVu> dichVu) {
        KetQua ketQua = new KetQua();
        Map<String, Tong> tong = new LinkedHashMap<>();
        for (DichVu dv : dichVu) {
            UngVien tot = null;
            LocalDateTime xongTot = null;
            HangDoi hangTot = null;
            boolean ranhTot = false;
            for (UngVien u : ungVienTheoDichVu.getOrDefault(dv.getMaDichVu(), new ArrayList<>())) {
                CaLamViec ca = u.nhanVien.getCaLamViec();
                HangDoi hang = hangDoiTheoNhanVien.computeIfAbsent(u.nhanVien.getMaNV(), k -> new HangDoi(0, 0));
                LocalDateTime xong = congPhut(ca, congPhut(ca, hienTai, hang.phut), u.phut);
                boolean ranh = hang.soViec == 0 && dangTrongCa(ca, hienTai.toLocalTime());
                boolean hon = tot == null || (ranh && !ranhTot) || (ranh == ranhTot && (xong.isBefore(xongTot)
                        || (xong.equals(xongTot) && (hang.soViec < hangTot.soViec || (hang.soViec == hangTot.soViec
                        && u.nhanVien.getMaNV().compareTo(tot.nhanVien.getMaNV()) < 0)))));
                if (hon) {
                    tot = u;
                    xongTot = xong;
                    hangTot = hang;
                    ranhTot = ranh;
                }
            }
            if (tot == null) {
                ketQua.phanCong.put(dv, null);
                ketQua.ghiChu.add("Không có kỹ thuật viên đủ kỹ năng cho \"" + ten(dv) + "\".");
                continue;
            }
            if (!ranhTot) {
                ketQua.ghiChu.add("Đã giao \"" + ten(dv) + "\" cho " + tot.nhanVien.getHoTen()
                        + " (xong sớm nhất) vì chưa có kỹ thuật viên đủ kỹ năng đang rảnh.");
            }
            hangTot.phut += tot.phut;
            hangTot.soViec++;
            ketQua.phanCong.put(dv, tot.nhanVien);
            Tong t = tong.computeIfAbsent(tot.nhanVien.getMaNV(), k -> new Tong());
            t.nhanVien = tot.nhanVien;
            t.soViec++;
            t.xongCuoi = t.xongCuoi == null || xongTot.isAfter(t.xongCuoi) ? xongTot : t.xongCuoi;
            if (ketQua.duKien == null || xongTot.isAfter(ketQua.duKien)) {
                ketQua.duKien = xongTot;
            }
        }
        Tong phuTrach = null;
        for (Tong t : tong.values()) {
            if (phuTrach == null || t.soViec > phuTrach.soViec
                    || (t.soViec == phuTrach.soViec && (t.xongCuoi.isBefore(phuTrach.xongCuoi)
                    || (t.xongCuoi.equals(phuTrach.xongCuoi)
                    && t.nhanVien.getMaNV().compareTo(phuTrach.nhanVien.getMaNV()) < 0)))) {
                phuTrach = t;
            }
        }
        ketQua.phuTrach = phuTrach == null ? null : phuTrach.nhanVien;
        return ketQua;
    }

    public static boolean dangTrongCa(CaLamViec ca, LocalTime gio) {
        return !gio.isBefore(ca.getGioBatDau()) && gio.isBefore(ca.getGioKetThuc());
    }

    /**
     * Giờ xong khi bắt đầu lúc {@code tu} và chỉ tính thời gian nằm trong ca.
     * Trước giờ vào ca thì bắt đầu từ giờ vào ca; từ giờ tan ca trở đi thì bắt đầu ca ngày hôm sau.
     */
    public static LocalDateTime congPhut(CaLamViec ca, LocalDateTime tu, int phut) {
        if (phut <= 0) {
            return tu;
        }
        long can = phut * 60L;
        LocalDateTime t = tu;
        while (true) {
            LocalDateTime vao = t.toLocalDate().atTime(ca.getGioBatDau());
            LocalDateTime ra = t.toLocalDate().atTime(ca.getGioKetThuc());
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

    private static String ten(DichVu dv) {
        return dv.getTenDichVu() == null ? dv.getMaDichVu() : dv.getTenDichVu();
    }
}
