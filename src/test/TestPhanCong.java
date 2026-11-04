package test;

import connection.ConnectDB;
import dao.ChiTietSuaChua_DichVu_Dao;
import dao.DichVu_Dao;
import dao.NhanVien_Dao;
import dao.PhanCong_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import entity.CaLamViec;
import entity.ChiTietSuaChua_DichVu;
import entity.DichVu;
import entity.KhachHang;
import entity.KyNang;
import entity.MucUuTien;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import entity.VaiTro;
import entity.Xe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Kiểm thử tự động phân công kỹ thuật viên trên CSDL mẫu mới tạo.
 * Mọi phép chọn dùng giờ cố định nên kết quả không phụ thuộc đồng hồ máy.
 * Dữ liệu mẫu: NV0005 (CA03 08-17) có PTNSC00002 đang sửa DV0004 60 phút; NV0006 (CA02 13-17) rảnh.
 */
public class TestPhanCong {
    private static final LocalTime S8 = LocalTime.of(8, 0);
    private static final LocalTime S17 = LocalTime.of(17, 0);
    private static final LocalTime S13 = LocalTime.of(13, 0);
    /** Một ngày thứ Hai trong quá khứ để phiếu tiếp nhận không bị coi là ở tương lai. */
    private static final LocalDateTime D9 = LocalDateTime.of(2025, 11, 3, 9, 0);
    private static final LocalDateTime D13 = LocalDateTime.of(2025, 11, 3, 13, 0);

    private static final PhanCong_Dao pc = new PhanCong_Dao();
    private static int soLoi;
    private static int stt;

