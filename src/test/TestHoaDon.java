package test;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import connection.ConnectDB;
import controller.ChiTietHoaDonController;
import controller.QLPhuTungController;
import dao.HoaDon_Dao;
import dao.Thue_Dao;
import dao.TrangChuQuanLy_Dao;
import entity.HinhThucKhuyenMai;
import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.KhuyenMai;
import entity.PhieuTiepNhanVaSuaChua;
import entity.PhamViKhuyenMai;
import dao.TrangChuQuanLy_Dao.ThongKeChoThanhToan;
import entity.Thue;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;
import util.FormatUtil;
import util.PdfExporter;
import util.VietQRUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Chạy trên CSDL mẫu mới tạo. Mỗi tình huống tự thêm phiếu và phụ tùng riêng. */
public class TestHoaDon {
    private static final PhamViKhuyenMai TONG = PhamViKhuyenMai.TONG_HOA_DON;
    private static final PhamViKhuyenMai DICH_VU = PhamViKhuyenMai.NHOM_DICH_VU;
    private static final PhamViKhuyenMai PHU_TUNG = PhamViKhuyenMai.PHU_TUNG;
    private static final HinhThucKhuyenMai PT = HinhThucKhuyenMai.PHAN_TRAM;
    private static final HinhThucKhuyenMai TM = HinhThucKhuyenMai.TIEN_MAT;
    private static final String XE_KHONG_NO = "51H-123.45"; // KH0001, duocGhiNo = 0
    private static final String XE_CO_NO = "50E-999.12"; // KH0002, duocGhiNo = 1

    private static final HoaDon_Dao dao = new HoaDon_Dao();
    private static int soLoi;
    private static int stt;

    private interface Viec {
        void chay() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        kiemTraTinhTien();
        kiemTraEntityVaTienNhap();
        kiemTraQr();
        kiemTraDieuKienPhieu();
        kiemTraTonKho();
        kiemTraKhuyenMai();
        kiemTraCongNo();
        kiemTraHuy();
        kiemTraLapLai();
        kiemTraTimKiemVaPdf();
        kiemTraBoDemChoThanhToan();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : "SO LOI: " + soLoi);
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    private static void kiemTraTinhTien() {
        tien("Hóa đơn mẫu 1.506.600", tinh(750000, 800000, PT, 10, TONG, 8), 155000, 111600, 1506600);
        tien("% chỉ trên nhóm dịch vụ", tinh(750000, 800000, PT, 10, DICH_VU, 8), 75000, 118000, 1593000);
        tien("% chỉ trên phụ tùng", tinh(750000, 800000, PT, 10, PHU_TUNG, 8), 80000, 117600, 1587600);
        tien("Tiền mặt bị chặn theo cơ sở dịch vụ", tinh(100000, 800000, TM, 500000, DICH_VU, 10), 100000, 80000, 880000);
        tien("Tiền mặt bị chặn theo cơ sở phụ tùng", tinh(100000, 50000, TM, 500000, PHU_TUNG, 0), 50000, 0, 100000);
        tien("Tiền mặt bị chặn theo tổng", tinh(100000, 50000, TM, 500000, TONG, 8), 150000, 0, 0);
        tien("Giảm 100%", tinh(750000, 800000, PT, 100, TONG, 8), 1550000, 0, 0);
        tien("Không có phụ tùng", tinh(500000, 0, null, 0, null, 10), 0, 50000, 550000);
        tien("Khuyến mãi phụ tùng khi không có phụ tùng", tinh(500000, 0, PT, 50, PHU_TUNG, 10), 0, 50000, 550000);
        tien("Làm tròn nửa đồng lên", tinh(1005, 0, null, 0, null, 10), 0, 101, 1106);
        loi("Tiền âm", () -> tinh(-1, 0, null, 0, null, 8));
        loi("Phạm vi lạ", () -> tinh(1, 0, PT, 10, null, 8));
    }

