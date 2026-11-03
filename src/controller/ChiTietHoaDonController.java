package controller;

import com.itextpdf.text.DocumentException;
import dao.HoaDon_Dao;
import dao.Thue_Dao;
import entity.HoaDon;
import entity.KhuyenMai;
import entity.PhienDangNhap;
import entity.Thue;
import gui.ChiTietHoaDon;
import util.PdfExporter;
import util.VietQRUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Điều phối trang hóa đơn. Có maHD là xem hóa đơn; không có là lập hóa đơn cho maPhieu. */
public class ChiTietHoaDonController {
    private static final String THUE_MAC_DINH = "VAT08";
    private static final int CO_QR = 170;

    private final ChiTietHoaDon view;
    private final String maHD;
    private final String maPhieu;
    private final DieuHuongHoaDon dieuHuong;
    private final HoaDon_Dao dao = new HoaDon_Dao();
    private final NumberFormat dinhDangTien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    private HoaDon hoaDon;
    private double tongTienCong;
    private double tongTienPhuTung;
    private double tongHienTai;
    private String maQr;
    private int phienBanQr;

    public ChiTietHoaDonController(ChiTietHoaDon view, String maHD, String maPhieu, DieuHuongHoaDon dieuHuong) {
        this.view = view;
        this.maHD = maHD;
        this.maPhieu = maPhieu;
        this.dieuHuong = dieuHuong;
        view.setTieuDe(maHD != null ? "Chi tiết hóa đơn — " + maHD : "Lập hóa đơn — " + maPhieu);
        view.setHienThi(false, false, false, false, false, false);
        view.getBtnQuayLai().addActionListener(e -> dieuHuong.moDanhSachHoaDon());
        taiDuLieu();
    }

    /** Tiền khách đưa: số nguyên, cho phép dấu chấm ngăn cách hàng nghìn. */
    public static double docTien(String chuoi) {
        String s = chuoi == null ? "" : chuoi.trim();
        if (!s.matches("\\d{1,15}|\\d{1,3}(\\.\\d{3}){1,4}")) {
            throw new IllegalArgumentException("Tiền khách đưa phải là số, ví dụ 1.500.000.");
        }
        return Double.parseDouble(s.replace(".", ""));
    }

    private static final class DuLieu {
        HoaDon hoaDon;
        HoaDon.ThongTinPhieu phieu;
        List<Object[]> dichVu;
        List<Object[]> phuTung;
        List<KhuyenMai> khuyenMai = new ArrayList<>();
        List<Thue> thue = new ArrayList<>();
        String maDuDoan;
        String loi;
    }

