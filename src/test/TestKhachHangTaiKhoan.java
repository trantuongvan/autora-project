package test;

import connection.ConnectDB;
import controller.GiaoDienChinhController;
import dao.DangNhap_Dao;
import dao.KhachHang_Dao;
import dao.NhatKy_Dao;
import dao.TaiKhoan_Dao;
import dao.Xe_Dao;
import entity.KhachHang;
import entity.LoaiKhachHang;
import entity.NhanVien;
import entity.NhatKyHoatDong;
import entity.PhienDangNhap;
import entity.TaiKhoan;
import entity.VaiTro;
import entity.Xe;
import gui.QLKhachHang;
import gui.QLTaiKhoan;

import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/** Kiểm thử trên CSDL mẫu vừa tạo lại (không dùng framework). */
public class TestKhachHangTaiKhoan {
    private static final VaiTro CHU = VaiTro.CHU_CUA_HANG;
    private static final VaiTro LE_TAN = VaiTro.LE_TAN;
    private static final String MAT_KHAU = "BiMat_Khong_Lo_123";
    private static final String MAT_KHAU_MOI = "BiMat_Moi_456";

    private static int soLoi;
    private static final KhachHang_Dao khDao = new KhachHang_Dao();
    private static final TaiKhoan_Dao tkDao = new TaiKhoan_Dao();
    private static final DangNhap_Dao dangNhapDao = new DangNhap_Dao();

    public static void main(String[] args) throws Exception {
        quyTacEntity();
        khachHang();
        lichSu();
        taiKhoan();
        chuCuoi();
        chuCuoiDongThoi();
        nhatKy();
        phanQuyenKhopMenu();
        moManHinh();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        System.exit(soLoi == 0 ? 0 : 1);
    }

    private static void quyTacEntity() {
        KhachHang kh = new KhachHang();
        for (String sai : new String[] {"", "090123456", "09012345678", "1901234567", "09012a4567", null}) {
            loi("SĐT sai: " + sai, () -> kh.setSoDienThoai(sai));
        }
        dat("SĐT đúng", () -> kh.setSoDienThoai("0901234567"));
        loi("Email sai", () -> kh.setEmail("a@b"));
        dat("Email trống", () -> kh.setEmail(""));
        loi("MST sai", () -> kh.setMaSoThue("123"));
        dat("MST trống", () -> kh.setMaSoThue(null));

        TaiKhoan tk = new TaiKhoan();
        for (String sai : new String[] {null, "", "ab", "a b", " abc", "abc ", "a-b", "tên", "a@b",
            new String(new char[51]).replace('\0', 'a')}) {
            loi("Tên đăng nhập sai: " + sai, () -> tk.setTenDangNhap(sai));
        }
        for (String dung : new String[] {"abc", "thai.vy", "a_1", new String(new char[50]).replace('\0', 'a')}) {
            dat("Tên đăng nhập đúng: " + dung, () -> tk.setTenDangNhap(dung));
        }
        loi("Vai trò lạ", () -> VaiTro.fromDb("Quản lý"));
        loi("Vai trò null", () -> tk.setVaiTro(null));
        for (String sai : new String[] {null, "", "12345", "123 456", "123456\t", " 123456", "123 456"}) {
            loi("Mật khẩu sai: " + sai, () -> TaiKhoan.kiemTraMatKhau(sai));
        }
        dat("Mật khẩu 6 ký tự", () -> TaiKhoan.kiemTraMatKhau("123456"));
    }

