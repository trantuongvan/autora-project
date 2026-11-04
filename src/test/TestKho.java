package test;

import connection.ConnectDB;
import dao.DangNhap_Dao;
import dao.HoaDon_Dao;
import dao.NhanVien_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import dao.PhieuYeuCauXuatKho_Dao;
import dao.TienDoSuaChua_Dao;
import entity.ChiTietYeuCauXuatKho;
import entity.HinhThucThanhToan;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.PhieuYeuCauXuatKho;
import entity.PhuTung;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import entity.TrangThaiYeuCauXuat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
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
        kiem("Nhãn Chờ sửa là Chờ kiểm tra", TienDoSuaChua_Dao.chuanHoaTrangThaiDB("Chờ sửa") == TrangThaiPhieu.CHO_KIEM_TRA);

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
        thucThi("INSERT INTO PhieuTiepNhanVaSuaChua (maPhieuTNSC, bienSo, maNV_LapPhieu, trangThai) VALUES ('" + ma + "', '50E-999.12', 'NV0002', N'" + trangThai + "')");
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
