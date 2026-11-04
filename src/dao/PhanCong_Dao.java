package dao;

import connection.ConnectDB;
import entity.CaLamViec;
import entity.DichVu;
import entity.NhanVien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tự chọn kỹ thuật viên cho từng dịch vụ: người có kỹ năng và xong sớm nhất.
 * Thời điểm hiện tại luôn là tham số để kết quả tính lại được.
 */
public class PhanCong_Dao {
    // shortcut: ca chạy mọi ngày nên KTV nghỉ phép vẫn được giao việc, thêm bảng lịch trực/nghỉ khi chủ cửa hàng cần.
    // shortcut: việc đang sửa dở tính đủ thời lượng vào hàng đợi, trừ phần đã làm khi có giờ bắt đầu thực tế.
    // shortcut: hai lễ tân lưu cùng lúc có thể chọn cùng một KTV (chỉ lệch tải, không sai dữ liệu), khóa lại khi có nhiều quầy.

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

    private static class UngVien {
        NhanVien nhanVien;
        int phut;
    }

    private static class Tong {
        NhanVien nhanVien;
        int soViec;
        LocalDateTime xongCuoi;
    }

    /**
     * Giờ xong khi bắt đầu lúc {@code tu} và chỉ tính thời gian nằm trong ca [batDau, ketThuc).
     * Trước giờ vào ca thì bắt đầu từ giờ vào ca; từ giờ tan ca trở đi thì bắt đầu ca ngày hôm sau.
     */
    public static LocalDateTime congPhut(LocalDateTime tu, int phut, LocalTime batDau, LocalTime ketThuc) {
        if (!batDau.isBefore(ketThuc)) {
            throw new IllegalArgumentException("Giờ bắt đầu ca phải trước giờ kết thúc ca.");
        }
        if (phut <= 0) {
            return tu;
        }
        long can = phut * 60L;
        LocalDateTime t = tu;
        while (true) {
            LocalDateTime vao = t.toLocalDate().atTime(batDau);
            LocalDateTime ra = t.toLocalDate().atTime(ketThuc);
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

    public KetQua chon(LocalDateTime hienTai, List<DichVu> dichVu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return chon(connection, hienTai, dichVu);
        }
    }

    /** Chọn lần lượt theo thứ tự dịch vụ; việc vừa giao trong phiếu này tính vào hàng đợi cho dịch vụ kế tiếp. */
    public KetQua chon(Connection connection, LocalDateTime hienTai, List<DichVu> dichVu) throws SQLException {
        KetQua ketQua = new KetQua();
        if (dichVu.isEmpty()) {
            return ketQua;
        }
        Map<String, List<UngVien>> ungVien = docUngVien(connection, dichVu);
        Map<String, int[]> hangDoi = docHangDoi(connection);
        Map<String, Tong> tong = new LinkedHashMap<>();
        for (DichVu dv : dichVu) {
            UngVien tot = null;
            LocalDateTime xongTot = null;
            int[] hangTot = null;
            for (UngVien u : ungVien.getOrDefault(dv.getMaDichVu(), new ArrayList<>())) {
                CaLamViec ca = u.nhanVien.getCaLamViec();
                int[] hang = hangDoi.computeIfAbsent(u.nhanVien.getMaNV(), k -> new int[2]);
                LocalDateTime xong = congPhut(congPhut(hienTai, hang[0], ca.getGioBatDau(), ca.getGioKetThuc()),
                        u.phut, ca.getGioBatDau(), ca.getGioKetThuc());
                boolean hon = tot == null || xong.isBefore(xongTot)
                        || (xong.equals(xongTot) && (hang[1] < hangTot[1] || (hang[1] == hangTot[1]
                        && u.nhanVien.getMaNV().compareTo(tot.nhanVien.getMaNV()) < 0)));
                if (hon) {
                    tot = u;
                    xongTot = xong;
                    hangTot = hang;
                }
            }
            if (tot == null) {
                ketQua.phanCong.put(dv, null);
                ketQua.ghiChu.add("Không có kỹ thuật viên đủ kỹ năng cho \"" + ten(dv) + "\".");
                continue;
            }
            CaLamViec ca = tot.nhanVien.getCaLamViec();
            LocalTime gio = hienTai.toLocalTime();
            boolean ranh = hangTot[1] == 0 && !gio.isBefore(ca.getGioBatDau()) && gio.isBefore(ca.getGioKetThuc());
            if (!ranh) {
                ketQua.ghiChu.add("Mọi kỹ thuật viên đủ kỹ năng cho \"" + ten(dv) + "\" đều đang bận; đã giao cho "
                        + tot.nhanVien.getHoTen() + ".");
            }
            hangTot[0] += tot.phut;
            hangTot[1]++;
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

    private static String ten(DichVu dv) {
        return dv.getTenDichVu() == null ? dv.getMaDichVu() : dv.getTenDichVu();
    }

    /** Một câu SQL: KTV có kỹ năng và có ca, kèm số phút riêng của họ cho từng dịch vụ. */
    private Map<String, List<UngVien>> docUngVien(Connection connection, List<DichVu> dichVu) throws SQLException {
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < dichVu.size(); i++) {
            in.append(i == 0 ? "?" : ", ?");
        }
        String sql = "SELECT k.maDichVu, k.thoiGianPhut, ca.maCa, ca.tenCa, ca.gioBatDau, ca.gioKetThuc, "
                + NhanVien_Dao.cotNhanVien("n", "") + " "
                + "FROM NhanVien_DichVu k JOIN NhanVien n ON n.maNV = k.maNV "
                + "JOIN CaLamViec ca ON ca.maCa = n.maCa "
                + "WHERE k.maDichVu IN (" + in + ") ORDER BY n.maNV";
        Map<String, List<UngVien>> ketQua = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < dichVu.size(); i++) {
                statement.setString(i + 1, dichVu.get(i).getMaDichVu());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    UngVien u = new UngVien();
                    u.nhanVien = NhanVien_Dao.docNhanVien(rs, "");
                    u.nhanVien.setCaLamViec(NhanVien_Dao.docCa(rs));
                    u.phut = rs.getInt("thoiGianPhut");
                    ketQua.computeIfAbsent(rs.getString("maDichVu"), k -> new ArrayList<>()).add(u);
                }
            }
        }
        return ketQua;
    }

    /** Một câu SQL: tổng phút và số việc chưa xong của mỗi KTV; không có dòng kỹ năng thì dùng thời gian chuẩn. */
    private Map<String, int[]> docHangDoi(Connection connection) throws SQLException {
        String sql = "SELECT c.maNV_KyThuat, COUNT(*) AS soViec, SUM(COALESCE(k.thoiGianPhut, d.thoiGianChuan)) AS phut "
                + "FROM ChiTietSuaChua_DichVu c "
                + "JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = c.maPhieuTNSC "
                + "JOIN DichVu d ON d.maDichVu = c.maDichVu "
                + "LEFT JOIN NhanVien_DichVu k ON k.maNV = c.maNV_KyThuat AND k.maDichVu = c.maDichVu "
                + "WHERE c.maNV_KyThuat IS NOT NULL AND c.trangThaiTienDo <> N'Hoàn thành' "
                + "AND p.trangThai NOT IN (N'Hoàn thành', N'Đã hủy') "
                + "GROUP BY c.maNV_KyThuat";
        Map<String, int[]> ketQua = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ketQua.put(rs.getString("maNV_KyThuat"), new int[] {rs.getInt("phut"), rs.getInt("soViec")});
            }
        }
        return ketQua;
    }
}
