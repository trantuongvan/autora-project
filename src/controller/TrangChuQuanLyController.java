package controller;

import dao.PhuTung_Dao;
import dao.TrangChuQuanLy_Dao;
import entity.DoanhThuNgay;
import entity.PhuTung;
import entity.ThongKeChoThanhToan;
import gui.TrangChuQuanLy;
import util.XuatFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Nạp số liệu trang chủ quản lý; mọi con số lấy từ CSDL, ngày không có hóa đơn hiển thị là 0. */
public class TrangChuQuanLyController {
    private static final int SO_NGAY_TOI_DA = 62;
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter NGAY_THANG = DateTimeFormatter.ofPattern("dd/MM");
    private static final NumberFormat DINH_DANG_SO = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    private final TrangChuQuanLy view;
    private final DieuHuongPhieu dieuHuong;
    private final TrangChuQuanLy_Dao dao = new TrangChuQuanLy_Dao();
    private int phienBan;

    public TrangChuQuanLyController(TrangChuQuanLy view, DieuHuongPhieu dieuHuong) {
        this.view = view;
        this.dieuHuong = dieuHuong;
        initController();
        apDungKhoangThoiGian();
    }

    private void initController() {
        view.getCboKhoangThoiGian().addActionListener(e -> apDungKhoangThoiGian());
        view.getBtnLoc().addActionListener(e -> taiDuLieu());
        view.getBtnTiepNhan().addActionListener(e -> dieuHuong.moTiepNhanMoi());
        view.getBtnBaoCao().addActionListener(e -> dieuHuong.moBaoCao());
        view.getBtnXuatExcel().addActionListener(e -> xuatExcel());
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai");
        view.getActionMap().put("taiLai", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taiDuLieu();
            }
        });
    }

    private void apDungKhoangThoiGian() {
        LocalDate homNay = LocalDate.now();
        int chon = view.getCboKhoangThoiGian().getSelectedIndex();
        boolean tuyChon = chon == 2;
        view.getTxtTuNgay().setEnabled(tuyChon);
        view.getTxtDenNgay().setEnabled(tuyChon);
        if (!tuyChon) {
            LocalDate tu = chon == 0 ? homNay.with(DayOfWeek.MONDAY) : homNay.withDayOfMonth(1);
            view.getTxtTuNgay().setText(tu.format(DINH_DANG_NGAY));
            view.getTxtDenNgay().setText(homNay.format(DINH_DANG_NGAY));
            taiDuLieu();
        }
    }

    public void taiDuLieu() {
        LocalDate tu;
        LocalDate den;
        try {
            tu = LocalDate.parse(view.getTxtTuNgay().getText().trim(), DINH_DANG_NGAY);
            den = LocalDate.parse(view.getTxtDenNgay().getText().trim(), DINH_DANG_NGAY);
        } catch (DateTimeParseException ex) {
            view.hienLoiLoc("Ngày phải đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (tu.isAfter(den)) {
            view.hienLoiLoc("Từ ngày không được sau đến ngày.");
            return;
        }
        if (cacNgay(tu, den).size() > SO_NGAY_TOI_DA) {
            view.hienLoiLoc("Chỉ xem được tối đa " + SO_NGAY_TOI_DA + " ngày một lần.");
            return;
        }
        view.hienLoiLoc(null);

        int phien = ++phienBan;
        new SwingWorker<SoLieu, Void>() {
            @Override
            protected SoLieu doInBackground() throws SQLException {
                return docSoLieu(tu, den);
            }

            @Override
            protected void done() {
                if (phien != phienBan) {
                    return;
                }
                try {
                    hienThi(get(), tu, den);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(view, "Không thể tải dữ liệu tổng quan.\n" + ex.getCause().getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private SoLieu docSoLieu(LocalDate tu, LocalDate den) throws SQLException {
        LocalDate homNay = LocalDate.now();
        LocalDate thangTruoc = homNay.minusMonths(1);
        SoLieu s = new SoLieu();
        s.homNay = dao.doanhThuNgay(homNay);
        s.homQua = dao.doanhThuNgay(homNay.minusDays(1));
        s.thangNay = dao.doanhThuThang(homNay.getYear(), homNay.getMonthValue());
        s.thangTruoc = dao.doanhThuThang(thangTruoc.getYear(), thangTruoc.getMonthValue());
        s.luotXe = dao.luotXeHomNay();
        s.hoaDonCho = dao.hoaDonChoThanhToan();
        s.tyTrong = dao.tyTrongThang(homNay.getYear(), homNay.getMonthValue());
        s.bayNgay = dao.doanhThuTheoNgay(homNay.minusDays(6), homNay);
        s.bang = dao.bangDoanhThu(tu, den);
        s.tonKho = new PhuTung_Dao().findDuoiNguongCanhBao("");
        return s;
    }

    private void hienThi(SoLieu s, LocalDate tu, LocalDate den) {
        LocalDate homNay = LocalDate.now();
        int thang = homNay.getMonthValue();
        int thangTruoc = homNay.minusMonths(1).getMonthValue();
        view.setNgay(tenThu(homNay) + ", " + homNay.format(DINH_DANG_NGAY));
        view.setDoanhThuNgay(tien(s.homNay), moTaXuHuong(s.homNay, s.homQua, "hôm qua"));
        view.setDoanhThuThang("Doanh thu tháng " + thang, tien(s.thangNay), moTaXuHuong(s.thangNay, s.thangTruoc, "tháng " + thangTruoc));
        view.setLuotXe(s.luotXe[0], s.luotXe[1], s.luotXe[2]);
        view.setHoaDonCho(s.hoaDonCho.getSoLuong(), tien(s.hoaDonCho.getTongTien()) + " công nợ");

        double tong = s.tyTrong[0] + s.tyTrong[1];
        view.setTyTrong("Tỷ trọng doanh thu - Tháng " + thang, s.tyTrong[0], s.tyTrong[1],
                String.format(Locale.forLanguageTag("vi-VN"), "%.1f tr", tong / 1_000_000),
                phanTram(s.tyTrong[0], tong) + "% - " + tien(s.tyTrong[0]),
                phanTram(s.tyTrong[1], tong) + "% - " + tien(s.tyTrong[1]));

        List<String> nhan = new ArrayList<>();
        List<Double> giaTri = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate ngay = homNay.minusDays(i);
            nhan.add(ngay.format(NGAY_THANG));
            giaTri.add(s.bayNgay.getOrDefault(ngay, 0.0) / 1_000_000);
        }
        view.setBieuDoCot(nhan, giaTri);

        view.setBangDoanhThu("Doanh thu từ " + tu.format(NGAY_THANG) + " đến " + den.format(NGAY_THANG), dongDoanhThu(s.bang, tu, den));
        List<Object[]> tonKho = new ArrayList<>();
        for (PhuTung pt : s.tonKho) {
            tonKho.add(new Object[] {pt.getMaSKU(), pt.getTenPhuTung(), pt.getSoLuongTon(), pt.getNguongCanhBao(), "Cần nhập hàng"});
        }
        view.setBangTonKho(tonKho);
    }

    private static List<Object[]> dongDoanhThu(List<DoanhThuNgay> bang, LocalDate tu, LocalDate den) {
        Map<LocalDate, DoanhThuNgay> theoNgay = new HashMap<>();
        for (DoanhThuNgay r : bang) {
            theoNgay.put(r.getNgay(), r);
        }
        int tongPhieu = 0;
        double tongCong = 0;
        double tongPhuTung = 0;
        double tongVAT = 0;
        double tongTien = 0;
        List<Object[]> dong = new ArrayList<>();
        for (LocalDate ngay : cacNgay(tu, den)) {
            DoanhThuNgay r = theoNgay.getOrDefault(ngay, new DoanhThuNgay(ngay, 0, 0, 0, 0, 0));
            tongPhieu += r.getSoPhieu();
            tongCong += r.getTienCong();
            tongPhuTung += r.getTienPhuTung();
            tongVAT += r.getTienVAT();
            tongTien += r.getTongTien();
            dong.add(new Object[] {ngay.format(DINH_DANG_NGAY), r.getSoPhieu(), DINH_DANG_SO.format(r.getTienCong()),
                DINH_DANG_SO.format(r.getTienPhuTung()), DINH_DANG_SO.format(r.getTienVAT()), DINH_DANG_SO.format(r.getTongTien())});
        }
        dong.add(new Object[] {"Tổng cộng", tongPhieu, DINH_DANG_SO.format(tongCong), DINH_DANG_SO.format(tongPhuTung),
            DINH_DANG_SO.format(tongVAT), DINH_DANG_SO.format(tongTien)});
        return dong;
    }

    private void xuatExcel() {
        List<Object[]> dong = new ArrayList<>();
        for (int i = 0; i < view.getModelDoanhThu().getRowCount(); i++) {
            Object[] r = new Object[TrangChuQuanLy.COT_DOANH_THU.length];
            for (int j = 0; j < r.length; j++) {
                r[j] = view.getModelDoanhThu().getValueAt(i, j);
            }
            dong.add(r);
        }
        XuatFile.xuatExcel(view, "doanh-thu", TrangChuQuanLy.COT_DOANH_THU, dong);
    }

    private static String tien(double soTien) {
        return DINH_DANG_SO.format(soTien) + " đ";
    }

    private static long phanTram(double phan, double tong) {
        return tong == 0 ? 0 : Math.round(phan * 100 / tong);
    }

    private static String moTaXuHuong(double hienTai, double truoc, String moc) {
        String xuHuong = xuHuong(hienTai, truoc);
        return xuHuong == null ? "Chưa có dữ liệu " + moc : xuHuong + " so với " + moc;
    }

    /** Trả về null khi kỳ trước bằng 0 vì không tính được phần trăm. */
    public static String xuHuong(double hienTai, double truoc) {
        if (truoc == 0) {
            return null;
        }
        long phanTram = Math.round((hienTai - truoc) * 100 / truoc);
        return (phanTram >= 0 ? "▲ " : "▼ ") + Math.abs(phanTram) + "%";
    }

    public static String tenThu(LocalDate ngay) {
        String[] ten = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        return ten[ngay.getDayOfWeek().getValue() - 1];
    }

    /** Danh sách ngày từ mới đến cũ. */
    public static List<LocalDate> cacNgay(LocalDate tu, LocalDate den) {
        List<LocalDate> ngay = new ArrayList<>();
        for (LocalDate d = den; !d.isBefore(tu); d = d.minusDays(1)) {
            ngay.add(d);
            // Dừng sớm: khoảng ngày do người dùng nhập có thể rất dài.
            if (ngay.size() > SO_NGAY_TOI_DA) {
                break;
            }
        }
        return ngay;
    }

    private static class SoLieu {
        double homNay;
        double homQua;
        double thangNay;
        double thangTruoc;
        int[] luotXe;
        ThongKeChoThanhToan hoaDonCho;
        double[] tyTrong;
        Map<LocalDate, Double> bayNgay;
        List<DoanhThuNgay> bang;
        List<PhuTung> tonKho;
    }
}
