package test;

import controller.DangNhapController;
import controller.QLKhuyenMaiController;
import controller.QLTiepNhanSuaChuaController;
import controller.TrangChuQuanLyController;
import dao.ChiTietSuaChua_DichVu_Dao;
import dao.DangNhap_Dao;
import dao.KhuyenMai_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import entity.ChiTietSuaChua_DichVu;
import entity.DichVu;
import entity.HinhThucKhuyenMai;
import entity.HinhThucThanhToan;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.LoaiKhachHang;
import entity.MucUuTien;
import entity.NhanVien;
import entity.PhamViKhuyenMai;
import util.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import entity.TrangThaiYeuCauXuat;
import entity.VaiTro;
import entity.Xe;
import util.MatKhauUtil;
import util.PdfExporter;
import util.XuatFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Kiểm thử không cần thư viện ngoài. Chạy không tham số: chỉ kiểm tra quy tắc nghiệp vụ.
 * Chạy với tham số "db": kiểm tra thêm trên CSDL mẫu mới tạo (dữ liệu tạo ra được xóa ở cuối).
 */
public class TestNghiepVuTiepNhan {
    private static int soLoi;

    public static void main(String[] args) throws Exception {
        kiemTraEnum();
        kiemTraKhuyenMai();
        kiemTraPhieu();
        kiemTraDieuKienHoanThanh();
        kiemTraHoTro();
        if (args.length > 0 && "db".equals(args[0])) {
            kiemTraDangNhapCsdl();
            kiemTraPhieuCsdl();
            kiemTraKhuyenMaiCsdl();
            kiemTraTiepNhanCsdl();
            kiemTraDatLaiMatKhauCsdl();
        }
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    private static <E extends Enum<E>> void vongDoiEnum(String ten, Class<E> loai, java.util.function.Function<String, E> fromDb) {
        for (E giaTri : loai.getEnumConstants()) {
            dung(ten + " khứ hồi " + giaTri.name(), fromDb.apply(giaTri.toString()) == giaTri);
        }
        loi(ten + " nhận chữ lạ", () -> fromDb.apply("Chữ lạ"));
        loi(ten + " nhận null", () -> fromDb.apply(null));
    }

    private static void kiemTraEnum() {
        vongDoiEnum("HinhThucThanhToan", HinhThucThanhToan.class, HinhThucThanhToan::fromDb);
        vongDoiEnum("TrangThaiHoaDon", TrangThaiHoaDon.class, TrangThaiHoaDon::fromDb);
        vongDoiEnum("LoaiKhachHang", LoaiKhachHang.class, LoaiKhachHang::fromDb);
        vongDoiEnum("HinhThucKhuyenMai", HinhThucKhuyenMai.class, HinhThucKhuyenMai::fromDb);
        vongDoiEnum("PhamViKhuyenMai", PhamViKhuyenMai.class, PhamViKhuyenMai::fromDb);
        vongDoiEnum("VaiTro", VaiTro.class, VaiTro::fromDb);
        vongDoiEnum("TrangThaiPhieu", TrangThaiPhieu.class, TrangThaiPhieu::fromDb);
        vongDoiEnum("MucUuTien", MucUuTien.class, MucUuTien::fromDb);
        vongDoiEnum("TrangThaiTienDo", TrangThaiTienDo.class, TrangThaiTienDo::fromDb);
        vongDoiEnum("TrangThaiYeuCauXuat", TrangThaiYeuCauXuat.class, TrangThaiYeuCauXuat::fromDb);
        dung("Chờ sửa không phải trạng thái phiếu", !Arrays.asList(TrangThaiPhieu.values()).toString().contains("Chờ sửa"));
        dung("Phiếu Chờ kiểm tra hiện là Chờ sửa cho kỹ thuật viên",
                "Chờ sửa".equals(new PhieuTiepNhanVaSuaChua().getTrangThaiHienThiKTV()));
    }

    private static void kiemTraKhuyenMai() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 1, 31);
        dat("KM % 100 hợp lệ (biên)", () -> new KhuyenMai("KM1", "A", HinhThucKhuyenMai.PHAN_TRAM, 100, d1, d2, true));
        loi("KM % trên 100", () -> new KhuyenMai("KM1", "A", HinhThucKhuyenMai.PHAN_TRAM, 100.5, d1, d2, true));
        loi("KM giá trị 0", () -> new KhuyenMai("KM1", "A", HinhThucKhuyenMai.TIEN_MAT, 0, d1, d2, true));
        loi("KM hình thức lạ", () -> new KhuyenMai("KM1", "A", HinhThucKhuyenMai.fromDb("Tặng quà"), 5, d1, d2, true));
        loi("KM kết thúc trước bắt đầu", () -> new KhuyenMai("KM1", "A", HinhThucKhuyenMai.TIEN_MAT, 5, d2, d1, true));
        loi("KM thiếu tên", () -> new KhuyenMai("KM1", " ", HinhThucKhuyenMai.TIEN_MAT, 5, d1, d2, true));