    public static void main(String[] args) throws Exception {
        kiemTraCongPhut();
        kiemTraEntity();
        kiemTraDuLieuMau();
        kiemTraChonKyNangVaCa();
        kiemTraHangDoi();
        kiemTraNhieuDichVu();
        kiemTraKhongAiLamDuocVaBan();
        kiemTraHoaNhau();
        kiemTraTaoTiepNhan();
        kiemTraNhanVienDao();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    // ---------- congPhut ----------

    private static void gio(String ten, LocalDateTime tu, int phut, LocalTime bd, LocalTime kt, LocalDateTime mong) {
        LocalDateTime thuc = PhanCong_Dao.congPhut(tu, phut, bd, kt);
        dung(ten + " (được " + thuc + ")", mong.equals(thuc));
    }

    private static void kiemTraCongPhut() {
        gio("Trong ca", D9, 60, S8, S17, D9.withHour(10));
        gio("Qua giờ tan ca sang ngày sau", D9.withHour(16), 120, S8, S17, D9.plusDays(1).withHour(9));
        gio("Bắt đầu trước giờ vào ca", D9.withHour(6), 30, S8, S17, D9.withHour(8).withMinute(30));
        gio("Bắt đầu sau giờ tan ca", D9.withHour(18), 30, S8, S17, D9.plusDays(1).withHour(8).withMinute(30));
        gio("Bắt đầu đúng giờ tan ca", D9.withHour(17), 30, S8, S17, D9.plusDays(1).withHour(8).withMinute(30));
        gio("Kéo dài nhiều ngày", D9, 1200, S8, S17, D9.plusDays(2).withHour(11));
        gio("Xong đúng giờ tan ca", D9.withHour(15), 120, S8, S17, D9.withHour(17));
        gio("Ca chiều, bắt đầu giữa trưa", D9.withHour(12), 30, S13, S17, D9.withHour(13).withMinute(30));
        gio("Không có ngày nghỉ", LocalDateTime.of(2025, 11, 7, 16, 30), 60, S8, S17, LocalDateTime.of(2025, 11, 8, 8, 30));
        gio("Không có việc thì giữ nguyên giờ", D9.withHour(6), 0, S8, S17, D9.withHour(6));
        gio("Giữ giây lẻ", D9.withSecond(30), 60, S8, S17, D9.withHour(10).withSecond(30));
        try {
            PhanCong_Dao.congPhut(D9, 10, S17, S8);
            dung("Ca bắt đầu sau kết thúc phải báo lỗi", false);
        } catch (IllegalArgumentException ex) {
            dung("Ca bắt đầu sau kết thúc phải báo lỗi", true);
        }
    }

    private static void kiemTraEntity() {
        CaLamViec ca = new CaLamViec("CAX", "Ca sáng", S8, LocalTime.of(12, 0));
        dung("CaLamViec hiển thị trong combo", "Ca sáng (08:00 - 12:00)".equals(ca.toString()));
        loi("Ca giờ bắt đầu bằng giờ kết thúc", () -> new CaLamViec("CAX", "A", S8, S8));
        loi("Ca bắt đầu sau kết thúc", () -> new CaLamViec("CAX", "A", S17, S8));
        NhanVien nv = new NhanVien("NV0005");
        DichVu dv = new DichVu("DV0001");
        dung("KyNang hợp lệ", new KyNang(nv, dv, 1).getThoiGianPhut() == 1);
        loi("KyNang 0 phút", () -> new KyNang(nv, dv, 0));
        loi("KyNang âm", () -> new KyNang(nv, dv, -5));
        loi("DichVu thời gian chuẩn 0", () -> dv.setThoiGianChuan(0));
        dung("DichVu mặc định 60 phút", new DichVu("DVX").getThoiGianChuan() == 60);
        dung("NhanVien mới chưa có ca, kỹ năng rỗng", nv.getCaLamViec() == null && nv.getDanhSachKyNang().isEmpty());
    }

    private static void kiemTraDuLieuMau() throws Exception {
        List<DichVu> ds = new DichVu_Dao().findAll();
        dung("DichVu_Dao nạp thời gian chuẩn", ds.size() == 6 && ds.get(0).getThoiGianChuan() == 120
                && ds.get(1).getThoiGianChuan() == 45);
        NhanVien nv6 = new NhanVien_Dao().findById("NV0006");
        dung("NV0006 làm ca chiều", nv6.getCaLamViec() != null && "CA02".equals(nv6.getCaLamViec().getMaCa())
                && nv6.getCaLamViec().getGioBatDau().equals(S13) && nv6.getCaLamViec().getGioKetThuc().equals(S17));
        dung("NV0005 làm ca hành chính", "CA03".equals(new NhanVien_Dao().findById("NV0005").getCaLamViec().getMaCa()));
        dung("Đọc nhân viên không nạp kỹ năng", nv6.getDanhSachKyNang().isEmpty());
        List<KyNang> kn6 = new NhanVien_Dao().findKyNang("NV0006");
        dung("NV0006 có 4 kỹ năng, phanh 90 phút", kn6.size() == 4 && phutCua(kn6, "DV0005") == 90
                && phutCua(kn6, "DV0001") == 120);
        dung("NV0005 có 4 kỹ năng", new NhanVien_Dao().findKyNang("NV0005").size() == 4);
    }

    private static int phutCua(List<KyNang> ds, String maDichVu) {
        for (KyNang k : ds) {
            if (k.getDichVu().getMaDichVu().equals(maDichVu)) {
                return k.getThoiGianPhut();
            }
        }
        return -1;
    }

    // ---------- chọn kỹ thuật viên ----------

    private static PhanCong_Dao.KetQua chon(LocalDateTime gio, String... maDichVu) throws SQLException {
        List<DichVu> ds = new ArrayList<>();
        for (String ma : maDichVu) {
            ds.add(new DichVu(ma));
        }
        return pc.chon(gio, ds);
    }

    private static String tho(PhanCong_Dao.KetQua kq, String maDichVu) {
        NhanVien nv = kq.getPhanCong().get(new DichVu(maDichVu));
        return nv == null ? null : nv.getMaNV();
    }

    private static String phuTrach(PhanCong_Dao.KetQua kq) {
        return kq.getPhuTrach() == null ? null : kq.getPhuTrach().getMaNV();
    }

    private static void kiemTraChonKyNangVaCa() throws Exception {
        dung("Chỉ người có kỹ năng: DV0002 là NV0005", "NV0005".equals(tho(chon(D9, "DV0002"), "DV0002")));
        dung("Chỉ người có kỹ năng: DV0003 là NV0006", "NV0006".equals(tho(chon(D9, "DV0003"), "DV0003")));
        dung("Chỉ người có kỹ năng: DV0004 là NV0005", "NV0005".equals(tho(chon(D9, "DV0004"), "DV0004")));
        dung("Chỉ người có kỹ năng: DV0006 là NV0006", "NV0006".equals(tho(chon(D9, "DV0006"), "DV0006")));

        // 09:00: NV0005 còn 60 phút việc cũ nên xong 12:00; NV0006 chỉ vào ca 13:00 nên xong 15:00.
        PhanCong_Dao.KetQua kq = chon(D9, "DV0001");
        dung("Người trong ca thắng người ngoài ca", "NV0005".equals(tho(kq, "DV0001")));
        dung("Giờ dự kiến theo hàng đợi cộng thời lượng", D9.withHour(12).equals(kq.getDuKien()));

        // 13:00: NV0006 làm phanh 90 phút (không phải 120 chuẩn) nên xong 14:30, sớm hơn NV0005 (16:00).
        kq = chon(D13, "DV0005");
        dung("Thời gian riêng của KTV nhanh hơn được dùng", "NV0006".equals(tho(kq, "DV0005"))
                && D13.withHour(14).withMinute(30).equals(kq.getDuKien()));
        dung("KTV rảnh trong ca thì không có ghi chú bận", kq.getGhiChu().isEmpty());
    }

    private static void kiemTraHangDoi() throws Exception {
        xoaDuLieuThu();
        // Việc đã xong, phiếu đã hủy hoặc đã hoàn thành không tính vào hàng đợi.
        taoPhieu("Chờ kiểm tra", "DV0001", "NV0005", "Hoàn thành");
        taoPhieu("Đã hủy", "DV0002", "NV0005", "Chờ sửa");
        taoPhieu("Hoàn thành", "DV0005", "NV0005", "Đang sửa");
        PhanCong_Dao.KetQua kq = chon(D9, "DV0001");
        dung("Việc xong, phiếu hủy, phiếu hoàn thành không tính", D9.withHour(12).equals(kq.getDuKien()));

        // Việc chưa xong trên phiếu còn mở thì tính, cả khi đang sửa dở.
        taoPhieu("Đang sửa", "DV0002", "NV0005", "Đang sửa");
        kq = chon(D9, "DV0001");
        dung("Việc chưa xong tính đủ thời lượng (45 phút)", D9.withHour(12).withMinute(45).equals(kq.getDuKien()));

        // Dịch vụ KTV chưa có dòng kỹ năng thì tính theo thời gian chuẩn.
        xoaDuLieuThu();
        thucThi("INSERT INTO DichVu (maDichVu, tenDichVu, tienCong, thoiGianChuan) VALUES ('DVT01', N'Dịch vụ thử', 0, 30)");
        taoPhieu("Đang sửa", "DVT01", "NV0006", "Chờ sửa");
        kq = chon(D13, "DV0003");
        dung("Hàng đợi dùng thời gian chuẩn khi không có dòng kỹ năng", D13.withHour(15).equals(kq.getDuKien())
                && "NV0006".equals(tho(kq, "DV0003")));
        xoaDuLieuThu();
        thucThi("DELETE FROM DichVu WHERE maDichVu = 'DVT01'");
    }

    private static void kiemTraNhieuDichVu() throws Exception {
        xoaDuLieuThu();
        // DV0001 -> NV0006 (xong 15:00). Việc đó vào hàng đợi nên DV0005 -> NV0005 (16:00, NV0006 sẽ 16:30).
        PhanCong_Dao.KetQua kq = chon(D13, "DV0001", "DV0005");
        dung("Việc trong cùng phiếu dàn đều: DV0001 -> NV0006", "NV0006".equals(tho(kq, "DV0001")));
        dung("Việc trong cùng phiếu dàn đều: DV0005 -> NV0005", "NV0005".equals(tho(kq, "DV0005")));
        dung("Bằng số việc thì người xong sớm hơn phụ trách", "NV0006".equals(phuTrach(kq)));
        dung("Dự kiến là giờ xong muộn nhất", D13.withHour(16).equals(kq.getDuKien()));

        kq = chon(D9, "DV0002", "DV0004", "DV0003");
        dung("Phụ trách là người nhiều việc nhất", "NV0005".equals(phuTrach(kq)) && "NV0006".equals(tho(kq, "DV0003")));
        dung("Danh sách giữ đủ từng dịch vụ", kq.getPhanCong().size() == 3);
    }

    private static void kiemTraKhongAiLamDuocVaBan() throws Exception {
        xoaDuLieuThu();
        thucThi("INSERT INTO DichVu (maDichVu, tenDichVu, tienCong, thoiGianChuan) VALUES ('DVT01', N'Dịch vụ thử', 0, 30)");
        PhanCong_Dao.KetQua kq = chon(D9, "DVT01");
        dung("Không ai làm được: null kèm ghi chú", kq.getPhanCong().containsKey(new DichVu("DVT01"))
                && tho(kq, "DVT01") == null && kq.getGhiChu().size() == 1);
        dung("Không giao được gì thì không có phụ trách và dự kiến", kq.getPhuTrach() == null && kq.getDuKien() == null);
        kq = chon(D9, "DVT01", "DV0002");
        dung("Dịch vụ còn lại vẫn được giao", "NV0005".equals(tho(kq, "DV0002")) && "NV0005".equals(phuTrach(kq))
                && D9.withHour(10).withMinute(45).equals(kq.getDuKien()));
        thucThi("DELETE FROM DichVu WHERE maDichVu = 'DVT01'");

        // 09:00: NV0005 đang có việc, NV0006 ngoài ca: vẫn giao nhưng ghi chú bận.
        kq = chon(D9, "DV0001");
        dung("Ai cũng bận vẫn giao việc", "NV0005".equals(tho(kq, "DV0001")));
        dung("Ai cũng bận có ghi chú", kq.getGhiChu().size() == 1 && kq.getGhiChu().get(0).contains("đang rảnh"));
        kq = chon(D9, "DV0003");
        dung("KTV ngoài ca không phải là rảnh", "NV0006".equals(tho(kq, "DV0003")) && kq.getGhiChu().size() == 1);
        kq = chon(D13, "DV0003");
        dung("KTV trong ca và không có việc là rảnh", kq.getGhiChu().isEmpty());

        // 13:00: NV0006 còn 15 phút việc nên sẽ xong 14:45, NV0005 rảnh nhưng 15:00 mới xong: vẫn ưu tiên người rảnh.
        thucThi("INSERT INTO DichVu (maDichVu, tenDichVu, tienCong, thoiGianChuan) VALUES ('DVT01', N'Dịch vụ thử', 0, 15)");
        thucThi("UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = N'Hoàn thành' WHERE maPhieuTNSC = 'PTNSC00002'");
        try {
            taoPhieu("Đang sửa", "DVT01", "NV0006", "Chờ sửa");
            kq = chon(D13, "DV0005");
            dung("Người rảnh thắng người bận dù người bận xong sớm hơn", "NV0005".equals(tho(kq, "DV0005")));
            dung("Có người rảnh thì không ghi chú bận", kq.getGhiChu().isEmpty());
        } finally {
            thucThi("UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = N'Đang sửa' WHERE maPhieuTNSC = 'PTNSC00002'");
            xoaDuLieuThu();
            thucThi("DELETE FROM DichVu WHERE maDichVu = 'DVT01'");
        }
    }

    private static void kiemTraHoaNhau() throws Exception {
        xoaDuLieuThu();
        thucThi("INSERT INTO DichVu (maDichVu, tenDichVu, tienCong, thoiGianChuan) VALUES ('DVT01', N'Dịch vụ thử', 0, 30)");
        thucThi("UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = N'Hoàn thành' WHERE maPhieuTNSC = 'PTNSC00002'");
        try {
            // Cùng giờ xong 16:00: NV0005 có 2 việc, NV0006 có 1 việc (60 phút chuẩn của DV0004) nên NV0006 thắng.
            taoPhieu("Đang sửa", "DVT01", "NV0005", "Chờ sửa");
            taoPhieu("Đang sửa", "DVT01", "NV0005", "Chờ sửa");
            taoPhieu("Đang sửa", "DV0004", "NV0006", "Chờ sửa");
            PhanCong_Dao.KetQua kq = chon(D13, "DV0001");
            dung("Hòa giờ xong thì ít việc hơn thắng", "NV0006".equals(tho(kq, "DV0001"))
                    && D13.withHour(16).equals(kq.getDuKien()));

            // Cùng giờ, cùng số việc: mã nhỏ nhất thắng.
            xoaDuLieuThu();
            kq = chon(D13, "DV0001");
            dung("Hòa hoàn toàn thì mã nhỏ nhất thắng", "NV0005".equals(tho(kq, "DV0001"))
                    && D13.withHour(15).equals(kq.getDuKien()));
        } finally {
            xoaDuLieuThu();
            thucThi("DELETE FROM DichVu WHERE maDichVu = 'DVT01'");
            thucThi("UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = N'Đang sửa' WHERE maPhieuTNSC = 'PTNSC00002'");
        }
    }

    // ---------- taoTiepNhan ----------

    private static void kiemTraTaoTiepNhan() throws Exception {
        PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
        ChiTietSuaChua_DichVu_Dao chiTiet = new ChiTietSuaChua_DichVu_Dao();
        List<DichVu> dv = Arrays.asList(dichVu("DV0001", 250000), dichVu("DV0005", 350000));

        PhieuTiepNhanVaSuaChua p = phieuMau("59A-901.01", D13);
        PhanCong_Dao.KetQua kq = dao.taoTiepNhan(khachMau("0911000101"), xeMau("59A-901.01"), p, dv);
        PhieuTiepNhanVaSuaChua luu = dao.findById(p.getMaPhieuTNSC());
        dung("taoTiepNhan lưu người phụ trách", luu.getNhanVienPhuTrach() != null
                && "NV0006".equals(luu.getNhanVienPhuTrach().getMaNV()));
        dung("taoTiepNhan lưu giờ dự kiến tự tính", D13.withHour(16).equals(luu.getThoiGianDuKien()));
        dung("taoTiepNhan trả kết quả cho giao diện", kq.getPhanCong().size() == 2 && "NV0006".equals(phuTrach(kq)));
        dung("taoTiepNhan lưu thợ từng dòng", thoCuaDong(chiTiet, p, "DV0001").equals("NV0006")
                && thoCuaDong(chiTiet, p, "DV0005").equals("NV0005"));

        // Ngày do lễ tân tự chọn thì giữ nguyên.
        LocalDateTime tayChon = D13.plusDays(5).withHour(10);
        PhieuTiepNhanVaSuaChua p2 = phieuMau("59A-901.02", D13);
        p2.setThoiGianDuKien(tayChon);
        dao.taoTiepNhan(khachMau("0911000102"), xeMau("59A-901.02"), p2, dv);
        PhieuTiepNhanVaSuaChua luu2 = dao.findById(p2.getMaPhieuTNSC());
        dung("Ngày tự chọn được giữ", tayChon.equals(luu2.getThoiGianDuKien()));
        dung("Ngày tự chọn vẫn có người phụ trách và thợ", luu2.getNhanVienPhuTrach() != null
                && !thoCuaDong(chiTiet, p2, "DV0001").isEmpty());

        // Dịch vụ không tồn tại làm hỏng bước ghi dòng: toàn bộ kể cả phân công phải hoàn tác.
        int phieuTruoc = dem("SELECT COUNT(*) FROM PhieuTiepNhanVaSuaChua");
        int dongTruoc = dem("SELECT COUNT(*) FROM ChiTietSuaChua_DichVu");
        PhieuTiepNhanVaSuaChua p3 = phieuMau("59A-901.03", D13);
        boolean baoLoi = false;
        try {
            dao.taoTiepNhan(khachMau("0911000103"), xeMau("59A-901.03"), p3,
                    Arrays.asList(dichVu("DV0001", 250000), dichVu("KHONGCO", 1)));
        } catch (SQLException ex) {
            baoLoi = true;
        }
        dung("Lỗi giữa chừng vẫn báo lỗi", baoLoi);
        dung("Lỗi giữa chừng hoàn tác phiếu, dòng, khách, xe", dem("SELECT COUNT(*) FROM PhieuTiepNhanVaSuaChua") == phieuTruoc
                && dem("SELECT COUNT(*) FROM ChiTietSuaChua_DichVu") == dongTruoc
                && dao.findKhachTheoSdt("0911000103") == null && dao.findXeDayDu("59A-901.03") == null);
        dung("Lỗi giữa chừng không làm đổi ngày dự kiến của phiếu truyền vào", p3.getThoiGianDuKien() == null
                && p3.getNhanVienPhuTrach() == null);
    }

    private static String thoCuaDong(ChiTietSuaChua_DichVu_Dao dao, PhieuTiepNhanVaSuaChua p, String maDichVu) throws SQLException {
        for (ChiTietSuaChua_DichVu d : dao.findByPhieu(p.getMaPhieuTNSC())) {
            if (d.getDichVu().getMaDichVu().equals(maDichVu)) {
                return d.getKyThuatVien() == null ? "" : d.getKyThuatVien().getMaNV();
            }
        }
        return "";
    }

    // ---------- NhanVien_Dao: ca và kỹ năng ----------

    private static void kiemTraNhanVienDao() throws Exception {
        NhanVien_Dao dao = new NhanVien_Dao();
        dung("Có 3 ca mẫu", dao.findAllCa().size() == 3);
        CaLamViec sang = ca(dao, "CA01");
        CaLamViec chieu = ca(dao, "CA02");

        String ma = dao.nextId();
        NhanVien nv = new NhanVien(ma, "Kỹ thuật viên thử", "0987000001", "Quận 1", VaiTro.KY_THUAT_VIEN);
        nv.setCaLamViec(sang);
        nv.setDanhSachKyNang(Arrays.asList(new KyNang(nv, new DichVu("DV0001"), 45), new KyNang(nv, new DichVu("DV0002"), 30)));
        dung("Thêm nhân viên kèm ca và kỹ năng", dao.insert(nv, "admin"));
        NhanVien doc = dao.findById(ma);
        dung("Đọc lại ca làm việc", doc.getCaLamViec() != null && "CA01".equals(doc.getCaLamViec().getMaCa())
                && "Ca sáng (08:00 - 12:00)".equals(doc.getCaLamViec().toString()));
        List<KyNang> kn = dao.findKyNang(ma);
        dung("Đọc lại kỹ năng và số phút", kn.size() == 2 && phutCua(kn, "DV0001") == 45 && phutCua(kn, "DV0002") == 30);

        nv.setCaLamViec(chieu);
        nv.setDanhSachKyNang(Arrays.asList(new KyNang(nv, new DichVu("DV0003"), 20)));
        dung("Sửa nhân viên thay kỹ năng", dao.update(nv, "admin"));
        kn = dao.findKyNang(ma);
        dung("Kỹ năng cũ bị thay hoàn toàn", kn.size() == 1 && phutCua(kn, "DV0003") == 20
                && "CA02".equals(dao.findById(ma).getCaLamViec().getMaCa()));

        nv.setCaLamViec(null);
        nv.setDanhSachKyNang(new ArrayList<>());
        dung("Bỏ ca và xóa hết kỹ năng", dao.update(nv, "admin") && dao.findKyNang(ma).isEmpty()
                && dao.findById(ma).getCaLamViec() == null);

        nv.setCaLamViec(sang);
        nv.setDanhSachKyNang(Arrays.asList(new KyNang(nv, new DichVu("DV0006"), 15)));
        dao.update(nv, "admin");
        // 09:00 trong ca sáng, 15 phút: xong 09:15, sớm hơn NV0006 (vào ca 13:00).
        dung("KTV mới có kỹ năng và ca được scheduler chọn", ma.equals(tho(chon(D9, "DV0006"), "DV0006")));
        dung("Trước khi xóa có dòng kỹ năng", dem("SELECT COUNT(*) FROM NhanVien_DichVu WHERE maNV = '" + ma + "'") == 1);
        dung("Xóa nhân viên", dao.delete(ma, "admin"));
        dung("Xóa nhân viên xóa luôn dòng kỹ năng", dem("SELECT COUNT(*) FROM NhanVien_DichVu WHERE maNV = '" + ma + "'") == 0);
    }

    private static CaLamViec ca(NhanVien_Dao dao, String ma) throws SQLException {
        for (CaLamViec c : dao.findAllCa()) {
            if (c.getMaCa().equals(ma)) {
                return c;
            }
        }
        throw new IllegalStateException("Thiếu ca " + ma);
    }

    // ---------- tiện ích ----------

    private static DichVu dichVu(String ma, double tienCong) {
        DichVu dv = new DichVu(ma);
        dv.setTienCong(tienCong);
        return dv;
    }

    /** Phiếu thử có mã PCT..., một dòng dịch vụ đã giao thợ; xóa bằng xoaDuLieuThu. */
    private static void taoPhieu(String trangThaiPhieu, String maDichVu, String maNV, String trangThaiTienDo) throws SQLException {
        String ma = "PCT" + (++stt);
        thucThi("INSERT INTO PhieuTiepNhanVaSuaChua (maPhieuTNSC, bienSo, maNV_LapPhieu, trangThai) "
                + "VALUES ('" + ma + "', '51H-123.45', 'NV0002', N'" + trangThaiPhieu + "')");
        thucThi("INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, maNV_KyThuat, trangThaiTienDo) "
                + "VALUES ('" + ma + "', '" + maDichVu + "', '" + maNV + "', N'" + trangThaiTienDo + "')");
    }

    private static void xoaDuLieuThu() throws SQLException {
        thucThi("DELETE FROM PhieuTiepNhanVaSuaChua WHERE maPhieuTNSC LIKE 'PCT%'");
    }

    private static KhachHang khachMau(String sdt) {
        KhachHang kh = new KhachHang();
        kh.setHoTen("Nguyễn Văn Thử");
        kh.setSoDienThoai(sdt);
        kh.setDiaChi("Quận 1");
        kh.setEmail("thu@example.com");
        return kh;
    }

    private static Xe xeMau(String bienSo) {
        Xe xe = new Xe();
        xe.setBienSo(bienSo);
        xe.setSoVIN("VIN" + bienSo);
        xe.setHangXe("Toyota");
        xe.setDongXe("Vios");
        xe.setDoiXe(2021);
        xe.setMauSon("Bạc");
        xe.setKhachHang(new KhachHang("MOI"));
        return xe;
    }

    private static PhieuTiepNhanVaSuaChua phieuMau(String bienSo, LocalDateTime tiepNhan) {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setXe(new Xe(bienSo));
        p.setNhanVienLap(new NhanVien("NV0003"));
        p.setYeuCauBanDau("Kiểm tra");
        p.setSoKm(100);
        p.setMucNhienLieu(50);
        p.setThoiGianTiepNhan(tiepNhan);
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

    private static void thucThi(String sql) throws SQLException {
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.executeUpdate();
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
}