    private static void khachHang() throws Exception {
        KhachHang moi = new KhachHang();
        moi.setHoTen("Khách & Thử");
        moi.setSoDienThoai("0911000001");
        moi.setDiaChi("Gò Vấp");
        moi.setEmail("");
        moi.setMaSoThue("");
        khDao.insert(moi);
        dung("Mã KH tự cấp là KH0004: " + moi.getMaKH(), "KH0004".equals(moi.getMaKH()));

        KhachHang trung = new KhachHang();
        trung.setHoTen("B");
        trung.setSoDienThoai("0911000001");
        trung.setDiaChi("C");
        trungSdt("Thêm trùng SĐT", () -> khDao.insert(trung));
        dung("Thêm trùng SĐT không tạo dòng", khDao.search("0911000001", null, null).size() == 1);

        moi.setSoDienThoai("0988111222");
        trungSdt("Sửa trùng SĐT của khách khác", () -> khDao.update(moi));
        moi.setSoDienThoai("0911000001");
        moi.setLoaiKhachHang(LoaiKhachHang.VIP);
        moi.setDuocGhiNo(true);
        dung("Sửa giữ nguyên SĐT của chính mình", khDao.update(moi));
        dung("Sửa khách không tồn tại trả về false", !khDao.update(new KhachHang("KH9999") {{
            setHoTen("X");
            setSoDienThoai("0911000002");
            setDiaChi("Y");
        }}));

        dung("Tìm theo biển số", dungMotKhach(khDao.search("51K-888", null, null), "KH0001"));
        dung("Danh sách xe gồm mọi biển số", "51H-123.45, 51K-888.88".equals(bienSoCuaKhach(khDao.search("51K-888", null, null).get(0))));
        dung("Tìm theo SĐT", dungMotKhach(khDao.search("0977222", null, null), "KH0002"));
        dung("Tìm theo tên có dấu", dungMotKhach(khDao.search("Thu Thảo", null, null), "KH0003"));
        dung("Lọc VIP và được ghi nợ", khDao.search("", LoaiKhachHang.VIP, true).size() == 2);
        dung("Lọc không được ghi nợ loại VIP", khDao.search("", LoaiKhachHang.VIP, false).isEmpty());
        dung("Từ khóa kết hợp bộ lọc", khDao.search("51H", LoaiKhachHang.VIP, null).isEmpty());

        dung("Chặn xóa khách có xe", !khDao.delete("KH0001"));
        dung("Khách có xe vẫn còn", dungMotKhach(khDao.search("0988111222", null, null), "KH0001"));
        dung("Xóa khách không có xe", khDao.delete("KH0004"));
        dung("Xóa lần hai trả về false", !khDao.delete("KH0004"));
        dung("Xe liên kết", khDao.xeLienKet("KH0001").size() == 2 && "Honda".equals(khDao.xeLienKet("KH0001").get(0).getHangXe()));
        List<Xe> dsXe = new Xe_Dao().search("51H-123");
        dung("Xe_Dao.search trả về xe kèm chủ xe", dsXe.size() == 1 && "KH0001".equals(dsXe.get(0).getKhachHang().getMaKH())
                && "Phạm Minh Hoàng".equals(dsXe.get(0).getKhachHang().getHoTen()));
    }

    private static void lichSu() throws Exception {
        dung("Lịch sử mẫu: hóa đơn còn hiệu lực", Double.valueOf(1506600).equals(hangLichSu("KH0001", "PTNSC00001")[3]));
        dung("Lịch sử mẫu: chưa có hóa đơn", hangLichSu("KH0002", "PTNSC00002")[3] == null);

        chay("INSERT INTO PhieuTiepNhanVaSuaChua (maPhieuTNSC, bienSo, maNV_LapPhieu, thoiGianTiepNhan) VALUES "
                + "('PTEST1', '60A-456.78', 'NV0002', '2026-11-01'), ('PTEST2', '60A-456.78', 'NV0002', '2026-11-02')");
        chay("INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, tienCongThucTe) VALUES "
                + "('PTEST1', 'DV0002', 1), ('PTEST1', 'DV0003', 1)");
        chay("INSERT INTO HoaDon (maHD, maPhieuTNSC, maNV_Lap, tongThanhToan, trangThaiThanhToan) VALUES "
                + "('HDTEST1', 'PTEST1', 'NV0002', 999, N'Đã hủy')");
        Object[] phieu = hangLichSu("KH0003", "PTEST1");
        dung("Hóa đơn đã hủy không tính tổng tiền", phieu[3] == null);
        dung("Hạng mục giữ nguyên ký tự &, không bị mã hóa XML: " + phieu[2],
                "Thay dầu động cơ & lọc nhớt, Vệ sinh kim phun buồng đốt".equals(phieu[2]));
        dung("Phiếu không có dịch vụ thì hạng mục rỗng", hangLichSu("KH0003", "PTEST2")[2] == null);

        chay("INSERT INTO HoaDon (maHD, maPhieuTNSC, maNV_Lap, tongThanhToan, trangThaiThanhToan) VALUES "
                + "('HDTEST2', 'PTEST1', 'NV0002', 500, N'Công nợ')");
        List<Object[]> dsPhieu = khDao.lichSuSuaChua("KH0003");
        dung("Hủy rồi lập lại: một dòng, lấy hóa đơn còn hiệu lực",
                dsPhieu.size() == 2 && Double.valueOf(500).equals(hangLichSu("KH0003", "PTEST1")[3]));
        dung("Lịch sử mới nhất trước", "PTEST2".equals(dsPhieu.get(0)[0]));
    }

