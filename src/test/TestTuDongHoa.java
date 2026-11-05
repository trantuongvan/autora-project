package test;

import controller.QLNhapKhoController;
import controller.QLTaiKhoanController;
import dao.HoaDon_Dao;
import dao.PhieuNhapKho_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import dao.TaiKhoan_Dao;
import entity.ChiTietNhapKho;
import entity.DichVu;
import entity.HinhThucKhuyenMai;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.MucUuTien;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhamViKhuyenMai;
import entity.PhieuNhapKho;
import entity.PhieuTiepNhanVaSuaChua;
import entity.PhuTung;
import entity.TrangThaiPhieu;
import entity.Xe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

/** Kiểm thử các tự động hóa: khuyến mãi tốt nhất, số lượng nhập mặc định, nhà cung cấp gần nhất, tên đăng nhập, phiếu không có phụ tùng dự kiến. */
public class TestTuDongHoa {
    private static int soLoi;

    public static void main(String[] args) throws Exception {
        kiemTraKhuyenMaiTotNhat();
        kiemTraSoLuongNhap();
        kiemTraNhaCungCapGanNhat();
        kiemTraTenDangNhap();
        kiemTraPhieuKhongPhuTungDuKien();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    private static KhuyenMai km(String ma, HinhThucKhuyenMai hinhThuc, double giaTri, PhamViKhuyenMai phamVi) {
        KhuyenMai k = new KhuyenMai(ma, "CT " + ma, hinhThuc, giaTri, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), true);
        k.setPhamViApDung(phamVi);
        return k;
    }

    private static void kiemTraKhuyenMaiTotNhat() {
        KhuyenMai pt10 = km("KM01", HinhThucKhuyenMai.PHAN_TRAM, 10, PhamViKhuyenMai.TONG_HOA_DON); // 10% của 1.000.000 = 100.000
        KhuyenMai tien50 = km("KM02", HinhThucKhuyenMai.TIEN_MAT, 50000, PhamViKhuyenMai.TONG_HOA_DON);
        KhuyenMai tien100 = km("KM03", HinhThucKhuyenMai.TIEN_MAT, 100000, PhamViKhuyenMai.TONG_HOA_DON);
        KhuyenMai tien200 = km("KM04", HinhThucKhuyenMai.TIEN_MAT, 200000, PhamViKhuyenMai.TONG_HOA_DON);
        KhuyenMai chiPhuTung = km("KM05", HinhThucKhuyenMai.PHAN_TRAM, 50, PhamViKhuyenMai.PHU_TUNG);
        KhuyenMai chiCong = km("KM06", HinhThucKhuyenMai.PHAN_TRAM, 5, PhamViKhuyenMai.NHOM_DICH_VU);

        dung("Giảm nhiều nhất thắng", KhuyenMai.totNhat(Arrays.asList(pt10, tien50, tien200), 600000, 400000) == tien200);
        dung("Phần trăm và tiền mặt so theo số tiền thật",
                KhuyenMai.totNhat(Arrays.asList(tien50, pt10), 600000, 400000) == pt10
                && KhuyenMai.totNhat(Arrays.asList(pt10, tien50), 100000, 0) == tien50);
        dung("Hòa thì lấy cái đứng trước", KhuyenMai.totNhat(Arrays.asList(pt10, tien100, tien50), 600000, 400000) == pt10
                && KhuyenMai.totNhat(Arrays.asList(tien100, pt10), 600000, 400000) == tien100);
        dung("Danh sách rỗng cho null", KhuyenMai.totNhat(Collections.<KhuyenMai>emptyList(), 600000, 400000) == null);
        dung("Phạm vi phụ tùng khi không có phụ tùng cho null", KhuyenMai.totNhat(Arrays.asList(chiPhuTung), 600000, 0) == null);
        dung("Phạm vi công khi không có công cho null", KhuyenMai.totNhat(Arrays.asList(chiCong), 0, 500000) == null);
        dung("Bỏ qua khuyến mãi không giảm, chọn cái có giảm",
                KhuyenMai.totNhat(Arrays.asList(chiPhuTung, chiCong), 600000, 0) == chiCong);
    }

    private static int deXuat(int nguong, int ton) {
        PhuTung pt = new PhuTung("SKU_X");
        pt.setNguongCanhBao(nguong);
        pt.setSoLuongTon(ton);
        return pt.soLuongNhapDeXuat();
    }

    private static void kiemTraSoLuongNhap() {
        dung("Dưới ngưỡng: 6*2-4", deXuat(6, 4) == 8);
        dung("Đúng ngưỡng lấy bằng ngưỡng", deXuat(5, 5) == 5);
        dung("Tồn sát ngưỡng gấp đôi vẫn lấy ngưỡng", deXuat(5, 8) == 5);
        dung("Tồn rất cao vẫn lấy ngưỡng", deXuat(5, 500) == 5);
        dung("Hết hàng: gấp đôi ngưỡng", deXuat(5, 0) == 10);
        dung("Ngưỡng 0 vẫn ít nhất 1", deXuat(0, 0) == 1
                && deXuat(0, 30) == 1);
    }

