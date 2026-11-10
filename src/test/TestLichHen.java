package test;

import connection.ConnectDB;
import dao.KhachHang_Dao;
import dao.LichHen_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import dao.Xe_Dao;
import entity.DichVu;
import entity.KhachHang;
import entity.LichHen;
import entity.MucUuTien;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiLichHen;
import entity.TrangThaiPhieu;
import entity.Xe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kiểm thử lịch hẹn trên CSDL mẫu mới tạo. Dữ liệu thử dùng khách KHL.. và xe 77L-... để không đụng dữ liệu mẫu;
 * lịch thử ghi thẳng bằng SQL (mã LH9....) khi cần giờ trong quá khứ mà LichHen_Dao.insert từ chối.
 */
public class TestLichHen {
    private static final LichHen_Dao dao = new LichHen_Dao();
    private static final KhachHang_Dao khDao = new KhachHang_Dao();
    private static final LocalDateTime HOM_NAY = LocalDate.now().atStartOfDay();
    private static int soLoi;
    private static int stt;

    public static void main(String[] args) throws Exception {
        kiemTraEnumVaEntity();
        kiemTraTaoVaDanhSach();
        kiemTraGioQuaKhu();
        kiemTraHuy();
        kiemTraBoLocKhach();
        kiemTraDanhSachHomNay();
        kiemTraTuDongDong();
        kiemTraXoaKhachVaXe();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    // ---------- enum và entity ----------

    private static void kiemTraEnumVaEntity() {
        dung("fromDb Đang hẹn", TrangThaiLichHen.fromDb("Đang hẹn") == TrangThaiLichHen.DANG_HEN);
        dung("fromDb Đã đến", TrangThaiLichHen.fromDb("Đã đến") == TrangThaiLichHen.DA_DEN);
        dung("fromDb Đã hủy", TrangThaiLichHen.fromDb("Đã hủy") == TrangThaiLichHen.DA_HUY);
        dung("getText và toString là chuỗi lưu CSDL", "Đã đến".equals(TrangThaiLichHen.DA_DEN.getText())
                && "Đã hủy".equals(TrangThaiLichHen.DA_HUY.toString()));
        loi("fromDb giá trị lạ", () -> TrangThaiLichHen.fromDb("Hoãn"));
        loi("fromDb null", () -> TrangThaiLichHen.fromDb(null));

        LichHen lh = new LichHen();
        dung("Mới tạo ở Đang hẹn, ghi chú rỗng", lh.getTrangThai() == TrangThaiLichHen.DANG_HEN && lh.getGhiChu().isEmpty());
        loi("Khách null", () -> lh.setKhachHang(null));
        loi("Giờ hẹn null", () -> lh.setThoiGianHen(null));
        loi("Trạng thái null", () -> lh.setTrangThai(null));
        loi("Ghi chú quá 255 ký tự", () -> lh.setGhiChu("x".repeat(256)));
        loi("Mã sai định dạng", () -> lh.setMaLichHen("LH1"));
        lh.setGhiChu("  nhắc mang sổ  ");
        lh.setXe(null);
        dung("Ghi chú được cắt khoảng trắng, xe cho phép null", "nhắc mang sổ".equals(lh.getGhiChu()) && lh.getXe() == null);
    }

    // ---------- tạo và liệt kê ----------

    private static void kiemTraTaoVaDanhSach() throws Exception {
        khach("KHL01", "0970000001");
        xe("77L-001.01", "KHL01");
        LocalDateTime gio = HOM_NAY.plusDays(2).withHour(9);

        LichHen a = lichMoi("KHL01", "77L-001.01", gio, "Thay nhớt");
        dao.insert(a);
        dung("Insert cấp mã LH và 5 chữ số", a.getMaLichHen() != null && a.getMaLichHen().matches("^LH\\d{5}$"));
        LichHen b = lichMoi("KHL01", null, gio.plusDays(1), "");
        dao.insert(b);
        dung("Mã tăng dần", Integer.parseInt(b.getMaLichHen().substring(2)) == Integer.parseInt(a.getMaLichHen().substring(2)) + 1);

        List<LichHen> ds = dao.findByKhachHang("KHL01");
        dung("Liệt kê theo khách có 2 lịch, mới nhất trước", ds.size() == 2 && ds.get(0).getMaLichHen().equals(b.getMaLichHen()));
        LichHen docA = ds.get(1);
        dung("Đọc lại giờ, ghi chú, trạng thái", gio.equals(docA.getThoiGianHen()) && "Thay nhớt".equals(docA.getGhiChu())
                && docA.getTrangThai() == TrangThaiLichHen.DANG_HEN);
        dung("Đọc lại xe", docA.getXe() != null && "77L-001.01".equals(docA.getXe().getBienSo())
                && "Toyota".equals(docA.getXe().getHangXe()));
        dung("Lịch không gắn xe đọc ra xe null và ghi chú rỗng", ds.get(0).getXe() == null && ds.get(0).getGhiChu().isEmpty());
        dung("Khách khác không thấy lịch này", dao.findByKhachHang("KH0002").isEmpty());
    }

    private static void kiemTraGioQuaKhu() throws Exception {
        int truoc = dem("SELECT COUNT(*) FROM LichHen");
        loi("Giờ hẹn trong quá khứ (1 giờ trước)", () -> dao.insert(lichMoi("KHL01", null, LocalDateTime.now().minusHours(1), "")));
        loi("Giờ hẹn hôm qua", () -> dao.insert(lichMoi("KHL01", null, HOM_NAY.minusDays(1).withHour(10), "")));
        dung("Giờ quá khứ không tạo dòng", dem("SELECT COUNT(*) FROM LichHen") == truoc);
        LichHen ganDay = lichMoi("KHL01", null, LocalDateTime.now().plusMinutes(5), "");
        dao.insert(ganDay);
        dung("Giờ sắp tới được nhận", ganDay.getMaLichHen() != null);
    }

    // ---------- hủy ----------

    private static void kiemTraHuy() throws Exception {
        khach("KHL02", "0970000002");
        String dangHen = lichSql("KHL02", HOM_NAY.plusDays(1), null, "Đang hẹn");
        String daDen = lichSql("KHL02", HOM_NAY.plusDays(1), null, "Đã đến");
        String daHuy = lichSql("KHL02", HOM_NAY.plusDays(1), null, "Đã hủy");
        dung("Hủy lịch Đang hẹn", dao.huy(dangHen) && trangThai(dangHen).equals("Đã hủy"));
        dung("Hủy lần hai không được", !dao.huy(dangHen));
        dung("Không hủy được lịch Đã đến", !dao.huy(daDen) && trangThai(daDen).equals("Đã đến"));
        dung("Không hủy được lịch Đã hủy", !dao.huy(daHuy));
        dung("Hủy mã không tồn tại trả về false", !dao.huy("LH99999"));
    }

    // ---------- bộ lọc khách ----------

    private static void kiemTraBoLocKhach() throws Exception {
        String homNay = khachCoLich("KHL10", "0970000010", HOM_NAY, "Đang hẹn");
        String quaHan = khachCoLich("KHL11", "0970000011", HOM_NAY.minusDays(1).withHour(10), "Đang hẹn");
        String daHuy = khachCoLich("KHL12", "0970000012", HOM_NAY.plusDays(1), "Đã hủy");
        String daDen = khachCoLich("KHL13", "0970000013", HOM_NAY.plusDays(1), "Đã đến");
        khach("KHL14", "0970000014");
        LocalDateTime gan = HOM_NAY.plusDays(2).withHour(9);
        khach("KHL15", "0970000015");
        lichSql("KHL15", HOM_NAY.plusDays(5).withHour(8), null, "Đang hẹn");
        lichSql("KHL15", gan, null, "Đang hẹn");
        lichSql("KHL15", HOM_NAY.plusDays(1), null, "Đã hủy");

        Set<String> tatCa = maKhach(khDao.search("", null, null, null));
        Set<String> co = maKhach(khDao.search("", null, null, true));
        Set<String> khong = maKhach(khDao.search("", null, null, false));
        dung("Tất cả gồm cả hai nhóm", tatCa.containsAll(co) && tatCa.containsAll(khong) && tatCa.size() == co.size() + khong.size());
        dung("Hai nhóm không giao nhau", giao(co, khong).isEmpty());
        dung("Lịch Đang hẹn hôm nay tính là Có", co.contains("KHL10"));
        dung("Lịch Đang hẹn tương lai tính là Có", co.contains("KHL15"));
        dung("Lịch quá hẹn không tính là Có", !co.contains("KHL11") && khong.contains("KHL11"));
        dung("Lịch Đã hủy không tính là Có", !co.contains("KHL12") && khong.contains("KHL12"));
        dung("Lịch Đã đến không tính là Có", !co.contains("KHL13") && khong.contains("KHL13"));
        dung("Khách không có lịch là Không", khong.contains("KHL14") && !co.contains("KHL14"));
        dung("Dữ liệu mẫu: KH0001 và KH0003 có lịch, KH0002 không",
                co.contains("KH0001") && co.contains("KH0003") && khong.contains("KH0002"));
        dung("Bộ lọc lịch kết hợp với từ khóa", maKhach(khDao.search("0970000015", null, null, true)).equals(Set.of("KHL15"))
                && khDao.search("0970000015", null, null, false).isEmpty());
        dung("Ba tham số cũ vẫn không lọc lịch", maKhach(khDao.search("", null, null)).equals(tatCa));

        KhachHang kh15 = timKhach("KHL15");
        dung("Lịch gần nhất là lịch Đang hẹn sớm nhất từ hôm nay", kh15.getLichHenGanNhat() != null
                && gan.equals(kh15.getLichHenGanNhat().getThoiGianHen()));
        dung("Lịch gần nhất của lịch hôm nay", HOM_NAY.equals(timKhach("KHL10").getLichHenGanNhat().getThoiGianHen()));
        dung("Lịch gần nhất rỗng khi không tính được", timKhach("KHL11").getLichHenGanNhat() == null
                && timKhach("KHL12").getLichHenGanNhat() == null && timKhach("KHL14").getLichHenGanNhat() == null);
        dung("Khách nhiều xe vẫn đúng một dòng", khDao.search("0988111222", null, null, true).size() == 1
                && khDao.search("0988111222", null, null, true).get(0).getDanhSachXe().size() == 2);
        dung("Lịch mẫu hôm nay là lịch gần nhất của KH0001", timKhach("KH0001").getLichHenGanNhat() != null
                && HOM_NAY.plusHours(15).equals(timKhach("KH0001").getLichHenGanNhat().getThoiGianHen()));
        dung("Mã dùng trong bộ lọc được dùng", homNay != null && quaHan != null && daHuy != null && daDen != null);
    }

    // ---------- danh sách hôm nay ----------

    private static void kiemTraDanhSachHomNay() throws Exception {
        khach("KHL20", "0970000020");
        xe("77L-020.01", "KHL20");
        String muon = lichSql("KHL20", HOM_NAY.withHour(17), "77L-020.01", "Đang hẹn");
        String som = lichSql("KHL20", HOM_NAY.withHour(8), null, "Đang hẹn");
        String huy = lichSql("KHL20", HOM_NAY.withHour(10), null, "Đã hủy");
        String den = lichSql("KHL20", HOM_NAY.withHour(11), null, "Đã đến");
        String hqua = lichSql("KHL20", HOM_NAY.minusDays(1).withHour(12), null, "Đang hẹn");
        String ngayMai = lichSql("KHL20", HOM_NAY.plusDays(1).withHour(1), null, "Đang hẹn");

        List<LichHen> ds = dao.findHomNay();
        List<String> ma = new ArrayList<>();
        for (LichHen l : ds) {
            ma.add(l.getMaLichHen());
        }
        dung("Hôm nay có lịch Đang hẹn của hôm nay", ma.contains(muon) && ma.contains(som));
        dung("Hôm nay loại lịch Đã hủy và Đã đến", !ma.contains(huy) && !ma.contains(den));
        dung("Hôm nay loại lịch ngày khác", !ma.contains(hqua) && !ma.contains(ngayMai));
        dung("Hôm nay gồm lịch mẫu hôm nay, không gồm lịch mẫu ngày sau", ma.contains("LH00001")
                && !ma.contains("LH00002") && !ma.contains("LH00003"));
        dung("Sắp theo giờ", ma.indexOf(som) >= 0 && ma.indexOf(som) < ma.indexOf(muon));
        LichHen docMuon = ds.get(ma.indexOf(muon));
        dung("Khách điền sẵn từ JOIN", "0970000020".equals(docMuon.getKhachHang().getSoDienThoai())
                && "Nguyễn Văn Thử".equals(docMuon.getKhachHang().getHoTen()));
        dung("Xe điền sẵn từ JOIN", docMuon.getXe() != null && "77L-020.01".equals(docMuon.getXe().getBienSo())
                && "Toyota".equals(docMuon.getXe().getHangXe()) && "Vios".equals(docMuon.getXe().getDongXe()));
        dung("Lịch không xe có xe null", ds.get(ma.indexOf(som)).getXe() == null);
    }

    // ---------- tự đóng khi lập phiếu ----------

    private static void kiemTraTuDongDong() throws Exception {
        PhieuTiepNhanVaSuaChua_Dao pdao = new PhieuTiepNhanVaSuaChua_Dao();
        List<DichVu> dv = Arrays.asList(dichVu("DV0001", 250000));
        khach("KHL30", "0970000030");
        khach("KHL31", "0970000031");
        xe("77L-030.01", "KHL30");
        xe("77L-030.02", "KHL30");
        xe("77L-031.01", "KHL31");
        LocalDateTime quaHan = HOM_NAY.minusDays(2).withHour(9);
        String xeHomNay = lichSql("KHL30", HOM_NAY.withHour(23), "77L-030.01", "Đang hẹn");
        String xeQuaHan = lichSql("KHL30", quaHan, "77L-030.01", "Đang hẹn");
        String khachHomNay = lichSql("KHL30", HOM_NAY.withHour(1), null, "Đang hẹn");
        String khachQuaHan = lichSql("KHL30", quaHan, null, "Đang hẹn");
        String xeTuongLai = lichSql("KHL30", HOM_NAY.plusDays(3), "77L-030.01", "Đang hẹn");
        String khachTuongLai = lichSql("KHL30", HOM_NAY.plusDays(2), null, "Đang hẹn");
        String xeKhac = lichSql("KHL30", HOM_NAY.withHour(12), "77L-030.02", "Đang hẹn");
        String khachKhacXeKhac = lichSql("KHL31", HOM_NAY.withHour(12), "77L-031.01", "Đang hẹn");
        String khachKhacKhongXe = lichSql("KHL31", HOM_NAY.withHour(12), null, "Đang hẹn");
        String daHuy = lichSql("KHL30", HOM_NAY.withHour(12), "77L-030.01", "Đã hủy");

        // Lỗi ở bước ghi dòng dịch vụ (sau khi đã đóng lịch): lịch phải còn nguyên.
        PhieuTiepNhanVaSuaChua loiPhieu = phieuMau("77L-030.01");
        boolean baoLoi = false;
        try {
            pdao.taoTiepNhan(null, null, loiPhieu, Arrays.asList(dichVu("DV0001", 250000), dichVu("KHONGCO", 1)));
        } catch (SQLException ex) {
            baoLoi = true;
        }
        dung("Lưu phiếu lỗi vẫn báo lỗi", baoLoi);
        dung("Lưu phiếu lỗi hoàn tác việc đóng lịch", trangThai(xeHomNay).equals("Đang hẹn")
                && trangThai(khachHomNay).equals("Đang hẹn") && trangThai(xeQuaHan).equals("Đang hẹn"));

        pdao.taoTiepNhan(null, null, phieuMau("77L-030.01"), dv);
        dung("Đóng lịch hôm nay của xe", trangThai(xeHomNay).equals("Đã đến"));
        dung("Đóng lịch quá hẹn của xe", trangThai(xeQuaHan).equals("Đã đến"));
        dung("Đóng lịch hôm nay của khách chưa gắn xe", trangThai(khachHomNay).equals("Đã đến"));
        dung("Đóng lịch quá hẹn của khách chưa gắn xe", trangThai(khachQuaHan).equals("Đã đến"));
        dung("Giữ lịch tương lai của xe", trangThai(xeTuongLai).equals("Đang hẹn"));
        dung("Giữ lịch tương lai của khách chưa gắn xe", trangThai(khachTuongLai).equals("Đang hẹn"));
        dung("Giữ lịch của xe khác cùng khách", trangThai(xeKhac).equals("Đang hẹn"));
        dung("Giữ lịch của khách khác", trangThai(khachKhacXeKhac).equals("Đang hẹn")
                && trangThai(khachKhacKhongXe).equals("Đang hẹn"));
        dung("Giữ nguyên lịch Đã hủy", trangThai(daHuy).equals("Đã hủy"));

        // Sửa phiếu không đóng lịch.
        String moi = lichSql("KHL30", HOM_NAY.withHour(13), "77L-030.01", "Đang hẹn");
        PhieuTiepNhanVaSuaChua daLuu = pdao.findById(maPhieuCuaXe("77L-030.01"));
        daLuu.setGhiChu("sửa ghi chú");
        dung("Sửa phiếu thành công", pdao.update(daLuu));
        dung("Sửa phiếu không đóng lịch", trangThai(moi).equals("Đang hẹn"));
    }

    // ---------- xóa khách, xóa xe ----------

    private static void kiemTraXoaKhachVaXe() throws Exception {
        khach("KHL40", "0970000040");
        lichSql("KHL40", HOM_NAY.plusDays(1), null, "Đang hẹn");
        lichSql("KHL40", HOM_NAY.plusDays(2), null, "Đã hủy");
        dung("Trước khi xóa khách có 2 lịch", dem("SELECT COUNT(*) FROM LichHen WHERE maKH = 'KHL40'") == 2);
        dung("Xóa khách", khDao.delete("KHL40", "admin"));
        dung("Xóa khách xóa luôn lịch hẹn", dem("SELECT COUNT(*) FROM LichHen WHERE maKH = 'KHL40'") == 0);

        khach("KHL41", "0970000041");
        xe("77L-041.01", "KHL41");
        String ma = lichSql("KHL41", HOM_NAY.plusDays(1), "77L-041.01", "Đang hẹn");
        dung("Xóa xe", new Xe_Dao().delete("77L-041.01"));
        List<LichHen> ds = dao.findByKhachHang("KHL41");
        dung("Xóa xe giữ lịch hẹn nhưng bỏ xe", ds.size() == 1 && ma.equals(ds.get(0).getMaLichHen())
                && ds.get(0).getXe() == null && ds.get(0).getTrangThai() == TrangThaiLichHen.DANG_HEN);
    }

    // ---------- tiện ích ----------

    private static LichHen lichMoi(String maKH, String bienSo, LocalDateTime gio, String ghiChu) {
        LichHen lh = new LichHen();
        lh.setKhachHang(new KhachHang(maKH));
        lh.setXe(bienSo == null ? null : new Xe(bienSo));
        lh.setThoiGianHen(gio);
        lh.setGhiChu(ghiChu);
        return lh;
    }

    private static String khachCoLich(String ma, String sdt, LocalDateTime gio, String trangThai) throws SQLException {
        khach(ma, sdt);
        return lichSql(ma, gio, null, trangThai);
    }

    private static void khach(String ma, String sdt) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement s = c.prepareStatement(
                        "INSERT INTO KhachHang (maKH, hoTen, soDienThoai, diaChi) VALUES (?, N'Nguyễn Văn Thử', ?, N'Quận 1')")) {
            s.setString(1, ma);
            s.setString(2, sdt);
            s.executeUpdate();
        }
    }

    private static void xe(String bienSo, String maKH) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement s = c.prepareStatement(
                        "INSERT INTO Xe (bienSo, hangXe, dongXe, maKH) VALUES (?, 'Toyota', 'Vios', ?)")) {
            s.setString(1, bienSo);
            s.setString(2, maKH);
            s.executeUpdate();
        }
    }

    /** Ghi thẳng một lịch hẹn, bỏ qua kiểm tra giờ quá khứ của DAO; trả về mã. */
    private static String lichSql(String maKH, LocalDateTime gio, String bienSo, String trangThai) throws SQLException {
        String ma = String.format("LH9%04d", ++stt);
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement s = c.prepareStatement(
                        "INSERT INTO LichHen (maLichHen, thoiGianHen, trangThai, maKH, bienSo) VALUES (?, ?, ?, ?, ?)")) {
            s.setString(1, ma);
            s.setTimestamp(2, Timestamp.valueOf(gio));
            s.setString(3, trangThai);
            s.setString(4, maKH);
            s.setString(5, bienSo);
            s.executeUpdate();
        }
        return ma;
    }

    private static String trangThai(String maLichHen) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement s = c.prepareStatement("SELECT trangThai FROM LichHen WHERE maLichHen = ?")) {
            s.setString(1, maLichHen);
            try (ResultSet rs = s.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }

    private static String maPhieuCuaXe(String bienSo) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement s = c.prepareStatement(
                        "SELECT TOP 1 maPhieuTNSC FROM PhieuTiepNhanVaSuaChua WHERE bienSo = ? ORDER BY maPhieuTNSC DESC")) {
            s.setString(1, bienSo);
            try (ResultSet rs = s.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private static Set<String> maKhach(List<KhachHang> ds) {
        Set<String> ma = new HashSet<>();
        for (KhachHang kh : ds) {
            ma.add(kh.getMaKH());
        }
        return ma;
    }

    private static KhachHang timKhach(String ma) throws SQLException {
        for (KhachHang kh : khDao.search("", null, null, null)) {
            if (kh.getMaKH().equals(ma)) {
                return kh;
            }
        }
        throw new IllegalStateException("Thiếu khách " + ma);
    }

    private static DichVu dichVu(String ma, double tienCong) {
        DichVu dv = new DichVu(ma);
        dv.setTienCong(tienCong);
        return dv;
    }

    private static PhieuTiepNhanVaSuaChua phieuMau(String bienSo) {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setXe(new Xe(bienSo));
        p.setNhanVienLap(new NhanVien("NV0003"));
        p.setYeuCauBanDau("Kiểm tra");
        p.setSoKm(100);
        p.setMucNhienLieu(50);
        p.setThoiGianTiepNhan(LocalDateTime.now().minusMinutes(1));
        p.setTrangThai(TrangThaiPhieu.CHO_KIEM_TRA);
        p.setUuTien(MucUuTien.THUONG);
        p.setDanhGiaNoiThat("Ghế sạch");
        return p;
    }

    private static int dem(String sql) throws SQLException {
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(sql);
                ResultSet rs = s.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private interface ThaoTac {
        void chay() throws Exception;
    }

    private static void loi(String ten, ThaoTac t) {
        try {
            t.chay();
            dung(ten + " - phải báo lỗi", false);
        } catch (IllegalArgumentException ex) {
            // Đúng kỳ vọng.
        } catch (Exception ex) {
            dung(ten + " - sai loại lỗi " + ex, false);
        }
    }

    private static void dung(String ten, boolean ketQua) {
        if (!ketQua) {
            soLoi++;
            System.out.println("LOI: " + ten);
        }
    }

    private static Set<String> giao(Set<String> a, Set<String> b) {
        Set<String> r = new HashSet<>(a);
        r.retainAll(b);
        return r;
    }
}