    private static void taiKhoan() throws Exception {
        chay("INSERT INTO NhanVien (maNV, hoTen, soDienThoai, viTriLamViec) VALUES "
                + "('NVT1', N'Nhân viên một', '0900000001', N'Thủ kho'), ('NVT2', N'Nhân viên hai', '0900000002', N'Lễ tân'), "
                + "('NVT3', N'Nhân viên ba', '0900000003', N'Lễ tân')");
        List<NhanVien> chuaCo = tkDao.nhanVienChuaCoTaiKhoan();
        dung("Nhân viên chưa có tài khoản", chuaCo.size() == 3 && "NVT1".equals(chuaCo.get(0).getMaNV()));

        TaiKhoan tk = new TaiKhoan("thu.nghiem", LE_TAN, true, new NhanVien("NVT1"));
        tkDao.insert(tk, MAT_KHAU, "admin");
        dung("exists sau khi tạo", tkDao.exists("thu.nghiem"));
        dung("exists tài khoản chưa có", !tkDao.exists("khong.co"));
        String bam = giaTriMatKhau("thu.nghiem");
        dung("Lưu giá trị băm, không lưu mật khẩu rõ", bam.startsWith("pbkdf2$") && !bam.contains(MAT_KHAU));
        dung("Đăng nhập qua DangNhap_Dao", dangNhapDao.xacThuc("thu.nghiem", MAT_KHAU) != null);
        dung("Sai mật khẩu không đăng nhập được", dangNhapDao.xacThuc("thu.nghiem", "SaiRoi123") == null);
        dung("Tìm theo tên nhân viên", tkDao.search("viên một").size() == 1);
        dung("Hết nhân viên đó trong danh sách chưa có", tkDao.nhanVienChuaCoTaiKhoan().size() == 2);

        try {
            tkDao.insert(new TaiKhoan("thu.nghiem", LE_TAN, true, new NhanVien("NVT2")), MAT_KHAU, "admin");
            dung("Trùng tên đăng nhập phải bị chặn", false);
        } catch (SQLIntegrityConstraintViolationException ex) {
            dung("Thông báo trùng tên", TaiKhoan_Dao.TRUNG_TEN_DANG_NHAP.equals(ex.getMessage()));
        }
        try {
            tkDao.insert(new TaiKhoan("ten.khac", LE_TAN, true, new NhanVien("NVT1")), MAT_KHAU, "admin");
            dung("Nhân viên có hai tài khoản phải bị chặn", false);
        } catch (SQLIntegrityConstraintViolationException ex) {
            dung("Thông báo trùng nhân viên", TaiKhoan_Dao.NHAN_VIEN_DA_CO_TAI_KHOAN.equals(ex.getMessage()));
        }
        loi("Tạo với mật khẩu ngắn", () -> {
            try {
                tkDao.insert(new TaiKhoan("ngan.qua", LE_TAN, true, new NhanVien("NVT2")), "123", "admin");
            } catch (SQLException ex) {
                throw new IllegalStateException(ex);
            }
        });
        dung("Mật khẩu ngắn không tạo tài khoản", !tkDao.exists("ngan.qua"));

        dung("Đổi mật khẩu", tkDao.doiMatKhau("thu.nghiem", MAT_KHAU_MOI, "admin"));
        dung("Mật khẩu cũ hết hiệu lực", dangNhapDao.xacThuc("thu.nghiem", MAT_KHAU) == null);
        dung("Mật khẩu mới dùng được", dangNhapDao.xacThuc("thu.nghiem", MAT_KHAU_MOI) != null);
        String bamTruoc = giaTriMatKhau("thu.nghiem");
        dung("Sửa vai trò, mật khẩu trống là giữ nguyên",
                tkDao.capNhat("thu.nghiem", VaiTro.THU_KHO, null, true, "admin")
                && bamTruoc.equals(giaTriMatKhau("thu.nghiem")));
        dung("Vai trò đã đổi", dangNhapDao.xacThuc("thu.nghiem", MAT_KHAU_MOI).getVaiTro() == VaiTro.THU_KHO);

        dung("Khóa", tkDao.datTrangThai("thu.nghiem", false, "admin"));
        dung("Tài khoản khóa không đăng nhập được", dangNhapDao.xacThuc("thu.nghiem", MAT_KHAU_MOI) == null);
        dung("Mở khóa", tkDao.datTrangThai("thu.nghiem", true, "admin"));
        dung("Mở khóa xong đăng nhập được", dangNhapDao.xacThuc("thu.nghiem", MAT_KHAU_MOI) != null);
        dung("Sửa tài khoản không tồn tại", !tkDao.doiVaiTro("khong.co", LE_TAN, "admin"));
        dung("Xóa tài khoản", tkDao.delete("thu.nghiem", "admin") && !tkDao.exists("thu.nghiem"));
        dung("Xóa tài khoản không tồn tại", !tkDao.delete("thu.nghiem", "admin"));
    }