        KhuyenMai km = new KhuyenMai("KM1", "A", HinhThucKhuyenMai.TIEN_MAT, 5, d1, d2, true);
        dung("Sắp diễn ra", "Sắp diễn ra".equals(km.getTinhTrang(d1.minusDays(1))));
        dung("Đang áp dụng ngày đầu", "Đang áp dụng".equals(km.getTinhTrang(d1)));
        dung("Đang áp dụng ngày cuối", "Đang áp dụng".equals(km.getTinhTrang(d2)));
        dung("Hết hạn", "Hết hạn".equals(km.getTinhTrang(d2.plusDays(1))));
        km.setTrangThai(false);
        dung("Ngừng áp dụng ưu tiên", "Ngừng áp dụng".equals(km.getTinhTrang(d1)));
    }

    private static void kiemTraPhieu() {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua("P1");
        loi("Số km âm", () -> p.setSoKm(-1));
        loi("Nhiên liệu trên 100", () -> p.setMucNhienLieu(100.1));
        loi("Trạng thái lạ", () -> p.setTrangThai(TrangThaiPhieu.fromDb("Đã xóa")));
        LocalDateTime gio = LocalDateTime.of(2026, 1, 1, 8, 0);
        PhieuTiepNhanVaSuaChua q = new PhieuTiepNhanVaSuaChua("P2");
        q.setXe(new Xe("51H-123.45"));
        q.setNhanVienLap(new NhanVien("NV0002"));
        q.setThoiGianTiepNhan(gio);
        q.setThoiGianDuKien(gio.minusMinutes(1));
        loi("Dự kiến trước tiếp nhận", q::kiemTraHopLe);
    }

    private static boolean coTheHoanThanh(List<ChiTietSuaChua_DichVu> dong) {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setDanhSachDichVu(dong);
        return p.coTheHoanThanh();
    }

    private static int tienDo(TrangThaiPhieu trangThai, int tong, int xong) {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setTrangThai(trangThai);
        p.setTongSoHangMuc(tong);
        p.setSoHangMucHoanThanh(xong);
        return p.phanTramTienDo();
    }

    private static void kiemTraDieuKienHoanThanh() {
        dung("Tiến độ: xong là 100", tienDo(TrangThaiPhieu.HOAN_THANH, 2, 0) == 100);
        dung("Tiến độ: chờ kiểm tra chưa làm là 0", tienDo(TrangThaiPhieu.CHO_KIEM_TRA, 2, 0) == 0);
        dung("Tiến độ: đang sửa theo hạng mục", tienDo(TrangThaiPhieu.DANG_SUA, 4, 1) == 25);
        dung("Tiến độ: đang sửa chưa xong mục nào là 50", tienDo(TrangThaiPhieu.DANG_SUA, 4, 0) == 50
                && tienDo(TrangThaiPhieu.DANG_SUA, 0, 0) == 50);
        dung("Tiến độ: chờ phụ tùng theo hạng mục", tienDo(TrangThaiPhieu.CHO_PHU_TUNG, 2, 1) == 50
                && tienDo(TrangThaiPhieu.CHO_PHU_TUNG, 0, 0) == 0);
        dung("Phiếu rỗng không hoàn thành được", !coTheHoanThanh(new ArrayList<>()));
        dung("Còn dòng chưa xong", !coTheHoanThanh(Arrays.asList(dong(TrangThaiTienDo.HOAN_THANH), dong(TrangThaiTienDo.DANG_SUA))));
        dung("Mọi dòng đã xong", coTheHoanThanh(Arrays.asList(dong(TrangThaiTienDo.HOAN_THANH))));
    }

    private static void kiemTraHoTro() {
        dung("Mật khẩu quá ngắn", DangNhapController.kiemTraMatKhauMoi("a", "12345", "12345") != null);
        dung("Mật khẩu không khớp", DangNhapController.kiemTraMatKhauMoi("a", "123456", "123457") != null);
        dung("Thiếu tài khoản", DangNhapController.kiemTraMatKhauMoi("", "123456", "123456") != null);
        dung("Mật khẩu có khoảng trắng", DangNhapController.kiemTraMatKhauMoi("a", "123 456", "123 456") != null);
        String bam = MatKhauUtil.bam("abc123");
        dung("Băm khớp", MatKhauUtil.kiemTra("abc123", bam));
        dung("Băm không chứa mật khẩu", !bam.contains("abc123"));
        dung("Băm sai mật khẩu", !MatKhauUtil.kiemTra("abc124", bam));
        dung("Mỗi lần băm khác muối", !bam.equals(MatKhauUtil.bam("abc123")));
        dung("Mật khẩu rõ còn sót không đăng nhập được", !MatKhauUtil.kiemTra("123456", "123456"));
        dung("Giá trị băm hỏng", !MatKhauUtil.kiemTra("a", "pbkdf2$x$y$z") && !MatKhauUtil.kiemTra("a", null));

        dung("Luồng hợp lệ", TrangThaiPhieu.CHO_KIEM_TRA.coTheChuyenSang(TrangThaiPhieu.DANG_SUA)
                && TrangThaiPhieu.DANG_SUA.coTheChuyenSang(TrangThaiPhieu.HOAN_THANH)
                && TrangThaiPhieu.CHO_PHU_TUNG.coTheChuyenSang(TrangThaiPhieu.DANG_SUA));
        dung("Luồng không hợp lệ", !TrangThaiPhieu.CHO_KIEM_TRA.coTheChuyenSang(TrangThaiPhieu.HOAN_THANH)
                && !TrangThaiPhieu.HOAN_THANH.coTheChuyenSang(TrangThaiPhieu.DANG_SUA)
                && !TrangThaiPhieu.DA_HUY.coTheChuyenSang(TrangThaiPhieu.DANG_SUA));
        loi("Tiếp nhận ở tương lai", () -> new PhieuTiepNhanVaSuaChua().setThoiGianTiepNhan(LocalDateTime.now().plusDays(1)));

        dung("VIN đúng", QLTiepNhanSuaChuaController.soVinHopLe("1HGCR2F83HA001234"));
        dung("VIN ngắn", !QLTiepNhanSuaChuaController.soVinHopLe("VINTEST"));
        dung("VIN có chữ O", !QLTiepNhanSuaChuaController.soVinHopLe("1HGCR2F83HO001234"));
        KhachHang kh = khachMau("0911000009");
        dat("Email và mã số thuế được để trống", () -> {
            kh.setEmail("");
            kh.setMaSoThue("");
        });
        dat("Mã số thuế đúng", () -> kh.setMaSoThue("0312456789"));
        loi("Mã số thuế sai", () -> kh.setMaSoThue("12"));
        dat("Ba loại khách hàng hợp lệ", () -> {
            for (LoaiKhachHang loai : LoaiKhachHang.values()) {
                kh.setLoaiKhachHang(loai);
            }
        });
        dung("Loại mặc định là Khách vãng lai", new KhachHang().getLoaiKhachHang() == LoaiKhachHang.KHACH_VANG_LAI);
        kiemTraXuatFile();

        dung("Mật khẩu hợp lệ", DangNhapController.kiemTraMatKhauMoi("a", "123456", "123456") == null);

        dung("Xu hướng tăng", "\u25B2 50%".equals(TrangChuQuanLyController.xuHuong(150, 100)));
        dung("Xu hướng giảm", "\u25BC 25%".equals(TrangChuQuanLyController.xuHuong(75, 100)));
        dung("Kỳ trước bằng 0", TrangChuQuanLyController.xuHuong(5, 0) == null);
        dung("Tên thứ", "Thứ Hai".equals(TrangChuQuanLyController.tenThu(LocalDate.of(2026, 11, 2))));
        List<LocalDate> ngay = TrangChuQuanLyController.cacNgay(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));
        dung("Danh sách ngày mới đến cũ", ngay.size() == 3 && ngay.get(0).getDayOfMonth() == 3);

        dung("Giá trị tiền mặt có dấu chấm", QLKhuyenMaiController.docGiaTri("100.000", HinhThucKhuyenMai.TIEN_MAT) == 100000);
        dung("Phần trăm có dấu phẩy", QLKhuyenMaiController.docGiaTri("7,5", HinhThucKhuyenMai.PHAN_TRAM) == 7.5);
        loi("Phần trăm trên 100", () -> QLKhuyenMaiController.docGiaTri("101", HinhThucKhuyenMai.PHAN_TRAM));
        loi("Giá trị chữ", () -> QLKhuyenMaiController.docGiaTri("abc", HinhThucKhuyenMai.TIEN_MAT));
        dung("Số lượt trống là không giới hạn", QLKhuyenMaiController.docSoLuot(" ") == null);
        loi("Số lượt bằng 0", () -> QLKhuyenMaiController.docSoLuot("0"));
        loi("Ngày không tồn tại", () -> QLKhuyenMaiController.docNgay("31/02/2026", "Ngày"));

        loi("Email sai", () -> new KhachHang().setEmail("abc"));
        loi("Loại khách cũ", () -> new KhachHang().setLoaiKhachHang(LoaiKhachHang.fromDb("Doanh nghiệp")));
        loi("Ưu tiên lạ", () -> new PhieuTiepNhanVaSuaChua().setUuTien(MucUuTien.fromDb("Rất gấp")));
        loi("Phạm vi lạ", () -> new KhuyenMai().setPhamViApDung(PhamViKhuyenMai.fromDb("Tất cả")));
    }

    private static void kiemTraTiepNhanCsdl() throws Exception {
        PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
        DichVu dv = new DichVu("DV0001");
        dv.setTienCong(250000);

        // Khách và xe mới được tạo cùng phiếu.
        KhachHang kh = khachMau("0911000001");
        Xe xe = xeMau("59A-111.11");
        PhieuTiepNhanVaSuaChua p = phieuMau("59A-111.11");
        dao.taoTiepNhan(kh, xe, p, Arrays.asList(dv));
        dung("Tạo khách, xe, phiếu cùng lúc", dao.findById(p.getMaPhieuTNSC()) != null
                && dao.findXeDayDu("59A-111.11") != null && dao.findKhachTheoSdt("0911000001") != null);
        dung("Có dòng dịch vụ", new ChiTietSuaChua_DichVu_Dao().findByPhieu(p.getMaPhieuTNSC()).size() == 1);
        dung("Lưu trường mới", dao.findById(p.getMaPhieuTNSC()).getUuTien() == MucUuTien.KHAN
                && "Ghế sạch".equals(dao.findById(p.getMaPhieuTNSC()).getDanhGiaNoiThat()));

        // SĐT trùng làm hỏng bước khách: không được để lại xe hay phiếu.
        KhachHang trung = khachMau("0911000001");
        Xe xe2 = xeMau("59A-222.22");
        PhieuTiepNhanVaSuaChua p2 = phieuMau("59A-222.22");
        boolean baoLoi = false;
        try {
            dao.taoTiepNhan(trung, xe2, p2, Arrays.asList(dv));
        } catch (java.sql.SQLException ex) {
            baoLoi = true;
        }
        dung("Trùng SĐT báo lỗi", baoLoi);
        dung("Hoàn tác xe khi lỗi", dao.findXeDayDu("59A-222.22") == null);

        // Dịch vụ không tồn tại làm hỏng bước cuối: khách và xe vừa tạo phải bị hủy.
        KhachHang kh3 = khachMau("0911000003");
        Xe xe3 = xeMau("59A-333.33");
        PhieuTiepNhanVaSuaChua p3 = phieuMau("59A-333.33");
        try {
            dao.taoTiepNhan(kh3, xe3, p3, Arrays.asList(new DichVu("KHONGCO")));
        } catch (java.sql.SQLException ex) {
            baoLoi = true;
        }
        dung("Hoàn tác khách khi lỗi bước cuối", dao.findKhachTheoSdt("0911000003") == null);
        dung("Hoàn tác xe khi lỗi bước cuối", dao.findXeDayDu("59A-333.33") == null);

        // Phiếu mới được tự phân công: DV0001 chỉ NV0005 và NV0006 làm được.
        NhanVien tuDong = dao.findById(p.getMaPhieuTNSC()).getNhanVienPhuTrach();
        dung("Phiếu mới được tự chọn KTV phụ trách có kỹ năng", tuDong != null
                && ("NV0005".equals(tuDong.getMaNV()) || "NV0006".equals(tuDong.getMaNV())));
        dung("Lưu phân công", dao.capNhatPhanCong(p.getMaPhieuTNSC(), "NV0005", "Kiểm tra lại")
                && dao.findById(p.getMaPhieuTNSC()).getNhanVienPhuTrach() != null
                && "NV0005".equals(dao.findById(p.getMaPhieuTNSC()).getNhanVienPhuTrach().getMaNV())
                && dao.findById(p.getMaPhieuTNSC()).getNhanVienPhuTrach().getHoTen() != null);
        dung("Phiếu khóa không lưu phân công", !dao.capNhatPhanCong("PTNSC00001", "NV0005", "x"));
        dung("Bước phiếu đã có hóa đơn", dao.buocHienTai("PTNSC00001") == 4);
        dung("Bước phiếu mới", dao.buocHienTai(p.getMaPhieuTNSC()) == 2);
        dao.huy(p.getMaPhieuTNSC(), "letan02");
    }

    private static void kiemTraXuatFile() {
        try {
            java.io.File xlsx = java.io.File.createTempFile("autora", ".xlsx");
            XuatFile.ghiXlsx(xlsx, new String[] {"Tên", "Số"}, Arrays.asList(new Object[] {"=SUM(A1) <&> đ", 5}, new Object[] {null, 2.5}));
            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(xlsx)) {
                String sheet = new java.util.Scanner(zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")), "UTF-8").useDelimiter("\\A").next();
                dung("Excel thoát ký tự và không tạo công thức", sheet.contains("=SUM(A1) &lt;&amp;&gt; đ") && !sheet.contains("<f>"));
                dung("Excel giữ số", sheet.contains("<v>5</v>"));
            }
            System.out.println("XLSX " + xlsx.getAbsolutePath());

            java.io.File pdf = java.io.File.createTempFile("autora", ".pdf");
            PhieuTiepNhanVaSuaChua p = phieuMau("59A-111.11");
            p.setMaPhieuTNSC("PTNSC00009");
            Xe xePdf = new Xe("59A-111.11");
            xePdf.setKhachHang(khachMau("0911000009"));
            p.setXe(xePdf);
            ChiTietSuaChua_DichVu d = dong(TrangThaiTienDo.CHO_SUA);
            d.setTienCongThucTe(150000);
            d.setDichVu(new DichVu("DV0001"));
            PdfExporter.xuatPhieuSuaChua(pdf, p, Arrays.asList(d));
            String chu = com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(new com.itextpdf.text.pdf.PdfReader(pdf.getAbsolutePath()), 1);
            dung("PDF có tiếng Việt và mã phiếu", chu.contains("PTNSC00009") && chu.contains("Nguyễn Văn Thử"));
            dung("PDF có tổng tiền công", chu.contains("150.000"));
            System.out.println("PDF " + pdf.getAbsolutePath());
        } catch (Exception ex) {
            dung("Xuất file: " + ex, false);
        }
    }

    private static boolean tonTaiNhatKy(String hanhDong, String doiTuong, String cu, String moi, String nguoi) throws Exception {
        for (entity.NhatKyHoatDong r : new dao.NhatKy_Dao().search(doiTuong)) {
            if (hanhDong.equals(r.getHanhDong()) && doiTuong.equals(r.getDoiTuong()) && java.util.Objects.equals(cu, r.getGiaTriCu())
                    && java.util.Objects.equals(moi, r.getGiaTriMoi()) && nguoi.equals(r.getTenDangNhap())) {
                return true;
            }
        }
        return false;
    }

    private static ChiTietSuaChua_DichVu dongMoi(String ma, String maDichVu) {
        ChiTietSuaChua_DichVu d = dong(TrangThaiTienDo.CHO_SUA);
        d.setPhieu(new PhieuTiepNhanVaSuaChua(ma));
        d.setDichVu(new DichVu(maDichVu));
        return d;
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

    private static PhieuTiepNhanVaSuaChua phieuMau(String bienSo) {
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua();
        p.setXe(new Xe(bienSo));
        p.setNhanVienLap(new NhanVien("NV0003"));
        p.setYeuCauBanDau("Kiểm tra");
        p.setSoKm(100);
        p.setMucNhienLieu(50);
        p.setThoiGianTiepNhan(LocalDateTime.now());
        p.setTrangThai(TrangThaiPhieu.CHO_KIEM_TRA);
        p.setUuTien(MucUuTien.KHAN);
        p.setDanhGiaNoiThat("Ghế sạch");
        return p;
    }

    private static void kiemTraDatLaiMatKhauCsdl() throws Exception {
        DangNhap_Dao dao = new DangNhap_Dao();
        dung("Đặt lại mật khẩu", dao.datLaiMatKhau("ktv02", "matkhaumoi", "admin"));
        dung("Đăng nhập bằng mật khẩu mới", dao.xacThuc("ktv02", "matkhaumoi") != null);
        dung("Mật khẩu cũ hết hiệu lực", dao.xacThuc("ktv02", "123456") == null);
        dung("Tài khoản không tồn tại", !dao.datLaiMatKhau("khongco", "matkhaumoi", "admin"));
        dung("Đặt lại có nhật ký, không lộ mật khẩu", tonTaiNhatKy("Đặt lại mật khẩu", "ktv02", null, null, "admin"));
        dao.datLaiMatKhau("ktv02", "123456", "admin");
    }

    private static ChiTietSuaChua_DichVu dong(TrangThaiTienDo tienDo) {
        ChiTietSuaChua_DichVu d = new ChiTietSuaChua_DichVu();
        d.setTrangThaiTienDo(tienDo);
        return d;
    }

    private static void kiemTraDangNhapCsdl() throws Exception {
        DangNhap_Dao dao = new DangNhap_Dao();
        PhienDangNhap phien = dao.xacThuc("letan02", "123456");
        dung("Đăng nhập đúng", phien != null && "NV0003".equals(phien.getMaNV()) && phien.getVaiTro() == VaiTro.LE_TAN);
        dung("Sai mật khẩu", dao.xacThuc("letan02", "sai") == null);
        dung("Mật khẩu phân biệt hoa thường", dao.xacThuc("admin", "ADMIN123") == null);
        dung("Chuỗi SQL injection không vào được", dao.xacThuc("admin' --", "x") == null && dao.xacThuc("admin", "' OR '1'='1") == null);
    }

    private static void kiemTraPhieuCsdl() throws Exception {
        PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
        ChiTietSuaChua_DichVu_Dao chiTietDao = new ChiTietSuaChua_DichVu_Dao();

        dung("Tra xe theo biển số", dao.findXeVaChuXe("60A-456.78") != null);
        dung("Xe không tồn tại", dao.findXeVaChuXe("99Z-000.00") == null);
        dung("Tra biển số theo SĐT", "60A-456.78".equals(dao.timBienSoTheoSdt("0966333444")));
        dung("Xe đang có phiếu mở", dao.coPhieuDangMo("50E-999.12"));
        dung("Xe không có phiếu mở", !dao.coPhieuDangMo("60A-456.78"));
        dung("Tìm theo khách có dấu", !dao.search("Phạm Minh", null, null, null).isEmpty());
        dung("Lọc trạng thái có dấu", dao.search("", TrangThaiPhieu.DANG_SUA, null, null).size() == 1);
        dung("Lọc theo khoảng ngày bao gồm ngày cuối",
                dao.search("", null, LocalDate.of(2026, 10, 23), LocalDate.of(2026, 10, 23)).size() == 1);

        // Phiếu mẫu 1 đã có hóa đơn nên phải bị khóa.
        String maKhoa = "PTNSC00001";
        dung("Phiếu có hóa đơn bị khóa", dao.isKhoa(maKhoa));
        dung("Không đổi trạng thái phiếu khóa", !dao.capNhatTrangThai(maKhoa, TrangThaiPhieu.HOAN_THANH, TrangThaiPhieu.DANG_SUA, "admin"));
        dung("Không xóa phiếu có hóa đơn", !dao.huy(maKhoa, "admin"));
        ChiTietSuaChua_DichVu dongKhoa = dong(TrangThaiTienDo.CHO_SUA);
        dongKhoa.setPhieu(new PhieuTiepNhanVaSuaChua(maKhoa));
        dongKhoa.setDichVu(new DichVu("DV0006"));
        dung("Không thêm dòng vào phiếu khóa", !chiTietDao.insert(dongKhoa));
        dung("Không xóa dòng phiếu khóa", !chiTietDao.delete(maKhoa, "DV0001"));

        // Vòng đời phiếu mới.
        String ma = dao.nextId();
        dung("Sinh mã phiếu kế tiếp", "PTNSC00003".equals(ma));
        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua(ma);
        p.setXe(new Xe("60A-456.78"));
        p.setNhanVienLap(new NhanVien("NV0003"));
        p.setYeuCauBanDau("Thay nhớt và kiểm tra tiếng ồn");
        p.setSoKm(1000);
        p.setMucNhienLieu(40);
        p.setThoiGianTiepNhan(LocalDateTime.now());
        p.setTrangThai(TrangThaiPhieu.CHO_KIEM_TRA);
        dung("Thêm phiếu", dao.insert(p));
        PhieuTiepNhanVaSuaChua doc = dao.findById(ma);
        dung("Đọc lại tiếng Việt nguyên vẹn", doc != null && p.getYeuCauBanDau().equals(doc.getYeuCauBanDau()));
        dung("Phiếu đọc lại có xe, chủ xe và nhân viên lập", doc.getXe() != null
                && "60A-456.78".equals(doc.getXe().getBienSo()) && doc.getXe().getHangXe() != null
                && doc.getXe().getKhachHang() != null && "0966333444".equals(doc.getXe().getKhachHang().getSoDienThoai())
                && doc.getXe().getKhachHang().getHoTen() != null
                && doc.getNhanVienLap() != null && "NV0003".equals(doc.getNhanVienLap().getMaNV())
                && doc.getNhanVienLap().getHoTen() != null);

        ChiTietSuaChua_DichVu d = dong(TrangThaiTienDo.CHO_SUA);
        d.setPhieu(new PhieuTiepNhanVaSuaChua(ma));
        d.setDichVu(new DichVu("DV0002"));
        d.setKyThuatVien(new NhanVien("NV0005"));
        d.setTienCongThucTe(150000);
        dung("Chỉ định thợ", chiTietDao.insert(d));
        d.setKyThuatVien(null);
        d.setTienCongThucTe(200000);
        dung("Bỏ chỉ định và đổi tiền công", chiTietDao.update(d));
        List<ChiTietSuaChua_DichVu> ds = chiTietDao.findByPhieu(ma);
        dung("Đọc dòng dịch vụ", ds.size() == 1 && ds.get(0).getKyThuatVien() == null && ds.get(0).getTienCongThucTe() == 200000
                && ds.get(0).getDichVu().getTenDichVu() != null);
        dung("Đổi trạng thái phiếu", dao.capNhatTrangThai(ma, TrangThaiPhieu.CHO_KIEM_TRA, TrangThaiPhieu.DANG_SUA, "admin"));
        dung("Không bỏ qua luồng trạng thái", !dao.capNhatTrangThai(ma, TrangThaiPhieu.CHO_KIEM_TRA, TrangThaiPhieu.HOAN_THANH, "admin"));
        dung("Trạng thái cũ phải khớp", !dao.capNhatTrangThai(ma, TrangThaiPhieu.CHO_KIEM_TRA, TrangThaiPhieu.CHO_PHU_TUNG, "admin"));
        d.setTienCongThucTe(300000);
        dung("Lưu phân công một lần", chiTietDao.luuPhanCong(ma, Arrays.asList(d), "NV0006", "Ghi chú", "letan02"));
        dung("Sửa tiền công có nhật ký", tonTaiNhatKy("Sửa tiền công", ma + " / DV0002", "200000", "300000", "letan02"));
        dung("Hủy phiếu", dao.huy(ma, "letan02"));
        dung("Hủy giữ lại dữ liệu", dao.findById(ma).getTrangThai() == TrangThaiPhieu.DA_HUY && chiTietDao.findByPhieu(ma).size() == 1);
        dung("Hủy có nhật ký", tonTaiNhatKy("Hủy phiếu sửa chữa", ma, "Đang sửa", "Đã hủy", "letan02"));
        dung("Không hủy hai lần", !dao.huy(ma, "letan02"));
        dung("Phiếu hủy bị khóa", dao.isKhoa(ma) && !dao.capNhatPhanCong(ma, "NV0005", "x"));
        dung("Phiếu hủy không thêm được dòng", !chiTietDao.insert(dongMoi(ma, "DV0003")));
        dung("Phiếu hủy không tính là đang mở", !dao.coPhieuDangMo("60A-456.78"));
    }

    private static void kiemTraKhuyenMaiCsdl() throws Exception {
        KhuyenMai_Dao dao = new KhuyenMai_Dao();
        dung("KM đã dùng trong hóa đơn", dao.isDangDuocDung("KM0001"));
        dung("Tìm KM theo tên có dấu", !dao.search("Tri ân", null).isEmpty());
        dung("Lọc KM theo hình thức", dao.search("", HinhThucKhuyenMai.TIEN_MAT).size() == 1);
        String ma = dao.nextId();
        dung("Sinh mã KM", "KM0003".equals(ma));
        LocalDate d = LocalDate.of(2026, 12, 1);
        KhuyenMai km = new KhuyenMai(ma, "Giảm cuối năm", HinhThucKhuyenMai.PHAN_TRAM, 5, d, d.plusDays(30), true);
        dung("Thêm KM", dao.insert(km));
        km.setTrangThai(false);
        dung("Sửa KM", dao.update(km));
        KhuyenMai doc = dao.findById(ma);
        dung("Đọc lại ngày và trạng thái", doc != null && d.equals(doc.getThoiGianBatDau()) && !doc.isTrangThai());
        km.setPhamViApDung(PhamViKhuyenMai.PHU_TUNG);
        km.setSoLuotToiDa(20);
        km.setGhiChu("Thử ghi chú");
        dao.update(km);
        doc = dao.findById(ma);
        dung("Đọc lại trường KM mới", doc.getPhamViApDung() == PhamViKhuyenMai.PHU_TUNG && doc.getSoLuotToiDa() == 20
                && "Thử ghi chú".equals(doc.getGhiChu()) && doc.getSoLuotDaDung() == 0);
        dung("Đếm lượt đã dùng từ hóa đơn", dao.findById("KM0001").getSoLuotDaDung() == 1);
        dung("Xóa KM chưa dùng", dao.delete(ma));
    }

    private interface ThaoTac {
        void chay();
    }

    private static void dat(String ten, ThaoTac t) {
        try {
            t.chay();
        } catch (IllegalArgumentException ex) {
            dung(ten + " - " + ex.getMessage(), false);
        }
    }

    private static void loi(String ten, ThaoTac t) {
        try {
            t.chay();
            dung(ten + " - phải báo lỗi", false);
        } catch (IllegalArgumentException ex) {
            // Đúng kỳ vọng.
        }
    }

    private static void dung(String ten, boolean ketQua) {
        if (!ketQua) {
            soLoi++;
            System.out.println("LOI: " + ten);
        }
    }
}
