package controller;

import dao.DichVu_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import entity.DichVu;
import entity.KhachHang;
import entity.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.Xe;
import gui.QLTiepNhanSuaChua;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/** Điều phối tiếp nhận: tra cứu xe/khách, lập phiếu cùng khách và xe mới, sửa phiếu đã có. */
public class QLTiepNhanSuaChuaController {
    private static final DateTimeFormatter DINH_DANG_GIO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final QLTiepNhanSuaChua view;
    private final DieuHuongPhieu dieuHuong;
    private final PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
    private final DichVu_Dao dichVuDao = new DichVu_Dao();
    private PhieuTiepNhanVaSuaChua phieuDangSua;
    private String maKhachDaCo;
    private boolean xeDaCo;

    public QLTiepNhanSuaChuaController(QLTiepNhanSuaChua view, DieuHuongPhieu dieuHuong) {
        this.view = view;
        this.dieuHuong = dieuHuong;
        initController();
        taiDichVu();
    }

    private void initController() {
        view.getBtnTraCuu().addActionListener(e -> traCuu());
        view.getTxtTraCuu().addActionListener(e -> traCuu());
        view.getBtnHuy().addActionListener(e -> moFormMoi());
        view.getBtnLuu().addActionListener(e -> luuPhieu(false));
        view.getBtnLuuVaChiDinh().addActionListener(e -> luuPhieu(true));
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "phieuMoi", this::moFormMoi);
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "luu", () -> luuPhieu(false));
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "tim",
                () -> view.getTxtTraCuu().requestFocusInWindow());
    }

    private void ganPhim(KeyStroke phim, String ten, Runnable viec) {
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(phim, ten);
        view.getActionMap().put(ten, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viec.run();
            }
        });
    }

    private void taiDichVu() {
        try {
            DefaultListModel<DichVu> model = (DefaultListModel<DichVu>) view.getLstDichVu().getModel();
            for (DichVu dv : dichVuDao.findAll()) {
                model.addElement(dv);
            }
        } catch (SQLException ex) {
            hienLoiDuLieu("Không thể tải danh mục dịch vụ.", ex);
        }
    }

    public void moFormMoi() {
        phieuDangSua = null;
        maKhachDaCo = null;
        xeDaCo = false;
        view.xoaTrangForm();
        view.setCheDoSua(false);
        view.getTxtTraCuu().requestFocusInWindow();
    }

    /** Mở phiếu đã có để sửa thông tin tiếp nhận; khách và xe chỉ xem. */
    public void moFormSua(String ma) {
        try {
            PhieuTiepNhanVaSuaChua p = dao.findById(ma);
            if (p == null) {
                JOptionPane.showMessageDialog(view, "Phiếu không còn tồn tại.");
                return;
            }
            if (PhieuTiepNhanVaSuaChua.DA_HUY.equals(p.getTrangThai())) {
                JOptionPane.showMessageDialog(view, "Phiếu đã hủy nên không thể sửa.");
                return;
            }
            moFormMoi();
            phieuDangSua = p;
            view.setCheDoSua(true);
            dienXe(dao.findXeDayDu(p.getBienSo()));
            dienKhach(dao.findKhachTheoSdt(p.getSdtKhachHang()));
            view.getTxtSoKm().setText(String.valueOf(p.getSoKm()));
            view.getSldNhienLieu().setValue((int) Math.round(p.getMucNhienLieu()));
            view.getTxtNgoaiThat().setText(p.getTrayXuoc());
            view.getTxtNoiThat().setText(p.getDanhGiaNoiThat());
            view.getTxtTaiSan().setText(p.getTaiSanTrenXe());
            view.getTxtGhiChu().setText(p.getGhiChu());
            view.getTxtYeuCau().setText(p.getYeuCauBanDau());
            view.getTxtPhuTungDuKien().setText(p.getPhuTungDuKien());
            view.getCboTrangThai().setSelectedItem(p.getTrangThai());
            view.getTxtDuKien().setText(p.getThoiGianDuKien() == null ? "" : p.getThoiGianDuKien().format(DINH_DANG_GIO));
            view.getRadKhan().setSelected("Khẩn".equals(p.getUuTien()));
            view.getRadThuong().setSelected(!"Khẩn".equals(p.getUuTien()));
        } catch (SQLException ex) {
            hienLoiDuLieu("Không thể tải phiếu.", ex);
        }
    }

    private void traCuu() {
        String nhap = view.getTxtTraCuu().getText().trim().toUpperCase(Locale.ROOT);
        if (nhap.isEmpty()) {
            view.hienLoi("Vui lòng nhập biển số xe hoặc số điện thoại.");
            return;
        }
        try {
            view.hienLoi(null);
            boolean laSdt = nhap.matches("0[0-9]{9}");
            String bienSo = laSdt ? dao.timBienSoTheoSdt(nhap) : nhap;
            String[] xe = bienSo == null ? null : dao.findXeDayDu(bienSo);
            String[] khach = xe != null ? dao.findKhachTheoSdt(xe[6]) : laSdt ? dao.findKhachTheoSdt(nhap) : null;
            if (xe == null && khach == null) {
                // Chưa có trong hệ thống: nhập mới ngay trên form, giữ lại giá trị vừa tra cứu.
                view.xoaTrangForm();
                maKhachDaCo = null;
                xeDaCo = false;
                if (laSdt) {
                    view.getTxtSoDienThoai().setText(nhap);
                } else {
                    view.getTxtBienSo().setText(nhap);
                }
                view.hienLoi("Chưa có trong hệ thống. Vui lòng nhập thông tin khách hàng và xe mới.");
                view.getTxtHoTen().requestFocusInWindow();
                return;
            }
            dienKhach(khach);
            dienXe(xe);
            if (xe != null && dao.coPhieuDangMo(xe[0])) {
                view.hienLoi("Xe " + xe[0] + " đang có phiếu chưa hoàn thành, không thể lập phiếu mới.");
            }
        } catch (SQLException ex) {
            hienLoiDuLieu("Không thể tra cứu.", ex);
        }
    }

    private void dienKhach(String[] khach) {
        maKhachDaCo = khach == null ? null : khach[0];
        if (khach != null) {
            view.getTxtHoTen().setText(khach[1]);
            view.getTxtSoDienThoai().setText(khach[2]);
            view.getTxtDiaChi().setText(khach[3]);
            view.getTxtEmail().setText(khach[4]);
            view.getTxtMaSoThue().setText(khach[6]);
            view.setLoaiKhachHang(khach[5]);
        }
        view.setChoSuaKhachHang(khach == null);
    }

    private void dienXe(String[] xe) {
        xeDaCo = xe != null;
        if (xe != null) {
            view.getTxtBienSo().setText(xe[0]);
            view.getTxtSoKhung().setText(xe[1]);
            view.getTxtHangXe().setText(xe[2]);
            view.getTxtDongXe().setText(xe[3]);
            view.getTxtDoiXe().setText(xe[4]);
            view.getTxtMauSon().setText(xe[5]);
        }
        view.setChoSuaXe(xe == null);
    }

    private void luuPhieu(boolean tiepTucChiDinh) {
        try {
            view.hienLoi(null);
            PhieuTiepNhanVaSuaChua p = docPhieu();
            if (phieuDangSua != null) {
                if (!dao.update(p)) {
                    view.hienLoi("Phiếu đã có hóa đơn hoặc đã hủy nên không thể sửa.");
                    return;
                }
            } else if (!taoPhieuMoi(p)) {
                return;
            }
            String ma = p.getMaPhieuTNSC();
            moFormMoi();
            if (tiepTucChiDinh) {
                dieuHuong.moChiTiet(ma);
            } else {
                JOptionPane.showMessageDialog(view, "Đã lưu phiếu " + ma + ".");
            }
        } catch (NumberFormatException ex) {
            view.hienLoi("Số km và đời xe phải là số nguyên.");
        } catch (IllegalArgumentException ex) {
            view.hienLoi(ex.getMessage());
        } catch (SQLException ex) {
            hienLoiDuLieu("Không thể lưu phiếu.", ex);
        }
    }

    private PhieuTiepNhanVaSuaChua docPhieu() {
        PhieuTiepNhanVaSuaChua p = phieuDangSua != null ? phieuDangSua : new PhieuTiepNhanVaSuaChua();
        p.setSoKm(Integer.parseInt(view.getTxtSoKm().getText().trim()));
        p.setMucNhienLieu(view.getSldNhienLieu().getValue());
        p.setTrayXuoc(batBuoc(view.getTxtNgoaiThat().getText(), "Đánh giá ngoại thất"));
        p.setDanhGiaNoiThat(batBuoc(view.getTxtNoiThat().getText(), "Đánh giá nội thất"));
        p.setTaiSanTrenXe(batBuoc(view.getTxtTaiSan().getText(), "Tài sản trên xe"));
        p.setGhiChu(view.getTxtGhiChu().getText());
        p.setYeuCauBanDau(batBuoc(view.getTxtYeuCau().getText(), "Yêu cầu của khách"));
        p.setPhuTungDuKien(batBuoc(view.getTxtPhuTungDuKien().getText(), "Phụ tùng dự kiến thay thế"));
        p.setUuTien(view.getRadKhan().isSelected() ? "Khẩn" : "Thường");
        p.setThoiGianDuKien(docDuKien(view.getTxtDuKien().getText()));
        return p;
    }

    /** Trả về false khi đã báo lỗi nghiệp vụ lên form. */
    private boolean taoPhieuMoi(PhieuTiepNhanVaSuaChua p) throws SQLException {
        List<DichVu> dichVu = view.getLstDichVu().getSelectedValuesList();
        if (dichVu.isEmpty()) {
            view.hienLoi("Vui lòng chọn ít nhất một hạng mục dịch vụ.");
            return false;
        }

        Xe xeMoi = xeDaCo ? null : docXeMoi();
        String bienSo = xeDaCo ? view.getTxtBienSo().getText().trim() : xeMoi.getBienSo();
        if (dao.coPhieuDangMo(bienSo)) {
            view.hienLoi("Xe " + bienSo + " đang có phiếu chưa hoàn thành, không thể lập phiếu mới.");
            return false;
        }
        KhachHang khachMoi = null;
        if (!xeDaCo) {
            // Xe mới nhưng biển số đã có nghĩa là người dùng chưa tra cứu: tránh ghi đè chủ xe.
            if (dao.findXeDayDu(bienSo) != null) {
                view.hienLoi("Biển số " + bienSo + " đã có trong hệ thống. Vui lòng tra cứu trước.");
                return false;
            }
            String[] khachCu = dao.findKhachTheoSdt(view.getTxtSoDienThoai().getText().trim());
            if (khachCu != null) {
                maKhachDaCo = khachCu[0];
            }
            if (maKhachDaCo == null) {
                khachMoi = docKhachMoi();
                // Mã thật được sinh trong transaction; giá trị tạm chỉ để qua kiểm tra của Xe.
                xeMoi.setMaKH("MOI");
            } else {
                xeMoi.setMaKH(maKhachDaCo);
            }
        }

        p.setBienSo(bienSo);
        p.setMaNVLapPhieu(PhienDangNhap.getHienTai().getMaNV());
        p.setThoiGianTiepNhan(LocalDateTime.now());
        p.setTrangThai(PhieuTiepNhanVaSuaChua.TRANG_THAI[0]);
        dao.taoTiepNhan(khachMoi, xeMoi, p, dichVu);
        return true;
    }

    private KhachHang docKhachMoi() {
        KhachHang kh = new KhachHang();
        kh.setHoTen(view.getTxtHoTen().getText());
        kh.setSoDienThoai(view.getTxtSoDienThoai().getText());
        kh.setDiaChi(view.getTxtDiaChi().getText());
        kh.setEmail(view.getTxtEmail().getText());
        kh.setLoaiKhachHang(view.getLoaiKhachHang());
        kh.setMaSoThue(view.getTxtMaSoThue().getText());
        return kh;
    }

    private Xe docXeMoi() {
        String bienSo = view.getTxtBienSo().getText().trim();
        if (!Xe.bienSoHopLe(bienSo)) {
            throw new IllegalArgumentException("Biển số không đúng định dạng, ví dụ 51H-123.45.");
        }
        String soVin = view.getTxtSoKhung().getText().trim().toUpperCase(Locale.ROOT);
        if (!soVinHopLe(soVin)) {
            throw new IllegalArgumentException("Số khung (VIN) phải đủ 17 ký tự chữ và số, không dùng I, O, Q.");
        }
        Xe xe = new Xe();
        xe.setBienSo(bienSo);
        xe.setSoVIN(soVin);
        xe.setHangXe(view.getTxtHangXe().getText());
        xe.setDongXe(batBuoc(view.getTxtDongXe().getText(), "Dòng xe"));
        xe.setDoiXe(Integer.valueOf(view.getTxtDoiXe().getText().trim()));
        xe.setMauSon(batBuoc(view.getTxtMauSon().getText(), "Màu sơn"));
        return xe;
    }

    public static boolean soVinHopLe(String soVin) {
        return soVin != null && soVin.matches("^[A-HJ-NPR-Z0-9]{17}$");
    }

    private static LocalDateTime docDuKien(String nhap) {
        String s = nhap.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, DINH_DANG_GIO);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Dự kiến hoàn thành phải đúng định dạng dd/MM/yyyy HH:mm.");
        }
    }

    private static String batBuoc(String giaTri, String ten) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new IllegalArgumentException(ten + " không được để trống.");
        }
        return giaTri.trim();
    }

    private void hienLoiDuLieu(String thongBao, Exception ex) {
        JOptionPane.showMessageDialog(view, thongBao + "\n" + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
