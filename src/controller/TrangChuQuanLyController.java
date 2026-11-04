package controller;

import dao.TrangChuQuanLy_Dao;
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
        s.tonKho = dao.canhBaoTonKho();
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
        view.setHoaDonCho((Integer) s.hoaDonCho[0], tien((Double) s.hoaDonCho[1]) + " công nợ");

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
        for (Object[] r : s.tonKho) {
            tonKho.add(new Object[] {r[0], r[1], r[2], r[3], "Cần nhập hàng"});
        }
        view.setBangTonKho(tonKho);
    }

    private static List<Object[]> dongDoanhThu(List<Object[]> bang, LocalDate tu, LocalDate den) {
        Map<LocalDate, Object[]> theoNgay = new HashMap<>();
        for (Object[] r : bang) {
            theoNgay.put((LocalDate) r[0], r);
        }
        int tongPhieu = 0;
        double[] tong = new double[4];
        List<Object[]> dong = new ArrayList<>();
        for (LocalDate ngay : cacNgay(tu, den)) {
            Object[] r = theoNgay.get(ngay);
            int soPhieu = r == null ? 0 : (Integer) r[1];
            Object[] hienThi = {ngay.format(DINH_DANG_NGAY), soPhieu, "", "", "", ""};
            tongPhieu += soPhieu;
            for (int i = 0; i < 4; i++) {
                double giaTri = r == null ? 0 : (Double) r[i + 2];
                tong[i] += giaTri;
                hienThi[i + 2] = DINH_DANG_SO.format(giaTri);
            }
            dong.add(hienThi);
        }
        dong.add(new Object[] {"Tổng cộng", tongPhieu, DINH_DANG_SO.format(tong[0]), DINH_DANG_SO.format(tong[1]),
            DINH_DANG_SO.format(tong[2]), DINH_DANG_SO.format(tong[3])});
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
        Object[] hoaDonCho;
        double[] tyTrong;
        Map<LocalDate, Double> bayNgay;
        List<Object[]> bang;
        List<Object[]> tonKho;
    }
}