    private static void kiemTraEntityVaTienNhap() {
        loi("Mã HĐ sai dạng", () -> new HoaDon("HD1"));
        loi("Hình thức lạ", () -> HinhThucThanhToan.fromDb("Séc"));
        loi("Hình thức null", () -> new HoaDon().setHinhThucThanhToan(null));
        loi("Tổng âm", () -> new HoaDon(TrangThaiHoaDon.DA_THANH_TOAN, -1));
        loi("Thuế suất trên 100", () -> new Thue("X", "A", 100.5));
        kiem("Thuế tính tiền", new Thue("X", "A", 8).tinhTienThue(1395000) == 111600);
        kiem("Tiền có dấu chấm", ChiTietHoaDonController.docTien("1.500.000") == 1500000);
        kiem("Tiền không dấu", ChiTietHoaDonController.docTien("1500000") == 1500000);
        loi("Tiền có chữ", () -> ChiTietHoaDonController.docTien("15a"));
        loi("Tiền âm nhập tay", () -> ChiTietHoaDonController.docTien("-5"));
        loi("Tiền chấm sai", () -> ChiTietHoaDonController.docTien("1.50"));
        loi("Tiền rỗng", () -> ChiTietHoaDonController.docTien(" "));
        kiem("Giá nhập hiển thị 150.000đ", FormatUtil.parseCurrency("150.000đ") == 150000);
        kiem("Giá nhập hiển thị 1.500.000đ", FormatUtil.parseCurrency("1.500.000đ") == 1500000);
        kiem("Giá nhập gõ số thuần", FormatUtil.parseCurrency("150000") == 150000);
        kiem("Giá nhập chỉ số 0", FormatUtil.parseCurrency("0") == 0);
        loi("Giá nhập chữ", () -> FormatUtil.parseCurrency("abc"));
        loi("Giá nhập chấm sai 1.50đ", () -> FormatUtil.parseCurrency("1.50đ"));
        loi("Giá nhập âm", () -> FormatUtil.parseCurrency("-150.000đ"));
        loi("Giá nhập rỗng", () -> FormatUtil.parseCurrency(""));
        loi("Giá nhập chỉ có đ", () -> FormatUtil.parseCurrency("đ"));
        loi("Giá nhập null", () -> FormatUtil.parseCurrency(null));
        kiem("Giá phụ tùng dấu chấm", QLPhuTungController.docGia("150.000") == 150000);
        kiem("Giá phụ tùng dấu phẩy", QLPhuTungController.docGia("1,500,000") == 1500000);
        kiem("Giá phụ tùng số thuần", QLPhuTungController.docGia("85000") == 85000);
        loi("Giá phụ tùng thập phân", () -> QLPhuTungController.docGia("150.5"));
        loi("Giá phụ tùng rỗng", () -> QLPhuTungController.docGia(" "));
    }

    private static void kiemTraQr() {
        String url = VietQRUtil.taoUrl(1506600.4, "HD 0&1=é");
        kiem("QR đúng tiền nguyên", url.contains("amount=1506600&"));
        kiem("QR mã hóa nội dung", url.contains("addInfo=HD%200%261%3D%C3%A9&"));
        kiem("QR mã hóa tên tài khoản", url.endsWith("accountName=AUTORA%20GARAGE%20TEST"));
        kiem("QR đúng đường dẫn", url.startsWith("https://img.vietqr.io/image/" + VietQRUtil.MA_BIN + "-"
                + VietQRUtil.SO_TAI_KHOAN + "-compact2.png?"));
        try {
            java.awt.image.BufferedImage anh = VietQRUtil.veQrTuSvg("<path d=\"M0 0h1v1h-1zM2 1h1v1h-1z\"/>");
            // 3 ô cộng viền 2 ô mỗi bên, mỗi ô 6 điểm ảnh.
            kiem("QR từ SVG đúng kích thước", anh.getWidth() == 42);
            kiem("QR từ SVG vẽ ô đen", (anh.getRGB(12, 12) & 0xFFFFFF) == 0);
            kiem("QR từ SVG để trống ô trắng", (anh.getRGB(18, 12) & 0xFFFFFF) == 0xFFFFFF);
        } catch (java.io.IOException ex) {
            kiem("QR từ SVG không được lỗi", false);
        }
        bi("SVG không có ô QR", "không trả về ảnh", () -> VietQRUtil.veQrTuSvg("<html>lỗi</html>"));
    }