    private static void chuCuoi() throws Exception {
        int soNhatKy = demNhatKy("admin");
        String bamAdmin = giaTriMatKhau("admin");
        biChan("Khóa chủ cuối", () -> tkDao.datTrangThai("admin", false, "x"));
        biChan("Đổi vai trò chủ cuối", () -> tkDao.doiVaiTro("admin", LE_TAN, "x"));
        biChan("Xóa chủ cuối", () -> tkDao.delete("admin", "x"));
        biChan("Đổi nhiều thứ cùng lúc trên chủ cuối", () -> tkDao.capNhat("admin", LE_TAN, MAT_KHAU, null, "x"));
        dung("Bị chặn thì mật khẩu không đổi (rollback)", bamAdmin.equals(giaTriMatKhau("admin")));
        dung("Bị chặn thì không có nhật ký", demNhatKy("admin") == soNhatKy);
        dung("Chủ cuối vẫn đăng nhập được", dangNhapDao.xacThuc("admin", "admin123") != null);
        dung("Giữ nguyên vai trò và trạng thái chủ cuối thì được, đổi mật khẩu cũng được",
                tkDao.capNhat("admin", CHU, "admin123", true, "x"));
        dung("Vẫn đăng nhập được với mật khẩu đặt lại", dangNhapDao.xacThuc("admin", "admin123") != null);

        // Chủ bị khóa không được tính; tài khoản chủ đang khóa thì xóa được và đổi vai trò được.
        tkDao.insert(new TaiKhoan("chu.hai", CHU, false, new NhanVien("NVT1")), MAT_KHAU, "x");
        biChan("Chủ khóa không được tính là còn chủ", () -> tkDao.datTrangThai("admin", false, "x"));
        dung("Đổi vai trò chủ đang khóa", tkDao.doiVaiTro("chu.hai", LE_TAN, "x"));
        dung("Trả lại vai trò chủ", tkDao.doiVaiTro("chu.hai", CHU, "x"));
        dung("Xóa chủ đang khóa", tkDao.delete("chu.hai", "x"));

        tkDao.insert(new TaiKhoan("chu.hai", CHU, true, new NhanVien("NVT1")), MAT_KHAU, "x");
        dung("Có hai chủ thì khóa được một", tkDao.datTrangThai("chu.hai", false, "x"));
        biChan("Sau đó admin là chủ cuối", () -> tkDao.delete("admin", "x"));
        dung("Mở khóa lại", tkDao.datTrangThai("chu.hai", true, "x"));
        dung("Có hai chủ thì đổi vai trò được một", tkDao.doiVaiTro("chu.hai", LE_TAN, "x"));
        dung("Có một chủ khác nên xóa được tài khoản lễ tân", tkDao.delete("chu.hai", "x"));
    }