    private void taiDuLieu() {
        new SwingWorker<DuLieu, Void>() {
            @Override
            protected DuLieu doInBackground() throws Exception {
                DuLieu d = new DuLieu();
                String ma = maPhieu;
                if (maHD != null) {
                    d.hoaDon = dao.findById(maHD);
                    if (d.hoaDon == null) {
                        d.loi = "Hóa đơn " + maHD + " không còn tồn tại.";
                        return d;
                    }
                    ma = d.hoaDon.getMaPhieuTNSC();
                }
                d.phieu = dao.findThongTinPhieu(ma);
                if (d.phieu == null) {
                    d.loi = "Phiếu " + ma + " không còn tồn tại.";
                    return d;
                }
                if (maHD == null) {
                    if (!"Hoàn thành".equals(d.phieu.trangThaiPhieu) || dao.coHoaDonHieuLuc(ma)) {
                        d.loi = "Phiếu " + ma + " chưa hoàn thành hoặc đã có hóa đơn.";
                        return d;
                    }
                    d.khuyenMai = dao.findKhuyenMaiHopLe();
                    d.thue = new Thue_Dao().findAll();
                    d.maDuDoan = dao.duDoanMaHD();
                }
                d.dichVu = dao.findDichVu(ma);
                d.phuTung = dao.findPhuTung(ma);
                return d;
            }

            @Override
            protected void done() {
                try {
                    DuLieu d = get();
                    if (d.loi != null) {
                        JOptionPane.showMessageDialog(view, d.loi, "Hóa đơn", JOptionPane.WARNING_MESSAGE);
                        dieuHuong.moDanhSachHoaDon();
                        return;
                    }
                    hienDuLieu(d);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Không thể tải dữ liệu hóa đơn.\n" + ex.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void hienDuLieu(DuLieu d) {
        hoaDon = d.hoaDon;
        HoaDon.ThongTinPhieu p = d.phieu;
        view.setThongTin(
                new String[] {p.hoTen + " (" + p.maKH + " — " + p.loaiKhachHang + ")", "SĐT: " + p.soDienThoai,
                    "Địa chỉ: " + rong(p.diaChi), "Email: " + rong(p.email)},
                new String[] {"Biển số: " + p.bienSo, p.xe, "VIN: " + rong(p.soVIN), "Mã phiếu: " + p.maPhieu},
                new String[] {
                    "Ngày lập: " + (hoaDon == null || hoaDon.getNgayLap() == null ? "—"
                            : hoaDon.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                    "Hình thức: " + (hoaDon == null ? "—" : hoaDon.getHinhThucThanhToan()),
                    "Mã khuyến mãi: " + (hoaDon == null ? "—" : rong(hoaDon.getMaKM()))},
                hoaDon == null ? null : hoaDon.getTrangThaiThanhToan());
        view.setChiTiet(dongDichVu(d.dichVu), dongPhuTung(d.phuTung));
        if (hoaDon == null) {
            hienCheDoLap(d);
        } else {
            hienCheDoXem();
        }
    }

    private static String rong(String s) {
        return s == null || s.trim().isEmpty() ? "—" : s;
    }

    private List<Object[]> dongDichVu(List<Object[]> nguon) {
        List<Object[]> dong = new ArrayList<>();
        for (Object[] d : nguon) {
            tongTienCong += (double) d[4];
            dong.add(new Object[] {d[0], d[1], d[2], d[3], tien(d[4])});
        }
        return dong;
    }

    private List<Object[]> dongPhuTung(List<Object[]> nguon) {
        List<Object[]> dong = new ArrayList<>();
        for (Object[] d : nguon) {
            tongTienPhuTung += (double) d[4];
            dong.add(new Object[] {d[0], d[1], d[2], tien(d[3]), tien(d[4])});
        }
        return dong;
    }

    private String tien(Object soTien) {
        return dinhDangTien.format(soTien) + "đ";
    }

    private void hienCheDoLap(DuLieu d) {
        maQr = d.maDuDoan;
        view.getBtnIn().setEnabled(false);
        view.getCboKhuyenMai().addItem(null);
        for (KhuyenMai km : d.khuyenMai) {
            view.getCboKhuyenMai().addItem(km);
        }
        for (Thue t : d.thue) {
            view.getCboThue().addItem(t);
            if (THUE_MAC_DINH.equals(t.getMaThue())) {
                view.getCboThue().setSelectedItem(t);
            }
        }
        view.getCboHinhThuc().addItem(HoaDon.TIEN_MAT);
        view.getCboHinhThuc().addItem(HoaDon.CHUYEN_KHOAN);
        if (d.phieu.duocGhiNo) {
            view.getCboHinhThuc().addItem(HoaDon.CONG_NO);
        }
        view.getCboKhuyenMai().addActionListener(e -> tinhLaiXemTruoc());
        view.getCboThue().addActionListener(e -> tinhLaiXemTruoc());
        ganSuKienThanhToan();
        view.getBtnXacNhan().addActionListener(e -> luuHoaDon());
        view.getBtnGhiCongNo().addActionListener(e -> luuHoaDon());
        tinhLaiXemTruoc();
    }

    private void hienCheDoXem() {
        tongHienTai = hoaDon.getTongThanhToan();
        maQr = hoaDon.getMaHD();
        view.getBtnIn().addActionListener(e -> inHoaDon(view, hoaDon.getMaHD()));
        hienTongHop(hoaDon.getTongTienCong(), hoaDon.getTongTienPhuTung(), hoaDon.getTongTienGiam(),
                hoaDon.getThueSuat(), hoaDon.getTongTienVAT(), hoaDon.getTongThanhToan());

        String trangThai = hoaDon.getTrangThaiThanhToan();
        if (HoaDon.CONG_NO.equals(trangThai)) {
            view.getCboHinhThuc().addItem(HoaDon.TIEN_MAT);
            view.getCboHinhThuc().addItem(HoaDon.CHUYEN_KHOAN);
            ganSuKienThanhToan();
            view.getBtnXacNhan().addActionListener(e -> thuCongNo());
            view.getBtnHuy().addActionListener(e -> huyHoaDon());
            capNhatTheoHinhThuc();
            return;
        }
        // Đã thanh toán hoặc đã hủy: chỉ xem; đã thanh toán vẫn được hủy khi xe chưa bàn giao.
        boolean daHuy = HoaDon.DA_HUY.equals(trangThai);
        view.getCboHinhThuc().addItem(hoaDon.getHinhThucThanhToan());
        view.getCboHinhThuc().setEnabled(false);
        view.hienChuQr("VietQR\n" + tien(tongHienTai));
        view.getTxtLyDoHuy().setText(daHuy ? hoaDon.getLyDoHuy() : "");
        view.getTxtLyDoHuy().setEditable(!daHuy);
        view.setHienThi(false, false, true, false, !daHuy, false);
        if (!daHuy) {
            view.getBtnHuy().addActionListener(e -> huyHoaDon());
        }
    }

    private void ganSuKienThanhToan() {
        view.getCboHinhThuc().addActionListener(e -> capNhatTheoHinhThuc());
        view.getTxtTienKhachDua().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { capNhatTienThua(); }
            @Override public void removeUpdate(DocumentEvent e) { capNhatTienThua(); }
            @Override public void changedUpdate(DocumentEvent e) { capNhatTienThua(); }
        });
    }

    /** Chỉ để xem trước; số tiền thật được tính lại khi lưu. */
    private void tinhLaiXemTruoc() {
        KhuyenMai km = (KhuyenMai) view.getCboKhuyenMai().getSelectedItem();
        Thue thue = (Thue) view.getCboThue().getSelectedItem();
        double thueSuat = thue == null ? 0 : thue.getThueSuat();
        HoaDon.TienHoaDon t = HoaDon.tinhTien(tongTienCong, tongTienPhuTung, km == null ? null : km.getHinhThuc(),
                km == null ? 0 : km.getGiaTri(), km == null ? null : km.getPhamViApDung(), thueSuat);
        tongHienTai = t.tong;
        hienTongHop(t.cong, t.phuTung, t.giam, thueSuat, t.vat, t.tong);
        capNhatTheoHinhThuc();
    }

    private void hienTongHop(double cong, double phuTung, double giam, double thueSuat, double vat, double tong) {
        view.setTongHop(tien(cong), tien(phuTung), tien(cong + phuTung), "− " + tien(giam),
                "VAT (" + dinhDangTien.format(thueSuat) + "%)", tien(vat), tien(tong));
    }

    private void capNhatTheoHinhThuc() {
        String hinhThuc = String.valueOf(view.getCboHinhThuc().getSelectedItem());
        boolean ghiNo = HoaDon.CONG_NO.equals(hinhThuc);
        boolean dangLap = hoaDon == null;
        view.setHienThi(dangLap, HoaDon.TIEN_MAT.equals(hinhThuc), !dangLap, ghiNo, !dangLap, !ghiNo);
        capNhatTienThua();
        if (HoaDon.CHUYEN_KHOAN.equals(hinhThuc)) {
            taiQr();
        } else {
            phienBanQr++;
            view.hienChuQr("VietQR\n" + tien(tongHienTai));
        }
    }

    private void capNhatTienThua() {
        try {
            double thua = docTien(view.getTxtTienKhachDua().getText()) - tongHienTai;
            view.setTienThua(thua >= 0 ? "Tiền thừa trả lại: " + tien(thua) : "Còn thiếu: " + tien(-thua));
        } catch (IllegalArgumentException ex) {
            view.setTienThua(null);
        }
    }

    private void taiQr() {
        int phienBan = ++phienBanQr;
        double soTien = tongHienTai;
        view.hienChuQr("Đang tải mã QR...");
        new SwingWorker<Image, Void>() {
            @Override
            protected Image doInBackground() throws Exception {
                BufferedImage anh = VietQRUtil.taiAnh(VietQRUtil.taoUrl(soTien, maQr));
                int cao = anh.getHeight() * CO_QR / anh.getWidth();
                return anh.getScaledInstance(CO_QR, cao, Image.SCALE_SMOOTH);
            }

            @Override
            protected void done() {
                if (phienBan != phienBanQr) {
                    return;
                }
                try {
                    view.hienQr(get());
                } catch (Exception ex) {
                    // Không có mạng vẫn thu được tiền: hiện thông tin để khách chuyển tay.
                    view.hienChuQr("Không tải được mã QR.\nSTK: " + VietQRUtil.SO_TAI_KHOAN + "\nBIN: " + VietQRUtil.MA_BIN
                            + "\n" + VietQRUtil.TEN_TAI_KHOAN + "\nSố tiền: " + tien(soTien) + "\nNội dung: " + maQr);
                }
            }
        }.execute();
    }

    /** Đọc tiền khách đưa khi trả tiền mặt; trả về null và báo lỗi nếu không hợp lệ. */
    private Double docTienKhachDua() {
        try {
            double dua = docTien(view.getTxtTienKhachDua().getText());
            if (dua < tongHienTai) {
                view.hienLoi("Tiền khách đưa phải từ " + tien(tongHienTai) + " trở lên.");
                return null;
            }
            return dua;
        } catch (IllegalArgumentException ex) {
            view.hienLoi(ex.getMessage());
            return null;
        }
    }

    private void luuHoaDon() {
        String hinhThuc = String.valueOf(view.getCboHinhThuc().getSelectedItem());
        Thue thue = (Thue) view.getCboThue().getSelectedItem();
        if (thue == null) {
            view.hienLoi("Vui lòng chọn loại thuế.");
            return;
        }
        Double dua = null;
        if (HoaDon.TIEN_MAT.equals(hinhThuc)) {
            dua = docTienKhachDua();
            if (dua == null) {
                return;
            }
        }
        KhuyenMai km = (KhuyenMai) view.getCboKhuyenMai().getSelectedItem();
        try {
            String ma = dao.lapHoaDon(maPhieu, km == null ? null : km.getMaKM(), thue.getMaThue(), hinhThuc, dua,
                    PhienDangNhap.getHienTai().getMaNV());
            String thongBao = "Đã lập hóa đơn " + ma + ".";
            if (HoaDon.CHUYEN_KHOAN.equals(hinhThuc) && !ma.equals(maQr)) {
                thongBao += "\nNội dung trên mã QR ghi " + maQr + " vì máy khác vừa lập hóa đơn trước; hãy đối soát theo " + ma + ".";
            }
            JOptionPane.showMessageDialog(view, thongBao);
            dieuHuong.moChiTietHoaDon(ma);
        } catch (IllegalArgumentException | SQLException ex) {
            view.hienLoi(null);
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể lập hóa đơn", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void thuCongNo() {
        String hinhThuc = String.valueOf(view.getCboHinhThuc().getSelectedItem());
        Double dua = null;
        if (HoaDon.TIEN_MAT.equals(hinhThuc)) {
            dua = docTienKhachDua();
            if (dua == null) {
                return;
            }
        }
        try {
            dao.thanhToanCongNo(hoaDon.getMaHD(), hinhThuc, dua);
            dieuHuong.moChiTietHoaDon(hoaDon.getMaHD());
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể thanh toán", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void huyHoaDon() {
        String lyDo = view.getTxtLyDoHuy().getText();
        if (lyDo.trim().isEmpty()) {
            view.hienLoi("Vui lòng nhập lý do hủy.");
            return;
        }
        int chon = JOptionPane.showConfirmDialog(view, "Hủy hóa đơn " + hoaDon.getMaHD() + "? Phụ tùng không được hoàn lại kho.",
                "Xác nhận hủy hóa đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.huy(hoaDon.getMaHD(), lyDo, PhienDangNhap.getHienTai().getTenDangNhap());
            dieuHuong.moChiTietHoaDon(hoaDon.getMaHD());
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể hủy hóa đơn", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Dùng chung cho nút In ở danh sách và trang chi tiết. */
    public static void inHoaDon(Component cha, String maHD) {
        HoaDon_Dao dao = new HoaDon_Dao();
        JFileChooser chon = new JFileChooser();
        chon.setSelectedFile(new File("hoa-don-" + maHD + ".pdf"));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            HoaDon hd = dao.findById(maHD);
            if (hd == null) {
                JOptionPane.showMessageDialog(cha, "Hóa đơn " + maHD + " không còn tồn tại.");
                return;
            }
            File tep = chon.getSelectedFile();
            PdfExporter.xuatHoaDon(tep, hd, dao.findThongTinPhieu(hd.getMaPhieuTNSC()),
                    dao.findDichVu(hd.getMaPhieuTNSC()), dao.findPhuTung(hd.getMaPhieuTNSC()));
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tep);
            }
        } catch (SQLException | IOException | DocumentException ex) {
            JOptionPane.showMessageDialog(cha, "Không thể xuất hóa đơn.\n" + ex.getMessage(), "Lỗi xuất PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
