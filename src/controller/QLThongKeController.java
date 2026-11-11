package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.ThongKe_Dao;
import entity.DoanhThuNgay;
import entity.DuLieuThongKe;
import entity.NangSuatKTV;
import entity.ThongKePhuTung;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class QLThongKeController {
    private final QLThongKe view;
    private final ThongKe_Dao dao = new ThongKe_Dao();
    private DuLieuThongKe duLieuHienTai;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dtfNgan = DateTimeFormatter.ofPattern("dd/MM");
    private final DecimalFormat dfTien = new DecimalFormat("#,##0");
    private ChiSo chiSo;
    private String chuoiKyNgan = "";

    /** Các con số báo cáo tính từ số liệu đã đọc của kỳ: tổng, tỷ trọng, lợi nhuận, tồn kho và năng suất. */
    public static final class ChiSo {
        public final int tongSoHoaDon;
        public final double tongDoanhThu, tongTienCong, tongTienPhuTung, tongGiamGia, tongVAT;
        public final double tyTrongTienCong, tyTrongPhuTung, loiNhuan;
        public final int soMatHang, tongSoLuongNhap, tongSoLuongXuat;
        public final double tongGiaTriNhap, tongGiaTriXuat, giaTriTonKho;
        public final int tongHangMucXong, tongHangMucDangLam, soKyThuatVienHoatDong;
        public final double tyLeXeSuaXong, trungBinhHangMucTrenXe, tyLeHoanThanhChung;

        ChiSo(DuLieuThongKe dl) {
            int soHD = 0;
            double cong = 0, phuTung = 0, giam = 0, vat = 0, tong = 0;
            for (DoanhThuNgay d : dl.getDanhSachDoanhThuNgay()) {
                soHD += d.getSoHoaDon();
                cong += d.getTienCong();
                phuTung += d.getTienPhuTung();
                giam += d.getGiamGia();
                vat += d.getTienVAT();
                tong += d.getTongThanhToan();
            }
            tongSoHoaDon = soHD;
            tongTienCong = cong;
            tongTienPhuTung = phuTung;
            tongGiamGia = giam;
            tongVAT = vat;
            tongDoanhThu = tong;
            tyTrongTienCong = phanTram(cong, cong + phuTung);
            tyTrongPhuTung = cong + phuTung > 0 ? Math.round((100.0 - tyTrongTienCong) * 10.0) / 10.0 : 0;
            // Lợi nhuận gộp có thể âm; giá vốn tính theo giá nhập hiện tại của phụ tùng.
            loiNhuan = cong + phuTung - giam - dl.getTongGiaVonPhuTung();

            int slNhap = 0, slXuat = 0;
            double gtNhap = 0, gtXuat = 0, gtTon = 0;
            for (ThongKePhuTung t : dl.getDanhSachPhuTung()) {
                slNhap += t.getSoLuongNhap();
                slXuat += t.getSoLuongXuat();
                gtNhap += t.getGiaTriNhap();
                gtXuat += t.getGiaTriXuat();
                gtTon += NghiepVuKho.giaTriTon(t.getPhuTung());
            }
            soMatHang = dl.getDanhSachPhuTung().size();
            tongSoLuongNhap = slNhap;
            tongSoLuongXuat = slXuat;
            tongGiaTriNhap = gtNhap;
            tongGiaTriXuat = gtXuat;
            giaTriTonKho = gtTon;

            int xong = 0, dang = 0, hoatDong = 0;
            for (NangSuatKTV k : dl.getDanhSachNangSuatKTV()) {
                xong += k.getHangMucHoanThanh();
                dang += k.getHangMucDangThucHien();
                if (k.getSoXeThamGia() > 0) {
                    hoatDong++;
                }
            }
            tongHangMucXong = xong;
            tongHangMucDangLam = dang;
            soKyThuatVienHoatDong = hoatDong;
            tyLeXeSuaXong = phanTram(dl.getTongXeSuaXong(), dl.getTongLuotXeVaoXuong());
            trungBinhHangMucTrenXe = dl.getTongLuotXeVaoXuong() > 0
                    ? Math.round(xong * 10.0 / dl.getTongLuotXeVaoXuong()) / 10.0 : 0;
            tyLeHoanThanhChung = phanTram(xong, xong + dang);
        }
    }

    public static ChiSo tinhChiSo(DuLieuThongKe dl) {
        return new ChiSo(dl);
    }

    /** Phần trăm hạng mục đã xong trên tổng hạng mục được giao của một kỹ thuật viên. */
    public static double tyLeHoanThanh(NangSuatKTV k) {
        return phanTram(k.getHangMucHoanThanh(), k.getHangMucHoanThanh() + k.getHangMucDangThucHien());
    }

    /** Phần trăm làm tròn 1 chữ số; mẫu bằng 0 thì 0. */
    static double phanTram(double tu, double mau) {
        return mau > 0 ? Math.round(tu * 1000.0 / mau) / 10.0 : 0;
    }

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
        util.UIComponents.tuTaiLai(this::locBangDoanhThu, view.getTxtTimKiem(0));
        util.UIComponents.tuTaiLai(this::locBangPhuTung, view.getTxtTimKiem(1));
        util.UIComponents.tuTaiLai(this::locBangKTV, view.getTxtTimKiem(2));
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
            chiSo = tinhChiSo(duLieuHienTai);
            chuoiKyNgan = "Kỳ " + tuNgay.format(dtfNgan) + " – " + denNgay.format(dtfNgan);
            view.capNhatGiaoDien(duLieuHienTai, chiSo.tongTienCong, chiSo.tongTienPhuTung, chiSo.tyTrongTienCong,
                    chiSo.tyTrongPhuTung, tuNgay, denNgay);
            locBangDoanhThu();
            locBangPhuTung();
            locBangKTV();
            view.setKpi(kpi());

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

    private String tien(double soTien) {
        return dfTien.format(soTien) + "đ";
    }

    /** 4 thẻ tóm tắt cho mỗi tab: doanh thu, phụ tùng, hiệu suất kỹ thuật viên. */
    private String[][][] kpi() {
        DuLieuThongKe dl = duLieuHienTai;
        return new String[][][] {
            {
                {"Tổng doanh thu", tien(chiSo.tongDoanhThu), "Sau giảm giá và VAT"},
                {"Tiền công dịch vụ", tien(chiSo.tongTienCong), chiSo.tyTrongTienCong + "% doanh thu"},
                {"Tiền phụ tùng", tien(chiSo.tongTienPhuTung), chiSo.tyTrongPhuTung + "% doanh thu"},
                {"Lợi nhuận ước tính", tien(chiSo.loiNhuan), chuoiKyNgan + " • giá vốn theo giá nhập hiện tại"}
            },
            {
                {"Giá trị tồn kho hiện tại", tien(chiSo.giaTriTonKho), chiSo.soMatHang + " mặt hàng đang quản lý"},
                {"Số lượng nhập", String.valueOf(chiSo.tongSoLuongNhap), "Giá trị " + tien(chiSo.tongGiaTriNhap)},
                {"Số lượng xuất", String.valueOf(chiSo.tongSoLuongXuat), "Giá trị " + tien(chiSo.tongGiaTriXuat)},
                {"Phiếu nhập trong kỳ", String.valueOf(dl.getSoPhieuNhapTrongKy()), chuoiKyNgan}
            },
            {
                {"Lượt xe vào xưởng", String.valueOf(dl.getTongLuotXeVaoXuong()), "Tiếp nhận trong kỳ"},
                {"Xe sửa xong", String.valueOf(dl.getTongXeSuaXong()), chiSo.tyLeXeSuaXong + "% lượt tiếp nhận"},
                {"Hạng mục hoàn thành", String.valueOf(chiSo.tongHangMucXong), "Trung bình " + chiSo.trungBinhHangMucTrenXe + " hạng mục/xe"},
                {"Kỹ thuật viên hoạt động", String.valueOf(chiSo.soKyThuatVienHoatDong), "Có phiếu trong kỳ"}
            }
        };
    }

    private void locBangDoanhThu() {
        if (duLieuHienTai == null) return;
        String kw = view.getTuKhoa(0);
        List<DoanhThuNgay> dsDT = duLieuHienTai.getDanhSachDoanhThuNgay();
        List<Object[]> dong = new ArrayList<>();
        int tongHD = 0;
        double tongCong = 0, tongPT = 0, tongGiam = 0, tongVAT = 0, tongThanhToan = 0;
        for (DoanhThuNgay d : dsDT) {
            String chuoiNgay = d.getNgay().format(dtf);
            String chuoiTong = dfTien.format(d.getTongThanhToan());
            if (!kw.isEmpty() && !chuoiNgay.toLowerCase().contains(kw)
                    && !String.valueOf(d.getSoHoaDon()).contains(kw) && !chuoiTong.contains(kw)) {
                continue;
            }
            dong.add(new Object[]{chuoiNgay, d.getSoHoaDon(), tien(d.getTienCong()), tien(d.getTienPhuTung()),
                    tien(d.getGiamGia()), tien(d.getTienVAT()), chuoiTong + "đ"});
            tongHD += d.getSoHoaDon();
            tongCong += d.getTienCong();
            tongPT += d.getTienPhuTung();
            tongGiam += d.getGiamGia();
            tongVAT += d.getTienVAT();
            tongThanhToan += d.getTongThanhToan();
        }
        int soNgay = dong.size();
        dong.add(new Object[]{"Tổng kỳ (" + soNgay + " ngày)", tongHD, tien(tongCong), tien(tongPT), tien(tongGiam),
                tien(tongVAT), tien(tongThanhToan)});
        view.setBangDoanhThu(dong, "Hiển thị " + soNgay + " / " + dsDT.size() + " ngày có giao dịch");
    }

    private void locBangPhuTung() {
        if (duLieuHienTai == null) return;
        String kw = view.getTuKhoa(1);
        List<ThongKePhuTung> dsPT = duLieuHienTai.getDanhSachPhuTung();
        List<Object[]> dong = new ArrayList<>();
        List<Boolean> canhBao = new ArrayList<>();
        int tongNhap = 0, tongXuat = 0;
        double tongGiaTriTon = 0;
        for (ThongKePhuTung item : dsPT) {
            String ma = item.getPhuTung().getMaSKU() == null ? "" : item.getPhuTung().getMaSKU();
            String ten = item.getPhuTung().getTenPhuTung() == null ? "" : item.getPhuTung().getTenPhuTung();
            String hang = item.getPhuTung().getHangSanXuat() == null ? "" : item.getPhuTung().getHangSanXuat();
            if (!kw.isEmpty() && !ma.toLowerCase().contains(kw) && !ten.toLowerCase().contains(kw)
                    && !hang.toLowerCase().contains(kw)) {
                continue;
            }
            double giaTriTon = NghiepVuKho.giaTriTon(item.getPhuTung());
            dong.add(new Object[]{ma, ten, hang, item.getSoLuongNhap(), item.getSoLuongXuat(),
                    item.getPhuTung().getSoLuongTon(), tien(giaTriTon)});
            canhBao.add(NghiepVuKho.duoiNguongCanhBao(item.getPhuTung()));
            tongNhap += item.getSoLuongNhap();
            tongXuat += item.getSoLuongXuat();
            tongGiaTriTon += giaTriTon;
        }
        int soDong = dong.size();
        dong.add(new Object[]{"Tổng kỳ báo cáo", "", "", tongNhap, tongXuat, "", tien(tongGiaTriTon)});
        view.setBangPhuTung(dong, canhBao, "Hiển thị " + soDong + " / " + dsPT.size() + " mặt hàng");
    }

    private void locBangKTV() {
        if (duLieuHienTai == null) return;
        String kw = view.getTuKhoa(2);
        List<NangSuatKTV> dsKTV = duLieuHienTai.getDanhSachNangSuatKTV();
        List<Object[]> dong = new ArrayList<>();
        int tongHMXong = 0, tongHMDangLam = 0;
        double tongTienCong = 0;
        for (NangSuatKTV k : dsKTV) {
            String ten = k.getKyThuatVien().getHoTen() == null ? "" : k.getKyThuatVien().getHoTen();
            String ma = k.getKyThuatVien().getMaNV() == null ? "" : k.getKyThuatVien().getMaNV();
            if (!kw.isEmpty() && !ten.toLowerCase().contains(kw) && !ma.toLowerCase().contains(kw)) {
                continue;
            }
            dong.add(new Object[]{ten, k.getSoXeThamGia(), k.getHangMucHoanThanh(), k.getHangMucDangThucHien(),
                    tien(k.getTienCongMangLai()), tyLeHoanThanh(k) + "%"});
            tongHMXong += k.getHangMucHoanThanh();
            tongHMDangLam += k.getHangMucDangThucHien();
            tongTienCong += k.getTienCongMangLai();
        }
        int soDong = dong.size();
        dong.add(new Object[]{"Tổng kỳ báo cáo", kw.isEmpty() ? duLieuHienTai.getTongXeSuaXong() : "-", tongHMXong,
                tongHMDangLam, tien(tongTienCong), phanTram(tongHMXong, tongHMXong + tongHMDangLam) + "%"});
        view.setBangKTV(dong, "Hiển thị " + soDong + " / " + dsKTV.size() + " kỹ thuật viên");
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