package test;

import connection.ConnectDB;
import dao.ChiTietSuaChua_DichVu_Dao;
import dao.DangNhap_Dao;
import dao.HoaDon_Dao;
import dao.KhachHang_Dao;
import dao.NhanVien_Dao;
import dao.PhieuBanGiao_Dao;
import dao.PhieuNhapKho_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import dao.PhieuYeuCauXuatKho_Dao;
import dao.PhuTung_Dao;
import dao.TienDoSuaChua_Dao;
import dao.TrangChuQuanLy_Dao;
import dao.Xe_Dao;
import entity.ChiTietNhapKho;
import entity.ChiTietSuaChua_DichVu;
import entity.ChiTietYeuCauXuatKho;
import entity.DichVu;
import entity.HinhThucThanhToan;
import entity.KhachHang;
import entity.MucUuTien;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhieuNhapKho;
import entity.PhieuTiepNhanVaSuaChua;
import entity.PhieuYeuCauXuatKho;
import entity.PhuTung;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import entity.TrangThaiYeuCauXuat;
import entity.Xe;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Chạy trên CSDL mẫu mới tạo: duyệt và từ chối xuất kho, nhật ký, hóa đơn còn phụ tùng chờ, khóa tiến độ, xóa nhân viên. */
public class TestKho {
    private static final String NGUOI = "kiem.tra";
    private static final PhieuYeuCauXuatKho_Dao yc = new PhieuYeuCauXuatKho_Dao();
    private static final TienDoSuaChua_Dao tienDo = new TienDoSuaChua_Dao();
    private static final PhieuTiepNhanVaSuaChua_Dao phieuDao = new PhieuTiepNhanVaSuaChua_Dao();
    private static final HoaDon_Dao hoaDon = new HoaDon_Dao();
    private static int soLoi;
    private static int stt;