    private static void kiemTraDieuKienPhieu() throws Exception {
        String chua = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.DANG_SUA, 100000);
        bi("Phiếu chưa hoàn thành", "chưa hoàn thành", () -> lap(chua, null, HinhThucThanhToan.CHUYEN_KHOAN));
        kiem("Phiếu chưa xong không nằm trong danh sách chờ", !choLap(chua));
        bi("Phiếu mẫu đã có hóa đơn", "còn hiệu lực", () -> lap("PTNSC00001", null, HinhThucThanhToan.CHUYEN_KHOAN));
        kiem("Phiếu đã có hóa đơn không nằm trong danh sách chờ", !choLap("PTNSC00001"));
        bi("Phiếu không tồn tại", "không tồn tại", () -> lap("KHONGCO", null, HinhThucThanhToan.CHUYEN_KHOAN));

        String p = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        kiem("Phiếu hoàn thành nằm trong danh sách chờ", choLap(p));
        bi("Thiếu thuế", "chọn loại thuế", () -> dao.lapHoaDon(p, null, null, HinhThucThanhToan.CHUYEN_KHOAN, null, "NV0002"));
        bi("Tiền mặt thiếu", "chưa đủ", () -> dao.lapHoaDon(p, null, "VAT08", HinhThucThanhToan.TIEN_MAT, 107999.0, "NV0002"));
        bi("Tiền mặt không nhập", "chưa đủ", () -> dao.lapHoaDon(p, null, "VAT08", HinhThucThanhToan.TIEN_MAT, null, "NV0002"));
        kiem("Từ chối không để lại hóa đơn", !dao.coHoaDonHieuLuc(p));
        String ma = dao.lapHoaDon(p, null, "VAT08", HinhThucThanhToan.TIEN_MAT, 108000.0, "NV0002");
        kiem("Tiền mặt vừa đủ được nhận", ma.matches("HD\\d{6}"));
        kiem("Tiền mặt thành Đã thanh toán", dao.findById(ma).getTrangThaiThanhToan() == TrangThaiHoaDon.DA_THANH_TOAN);
        kiem("Hóa đơn không khuyến mãi có khuyến mãi null", dao.findById(ma).getKhuyenMai() == null
                && dao.findById(ma).getThue() != null);
    }

    private static void kiemTraTonKho() throws Exception {
        String a = taoPhuTung(10);
        String b = taoPhuTung(5);
        String p = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 750000);
        themPhuTung(p, a, 2, 300000);
        themPhuTung(p, b, 1, 200000);
        String ma = lap(p, "KM0001", HinhThucThanhToan.CHUYEN_KHOAN);
        kiem("Lập hóa đơn không đổi tồn dòng 1", ton(a) == 10);
        kiem("Lập hóa đơn không đổi tồn dòng 2", ton(b) == 5);
        HoaDon hd = dao.findById(ma);
        kiem("Tiền do máy chủ tính", hd.getTongTienCong() == 750000 && hd.getTongTienPhuTung() == 800000
                && hd.getTongTienGiam() == 155000 && hd.getTongTienVAT() == 111600 && hd.getTongThanhToan() == 1506600);

        // Phụ tùng đã rời kho từ lúc duyệt xuất, nên tồn về 0 không được chặn lập hóa đơn.
        String het = taoPhuTung(0);
        String q = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(q, het, 2, 1000);
        kiem("Tồn 0 vẫn lập được hóa đơn", lap(q, null, HinhThucThanhToan.CHUYEN_KHOAN) != null && ton(het) == 0);
    }

    private static void kiemTraKhuyenMai() throws Exception {
        LocalDate nay = LocalDate.now();
        String hetHan = taoKM(PT, 10, TONG, nay.minusDays(10), nay.minusDays(1), true, null);
        String chuaToi = taoKM(PT, 10, TONG, nay.plusDays(1), nay.plusDays(9), true, null);
        String ngung = taoKM(PT, 10, TONG, nay.minusDays(1), nay.plusDays(1), false, null);
        String mot = taoKM(TM, 50000, DICH_VU, nay, nay, true, 1);
        for (String km : new String[] {hetHan, chuaToi, ngung}) {
            String p = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
            bi("Khuyến mãi " + km + " bị từ chối", km, () -> lap(p, km, HinhThucThanhToan.CHUYEN_KHOAN));
            kiem("Không có trong danh sách chọn " + km, !coTrongDanhSach(km));
            kiem("Từ chối không để lại hóa đơn", !dao.coHoaDonHieuLuc(p));
        }
        kiem("Khuyến mãi còn lượt được chọn, ngày cuối vẫn dùng được", coTrongDanhSach(mot));
        String ma = lap(taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000), mot, HinhThucThanhToan.CHUYEN_KHOAN);
        kiem("Khuyến mãi tiền mặt được trừ", dao.findById(ma).getTongTienGiam() == 50000);
        kiem("Hết lượt thì không được chọn", !coTrongDanhSach(mot));
        String p2 = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        bi("Hết lượt bị từ chối", mot, () -> lap(p2, mot, HinhThucThanhToan.CHUYEN_KHOAN));
        dao.huy(ma, "Trả lượt", "admin");
        kiem("Hủy hóa đơn trả lại lượt", coTrongDanhSach(mot));
        kiem("Lập được với khuyến mãi vừa trả lượt", lap(p2, mot, HinhThucThanhToan.CHUYEN_KHOAN) != null);
    }

    private static void kiemTraCongNo() throws Exception {
        String a = taoPhuTung(10);
        String khongNo = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(khongNo, a, 1, 1000);
        bi("Khách không được ghi nợ", "ghi nợ", () -> lap(khongNo, null, HinhThucThanhToan.CONG_NO));
        kiem("Từ chối ghi nợ không đổi tồn", ton(a) == 10);

        String b = taoPhuTung(10);
        String coNo = taoPhieu(XE_CO_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(coNo, b, 2, 1000);
        String ma = lap(coNo, null, HinhThucThanhToan.CONG_NO);
        HoaDon hd = dao.findById(ma);
        kiem("Ghi nợ đúng trạng thái", hd.getTrangThaiThanhToan() == TrangThaiHoaDon.CONG_NO
                && hd.getHinhThucThanhToan() == HinhThucThanhToan.CONG_NO);
        kiem("Ghi nợ không đổi tồn", ton(b) == 10);

        bi("Thu nợ không chọn Công nợ lần nữa", "không hợp lệ", () -> dao.thanhToanCongNo(ma, HinhThucThanhToan.CONG_NO, null, "admin"));
        bi("Thu nợ tiền mặt thiếu", "chưa đủ", () -> dao.thanhToanCongNo(ma, HinhThucThanhToan.TIEN_MAT, 1.0, "admin"));
        kiem("Thu nợ lỗi giữ nguyên trạng thái", dao.findById(ma).getTrangThaiThanhToan() == TrangThaiHoaDon.CONG_NO);
        dao.thanhToanCongNo(ma, HinhThucThanhToan.CHUYEN_KHOAN, null, "admin");
        hd = dao.findById(ma);
        kiem("Thu nợ xong đổi trạng thái và hình thức", hd.getTrangThaiThanhToan() == TrangThaiHoaDon.DA_THANH_TOAN
                && hd.getHinhThucThanhToan() == HinhThucThanhToan.CHUYEN_KHOAN);
        kiem("Thu nợ không đổi tồn", ton(b) == 10);
        bi("Không thu nợ hai lần", "Công nợ", () -> dao.thanhToanCongNo(ma, HinhThucThanhToan.TIEN_MAT, 999999999.0, "admin"));
    }

    private static void kiemTraHuy() throws Exception {
        String a = taoPhuTung(10);
        String p = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(p, a, 4, 1000);
        String ma = lap(p, null, HinhThucThanhToan.CHUYEN_KHOAN);
        bi("Hủy thiếu lý do", "lý do", () -> dao.huy(ma, "  ", "admin"));
        bi("Hủy lý do null", "lý do", () -> dao.huy(ma, null, "admin"));
        StringBuilder dai = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            dai.append('x');
        }
        bi("Hủy lý do quá dài", "255", () -> dao.huy(ma, dai.toString(), "admin"));

        dao.huy(ma, "Khách đổi ý", "admin");
        HoaDon hd = dao.findById(ma);
        kiem("Hủy đổi trạng thái và lưu lý do", hd.getTrangThaiThanhToan() == TrangThaiHoaDon.DA_HUY
                && "Khách đổi ý".equals(hd.getLyDoHuy()));
        kiem("Hủy không đổi tồn", ton(a) == 10);
        kiem("Hủy ghi nhật ký", demNhatKy(ma, TrangThaiHoaDon.DA_THANH_TOAN.getText(), "%Đã hủy%Khách đổi ý%") == 1);
        bi("Không hủy hai lần", "hủy trước đó", () -> dao.huy(ma, "Lần hai", "admin"));
        kiem("Hủy lần hai không ghi thêm nhật ký", demNhatKy(ma, "%", "%") == 1);
        bi("Không thu nợ hóa đơn đã hủy", "Công nợ", () -> dao.thanhToanCongNo(ma, HinhThucThanhToan.CHUYEN_KHOAN, null, "admin"));

        String b = taoPhuTung(10);
        String q = taoPhieu(XE_CO_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(q, b, 1, 1000);
        String maNo = lap(q, null, HinhThucThanhToan.CONG_NO);
        dao.huy(maNo, "Sai khách", "admin");
        kiem("Hủy hóa đơn công nợ không đổi tồn", ton(b) == 10);
        kiem("Nhật ký ghi trạng thái cũ Công nợ", demNhatKy(maNo, TrangThaiHoaDon.CONG_NO.getText(), "%") == 1);

        String c = taoPhuTung(10);
        String r = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(r, c, 1, 1000);
        String maBG = lap(r, null, HinhThucThanhToan.CHUYEN_KHOAN);
        thucThi("INSERT INTO PhieuBanGiao (maPhieuBG, maPhieuTNSC, maNV_BanGiao) VALUES (?, ?, 'NV0002')", "BG" + maBG, r);
        bi("Có bàn giao thì không hủy", "bàn giao", () -> dao.huy(maBG, "Muộn", "admin"));
        kiem("Có bàn giao giữ nguyên hóa đơn", dao.findById(maBG).getTrangThaiThanhToan() == TrangThaiHoaDon.DA_THANH_TOAN);
        kiem("Có bàn giao không đổi tồn", ton(c) == 10);
        kiem("Có bàn giao không ghi nhật ký", demNhatKy(maBG, "%", "%") == 0);
        bi("Hóa đơn mẫu đã bàn giao", "bàn giao", () -> dao.huy("HD000001", "Thử", "admin"));
        bi("Hủy hóa đơn không tồn tại", "không tồn tại", () -> dao.huy("HD999999", "Thử", "admin"));
    }

    private static void kiemTraLapLai() throws Exception {
        String a = taoPhuTung(10);
        String p = taoPhieu(XE_KHONG_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        themPhuTung(p, a, 3, 1000);
        String lan1 = lap(p, null, HinhThucThanhToan.CHUYEN_KHOAN);
        bi("Hóa đơn hiệu lực thứ hai bị từ chối", "còn hiệu lực", () -> lap(p, null, HinhThucThanhToan.CHUYEN_KHOAN));
        dao.huy(lan1, "Lập sai", "admin");
        kiem("Hủy xong phiếu lại chờ lập", choLap(p));
        String lan2 = lap(p, null, HinhThucThanhToan.CHUYEN_KHOAN);
        kiem("Lập lại có mã mới", !lan1.equals(lan2));
        kiem("Lập lại không đổi tồn", ton(a) == 10);
        kiem("Lập lại vẫn tính tiền phụ tùng trên phiếu", dao.findById(lan2).getTongTienPhuTung() == 3000);
        kiem("Hóa đơn hủy vẫn được giữ", dao.findById(lan1).getTrangThaiThanhToan() == TrangThaiHoaDon.DA_HUY);
        bi("Chỉ mục CSDL chặn hóa đơn hiệu lực thứ hai", "UX_HoaDon_PhieuConHieuLuc", () ->
                thucThi("INSERT INTO HoaDon (maHD, maPhieuTNSC, maNV_Lap) VALUES ('HD900001', ?, 'NV0002')", p));
    }

    private static void kiemTraTimKiemVaPdf() throws Exception {
        kiem("Tìm theo mã HĐ", dao.search("HD000001", null, null, null, null).size() == 1);
        kiem("Tìm theo mã phiếu", !dao.search("PTNSC00001", null, null, null, null).isEmpty());
        kiem("Tìm theo SĐT", !dao.search("0988111222", null, null, null, null).isEmpty());
        kiem("Tìm theo biển số", !dao.search("51H-123", null, null, null, null).isEmpty());
        kiem("Lọc Đã hủy chỉ có hóa đơn hủy", tatCaTrangThai(dao.search("", TrangThaiHoaDon.DA_HUY, null, null, null), TrangThaiHoaDon.DA_HUY));
        kiem("Lọc hình thức", !dao.search("HD000001", null, HinhThucThanhToan.CHUYEN_KHOAN, null, null).isEmpty()
                && dao.search("HD000001", null, HinhThucThanhToan.TIEN_MAT, null, null).isEmpty());
        LocalDate ngayMau = LocalDate.of(2026, 10, 20);
        kiem("Đến ngày gồm cả ngày đó", dao.search("HD000001", null, null, ngayMau, ngayMau).size() == 1);
        kiem("Ngoài khoảng không có", dao.search("HD000001", null, null, ngayMau.plusDays(1), null).isEmpty());

        List<Thue> thue = new Thue_Dao().findAll();
        kiem("Có thuế VAT08", thue.contains(new Thue("VAT08")));
        kiem("Dự đoán mã đúng dạng", dao.duDoanMaHD().matches("HD\\d{6}"));

        HoaDon hd = dao.findById("HD000001");
        kiem("Hóa đơn đọc có phiếu, xe, khách và thuế", hd.getPhieu() != null
                && "PTNSC00001".equals(hd.getPhieu().getMaPhieuTNSC())
                && hd.getPhieu().getXe() != null && "51H-123.45".equals(hd.getPhieu().getXe().getBienSo())
                && hd.getPhieu().getXe().getKhachHang() != null
                && "Phạm Minh Hoàng".equals(hd.getPhieu().getXe().getKhachHang().getHoTen())
                && hd.getKhuyenMai() != null && "KM0001".equals(hd.getKhuyenMai().getMaKM())
                && hd.getThue() != null && "VAT08".equals(hd.getThue().getMaThue()) && hd.getThue().getThueSuat() == 8);
        PhieuTiepNhanVaSuaChua phieuHd = dao.findThongTinPhieu(hd.getPhieu().getMaPhieuTNSC());
        kiem("Phiếu của hóa đơn có xe và chủ xe", phieuHd.getXe().getKhachHang().getSoDienThoai().equals("0988111222")
                && phieuHd.getXe().getHangXe() != null && phieuHd.getTrangThai() == TrangThaiPhieu.HOAN_THANH);
        kiem("Dịch vụ và phụ tùng của hóa đơn đủ đối tượng", dao.findDichVu("PTNSC00001").size() == 3
                && dao.findDichVu("PTNSC00001").get(0).getDichVu().getTenDichVu() != null
                && dao.findPhuTung("PTNSC00001").size() == 2
                && dao.findPhuTung("PTNSC00001").get(0).getPhuTung().getTenPhuTung() != null);
        File tep = File.createTempFile("hoadon", ".pdf");
        tep.deleteOnExit();
        String maPhieuHd = hd.getPhieu().getMaPhieuTNSC();
        PdfExporter.xuatHoaDon(tep, hd, dao.findThongTinPhieu(maPhieuHd),
                dao.findDichVu(maPhieuHd), dao.findPhuTung(maPhieuHd));
        PdfReader doc = new PdfReader(tep.getPath());
        String chu = PdfTextExtractor.getTextFromPage(doc, 1);
        doc.close();
        for (String can : new String[] {"HD000001", "Phạm Minh Hoàng", "51H-123.45", "SKU_OIL01", "DV0001",
            "Tổng tiền công", "Tổng tiền phụ tùng", "750.000", "800.000", "155.000", "111.600", "1.506.600"}) {
            kiem("PDF có " + can, chu.contains(can));
        }
    }

    // ---------- Hỗ trợ ----------

    private static void kiemTraBoDemChoThanhToan() throws Exception {
        TrangChuQuanLy_Dao trangChu = new TrangChuQuanLy_Dao();
        ThongKeChoThanhToan goc = trangChu.hoaDonChoThanhToan();
        int so = goc.getSoLuong();
        double tien = goc.getTongTien();
        taoPhieu(XE_CO_NO, TrangThaiPhieu.DANG_SUA, 100000);
        kiem("Phiếu chưa xong không tính", trangChu.hoaDonChoThanhToan().getSoLuong() == so);

        String tra = taoPhieu(XE_CO_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        ThongKeChoThanhToan coPhieu = trangChu.hoaDonChoThanhToan();
        kiem("Phiếu xong chưa hóa đơn được tính", coPhieu.getSoLuong() == so + 1);
        kiem("Phiếu chưa hóa đơn không cộng tiền nợ", coPhieu.getTongTien() == tien);
        lap(tra, null, HinhThucThanhToan.CHUYEN_KHOAN);
        kiem("Trả ngay thì hết chờ", trangChu.hoaDonChoThanhToan().getSoLuong() == so);

        String no = taoPhieu(XE_CO_NO, TrangThaiPhieu.HOAN_THANH, 100000);
        String maNo = lap(no, null, HinhThucThanhToan.CONG_NO);
        ThongKeChoThanhToan coNo = trangChu.hoaDonChoThanhToan();
        kiem("Công nợ được tính một lần", coNo.getSoLuong() == so + 1);
        kiem("Công nợ cộng tiền", coNo.getTongTien() > tien);
        dao.huy(maNo, "Lập sai", "admin");
        ThongKeChoThanhToan daHuy = trangChu.hoaDonChoThanhToan();
        kiem("Hủy hóa đơn nợ thì phiếu lại chờ", daHuy.getSoLuong() == so + 1);
        kiem("Hủy hóa đơn nợ thì hết tiền nợ", daHuy.getTongTien() == tien);
    }

    private static String lap(String maPhieu, String maKM, HinhThucThanhToan hinhThuc) throws SQLException {
        return dao.lapHoaDon(maPhieu, maKM, "VAT08", hinhThuc, null, "NV0002");
    }

    private static boolean choLap(String maPhieu) throws SQLException {
        for (PhieuTiepNhanVaSuaChua d : dao.findPhieuChoLap()) {
            if (d.getMaPhieuTNSC().equals(maPhieu)) {
                return true;
            }
        }
        return false;
    }

    private static boolean coTrongDanhSach(String maKM) throws SQLException {
        return dao.findKhuyenMaiHopLe().contains(new KhuyenMai(maKM));
    }

    private static boolean tatCaTrangThai(List<HoaDon> ds, TrangThaiHoaDon trangThai) {
        if (ds.isEmpty()) {
            return false;
        }
        for (HoaDon h : ds) {
            if (trangThai != h.getTrangThaiThanhToan()) {
                return false;
            }
        }
        return true;
    }

    private static String taoPhieu(String bienSo, TrangThaiPhieu trangThai, double tienCong) throws SQLException {
        String ma = "TEST" + (++stt);
        thucThi("INSERT INTO PhieuTiepNhanVaSuaChua (maPhieuTNSC, bienSo, maKH, maNV_LapPhieu, trangThai) "
                + "SELECT ?, bienSo, maKH, 'NV0002', ? FROM Xe WHERE bienSo = ?",
                ma, trangThai.getText(), bienSo);
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, tienCongThucTe, trangThaiTienDo) "
                + "VALUES (?, 'DV0001', ?, N'Hoàn thành')")) {
            s.setString(1, ma);
            s.setDouble(2, tienCong);
            s.executeUpdate();
        }
        return ma;
    }

    private static String taoPhuTung(int ton) throws SQLException {
        String ma = "SKUT" + (++stt);
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO PhuTung (maSKU, tenPhuTung, soLuongTon) VALUES (?, N'Phụ tùng thử', ?)")) {
            s.setString(1, ma);
            s.setInt(2, ton);
            s.executeUpdate();
        }
        return ma;
    }

    private static void themPhuTung(String maPhieu, String sku, int soLuong, double gia) throws SQLException {
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO ChiTietSuaChua_PhuTung (maPhieuTNSC, maSKU, soLuongSuDung, giaBanThucTe) VALUES (?, ?, ?, ?)")) {
            s.setString(1, maPhieu);
            s.setString(2, sku);
            s.setInt(3, soLuong);
            s.setDouble(4, gia);
            s.executeUpdate();
        }
    }

    private static String taoKM(HinhThucKhuyenMai hinhThuc, double giaTri, PhamViKhuyenMai phamVi, LocalDate bd, LocalDate kt,
            boolean trangThai, Integer soLuot) throws SQLException {
        String ma = "KMT" + (++stt);
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO KhuyenMai (maKM, tenChuongTrinh, hinhThuc, giaTri, thoiGianBatDau, thoiGianKetThuc, "
                + "trangThai, phamViApDung, soLuotToiDa) VALUES (?, N'Thử', ?, ?, ?, ?, ?, ?, ?)")) {
            s.setString(1, ma);
            s.setString(2, hinhThuc.getText());
            s.setDouble(3, giaTri);
            s.setDate(4, java.sql.Date.valueOf(bd));
            s.setDate(5, java.sql.Date.valueOf(kt));
            s.setBoolean(6, trangThai);
            s.setString(7, phamVi.getText());
            s.setObject(8, soLuot);
            s.executeUpdate();
        }
        return ma;
    }

    private static void thucThi(String sql, String... thamSo) throws SQLException {
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < thamSo.length; i++) {
                s.setString(i + 1, thamSo[i]);
            }
            s.executeUpdate();
        }
    }

    private static int ton(String sku) throws SQLException {
        return demMot("SELECT soLuongTon FROM PhuTung WHERE maSKU = ?", sku);
    }

    private static int demNhatKy(String maHD, String cu, String moi) throws SQLException {
        return demMot("SELECT COUNT(*) FROM NhatKyHoatDong WHERE hanhDong = N'Hủy hóa đơn' AND doiTuong = ? "
                + "AND giaTriCu LIKE ? AND giaTriMoi LIKE ?", maHD, cu, moi);
    }

    private static int demMot(String sql, String... thamSo) throws SQLException {
        try (Connection c = ConnectDB.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < thamSo.length; i++) {
                s.setString(i + 1, thamSo[i]);
            }
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    /** hinhThuc null là hóa đơn không có khuyến mãi. */
    private static HoaDon tinh(double cong, double phuTung, HinhThucKhuyenMai hinhThuc, double giaTri,
            PhamViKhuyenMai phamVi, double thueSuat) {
        KhuyenMai km = null;
        if (hinhThuc != null) {
            km = new KhuyenMai("KMX");
            km.setHinhThuc(hinhThuc);
            km.setGiaTri(giaTri);
            km.setPhamViApDung(phamVi);
        }
        Thue thue = new Thue("X", "A", thueSuat);
        ChiTietHoaDonController.KetQuaTinhToan ketQua = ChiTietHoaDonController.tinhToan(cong, phuTung, km, thue);
        return new HoaDon("HD000001", TrangThaiHoaDon.DA_THANH_TOAN, ketQua.tongTienCong, ketQua.tongTienPhuTung,
                ketQua.tongTienGiam, ketQua.tongTienVAT, ketQua.tongThanhToan, thue);
    }

    private static void tien(String ten, HoaDon t, double giam, double vat, double tong) {
        kiem(ten + " (giam=" + t.getTongTienGiam() + ", vat=" + t.getTongTienVAT() + ", tong=" + t.getTongThanhToan() + ")",
                t.getTongTienGiam() == giam && t.getTongTienVAT() == vat && t.getTongThanhToan() == tong);
    }

    private static void kiem(String ten, boolean dat) {
        if (!dat) {
            soLoi++;
            System.out.println("SAI: " + ten);
        }
    }

    private static void loi(String ten, Viec viec) {
        try {
            viec.chay();
            kiem(ten + " phải báo lỗi", false);
        } catch (IllegalArgumentException ex) {
            // Đúng mong đợi.
        } catch (Exception ex) {
            kiem(ten + " sai loại lỗi: " + ex, false);
        }
    }

    /** Thao tác phải bị từ chối với thông báo chứa đoạn chữ cho trước. */
    private static void bi(String ten, String chua, Viec viec) {
        try {
            viec.chay();
            kiem(ten + " phải bị từ chối", false);
        } catch (Exception ex) {
            kiem(ten + ": thông báo sai: " + ex.getMessage(), String.valueOf(ex.getMessage()).contains(chua));
        }
    }
}
