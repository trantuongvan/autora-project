package controller;

import com.itextpdf.text.DocumentException;
import dao.HoaDon_Dao;
import dao.Thue_Dao;
import entity.ChiTietSuaChua_DichVu;
import entity.ChiTietSuaChua_PhuTung;
import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import util.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.Thue;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;
import entity.Xe;
import gui.ChiTietHoaDon;
import util.FormatUtil;
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
        try {
            return FormatUtil.parseCurrency(chuoi);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Tiền khách đưa phải là số, ví dụ 1.500.000.");
        }
    }

    private static final class DuLieu {
        HoaDon hoaDon;
        PhieuTiepNhanVaSuaChua phieu;
        List<ChiTietSuaChua_DichVu> dichVu;
        List<ChiTietSuaChua_PhuTung> phuTung;
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
                    ma = d.hoaDon.getPhieu().getMaPhieuTNSC();
                }
                d.phieu = dao.findThongTinPhieu(ma);
                if (d.phieu == null) {
                    d.loi = "Phiếu " + ma + " không còn tồn tại.";
                    return d;
                }
                if (maHD == null) {
                    if (d.phieu.getTrangThai() != TrangThaiPhieu.HOAN_THANH || dao.coHoaDonHieuLuc(ma)) {
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
        Xe xe = d.phieu.getXe();
        KhachHang kh = xe.getKhachHang();
        view.setThongTin(
                new String[] {kh.getHoTen() + " (" + kh.getMaKH() + " — " + kh.getLoaiKhachHang() + ")",
                    "SĐT: " + kh.getSoDienThoai(), "Địa chỉ: " + rong(kh.getDiaChi()), "Email: " + rong(kh.getEmail())},
                new String[] {"Biển số: " + xe.getBienSo(), FormatUtil.moTaXeDayDu(xe), "VIN: " + rong(xe.getSoVIN()),
                    "Mã phiếu: " + d.phieu.getMaPhieuTNSC()},
                new String[] {
                    "Ngày lập: " + (hoaDon == null || hoaDon.getNgayLap() == null ? "—"
                            : hoaDon.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                    "Hình thức: " + (hoaDon == null ? "—" : hoaDon.getHinhThucThanhToan()),
                    "Mã khuyến mãi: " + (hoaDon == null ? "—" : rong(hoaDon.getKhuyenMai() == null ? null : hoaDon.getKhuyenMai().getMaKM()))},
                hoaDon == null ? null : hoaDon.getTrangThaiThanhToan().getText());
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

    private List<Object[]> dongDichVu(List<ChiTietSuaChua_DichVu> nguon) {
        List<Object[]> dong = new ArrayList<>();
        for (ChiTietSuaChua_DichVu d : nguon) {
            tongTienCong += d.getTienCongThucTe();
            dong.add(new Object[] {d.getDichVu().getTenDichVu(), d.getDichVu().getMaDichVu(), d.getThoHienThi(),
                d.getTrangThaiTienDo().getText(), tien(d.getTienCongThucTe())});
        }
        return dong;
    }

    private List<Object[]> dongPhuTung(List<ChiTietSuaChua_PhuTung> nguon) {
        List<Object[]> dong = new ArrayList<>();
        for (ChiTietSuaChua_PhuTung d : nguon) {
            double thanhTien = d.thanhTien();
            tongTienPhuTung += thanhTien;
            dong.add(new Object[] {d.getPhuTung().getTenPhuTung(), d.getPhuTung().getMaSKU(), d.getSoLuongSuDung(),
                tien(d.getGiaBanThucTe()), tien(thanhTien)});
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
        view.getCboHinhThuc().addItem(HinhThucThanhToan.TIEN_MAT);
        view.getCboHinhThuc().addItem(HinhThucThanhToan.CHUYEN_KHOAN);
        if (d.phieu.getXe().getKhachHang().duocThanhToanBang(HinhThucThanhToan.CONG_NO)) {
            view.getCboHinhThuc().addItem(HinhThucThanhToan.CONG_NO);
        }
        // Mặc định chọn khuyến mãi giảm nhiều nhất; nhân viên vẫn đổi hoặc bỏ chọn được.
        view.getCboKhuyenMai().setSelectedItem(KhuyenMai.totNhat(d.khuyenMai, tongTienCong, tongTienPhuTung));
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
                hoaDon.getThue() == null ? 0 : hoaDon.getThue().getThueSuat(), hoaDon.getTongTienVAT(), hoaDon.getTongThanhToan());

        TrangThaiHoaDon trangThai = hoaDon.getTrangThaiThanhToan();
        if (trangThai == TrangThaiHoaDon.CONG_NO) {
            view.getCboHinhThuc().addItem(HinhThucThanhToan.TIEN_MAT);
            view.getCboHinhThuc().addItem(HinhThucThanhToan.CHUYEN_KHOAN);
            ganSuKienThanhToan();
            view.getBtnXacNhan().addActionListener(e -> thuCongNo());
            view.getBtnHuy().addActionListener(e -> huyHoaDon());
            capNhatTheoHinhThuc();
            return;
        }
        // Đã thanh toán hoặc đã hủy: chỉ xem; đã thanh toán vẫn được hủy khi xe chưa bàn giao.
        boolean daHuy = trangThai == TrangThaiHoaDon.DA_HUY;
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
        Thue thue = (Thue) view.getCboThue().getSelectedItem();
        KhuyenMai khuyenMai = (KhuyenMai) view.getCboKhuyenMai().getSelectedItem();
        HoaDon.KetQuaTinhToan ketQua = HoaDon.tinhToan(tongTienCong, tongTienPhuTung, khuyenMai, thue);
        tongHienTai = ketQua.tongThanhToan;
        hienTongHop(ketQua.tongTienCong, ketQua.tongTienPhuTung, ketQua.tongTienGiam,
                thue == null ? 0 : thue.getThueSuat(), ketQua.tongTienVAT, tongHienTai);
        capNhatTheoHinhThuc();
    }

    private void hienTongHop(double cong, double phuTung, double giam, double thueSuat, double vat, double tong) {
        view.setTongHop(tien(cong), tien(phuTung), tien(cong + phuTung), "− " + tien(giam),
                "VAT (" + dinhDangTien.format(thueSuat) + "%)", tien(vat), tien(tong));
    }

    private void capNhatTheoHinhThuc() {
        HinhThucThanhToan hinhThuc = (HinhThucThanhToan) view.getCboHinhThuc().getSelectedItem();
        boolean ghiNo = hinhThuc == HinhThucThanhToan.CONG_NO;
        boolean dangLap = hoaDon == null;
        view.setHienThi(dangLap, hinhThuc == HinhThucThanhToan.TIEN_MAT, !dangLap, ghiNo, !dangLap, !ghiNo);
        capNhatTienThua();
        if (hinhThuc == HinhThucThanhToan.CHUYEN_KHOAN) {
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
        HinhThucThanhToan hinhThuc = (HinhThucThanhToan) view.getCboHinhThuc().getSelectedItem();
        Thue thue = (Thue) view.getCboThue().getSelectedItem();
        if (thue == null) {
            view.hienLoi("Vui lòng chọn loại thuế.");
            return;
        }
        Double dua = null;
        if (hinhThuc == HinhThucThanhToan.TIEN_MAT) {
            dua = docTienKhachDua();
            if (dua == null) {
                return;
            }
        }
        KhuyenMai km = (KhuyenMai) view.getCboKhuyenMai().getSelectedItem();
        try {
            String ma = dao.lapHoaDon(maPhieu, km == null ? null : km.getMaKM(), thue.getMaThue(), hinhThuc, dua,
                    PhienDangNhap.getHienTai().getMaNV());
            JOptionPane.showMessageDialog(view, "Đã lập hóa đơn " + ma + ".");
            dieuHuong.moChiTietHoaDon(ma);
        } catch (IllegalArgumentException | SQLException ex) {
            view.hienLoi(null);
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể lập hóa đơn", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void thuCongNo() {
        HinhThucThanhToan hinhThuc = (HinhThucThanhToan) view.getCboHinhThuc().getSelectedItem();
        Double dua = null;
        if (hinhThuc == HinhThucThanhToan.TIEN_MAT) {
            dua = docTienKhachDua();
            if (dua == null) {
                return;
            }
        }
        try {
            dao.thanhToanCongNo(hoaDon.getMaHD(), hinhThuc, dua, PhienDangNhap.getHienTai().getTenDangNhap());
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
            String maPhieu = hd.getPhieu().getMaPhieuTNSC();
            PdfExporter.xuatHoaDon(tep, hd, dao.findThongTinPhieu(maPhieu),
                    dao.findDichVu(maPhieu), dao.findPhuTung(maPhieu));
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tep);
            }
        } catch (SQLException | IOException | DocumentException ex) {
            JOptionPane.showMessageDialog(cha, "Không thể xuất hóa đơn.\n" + ex.getMessage(), "Lỗi xuất PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
