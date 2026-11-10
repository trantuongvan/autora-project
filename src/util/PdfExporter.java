package util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entity.ChiTietSuaChua_DichVu;
import entity.ChiTietSuaChua_PhuTung;
import entity.HoaDon;
import entity.PhieuTiepNhanVaSuaChua;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Xuất phiếu sửa chữa ra PDF bằng iText. */
public final class PdfExporter {
    // shortcut: dùng font Arial của Windows để có đủ dấu tiếng Việt, đóng gói font vào dự án nếu cần chạy ngoài Windows (NFR-07 chỉ yêu cầu Windows).
    private static final String DUONG_DAN_FONT = "C:/Windows/Fonts/arial.ttf";
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private PdfExporter() {
    }

    public static void xuatPhieuSuaChua(File tep, PhieuTiepNhanVaSuaChua p, List<ChiTietSuaChua_DichVu> dong)
            throws IOException, DocumentException {
        BaseFont goc = BaseFont.createFont(DUONG_DAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font thuong = new Font(goc, 11);
        Font dam = new Font(goc, 11, Font.BOLD);
        Font tieuDe = new Font(goc, 18, Font.BOLD);
        NumberFormat tien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

        Document tai = new Document();
        try (OutputStream ra = Files.newOutputStream(tep.toPath())) {
            PdfWriter.getInstance(tai, ra);
            tai.open();
            tai.add(new Paragraph("AUTORA GARAGE - PHIẾU SỬA CHỮA", tieuDe));
            tai.add(new Paragraph("Mã phiếu: " + p.getMaPhieuTNSC(), dam));
            tai.add(new Paragraph(" ", thuong));

            PdfPTable thongTin = new PdfPTable(new float[] {1, 2});
            thongTin.setWidthPercentage(100);
            String[][] truong = {
                {"Khách hàng", p.getXe().getKhachHang().getHoTen()},
                {"Số điện thoại", p.getXe().getKhachHang().getSoDienThoai()},
                {"Biển số", p.getXe().getBienSo()}, {"Xe", p.getTenXe()},
                {"Số km", String.valueOf(p.getSoKm())}, {"Mức nhiên liệu", p.getMucNhienLieu() + "%"},
                {"Trạng thái", p.getTrangThai().getText()}, {"Ưu tiên", p.getUuTien().getText()},
                {"Thời gian tiếp nhận", p.getThoiGianTiepNhan() == null ? "" : p.getThoiGianTiepNhan().format(DINH_DANG_GIO)},
                {"Dự kiến hoàn thành", p.getThoiGianDuKien() == null ? "" : p.getThoiGianDuKien().format(DINH_DANG_GIO)},
                {"Yêu cầu của khách", p.getYeuCauBanDau()}, {"Đánh giá ngoại thất", p.getTrayXuoc()},
                {"Đánh giá nội thất", p.getDanhGiaNoiThat()}, {"Tài sản trên xe", p.getTaiSanTrenXe()},
                {"Phụ tùng dự kiến", p.getPhuTungDuKien()}, {"Ghi chú kỹ thuật", p.getGhiChuKyThuat()}
            };
            for (String[] t : truong) {
                thongTin.addCell(new Phrase(t[0], dam));
                thongTin.addCell(new Phrase(t[1] == null ? "" : t[1], thuong));
            }
            tai.add(thongTin);
            tai.add(new Paragraph(" ", thuong));

            PdfPTable dichVu = new PdfPTable(new float[] {0.5f, 3, 2, 1.2f, 1.2f});
            dichVu.setWidthPercentage(100);
            for (String cot : new String[] {"STT", "Hạng mục dịch vụ", "Thợ phụ trách", "Trạng thái", "Tiền công (đ)"}) {
                dichVu.addCell(new Phrase(cot, dam));
            }
            double tong = 0;
            int stt = 1;
            for (ChiTietSuaChua_DichVu d : dong) {
                dichVu.addCell(new Phrase(String.valueOf(stt++), thuong));
                dichVu.addCell(new Phrase(d.getDichVu().getTenDichVu(), thuong));
                dichVu.addCell(new Phrase(d.getKyThuatVien() == null ? "Chờ phân công" : d.getKyThuatVien().getHoTen(), thuong));
                dichVu.addCell(new Phrase(d.getTrangThaiTienDo().getText(), thuong));
                dichVu.addCell(new Phrase(tien.format(d.getTienCongThucTe()), thuong));
                tong += d.getTienCongThucTe();
            }
            tai.add(dichVu);
            tai.add(new Paragraph("Tổng tiền công: " + tien.format(tong) + " đ", dam));
            tai.close();
        }
    }

    /** tt là phiếu có xe và chủ xe; thành tiền phụ tùng tính từ số lượng và giá bán thực tế. */
    public static void xuatHoaDon(File tep, HoaDon hd, PhieuTiepNhanVaSuaChua tt, List<ChiTietSuaChua_DichVu> dichVu,
            List<ChiTietSuaChua_PhuTung> phuTung)
            throws IOException, DocumentException {
        BaseFont goc = BaseFont.createFont(DUONG_DAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font thuong = new Font(goc, 11);
        Font dam = new Font(goc, 11, Font.BOLD);
        Font tieuDe = new Font(goc, 18, Font.BOLD);
        NumberFormat tien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

        Document tai = new Document();
        try (OutputStream ra = Files.newOutputStream(tep.toPath())) {
            PdfWriter.getInstance(tai, ra);
            tai.open();
            tai.add(new Paragraph("AUTORA GARAGE - HÓA ĐƠN THANH TOÁN", tieuDe));
            tai.add(new Paragraph("Mã hóa đơn: " + hd.getMaHD() + "   |   Trạng thái: " + hd.getTrangThaiThanhToan(), dam));
            if (hd.getLyDoHuy() != null) {
                tai.add(new Paragraph("Lý do hủy: " + hd.getLyDoHuy(), thuong));
            }
            tai.add(new Paragraph(" ", thuong));

            String[][] truong = {
                {"Khách hàng", tt.getXe().getKhachHang().getHoTen() + " (" + tt.getXe().getKhachHang().getMaKH() + ")"},
                {"Số điện thoại", tt.getXe().getKhachHang().getSoDienThoai()},
                {"Địa chỉ", tt.getXe().getKhachHang().getDiaChi()}, {"Email", tt.getXe().getKhachHang().getEmail()},
                {"Biển số", tt.getXe().getBienSo()}, {"Xe", FormatUtil.moTaXeDayDu(tt.getXe())},
                {"Số VIN", tt.getXe().getSoVIN()}, {"Mã phiếu", tt.getMaPhieuTNSC()},
                {"Ngày lập", hd.getNgayLap() == null ? "" : hd.getNgayLap().format(DINH_DANG_GIO)},
                {"Hình thức thanh toán", hd.getHinhThucThanhToan().getText()}
            };
            PdfPTable thongTin = new PdfPTable(new float[] {1, 2});
            thongTin.setWidthPercentage(100);
            for (String[] t : truong) {
                thongTin.addCell(new Phrase(t[0], dam));
                thongTin.addCell(new Phrase(t[1] == null ? "" : t[1], thuong));
            }
            tai.add(thongTin);

            tai.add(new Paragraph("Dịch vụ", dam));
            PdfPTable bangDichVu = new PdfPTable(new float[] {3, 1, 2.2f, 1.2f, 1.2f});
            bangDichVu.setWidthPercentage(100);
            for (String cot : new String[] {"Tên dịch vụ", "Mã DV", "Kỹ thuật viên", "Trạng thái", "Tiền công (đ)"}) {
                bangDichVu.addCell(new Phrase(cot, dam));
            }
            for (ChiTietSuaChua_DichVu d : dichVu) {
                bangDichVu.addCell(new Phrase(d.getDichVu().getTenDichVu(), thuong));
                bangDichVu.addCell(new Phrase(d.getDichVu().getMaDichVu(), thuong));
                bangDichVu.addCell(new Phrase(d.getThoHienThi(), thuong));
                bangDichVu.addCell(new Phrase(d.getTrangThaiTienDo().getText(), thuong));
                bangDichVu.addCell(new Phrase(tien.format(d.getTienCongThucTe()), thuong));
            }
            tai.add(bangDichVu);

            tai.add(new Paragraph("Phụ tùng", dam));
            PdfPTable bangPhuTung = new PdfPTable(new float[] {3, 1.5f, 0.8f, 1.2f, 1.2f});
            bangPhuTung.setWidthPercentage(100);
            for (String cot : new String[] {"Tên phụ tùng", "Mã SKU", "Số lượng", "Giá bán (đ)", "Thành tiền (đ)"}) {
                bangPhuTung.addCell(new Phrase(cot, dam));
            }
            for (ChiTietSuaChua_PhuTung d : phuTung) {
                bangPhuTung.addCell(new Phrase(d.getPhuTung().getTenPhuTung(), thuong));
                bangPhuTung.addCell(new Phrase(d.getPhuTung().getMaSKU(), thuong));
                bangPhuTung.addCell(new Phrase(String.valueOf(d.getSoLuongSuDung()), thuong));
                bangPhuTung.addCell(new Phrase(tien.format(d.getGiaBanThucTe()), thuong));
                bangPhuTung.addCell(new Phrase(tien.format(d.getSoLuongSuDung() * d.getGiaBanThucTe()), thuong));
            }
            tai.add(bangPhuTung);
            tai.add(new Paragraph(" ", thuong));

            String[][] tong = {
                {"Tổng tiền công", tien.format(hd.getTongTienCong()) + " đ"},
                {"Tổng tiền phụ tùng", tien.format(hd.getTongTienPhuTung()) + " đ"},
                {"Giảm giá" + (hd.getKhuyenMai() == null ? "" : " (" + hd.getKhuyenMai().getMaKM() + ")"), tien.format(hd.getTongTienGiam()) + " đ"},
                {"VAT (" + tien.format(hd.getThue() == null ? 0 : hd.getThue().getThueSuat()) + "%)", tien.format(hd.getTongTienVAT()) + " đ"},
                {"Tổng thanh toán", tien.format(hd.getTongThanhToan()) + " đ"}
            };
            PdfPTable bangTong = new PdfPTable(new float[] {2, 1});
            bangTong.setWidthPercentage(100);
            for (String[] t : tong) {
                bangTong.addCell(new Phrase(t[0], dam));
                bangTong.addCell(new Phrase(t[1], dam));
            }
            tai.add(bangTong);
            tai.close();
        }
    }
}
