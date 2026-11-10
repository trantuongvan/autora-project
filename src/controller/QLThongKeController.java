package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.ThongKe_Dao;
import entity.DoanhThuNgay;
import entity.DuLieuThongKe;
import entity.LuotXeNgay;
import entity.NangSuatKTV;
import entity.NhapXuatNgay;
import entity.PhuTung;
import entity.ThongKePhuTung;
import gui.QLThongKe;
import util.UIComponents;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class QLThongKeController {
    private final QLThongKe view;
    private final ThongKe_Dao dao = new ThongKe_Dao();
    private DuLieuThongKe duLieuHienTai;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat dfTien = new DecimalFormat("#,##0");

    public QLThongKeController(QLThongKe view) {
        this.view = view;
        initEvents();
        chonKyNhanh("THANG");
        taiVaHienThiBaoCao(false);
    }

    private void initEvents() {
        view.getBtnLocNgay().addActionListener(e -> { chonKyNhanh("NGAY"); taiVaHienThiBaoCao(true); });
        view.getBtnLocTuan().addActionListener(e -> { chonKyNhanh("TUAN"); taiVaHienThiBaoCao(true); });
        view.getBtnLocThang().addActionListener(e -> { chonKyNhanh("THANG"); taiVaHienThiBaoCao(true); });
        view.getBtnLocNam().addActionListener(e -> { chonKyNhanh("NAM"); taiVaHienThiBaoCao(true); });

        view.getBtnXemBaoCao().addActionListener(e -> taiVaHienThiBaoCao(true));
        view.getBtnXuatPDF().addActionListener(e -> xuatPDF());
        view.getBtnXuatExcel().addActionListener(e -> xuatExcel());
        view.getBtnIn().addActionListener(e -> inBaoCao());

        ganSuKienTimKiem(view.getFTimDoanhThu(), this::locBangDoanhThu);
        ganSuKienTimKiem(view.getFTimPhuTung(), this::locBangPhuTung);
        ganSuKienTimKiem(view.getFTimKTV(), this::locBangKTV);
    }

    private void ganSuKienTimKiem(UIComponents.IconField field, Runnable action) {
        if (field == null || field.getTextField() == null) return;
        field.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }

    private void chonKyNhanh(String loai) {
        LocalDate homNay = LocalDate.now();
        LocalDate tuNgay;
        switch (loai) {
            case "NGAY":
                tuNgay = homNay;
                break;
            case "TUAN":
                tuNgay = homNay.with(DayOfWeek.MONDAY);
                break;
            case "NAM":
                tuNgay = homNay.withDayOfYear(1);
                break;
            case "THANG":
            default:
                tuNgay = homNay.withDayOfMonth(1);
                break;
        }
        view.setKhoangThoiGian(tuNgay, homNay, loai);
    }

    /** Tính toán toàn bộ chỉ số nghiệp vụ báo cáo thống kê từ dữ liệu thô của DAO. */
    public DuLieuThongKe tinhToanDuLieuThongKe(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        // 1. Doanh thu
        List<DoanhThuNgay> dsNgay = dao.taiDoanhThuTheoNgay(tuNgay, denNgay);
        int tongHD = 0;
        double tongCong = 0, tongPT = 0, tongGiam = 0, tongVAT = 0, tongDT = 0;
        for (DoanhThuNgay d : dsNgay) {
            tongHD += d.getSoHoaDon();
            tongCong += d.getTienCong();
            tongPT += d.getTienPhuTung();
            tongGiam += d.getGiamGia();
            tongVAT += d.getTienVAT();
            tongDT += d.getTongThanhToan();
        }
        double tongGiaVonPhuTung = dao.taiTongGiaVonPhuTung(tuNgay, denNgay);
        double loiNhuan = (tongCong + tongPT - tongGiam) - tongGiaVonPhuTung;
        if (loiNhuan < 0) loiNhuan = 0;

        double tongTruocThue = tongCong + tongPT;
        double tyTrongCong = tongTruocThue > 0 ? Math.round((tongCong * 1000.0) / tongTruocThue) / 10.0 : 0;
        double tyTrongPT = tongTruocThue > 0 ? Math.round((100.0 - tyTrongCong) * 10.0) / 10.0 : 0;

        // 2. Phụ tùng
        double[] tongGTNhapXuat = new double[2];
        List<ThongKePhuTung> dsPTTho = dao.taiDanhSachPhuTungTho(tuNgay, denNgay, tongGTNhapXuat);
        List<ThongKePhuTung> dsPT = new ArrayList<>();
        double tongGiaTriTon = 0;
        int soMatHang = 0, tongSLNhap = 0, tongSLXuat = 0;

        for (ThongKePhuTung raw : dsPTTho) {
            PhuTung pt = raw.getPhuTung();
            int ton = raw.getTonHienTai();
            double gtTon = ton * pt.getGiaNhap();
            boolean canhBao = QLPhuTungController.kiemTraCanhBaoTon(pt);
            dsPT.add(new ThongKePhuTung(pt, raw.getSoLuongNhap(), raw.getSoLuongXuat(), ton, gtTon, canhBao));

            soMatHang++;
            tongGiaTriTon += gtTon;
            tongSLNhap += raw.getSoLuongNhap();
            tongSLXuat += raw.getSoLuongXuat();
        }
        int soPhieuNhap = dao.demSoPhieuNhap(tuNgay, denNgay);
        List<NhapXuatNgay> dsNXNgay = dao.taiNhapXuatTheoNgay(tuNgay, denNgay);

        // 3. Hiệu suất Kỹ thuật viên
        int[] luotXe = dao.taiSoLuotXeVaoXuongVaSuaXong(tuNgay, denNgay);
        int tongXe = luotXe[0];
        int xeXong = luotXe[1];

        List<NangSuatKTV> dsKTVTho = dao.taiDanhSachNangSuatKTVTho(tuNgay, denNgay);
        List<NangSuatKTV> dsKTV = new ArrayList<>();
        int tongHMXong = 0, tongHMDangLam = 0, ktvHoatDong = 0;

        for (NangSuatKTV raw : dsKTVTho) {
            if (raw.getSoXeThamGia() > 0) {
                ktvHoatDong++;
            }
            int hmXong = raw.getHangMucHoanThanh();
            int hmDang = raw.getHangMucDangThucHien();
            int tongHM = hmXong + hmDang;
            double tyLe = tongHM > 0 ? Math.round((hmXong * 1000.0) / tongHM) / 10.0 : 0.0;
            dsKTV.add(new NangSuatKTV(raw.getKyThuatVien(), raw.getSoXeThamGia(), hmXong, hmDang, raw.getTienCongMangLai(), tyLe));
            tongHMXong += hmXong;
            tongHMDangLam += hmDang;
        }
        List<LuotXeNgay> dsLuotNgay = dao.taiLuotXeTheoNgay(tuNgay, denNgay);

        double tyLeXeXong = tongXe > 0 ? Math.round((xeXong * 1000.0) / tongXe) / 10.0 : 0.0;
        double tbHangMuc = tongXe > 0 ? Math.round((tongHMXong * 10.0) / tongXe) / 10.0 : 0.0;
        int tongTatCaHM = tongHMXong + tongHMDangLam;
        double tyLeChung = tongTatCaHM > 0 ? Math.round((tongHMXong * 1000.0) / tongTatCaHM) / 10.0 : 0.0;

        return new DuLieuThongKe(
                tongDT, tongCong, tongPT, tongGiam, tongVAT, loiNhuan,
                tongHD, tyTrongCong, tyTrongPT, dsNgay,
                tongGiaTriTon, soMatHang, tongSLNhap, tongGTNhapXuat[0],
                tongSLXuat, tongGTNhapXuat[1], soPhieuNhap, dsPT, dsNXNgay,
                tongXe, xeXong, tyLeXeXong, tongHMXong, tongHMDangLam,
                tbHangMuc, ktvHoatDong, tyLeChung, dsKTV, dsLuotNgay
        );
    }

    public void taiVaHienThiBaoCao(boolean hienThongBaoRong) {
        LocalDate tuNgay;
        LocalDate denNgay;
        try {
            tuNgay = LocalDate.parse(view.getTxtTuNgay().getText().trim(), dtf);
            denNgay = LocalDate.parse(view.getTxtDenNgay().getText().trim(), dtf);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Định dạng ngày không hợp lệ (Chuẩn: dd/MM/yyyy).",
                    "Lỗi thời gian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (denNgay.isBefore(tuNgay)) {
            JOptionPane.showMessageDialog(view, "Ngày kết thúc không được nhỏ hơn ngày bắt đầu.",
                    "Lỗi thời gian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            duLieuHienTai = tinhToanDuLieuThongKe(tuNgay, denNgay);
            view.capNhatGiaoDien(duLieuHienTai, tuNgay, denNgay);
            locBangDoanhThu();
            locBangPhuTung();
            locBangKTV();

            boolean khongCoGiaoDich = duLieuHienTai.getDanhSachDoanhThuNgay().isEmpty()
                    && duLieuHienTai.getTongLuotXeVaoXuong() == 0
                    && duLieuHienTai.getSoPhieuNhapTrongKy() == 0;
            if (hienThongBaoRong && khongCoGiaoDich) {
                JOptionPane.showMessageDialog(view,
                        "Không có dữ liệu giao dịch trong khoảng thời gian này.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi truy xuất dữ liệu thống kê:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void locBangDoanhThu() {
        if (duLieuHienTai == null) return;
        String kw = layTuKhoaTimKiem(view.getFTimDoanhThu());
        DefaultTableModel model = view.getModelDoanhThu();
        model.setRowCount(0);

        List<DoanhThuNgay> dsDT = duLieuHienTai.getDanhSachDoanhThuNgay();
        int demNgay = 0;
        int tongHD = 0;
        double tongCong = 0, tongPT = 0, tongGiam = 0, tongVAT = 0, tongThanhToan = 0;

        for (DoanhThuNgay d : dsDT) {
            String chuoiNgay = d.getNgay().format(dtf);
            String chuoiTong = dfTien.format(d.getTongThanhToan());
            if (!kw.isEmpty() && !chuoiNgay.toLowerCase().contains(kw)
                    && !String.valueOf(d.getSoHoaDon()).contains(kw)
                    && !chuoiTong.contains(kw)) {
                continue;
            }
            model.addRow(new Object[]{
                    chuoiNgay,
                    d.getSoHoaDon(),
                    dfTien.format(d.getTienCong()) + "đ",
                    dfTien.format(d.getTienPhuTung()) + "đ",
                    dfTien.format(d.getGiamGia()) + "đ",
                    dfTien.format(d.getTienVAT()) + "đ",
                    chuoiTong + "đ"
            });
            demNgay++;
            tongHD += d.getSoHoaDon();
            tongCong += d.getTienCong();
            tongPT += d.getTienPhuTung();
            tongGiam += d.getGiamGia();
            tongVAT += d.getTienVAT();
            tongThanhToan += d.getTongThanhToan();
        }

        model.addRow(new Object[]{
                "Tổng kỳ (" + demNgay + " ngày)",
                tongHD,
                dfTien.format(tongCong) + "đ",
                dfTien.format(tongPT) + "đ",
                dfTien.format(tongGiam) + "đ",
                dfTien.format(tongVAT) + "đ",
                dfTien.format(tongThanhToan) + "đ"
        });
        view.getLblFooterDoanhThu().setText("Hiển thị " + demNgay + " / " + dsDT.size() + " ngày có giao dịch");
    }

    public void locBangPhuTung() {
        if (duLieuHienTai == null) return;
        String kw = layTuKhoaTimKiem(view.getFTimPhuTung());
        DefaultTableModel model = view.getModelPhuTung();
        model.setRowCount(0);
        List<Boolean> canhBao = view.getCanhBaoPhuTungHienThi();
        canhBao.clear();

        List<ThongKePhuTung> dsPT = duLieuHienTai.getDanhSachPhuTung();
        int dem = 0;
        int tongNhap = 0, tongXuat = 0;
        double tongGiaTriTon = 0;

        for (ThongKePhuTung item : dsPT) {
            String ma = item.getPhuTung().getMaSKU() == null ? "" : item.getPhuTung().getMaSKU();
            String ten = item.getPhuTung().getTenPhuTung() == null ? "" : item.getPhuTung().getTenPhuTung();
            String hang = item.getPhuTung().getHangSanXuat() == null ? "" : item.getPhuTung().getHangSanXuat();

            if (!kw.isEmpty() && !ma.toLowerCase().contains(kw)
                    && !ten.toLowerCase().contains(kw)
                    && !hang.toLowerCase().contains(kw)) {
                continue;
            }

            model.addRow(new Object[]{
                    ma,
                    ten,
                    hang,
                    item.getSoLuongNhap(),
                    item.getSoLuongXuat(),
                    item.getTonHienTai(),
                    dfTien.format(item.getGiaTriTon()) + "đ"
            });
            canhBao.add(item.isDuoiNguongCanhBao());
            dem++;
            tongNhap += item.getSoLuongNhap();
            tongXuat += item.getSoLuongXuat();
            tongGiaTriTon += item.getGiaTriTon();
        }

        model.addRow(new Object[]{
                "Tổng kỳ báo cáo", "", "",
                tongNhap,
                tongXuat,
                "",
                dfTien.format(tongGiaTriTon) + "đ"
        });
        view.getLblFooterPhuTung().setText("Hiển thị " + dem + " / " + dsPT.size() + " mặt hàng");
    }

    public void locBangKTV() {
        if (duLieuHienTai == null) return;
        String kw = layTuKhoaTimKiem(view.getFTimKTV());
        DefaultTableModel model = view.getModelKTV();
        model.setRowCount(0);

        List<NangSuatKTV> dsKTV = duLieuHienTai.getDanhSachNangSuatKTV();
        int dem = 0;
        int tongHMXong = 0, tongHMDangLam = 0;
        double tongTienCong = 0;

        for (NangSuatKTV k : dsKTV) {
            String ten = k.getKyThuatVien().getHoTen() == null ? "" : k.getKyThuatVien().getHoTen();
            String ma = k.getKyThuatVien().getMaNV() == null ? "" : k.getKyThuatVien().getMaNV();

            if (!kw.isEmpty() && !ten.toLowerCase().contains(kw) && !ma.toLowerCase().contains(kw)) {
                continue;
            }

            model.addRow(new Object[]{
                    ten,
                    k.getSoXeThamGia(),
                    k.getHangMucHoanThanh(),
                    k.getHangMucDangThucHien(),
                    dfTien.format(k.getTienCongMangLai()) + "đ",
                    k.getTyLeHoanThanh() + "%"
            });
            dem++;
            tongHMXong += k.getHangMucHoanThanh();
            tongHMDangLam += k.getHangMucDangThucHien();
            tongTienCong += k.getTienCongMangLai();
        }

        int tongHM = tongHMXong + tongHMDangLam;
        double tyLeChung = tongHM > 0 ? Math.round((tongHMXong * 1000.0) / tongHM) / 10.0 : 0.0;

        model.addRow(new Object[]{
                "Tổng kỳ báo cáo",
                kw.isEmpty() ? duLieuHienTai.getTongXeSuaXong() : "-",
                tongHMXong,
                tongHMDangLam,
                dfTien.format(tongTienCong) + "đ",
                tyLeChung + "%"
        });
        view.getLblFooterKTV().setText("Hiển thị " + dem + " / " + dsKTV.size() + " kỹ thuật viên");
    }

    private String layTuKhoaTimKiem(UIComponents.IconField field) {
        if (field == null || field.getTextField() == null) return "";
        String val = field.getTextField().getText().trim();
        Object ph = field.getTextField().getClientProperty("placeholder");
        if (ph != null && val.equalsIgnoreCase(ph.toString())) return "";
        if (val.startsWith("Tìm ")) return "";
        return val.toLowerCase();
    }

    private void xuatPDF() {
        if (duLieuHienTai == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo thống kê PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("BaoCaoThongKe_AUTORA.pdf"));

        if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 16, Font.BOLD);
            Font fontSub = new Font(bf, 11, Font.NORMAL);
            Font fontHead = new Font(bf, 10, Font.BOLD);
            Font fontCell = new Font(bf, 10, Font.NORMAL);

            int tab = view.getTabHienTai();
            String tenBaoCao = tab == 0 ? "BÁO CÁO THỐNG KÊ DOANH THU"
                    : tab == 1 ? "BÁO CÁO NHẬP - XUẤT - TỒN PHỤ TÙNG"
                    : "BÁO CÁO HIỆU SUẤT KỸ THUẬT VIÊN";

            Paragraph pTitle = new Paragraph("GARAGE Ô TÔ AUTORA - " + tenBaoCao, fontTitle);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitle);

            Paragraph pKy = new Paragraph("Kỳ báo cáo: " + view.getTxtTuNgay().getText()
                    + " đến " + view.getTxtDenNgay().getText(), fontSub);
            pKy.setAlignment(Element.ALIGN_CENTER);
            pKy.setSpacingAfter(14);
            doc.add(pKy);

            JTable table = view.getBangHienTai();
            PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
            pdfTable.setWidthPercentage(100);

            for (int c = 0; c < table.getColumnCount(); c++) {
                PdfPCell cell = new PdfPCell(new Phrase(table.getColumnName(c), fontHead));
                cell.setBackgroundColor(new BaseColor(241, 245, 249));
                cell.setPadding(6);
                pdfTable.addCell(cell);
            }

            for (int r = 0; r < table.getRowCount(); r++) {
                for (int c = 0; c < table.getColumnCount(); c++) {
                    Object val = table.getValueAt(r, c);
                    PdfPCell cell = new PdfPCell(new Phrase(val == null ? "" : val.toString(), fontCell));
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }
            }

            doc.add(pdfTable);
            doc.close();
            JOptionPane.showMessageDialog(view, "Đã xuất báo cáo PDF thành công:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Không thể xuất file PDF: " + ex.getMessage(),
                    "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatExcel() {
        if (duLieuHienTai == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xls, *.csv)", "xls", "csv"));
        chooser.setSelectedFile(new File("BaoCaoThongKe_AUTORA.xls"));

        if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xls") && !file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".xls");
        }

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.write('\ufeff');
            JTable table = view.getBangHienTai();
            for (int c = 0; c < table.getColumnCount(); c++) {
                out.print(table.getColumnName(c) + (c == table.getColumnCount() - 1 ? "" : "\t"));
            }
            out.println();
            for (int r = 0; r < table.getRowCount(); r++) {
                for (int c = 0; c < table.getColumnCount(); c++) {
                    Object val = table.getValueAt(r, c);
                    out.print((val == null ? "" : val.toString()) + (c == table.getColumnCount() - 1 ? "" : "\t"));
                }
                out.println();
            }
            JOptionPane.showMessageDialog(view, "Đã xuất báo cáo Excel thành công:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Không thể xuất file Excel: " + ex.getMessage(),
                    "Lỗi xuất Excel", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inBaoCao() {
        try {
            view.getBangHienTai().print(JTable.PrintMode.FIT_WIDTH);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(view, "Không thể in báo cáo: " + ex.getMessage(),
                    "Lỗi in", JOptionPane.ERROR_MESSAGE);
        }
    }
}
