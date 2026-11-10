package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.ThongKe_Dao;
import entity.DuLieuThongKe;
import gui.QLThongKe;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class QLThongKeController {
    private final QLThongKe view;
    private final ThongKe_Dao dao = new ThongKe_Dao();
    private DuLieuThongKe duLieuHienTai;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
            duLieuHienTai = dao.taiDuLieuThongKe(tuNgay, denNgay);
            view.capNhatGiaoDien(duLieuHienTai, tuNgay, denNgay);

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
        JTable table = view.getBangHienTai();
        String[] cot = new String[table.getColumnCount()];
        for (int c = 0; c < cot.length; c++) {
            cot[c] = table.getColumnName(c);
        }
        java.util.List<Object[]> dong = new java.util.ArrayList<>();
        for (int r = 0; r < table.getRowCount(); r++) {
            Object[] hang = new Object[cot.length];
            for (int c = 0; c < cot.length; c++) {
                hang[c] = table.getValueAt(r, c);
            }
            dong.add(hang);
        }
        util.XuatFile.xuatExcel(view, "bao-cao-thong-ke", cot, dong);
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