    private static void phieuNhap(PhieuNhapKho_Dao dao, String ma, LocalDateTime gio, String maNCC, String maSKU) throws Exception {
        PhieuNhapKho p = new PhieuNhapKho(ma, gio, 1000, new NhaCungCap(maNCC), new NhanVien("NV0004"));
        dao.insertWithDetails(p, Arrays.asList(new ChiTietNhapKho(p, new PhuTung(maSKU), 1, 1000)));
    }

    private static void kiemTraNhaCungCapGanNhat() throws Exception {
        PhieuNhapKho_Dao dao = new PhieuNhapKho_Dao();
        dung("Chưa có lịch sử cho null", dao.findNhaCungCapGanNhat("SKU_OIL01") == null);
        phieuNhap(dao, "PNT0001", LocalDateTime.of(2025, 1, 10, 9, 0), "NCC0002", "SKU_OIL01");
        phieuNhap(dao, "PNT0002", LocalDateTime.of(2025, 3, 10, 9, 0), "NCC0003", "SKU_OIL01");
        phieuNhap(dao, "PNT0003", LocalDateTime.of(2025, 2, 10, 9, 0), "NCC0001", "SKU_OIL01");
        NhaCungCap ncc = dao.findNhaCungCapGanNhat("SKU_OIL01");
        dung("Lấy nhà cung cấp của lần nhập mới nhất", ncc != null && "NCC0003".equals(ncc.getMaNCC()) && ncc.getTenNCC() != null);
        dung("Phụ tùng khác vẫn chưa có lịch sử", dao.findNhaCungCapGanNhat("SKU_OIL02") == null);
    }

    private static void kiemTraTenDangNhap() throws Exception {
        dung("Nguyễn Tuấn Vũ", "vunt".equals(QLTaiKhoanController.taoTenDangNhap("Nguyễn Tuấn Vũ")));
        dung("Trần Thị Tường Vân", "vanttt".equals(QLTaiKhoanController.taoTenDangNhap("Trần Thị Tường Vân")));
        dung("Chữ đ và Đ", "dungdd".equals(QLTaiKhoanController.taoTenDangNhap("Đỗ Đức Dũng"))
                && "dinhd".equals(QLTaiKhoanController.taoTenDangNhap("đặng Đình")));
        dung("Một từ", "vy".equals(QLTaiKhoanController.taoTenDangNhap("Vy")) && "thuy".equals(QLTaiKhoanController.taoTenDangNhap("  Thủy ")));
        dung("Khoảng trắng thừa", "anhnxv".equals(QLTaiKhoanController.taoTenDangNhap(" Nguyễn  Xuân Việt  Anh ")));
        dung("Rỗng", "".equals(QLTaiKhoanController.taoTenDangNhap("  ")) && "".equals(QLTaiKhoanController.taoTenDangNhap(null)));

        TaiKhoan_Dao dao = new TaiKhoan_Dao();
        dung("Tên còn trống giữ nguyên", "vunt".equals(dao.tenDangNhapConTrong("vunt")));
        dung("Tên trùng thêm số 2", "admin2".equals(dao.tenDangNhapConTrong("admin")));
        dung("Không phân biệt hoa thường", "ADMIN2".equals(dao.tenDangNhapConTrong("ADMIN")));
        dung("Tên gốc chưa có dù có tên bắt đầu giống", "ktv".equals(dao.tenDangNhapConTrong("ktv"))
                && "ktv012".equals(dao.tenDangNhapConTrong("ktv01")));
    }

    private static void kiemTraPhieuKhongPhuTungDuKien() throws Exception {
        PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
        KhachHang kh = new KhachHang();
        kh.setHoTen("Nguyễn Văn Thử");
        kh.setSoDienThoai("0911000999");
        kh.setDiaChi("Quận 1");
        kh.setEmail("thu@example.com");
        Xe xe = new Xe();
        xe.setBienSo("59A-999.99");
        xe.setSoVIN("VIN59A99999");
        xe.setHangXe("BMW");
        xe.setDongXe("X5");
        xe.setDoiXe(2021);
        xe.setMauSon("Bạc");
        xe.setKhachHang(new KhachHang("MOI"));
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setXe(new Xe("59A-999.99"));
        p.setNhanVienLap(new NhanVien("NV0003"));
        p.setYeuCauBanDau("Kiểm tra");
        p.setSoKm(100);
        p.setMucNhienLieu(50);
        p.setThoiGianTiepNhan(LocalDateTime.of(2025, 11, 3, 13, 0));
        p.setTrangThai(TrangThaiPhieu.CHO_KIEM_TRA);
        p.setUuTien(MucUuTien.THUONG);
        p.setTrayXuoc("Bình thường");
        p.setDanhGiaNoiThat("Bình thường");
        p.setTaiSanTrenXe("Không có");
        p.setPhuTungDuKien("");
        DichVu dv = new DichVu("DV0001");
        dv.setTienCong(250000);
        dao.taoTiepNhan(kh, xe, p, Arrays.asList(dv));
        PhieuTiepNhanVaSuaChua doc = dao.findById(p.getMaPhieuTNSC());
        dung("Phiếu không có phụ tùng dự kiến lưu và đọc lại", doc != null && doc.getPhuTungDuKien() != null
                && doc.getPhuTungDuKien().isEmpty() && "BMW".equals(doc.getXe().getHangXe()));
    }

    private static void dung(String ten, boolean ketQua) {
        if (!ketQua) {
            soLoi++;
            System.out.println("LOI: " + ten);
        }
    }
}
