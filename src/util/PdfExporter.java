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
                {"Khách hàng", p.getTenKhachHang()}, {"Số điện thoại", p.getSdtKhachHang()},
                {"Biển số", p.getBienSo()}, {"Xe", p.getThongTinXe()},
                {"Số km", String.valueOf(p.getSoKm())}, {"Mức nhiên liệu", p.getMucNhienLieu() + "%"},
                {"Trạng thái", p.getTrangThai()}, {"Ưu tiên", p.getUuTien()},
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
                dichVu.addCell(new Phrase(d.getTenDichVu(), thuong));
                dichVu.addCell(new Phrase(d.getTenKyThuatVien() == null ? "Chờ phân công" : d.getTenKyThuatVien(), thuong));
                dichVu.addCell(new Phrase(d.getTrangThaiTienDo(), thuong));
                dichVu.addCell(new Phrase(tien.format(d.getTienCongThucTe()), thuong));
                tong += d.getTienCongThucTe();
            }
            tai.add(dichVu);
            tai.add(new Paragraph("Tổng tiền công: " + tien.format(tong) + " đ", dam));
            tai.close();
        }
    }

    /** dichVu: {tên, mã, thợ, trạng thái, tiền công}; phuTung: {tên, SKU, số lượng, giá bán, thành tiền}. */
    public static void xuatHoaDon(File tep, HoaDon hd, HoaDon.ThongTinPhieu tt, List<Object[]> dichVu, List<Object[]> phuTung)
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
                {"Khách hàng", tt.hoTen + " (" + tt.maKH + ")"}, {"Số điện thoại", tt.soDienThoai},
                {"Địa chỉ", tt.diaChi}, {"Email", tt.email},
                {"Biển số", tt.bienSo}, {"Xe", tt.xe}, {"Số VIN", tt.soVIN}, {"Mã phiếu", tt.maPhieu},
                {"Ngày lập", hd.getNgayLap() == null ? "" : hd.getNgayLap().format(DINH_DANG_GIO)},
                {"Hình thức thanh toán", hd.getHinhThucThanhToan()}
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
            for (Object[] d : dichVu) {
                for (int i = 0; i < 4; i++) {
                    bangDichVu.addCell(new Phrase(String.valueOf(d[i]), thuong));
                }
                bangDichVu.addCell(new Phrase(tien.format(d[4]), thuong));
            }
            tai.add(bangDichVu);

            tai.add(new Paragraph("Phụ tùng", dam));
            PdfPTable bangPhuTung = new PdfPTable(new float[] {3, 1.5f, 0.8f, 1.2f, 1.2f});
            bangPhuTung.setWidthPercentage(100);
            for (String cot : new String[] {"Tên phụ tùng", "Mã SKU", "Số lượng", "Giá bán (đ)", "Thành tiền (đ)"}) {
                bangPhuTung.addCell(new Phrase(cot, dam));
            }
            for (Object[] d : phuTung) {
                bangPhuTung.addCell(new Phrase(String.valueOf(d[0]), thuong));
                bangPhuTung.addCell(new Phrase(String.valueOf(d[1]), thuong));
                bangPhuTung.addCell(new Phrase(String.valueOf(d[2]), thuong));
                bangPhuTung.addCell(new Phrase(tien.format(d[3]), thuong));
                bangPhuTung.addCell(new Phrase(tien.format(d[4]), thuong));
            }
            tai.add(bangPhuTung);
            tai.add(new Paragraph(" ", thuong));

            String[][] tong = {
                {"Tổng tiền công", tien.format(hd.getTongTienCong()) + " đ"},
                {"Tổng tiền phụ tùng", tien.format(hd.getTongTienPhuTung()) + " đ"},
                {"Giảm giá" + (hd.getMaKM() == null ? "" : " (" + hd.getMaKM() + ")"), tien.format(hd.getTongTienGiam()) + " đ"},
                {"VAT (" + tien.format(hd.getThueSuat()) + "%)", tien.format(hd.getTongTienVAT()) + " đ"},
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