    /** Hai người cùng khóa hai chủ khác nhau: phải còn đúng một chủ hoạt động. */
    private static void chuCuoiDongThoi() throws Exception {
        tkDao.insert(new TaiKhoan("chu.hai", CHU, true, new NhanVien("NVT1")), MAT_KHAU, "x");
        for (int lan = 0; lan < 5; lan++) {
            CountDownLatch batDau = new CountDownLatch(1);
            Thread[] luong = {luongKhoa("admin", batDau), luongKhoa("chu.hai", batDau)};
            for (Thread t : luong) {
                t.start();
            }
            batDau.countDown();
            for (Thread t : luong) {
                t.join();
            }
            dung("Đồng thời lần " + lan + ": còn đúng một chủ", demChuHoatDong() == 1);
            tkDao.datTrangThai("admin", true, "x");
            tkDao.datTrangThai("chu.hai", true, "x");
        }
        tkDao.delete("chu.hai", "x");
    }

    private static Thread luongKhoa(String ten, CountDownLatch batDau) {
        return new Thread(() -> {
            try {
                batDau.await();
                tkDao.datTrangThai(ten, false, "x");
            } catch (IllegalStateException ex) {
                // Một trong hai luồng bị chặn là đúng kỳ vọng.
            } catch (Exception ex) {
                dung("Lỗi ngoài dự kiến khi khóa đồng thời: " + ex, false);
            }
        });
    }