    public static void main(String[] args) throws Exception {
        kiemTraDuyetMotLan();
        kiemTraTuChoi();
        kiemTraNhatKy();
        kiemTraHoaDonConPhuTungCho();
        kiemTraKhoaTienDo();
        kiemTraDemHomNay();
        kiemTraXoaNhanVien();
        kiemTraCauHinh();
        kiemTraBanGiao();
        kiemTraThemDichVuPhieuXong();
        kiemTraSuaPhuTung();
        kiemTraYeuCauPhieuDaKhoa();
        kiemTraThuCongNo();
        kiemTraDoiChuXe();
        kiemTraHaiMayCungDoiTon();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    private interface Viec {
        void chay() throws Exception;
    }

    /** Hai việc bắt đầu cùng lúc như hai máy; trả về số việc thành công. */
    private static int chayCungLuc(Viec a, Viec b) throws Exception {
        CountDownLatch batDau = new CountDownLatch(1);
        AtomicInteger thanhCong = new AtomicInteger();
        List<Thread> luong = new ArrayList<>();
        for (Viec viec : Arrays.asList(a, b)) {
            Thread t = new Thread(() -> {
                try {
                    batDau.await();
                    viec.chay();
                    thanhCong.incrementAndGet();
                } catch (Exception ex) {
                    System.out.println("Một máy bị từ chối: " + ex.getMessage());
                }
            });
            t.start();
            luong.add(t);
        }
        batDau.countDown();
        for (Thread t : luong) {
            t.join();
        }
        return thanhCong.get();
    }

    private static void kiemTraHaiMayCungDoiTon() throws Exception {
        PhieuNhapKho_Dao nhapKho = new PhieuNhapKho_Dao();
        String sku = "SKU_FLT01";
        for (int lan = 1; lan <= 5; lan++) {
            thucThi("UPDATE PhuTung SET soLuongTon = 1 WHERE maSKU = '" + sku + "'");
            String p1 = taoPhieu("Đang sửa");
            String p2 = taoPhieu("Đang sửa");
            String m1 = taoYeuCau(p1, sku, 1);
            String m2 = taoYeuCau(p2, sku, 1);
            int thanhCong = chayCungLuc(
                    () -> yc.duyetXuatKho(m1, p1, "NV0004", dong(m1, sku, 1), NGUOI),
                    () -> yc.duyetXuatKho(m2, p2, "NV0004", dong(m2, sku, 1), NGUOI));
            kiem("Hai máy duyệt phụ tùng cuối: đúng một máy thành công, lần " + lan, thanhCong == 1);
            kiem("Hai máy duyệt phụ tùng cuối: tồn về 0, lần " + lan, ton(sku) == 0);
            kiem("Máy thua không làm đổi yêu cầu của mình, lần " + lan,
                    "Đã xuất".equals(trangThaiYeuCau(m1)) != "Đã xuất".equals(trangThaiYeuCau(m2)));

            thucThi("UPDATE PhuTung SET soLuongTon = 10 WHERE maSKU = '" + sku + "'");
            PhieuNhapKho a = new PhieuNhapKho("PNH9" + lan + "01", LocalDateTime.now(), 3000, new NhaCungCap("NCC0001"), new NhanVien("NV0004"));
            PhieuNhapKho b = new PhieuNhapKho("PNH9" + lan + "02", LocalDateTime.now(), 4000, new NhaCungCap("NCC0001"), new NhanVien("NV0004"));
            thanhCong = chayCungLuc(
                    () -> nhapKho.insertWithDetails(a, Arrays.asList(new ChiTietNhapKho(a, new PhuTung(sku), 3, 1000))),
                    () -> nhapKho.insertWithDetails(b, Arrays.asList(new ChiTietNhapKho(b, new PhuTung(sku), 4, 1000))));
            kiem("Hai máy cùng nhập kho đều thành công, lần " + lan, thanhCong == 2);
            kiem("Hai máy cùng nhập kho: cộng đủ cả hai, lần " + lan, ton(sku) == 17);
        }
        PhuTung pt = new PhuTung(sku);
        pt.setSoLuongTon(2);
        kiem("Đủ tồn", pt.duTon(2) && !pt.duTon(3));
        pt.nhapKho(1, 5000);
        kiem("Nhập kho cộng tồn và đổi giá nhập", pt.getSoLuongTon() == 3 && pt.getGiaNhap() == 5000);
        pt.xuatKho(3);
        kiem("Xuất kho trừ tồn", pt.getSoLuongTon() == 0);
        try {
            pt.xuatKho(1);
            kiem("Xuất quá tồn phải báo lỗi", false);
        } catch (IllegalArgumentException ex) {
            kiem("Xuất quá tồn giữ nguyên số tồn", pt.getSoLuongTon() == 0);
        }
    }

    private static void kiemTraDuyetMotLan() throws Exception {
        String p = taoPhieu("Đang sửa");
        String m = taoYeuCau(p, "SKU_FLT02", 2);
        int ton = ton("SKU_FLT02");
        kiem("Duyệt lần đầu", yc.duyetXuatKho(m, p, "NV0004", dong(m, "SKU_FLT02", 2), NGUOI));
        kiem("Duyệt lần đầu trừ kho", ton("SKU_FLT02") == ton - 2);
        kiem("Duyệt xong phiếu về Đang sửa", "Đang sửa".equals(trangThaiPhieu(p)));
        boolean biChan = false;
        try {
            yc.duyetXuatKho(m, p, "NV0001", dong(m, "SKU_FLT02", 2), NGUOI);
        } catch (SQLException ex) {
            biChan = ex.getMessage().contains("không còn chờ xuất");
        }
        kiem("Duyệt lần hai bị chặn", biChan);
        kiem("Duyệt lần hai không trừ thêm kho", ton("SKU_FLT02") == ton - 2);
        kiem("Duyệt lần hai không tính thêm tiền", 2 == so("SELECT soLuongSuDung FROM ChiTietSuaChua_PhuTung WHERE maPhieuTNSC='" + p + "'"));
        kiem("Duyệt lần hai giữ thủ kho đầu", "NV0004".equals(q("SELECT maNV_ThuKho FROM PhieuYeuCauXuatKho WHERE maPhieuYC='" + m + "'")));
        kiem("Không từ chối yêu cầu đã xuất", !yc.tuChoiYeuCau(m, "NV0004", NGUOI));
        kiem("Yêu cầu đã xuất giữ nguyên", "Đã xuất".equals(trangThaiYeuCau(m)));
    }

    private static void kiemTraTuChoi() throws Exception {
        String p = taoPhieu("Đang sửa");
        String a = taoYeuCau(p, "SKU_FLT01", 1);
        String b = taoYeuCau(p, "SKU_FLT02", 1);
        kiem("Còn yêu cầu chờ thì không hủy được phiếu", !phieuDao.huy(p, NGUOI));
        int ton = ton("SKU_FLT01");
        kiem("Từ chối yêu cầu thứ nhất", yc.tuChoiYeuCau(a, "NV0004", NGUOI));
        kiem("Từ chối không đổi tồn", ton("SKU_FLT01") == ton);
        kiem("Còn yêu cầu khác thì vẫn Chờ phụ tùng", "Chờ phụ tùng".equals(trangThaiPhieu(p)));
        kiem("Từ chối hết thì về Đang sửa", yc.tuChoiYeuCau(b, "NV0004", NGUOI) && "Đang sửa".equals(trangThaiPhieu(p)));
        kiem("Không từ chối hai lần", !yc.tuChoiYeuCau(b, "NV0004", NGUOI));
        kiem("Phiếu chỉ có yêu cầu bị từ chối thì hủy được", phieuDao.huy(p, NGUOI));

        String c = taoPhieu("Đang sửa");
        String d = taoYeuCau(c, "SKU_FLT01", 1);
        String e = taoYeuCau(c, "SKU_FLT02", 1);
        yc.duyetXuatKho(d, c, "NV0004", dong(d, "SKU_FLT01", 1), NGUOI);
        kiem("Duyệt một yêu cầu, còn yêu cầu khác thì vẫn Chờ phụ tùng", "Chờ phụ tùng".equals(trangThaiPhieu(c)));
        yc.tuChoiYeuCau(e, "NV0004", NGUOI);
        kiem("Phiếu đã xuất phụ tùng thì không hủy được", !phieuDao.huy(c, NGUOI));
    }

    private static void kiemTraNhatKy() throws Exception {
        String ai = "nk.kho";
        String p = taoPhieu("Đang sửa");
        String m = yc.nextId();
        yc.insertWithDetails(yeuCau(m, p), dong(m, "SKU_FLT01", 1), ai);
        kiem("Ghi nhật ký lập yêu cầu", coNhatKy(ai, "Lập phiếu yêu cầu xuất kho", m));
        yc.duyetXuatKho(m, p, "NV0004", dong(m, "SKU_FLT01", 1), ai);
        kiem("Ghi nhật ký duyệt", coNhatKy(ai, "Duyệt xuất kho phụ tùng", m));
        String n = yc.nextId();
        yc.insertWithDetails(yeuCau(n, p), dong(n, "SKU_FLT01", 1), ai);
        yc.tuChoiYeuCau(n, "NV0004", ai);
        kiem("Ghi nhật ký từ chối", coNhatKy(ai, "Từ chối yêu cầu xuất kho", n));
        int truoc = demNhatKy(ai);
        kiem("Từ chối bị chặn không ghi nhật ký", !yc.tuChoiYeuCau(n, "NV0004", ai) && demNhatKy(ai) == truoc);
        tienDo.capNhatTienDoHangMuc(p, "DV0001", TrangThaiTienDo.DANG_SUA, ai);
        kiem("Ghi nhật ký hạng mục", coNhatKy(ai, "Cập nhật hạng mục sửa chữa", p));
        kiem("Đổi trạng thái phiếu", phieuDao.capNhatTrangThai(p, TrangThaiPhieu.DANG_SUA, TrangThaiPhieu.CHO_PHU_TUNG, ai));
        kiem("Ghi nhật ký trạng thái phiếu", coNhatKy(ai, "Cập nhật trạng thái phiếu", p));
    }

    private static void kiemTraHoaDonConPhuTungCho() throws Exception {
        String p = taoPhieu("Đang sửa");
        String m = taoYeuCau(p, "SKU_OIL01", 1);
        tienDo.capNhatTienDoHangMuc(p, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        kiem("Xong hạng mục cuối thì phiếu Hoàn thành", "Hoàn thành".equals(trangThaiPhieu(p)));
        boolean biChan = false;
        try {
            lap(p);
        } catch (SQLException ex) {
            biChan = ex.getMessage().contains("chờ xuất kho");
        }
        kiem("Không lập hóa đơn khi còn phụ tùng chờ", biChan);
        kiem("Từ chối lập không tạo hóa đơn", 0 == so("SELECT COUNT(*) FROM HoaDon WHERE maPhieuTNSC='" + p + "'"));
        yc.duyetXuatKho(m, p, "NV0004", dong(m, "SKU_OIL01", 1), NGUOI);
        String ma = lap(p);
        kiem("Duyệt xong thì hóa đơn tính đủ phụ tùng", 650000 == so("SELECT CAST(tongTienPhuTung AS INT) FROM HoaDon WHERE maHD='" + ma + "'"));

        String r = taoPhieu("Đang sửa");
        String t = taoYeuCau(r, "SKU_OIL01", 1);
        tienDo.capNhatTienDoHangMuc(r, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        yc.tuChoiYeuCau(t, "NV0004", NGUOI);
        kiem("Từ chối khi phiếu đã xong thì giữ Hoàn thành", "Hoàn thành".equals(trangThaiPhieu(r)));
        kiem("Từ chối xong thì lập được hóa đơn", lap(r) != null);
    }

    private static void kiemTraKhoaTienDo() throws Exception {
        kiem("Nhãn Chờ sửa là Chờ kiểm tra", TrangThaiPhieu.tuNhan("Chờ sửa") == TrangThaiPhieu.CHO_KIEM_TRA);

        String p = taoPhieu("Đang sửa");
        thucThi("INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, tienCongThucTe, trangThaiTienDo) VALUES ('" + p + "', 'DV0002', 1000, N'Chờ sửa')");
        tienDo.capNhatTienDoHangMuc(p, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        kiem("Đổi một hạng mục không đụng hạng mục khác", "Chờ sửa".equals(trangThaiHangMuc(p, "DV0002")));
        kiem("Còn hạng mục chưa xong thì phiếu chưa xong", "Đang sửa".equals(trangThaiPhieu(p)));
        tienDo.capNhatTienDoHangMuc(p, "DV0002", TrangThaiTienDo.HOAN_THANH, NGUOI);
        lap(p);
        kiem("Phiếu có hóa đơn không đổi được hạng mục", !tienDo.capNhatTienDoHangMuc(p, "DV0002", TrangThaiTienDo.DANG_SUA, NGUOI));
        kiem("Phiếu có hóa đơn giữ hạng mục", "Hoàn thành".equals(trangThaiHangMuc(p, "DV0002")));
        kiem("Phiếu có hóa đơn giữ trạng thái", "Hoàn thành".equals(trangThaiPhieu(p)));
        kiem("Phiếu có hóa đơn không đổi được trạng thái",
                !phieuDao.capNhatTrangThai(p, TrangThaiPhieu.HOAN_THANH, TrangThaiPhieu.DANG_SUA, NGUOI));

        String h = taoPhieu("Đã hủy");
        kiem("Phiếu đã hủy không đổi được hạng mục", !tienDo.capNhatTienDoHangMuc(h, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI));
        kiem("Phiếu đã hủy không mở lại được", "Đã hủy".equals(trangThaiPhieu(h)));
    }

    private static void kiemTraDemHomNay() throws Exception {
        int goc = yc.countDaXuatHomNay();
        String p = taoPhieu("Đang sửa");
        String m = taoYeuCau(p, "SKU_FLT02", 1);
        yc.duyetXuatKho(m, p, "NV0004", dong(m, "SKU_FLT02", 1), NGUOI);
        kiem("Duyệt hôm nay được đếm", yc.countDaXuatHomNay() == goc + 1);
        thucThi("UPDATE PhieuYeuCauXuatKho SET ngayXuat = DATEADD(month, -3, GETDATE()) WHERE maPhieuYC = '" + m + "'");
        kiem("Ngày khác không được đếm", yc.countDaXuatHomNay() == goc);
    }

    private static void kiemTraXoaNhanVien() throws Exception {
        NhanVien_Dao dao = new NhanVien_Dao();
        boolean biChan = false;
        try {
            dao.delete("NV0001", NGUOI);
        } catch (IllegalStateException ex) {
            biChan = true;
        }
        kiem("Không xóa nhân viên giữ tài khoản chủ cuối", biChan);
        kiem("Chủ cuối vẫn còn nhân viên và tài khoản", 1 == so("SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = 'admin'")
                && 1 == so("SELECT COUNT(*) FROM NhanVien WHERE maNV = 'NV0001'"));
        kiem("Chủ cuối vẫn đăng nhập được", new DangNhap_Dao().xacThuc("admin", "admin123") != null);

        thucThi("INSERT INTO NhanVien (maNV, hoTen, soDienThoai, viTriLamViec) VALUES ('NVK01', N'Chủ thứ hai', '0999000101', N'Chủ cửa hàng')");
        thucThi("INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThai, maNV) VALUES ('chu.kho', 'x', N'Chủ cửa hàng', 1, 'NVK01')");
        kiem("Có chủ khác thì xóa được nhân viên chủ", dao.delete("NVK01", NGUOI));
        kiem("Tài khoản bị xóa theo", 0 == so("SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = 'chu.kho'"));
        kiem("Ghi nhật ký xóa nhân viên", coNhatKy(NGUOI, "Xóa nhân viên", "NVK01"));

        thucThi("INSERT INTO NhanVien (maNV, hoTen, soDienThoai, viTriLamViec) VALUES ('NVK02', N'Không tài khoản', '0999000102', N'Lễ tân')");
        kiem("Xóa nhân viên không có tài khoản", dao.delete("NVK02", NGUOI));
        kiem("Xóa nhân viên không tồn tại", !dao.delete("NVK02", NGUOI));

        int truoc = demNhatKy(NGUOI);
        biChan = false;
        try {
            dao.delete("NV0002", NGUOI);
        } catch (SQLException ex) {
            biChan = true;
        }
        kiem("Nhân viên đã lập phiếu không xóa được", biChan);
        kiem("Xóa thất bại giữ tài khoản", 1 == so("SELECT COUNT(*) FROM TaiKhoan WHERE maNV = 'NV0002'"));
        kiem("Xóa thất bại không ghi nhật ký", demNhatKy(NGUOI) == truoc);
    }

    private static void kiemTraCauHinh() throws Exception {
        File thuMuc = Files.createTempDirectory("autora").toFile();
        File tep = new File(thuMuc, "database.properties");
        try {
            kiem("Thiếu tệp thì báo tên tệp", loiCauHinh(tep).contains("database.properties"));
            Files.write(tep.toPath(), "url=jdbc:x\nuser=u\n".getBytes(StandardCharsets.UTF_8));
            kiem("Thiếu giá trị thì báo tên khóa", loiCauHinh(tep).contains("password"));
            Files.write(tep.toPath(), "url=jdbc:x\nuser=u\npassword=Mật!khẩu\n".getBytes(StandardCharsets.UTF_8));
            kiem("Đọc đủ cấu hình", "Mật!khẩu".equals(ConnectDB.docCauHinh(tep).getProperty("password")));
        } finally {
            tep.delete();
            thuMuc.delete();
        }
    }

    private static void kiemTraBanGiao() throws Exception {
        PhieuBanGiao_Dao dao = new PhieuBanGiao_Dao();
        kiem("Tất cả có phiếu đã và chưa bàn giao", coPhieu(dao.findDanhSach("", "Tất cả"), "PTNSC00001") && coPhieu(dao.findDanhSach("", "Tất cả"), "PTNSC00002"));
        kiem("Chờ bàn giao chỉ có phiếu chưa bàn giao", coPhieu(dao.findDanhSach("", "Chờ bàn giao"), "PTNSC00002") && !coPhieu(dao.findDanhSach("", "Chờ bàn giao"), "PTNSC00001"));
        kiem("Đã bàn giao chỉ có phiếu đã bàn giao", coPhieu(dao.findDanhSach("", "Đã bàn giao"), "PTNSC00001") && !coPhieu(dao.findDanhSach("", "Đã bàn giao"), "PTNSC00002"));
        kiem("Tìm theo biển số", coPhieu(dao.findDanhSach("50E-999", "Chờ bàn giao"), "PTNSC00002") && !coPhieu(dao.findDanhSach("51H-123", "Chờ bàn giao"), "PTNSC00002"));

        String p = taoPhieu("Đang sửa");
        tienDo.capNhatTienDoHangMuc(p, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        boolean biChan = false;
        try {
            dao.xacNhan(p, "NV0002", "");
        } catch (SQLException ex) {
            biChan = true;
        }
        kiem("Chưa có hóa đơn thì không bàn giao", biChan);
        lap(p);
        kiem("Có hóa đơn thì bàn giao được", dao.xacNhan(p, "NV0002", ""));
        kiem("Bàn giao xong chuyển sang danh sách đã bàn giao", coPhieu(dao.findDanhSach("", "Đã bàn giao"), p) && !coPhieu(dao.findDanhSach("", "Chờ bàn giao"), p));
        biChan = false;
        try {
            dao.xacNhan(p, "NV0002", "");
        } catch (SQLException ex) {
            biChan = true;
        }
        kiem("Không bàn giao hai lần", biChan);
        String h = taoPhieu("Đã hủy");
        kiem("Phiếu đã hủy không có trong danh sách", !coPhieu(dao.findDanhSach("", "Tất cả"), h));
    }

    private static boolean coPhieu(List<Object[]> dong, String ma) {
        for (Object[] d : dong) {
            if (ma.equals(d[0])) {
                return true;
            }
        }
        return false;
    }

    private static void kiemTraThemDichVuPhieuXong() throws Exception {
        ChiTietSuaChua_DichVu_Dao dao = new ChiTietSuaChua_DichVu_Dao();
        String xong = taoPhieu("Đang sửa");
        tienDo.capNhatTienDoHangMuc(xong, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        kiem("Không thêm dịch vụ vào phiếu đã hoàn thành", !dao.insert(dongDichVu(xong)));
        kiem("Phiếu đã hoàn thành vẫn một dịch vụ", 1 == so("SELECT COUNT(*) FROM ChiTietSuaChua_DichVu WHERE maPhieuTNSC = '" + xong + "'"));
        String ma = lap(xong);
        kiem("Hóa đơn chỉ tính dịch vụ đã làm", 100000 == so("SELECT CAST(tongTienCong AS INT) FROM HoaDon WHERE maHD = '" + ma + "'"));
        kiem("Xe vẫn bàn giao được", new PhieuBanGiao_Dao().xacNhan(xong, "NV0002", ""));
        kiem("Phiếu đang sửa vẫn thêm được dịch vụ", dao.insert(dongDichVu(taoPhieu("Đang sửa"))));
        kiem("Phiếu đã hủy không thêm được dịch vụ", !dao.insert(dongDichVu(taoPhieu("Đã hủy"))));
    }

    private static ChiTietSuaChua_DichVu dongDichVu(String p) {
        ChiTietSuaChua_DichVu d = new ChiTietSuaChua_DichVu();
        d.setPhieu(new PhieuTiepNhanVaSuaChua(p));
        d.setDichVu(new DichVu("DV0002"));
        d.setTrangThaiTienDo(TrangThaiTienDo.CHO_SUA);
        d.setTienCongThucTe(150000);
        return d;
    }

    private static void kiemTraSuaPhuTung() throws Exception {
        PhuTung_Dao dao = new PhuTung_Dao();
        PhuTung cu = dao.findById("SKU_BRK01");
        int ton = cu.getSoLuongTon();
        thucThi("UPDATE PhuTung SET soLuongTon = soLuongTon - 3 WHERE maSKU = 'SKU_BRK01'");
        double giaMoi = cu.getGiaBanLe() + 1000;
        cu.setGiaBanLe(giaMoi);
        kiem("Sửa phụ tùng thành công", dao.update(cu));
        kiem("Màn hình cũ không ghi đè tồn kho", ton("SKU_BRK01") == ton - 3);
        kiem("Sửa phụ tùng vẫn lưu giá mới", dao.findById("SKU_BRK01").getGiaBanLe() == giaMoi);
    }

    private static void kiemTraYeuCauPhieuDaKhoa() throws Exception {
        for (String trangThai : new String[] {"Đã hủy", "Hoàn thành"}) {
            String p = taoPhieu(trangThai);
            boolean biChan = false;
            try {
                taoYeuCau(p, "SKU_FLT01", 1);
            } catch (SQLException ex) {
                biChan = ex.getMessage().contains("không thể yêu cầu phụ tùng");
            }
            kiem("Không gửi yêu cầu cho phiếu " + trangThai, biChan);
            kiem("Phiếu " + trangThai + " giữ nguyên trạng thái", trangThai.equals(trangThaiPhieu(p)));
            kiem("Phiếu " + trangThai + " không có yêu cầu mồ côi", 0 == so("SELECT COUNT(*) FROM PhieuYeuCauXuatKho WHERE maPhieuTNSC = '" + p + "'"));
        }
    }

    private static void kiemTraThuCongNo() throws Exception {
        TrangChuQuanLy_Dao trangChu = new TrangChuQuanLy_Dao();
        LocalDate homNay = LocalDate.now();
        kiem("Hóa đơn mẫu tính vào ngày thanh toán", trangChu.doanhThuNgay(LocalDate.of(2026, 10, 20)) >= 1506600);
        double goc = trangChu.doanhThuNgay(homNay);

        String p = taoPhieu("Đang sửa");
        tienDo.capNhatTienDoHangMuc(p, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        String ma = hoaDon.lapHoaDon(p, null, "VAT08", HinhThucThanhToan.CONG_NO, null, "NV0002");
        double tong = hoaDon.findById(ma).getTongThanhToan();
        kiem("Công nợ chưa thu chưa tính doanh thu", trangChu.doanhThuNgay(homNay) == goc);
        kiem("Công nợ chưa có ngày thanh toán", q("SELECT ngayThanhToan FROM HoaDon WHERE maHD = '" + ma + "'") == null);

        thucThi("UPDATE HoaDon SET ngayLap = DATEADD(month, -1, GETDATE()) WHERE maHD = '" + ma + "'");
        hoaDon.thanhToanCongNo(ma, HinhThucThanhToan.TIEN_MAT, 999999999.0, "thu.ngan");
        kiem("Thu nợ hôm nay tính vào doanh thu hôm nay", trangChu.doanhThuNgay(homNay) == goc + tong);
        kiem("Thu nợ tính vào doanh thu tháng này", trangChu.doanhThuTheoNgay(homNay, homNay).get(homNay) == goc + tong);
        kiem("Thu nợ có trong bảng doanh thu hôm nay", !trangChu.bangDoanhThu(homNay, homNay).isEmpty());
        kiem("Ghi nhật ký thu nợ", coNhatKy("thu.ngan", "Thu tiền công nợ", ma));

        String tra = taoPhieu("Đang sửa");
        tienDo.capNhatTienDoHangMuc(tra, "DV0001", TrangThaiTienDo.HOAN_THANH, NGUOI);
        double truoc = trangChu.doanhThuNgay(homNay);
        String maTra = lap(tra);
        kiem("Trả ngay tính vào doanh thu ngay", trangChu.doanhThuNgay(homNay) == truoc + hoaDon.findById(maTra).getTongThanhToan());
        hoaDon.huy(maTra, "Lập sai", NGUOI);
        kiem("Hóa đơn hủy không tính doanh thu", trangChu.doanhThuNgay(homNay) == truoc);
    }

    private static void kiemTraDoiChuXe() throws Exception {
        thucThi("INSERT INTO KhachHang (maKH, hoTen, soDienThoai, diaChi) VALUES ('KHK1', N'Chủ cũ', '0999000201', N'A'), ('KHK2', N'Chủ mới', '0999000202', N'B')");
        thucThi("INSERT INTO Xe (bienSo, soVIN, hangXe, dongXe, doiXe, mauSon, maKH) VALUES ('77K-100.01', 'VINKHO0000000001', 'Honda', 'City', 2020, N'Đỏ', 'KHK1')");
        PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
        List<DichVu> dichVu = Arrays.asList(new DichVu("DV0002"));
        PhieuTiepNhanVaSuaChua cu = phieuXe("77K-100.01");
        dao.taoTiepNhan(null, null, cu, dichVu);
        String maCu = cu.getMaPhieuTNSC();
        kiem("Tiếp nhận lưu chủ xe lúc đó", "KHK1".equals(q("SELECT maKH FROM PhieuTiepNhanVaSuaChua WHERE maPhieuTNSC = '" + maCu + "'")));
        thucThi("UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = N'Hoàn thành' WHERE maPhieuTNSC = '" + maCu + "'");
        thucThi("UPDATE PhieuTiepNhanVaSuaChua SET trangThai = N'Hoàn thành' WHERE maPhieuTNSC = '" + maCu + "'");
        String ma = lap(maCu);

        Xe_Dao xeDao = new Xe_Dao();
        Xe xe = xeDao.findById("77K-100.01");
        xe.setKhachHang(new KhachHang("KHK2"));
        kiem("Đổi chủ xe", xeDao.update(xe));
        kiem("Hóa đơn cũ vẫn ghi chủ cũ", "Chủ cũ".equals(hoaDon.findById(ma).getPhieu().getXe().getKhachHang().getHoTen()));
        kiem("Thông tin in hóa đơn vẫn ghi chủ cũ", "Chủ cũ".equals(hoaDon.findThongTinPhieu(maCu).getXe().getKhachHang().getHoTen()));
        kiem("Phiếu cũ vẫn ghi chủ cũ", "Chủ cũ".equals(dao.findById(maCu).getXe().getKhachHang().getHoTen()));

        KhachHang_Dao khDao = new KhachHang_Dao();
        kiem("Lịch sử cũ thuộc chủ cũ", khDao.lichSuSuaChua("KHK1").size() == 1 && khDao.lichSuSuaChua("KHK2").isEmpty());
        kiem("Chủ cũ đã có phiếu thì không xóa được", !khDao.delete("KHK1"));

        PhieuTiepNhanVaSuaChua moi = phieuXe("77K-100.01");
        dao.taoTiepNhan(null, null, moi, dichVu);
        kiem("Phiếu mới ghi chủ mới", "KHK2".equals(q("SELECT maKH FROM PhieuTiepNhanVaSuaChua WHERE maPhieuTNSC = '" + moi.getMaPhieuTNSC() + "'")));
        kiem("Phiếu mới đọc ra chủ mới", "Chủ mới".equals(dao.findById(moi.getMaPhieuTNSC()).getXe().getKhachHang().getHoTen()));
    }

    private static PhieuTiepNhanVaSuaChua phieuXe(String bienSo) {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setXe(new Xe(bienSo));
        p.setNhanVienLap(new NhanVien("NV0003"));
        p.setYeuCauBanDau("Kiểm tra");
        p.setSoKm(100);
        p.setMucNhienLieu(50);
        p.setThoiGianTiepNhan(LocalDateTime.now());
        p.setTrangThai(TrangThaiPhieu.CHO_KIEM_TRA);
        p.setUuTien(MucUuTien.THUONG);
        return p;
    }

    private static String loiCauHinh(File tep) {
        try {
            ConnectDB.docCauHinh(tep);
            return "";
        } catch (SQLException ex) {
            return ex.getMessage();
        }
    }

    private static String lap(String p) throws SQLException {
        return hoaDon.lapHoaDon(p, null, "VAT08", HinhThucThanhToan.CHUYEN_KHOAN, null, "NV0002");
    }

    private static String taoPhieu(String trangThai) throws Exception {
        String ma = "KHO" + (++stt);
        thucThi("INSERT INTO PhieuTiepNhanVaSuaChua (maPhieuTNSC, bienSo, maKH, maNV_LapPhieu, trangThai) VALUES ('" + ma + "', '50E-999.12', 'KH0002', 'NV0002', N'" + trangThai + "')");
        thucThi("INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, tienCongThucTe, trangThaiTienDo) VALUES ('" + ma + "', 'DV0001', 100000, N'Đang sửa')");
        return ma;
    }

    private static String taoYeuCau(String p, String sku, int soLuong) throws Exception {
        String m = yc.nextId();
        yc.insertWithDetails(yeuCau(m, p), dong(m, sku, soLuong), NGUOI);
        return m;
    }

    private static PhieuYeuCauXuatKho yeuCau(String m, String p) {
        return new PhieuYeuCauXuatKho(m, LocalDateTime.now(), null, TrangThaiYeuCauXuat.CHO_XUAT,
                new PhieuTiepNhanVaSuaChua(p), new NhanVien("NV0005"), null);
    }

    private static List<ChiTietYeuCauXuatKho> dong(String m, String sku, int soLuong) {
        return Arrays.asList(new ChiTietYeuCauXuatKho(new PhieuYeuCauXuatKho(m), new PhuTung(sku), soLuong));
    }

    private static int ton(String sku) throws Exception {
        return so("SELECT soLuongTon FROM PhuTung WHERE maSKU = '" + sku + "'");
    }

    private static String trangThaiPhieu(String p) throws Exception {
        return q("SELECT trangThai FROM PhieuTiepNhanVaSuaChua WHERE maPhieuTNSC = '" + p + "'");
    }

    private static String trangThaiYeuCau(String m) throws Exception {
        return q("SELECT trangThai FROM PhieuYeuCauXuatKho WHERE maPhieuYC = '" + m + "'");
    }

    private static String trangThaiHangMuc(String p, String dv) throws Exception {
        return q("SELECT trangThaiTienDo FROM ChiTietSuaChua_DichVu WHERE maPhieuTNSC = '" + p + "' AND maDichVu = '" + dv + "'");
    }

    private static boolean coNhatKy(String ai, String hanhDong, String doiTuong) throws Exception {
        return so("SELECT COUNT(*) FROM NhatKyHoatDong WHERE tenDangNhap = '" + ai + "' AND hanhDong = N'" + hanhDong
                + "' AND doiTuong = '" + doiTuong + "'") > 0;
    }

    private static int demNhatKy(String ai) throws Exception {
        return so("SELECT COUNT(*) FROM NhatKyHoatDong WHERE tenDangNhap = '" + ai + "'");
    }

    private static void thucThi(String sql) throws Exception {
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    private static String q(String sql) throws Exception {
        try (Connection c = ConnectDB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getString(1) : null;
        }
    }

    private static int so(String sql) throws Exception {
        return Integer.parseInt(q(sql));
    }

    private static void kiem(String ten, boolean dat) {
        if (!dat) {
            soLoi++;
            System.out.println("SAI: " + ten);
        }
    }
}