    private static void nhatKy() throws Exception {
        tkDao.insert(new TaiKhoan("nk.test", LE_TAN, true, new NhanVien("NVT2")), MAT_KHAU, "nguoi.a");
        tkDao.doiVaiTro("nk.test", VaiTro.THU_KHO, "nguoi.a");
        tkDao.doiVaiTro("nk.test", VaiTro.THU_KHO, "nguoi.a");
        tkDao.doiMatKhau("nk.test", MAT_KHAU_MOI, "nguoi.a");
        tkDao.datTrangThai("nk.test", false, "nguoi.a");
        tkDao.datTrangThai("nk.test", false, "nguoi.a");
        tkDao.datTrangThai("nk.test", true, "nguoi.a");
        tkDao.delete("nk.test", "nguoi.a");

        List<String> hanhDong = new ArrayList<>();
        List<NhatKyHoatDong> dong = new NhatKy_Dao().search("nk.test");
        for (NhatKyHoatDong r : dong) {
            dung("Đúng người thực hiện", "nguoi.a".equals(r.getTenDangNhap()));
            hanhDong.add(0, r.getHanhDong());
        }
        dung("Đủ và đúng các dòng nhật ký, giá trị không đổi không ghi: " + hanhDong,
                hanhDong.equals(java.util.Arrays.asList("Tạo tài khoản", "Đổi vai trò", "Đổi mật khẩu",
                        "Khóa tài khoản", "Mở khóa tài khoản", "Xóa tài khoản")));
        NhatKyHoatDong doiVaiTro = dong.get(dong.size() - 2);
        dung("Nhật ký đổi vai trò có cũ và mới", LE_TAN.getText().equals(doiVaiTro.getGiaTriCu())
                && VaiTro.THU_KHO.getText().equals(doiVaiTro.getGiaTriMoi()));
        NhatKyHoatDong tao = dong.get(dong.size() - 1);
        dung("Dòng nhật ký có đủ các trường", tao.getMaNhatKy() > 0 && tao.getThoiGian() != null
                && "Tạo tài khoản".equals(tao.getHanhDong()) && "nk.test".equals(tao.getDoiTuong())
                && tao.getGiaTriCu() == null && tao.getGiaTriMoi() != null);

        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement();
                ResultSet r = s.executeQuery("SELECT COUNT(*) FROM NhatKyHoatDong WHERE CONCAT(hanhDong, doiTuong, giaTriCu, giaTriMoi) "
                        + "LIKE '%BiMat%' OR CONCAT(giaTriCu, giaTriMoi) LIKE '%pbkdf2%'")) {
            r.next();
            dung("Không có mật khẩu hay giá trị băm trong nhật ký", r.getInt(1) == 0);
        }
    }

    private static void phanQuyenKhopMenu() throws Exception {
        Field f = GiaoDienChinhController.class.getDeclaredField("MENU_THEO_VAI_TRO");
        f.setAccessible(true);
        Map<?, ?> menu = (Map<?, ?>) f.get(null);
        VaiTro[] vaiTro = VaiTro.values();
        List<Object> cardTrongBang = new ArrayList<>();
        for (Object[] dong : QLTaiKhoan.PHAN_QUYEN) {
            cardTrongBang.add(dong[1]);
            for (int i = 0; i < vaiTro.length; i++) {
                dung("Bảng phân quyền lệch menu: " + dong[0] + " / " + vaiTro[i],
                        ((List<?>) menu.get(vaiTro[i])).contains(dong[1]) == ((java.util.Set<?>) dong[2]).contains(vaiTro[i]));
            }
        }
        for (VaiTro vt : vaiTro) {
            for (Object card : (List<?>) menu.get(vt)) {
                dung("Menu có card chưa có trong bảng phân quyền: " + card,
                        String.valueOf(card).startsWith("TRANG_CHU") || cardTrongBang.contains(card));
            }
        }
    }

    private static void moManHinh() throws Exception {
        PhienDangNhap.batDau(new PhienDangNhap("admin", "NV0001", "A", CHU));
        SwingUtilities.invokeAndWait(() -> {
            dung("Mở màn hình khách hàng", QLKhachHang.createPanel() != null);
            dung("Mở màn hình tài khoản", QLTaiKhoan.createPanel() != null);
        });
        Thread.sleep(1500);
    }

    // ==========================================================
    // Hỗ trợ
    // ==========================================================

    private static boolean dungMotKhach(List<KhachHang> dong, String ma) {
        return dong.size() == 1 && ma.equals(dong.get(0).getMaKH());
    }

    private static String bienSoCuaKhach(KhachHang kh) {
        List<String> bienSo = new ArrayList<>();
        for (Xe xe : kh.getDanhSachXe()) {
            bienSo.add(xe.getBienSo());
        }
        return String.join(", ", bienSo);
    }

    private static Object[] hangLichSu(String maKH, String maPhieu) throws SQLException {
        for (Object[] r : khDao.lichSuSuaChua(maKH)) {
            if (maPhieu.equals(r[0])) {
                return r;
            }
        }
        throw new IllegalStateException("Không thấy phiếu " + maPhieu);
    }

    private static void chay(String sql) throws SQLException {
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    private static String giaTriMatKhau(String ten) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT matKhau FROM TaiKhoan WHERE tenDangNhap = ?")) {
            p.setString(1, ten);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }

    private static int demNhatKy(String doiTuong) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM NhatKyHoatDong WHERE doiTuong = ?")) {
            p.setString(1, doiTuong);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private static int demChuHoatDong() throws SQLException {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM TaiKhoan WHERE vaiTro = ? AND trangThai = 1")) {
            p.setString(1, CHU.getText());
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private interface ThaoTacDb {
        void chay() throws SQLException;
    }

    private static void trungSdt(String ten, ThaoTacDb t) throws SQLException {
        try {
            t.chay();
            dung(ten + " - phải báo trùng", false);
        } catch (SQLIntegrityConstraintViolationException ex) {
            dung(ten + " - thông báo thân thiện", KhachHang_Dao.TRUNG_SO_DIEN_THOAI.equals(ex.getMessage()));
        }
    }

    private static void biChan(String ten, ThaoTacDb t) throws SQLException {
        try {
            t.chay();
            dung(ten + " - phải bị chặn", false);
        } catch (IllegalStateException ex) {
            // Đúng kỳ vọng.
        }
    }

    private static void dat(String ten, Runnable t) {
        try {
            t.run();
        } catch (IllegalArgumentException ex) {
            dung(ten + " - " + ex.getMessage(), false);
        }
    }

    private static void loi(String ten, Runnable t) {
        try {
            t.run();
            dung(ten + " - phải báo lỗi", false);
        } catch (IllegalArgumentException ex) {
            // Đúng kỳ vọng.
        }
    }

    private static synchronized void dung(String ten, boolean ketQua) {
        if (!ketQua) {
            soLoi++;
            System.out.println("LOI: " + ten);
        }
    }
}
