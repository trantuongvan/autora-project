package controller;

import dao.TienDoSuaChua_Dao;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import gui.QLTienDoSuaChua;
import gui.TrangChuKyThuatVien;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class TrangChuKyThuatVienController {

    private static final int SO_PHIEU_GAN_NHAT = 7;

    private final TrangChuKyThuatVien view;
    private final TienDoSuaChua_Dao tienDoDao;
    private NhanVien kyThuatVienHienTai;
    private List<PhieuTiepNhanVaSuaChua> danhSachTimDuoc = new ArrayList<>();
    private List<PhieuTiepNhanVaSuaChua> danhSachHienThi = new ArrayList<>();
    private boolean dangXemTatCa = false;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public TrangChuKyThuatVienController(TrangChuKyThuatVien view) {
        this.view = view;
        this.tienDoDao = new TienDoSuaChua_Dao();
        initKyThuatVien();
        initController();
        loadData();
    }

    private void initKyThuatVien() {
        try {
            kyThuatVienHienTai = tienDoDao.findDefaultKyThuatVien();
            if (kyThuatVienHienTai != null) {
                String ten = kyThuatVienHienTai.getHoTen();
                view.capNhatThongTinKTVHeader(ten);
                NhanVien dayDu = new dao.NhanVien_Dao().findById(kyThuatVienHienTai.getMaNV());
                view.setCaLamViec(dayDu == null ? null : dayDu.getCaLamViec());
                String tenGoi = ten.contains(" ") ? ten.substring(ten.lastIndexOf(' ') + 1) : ten;
                view.getLblMoTaChao().setText("Xin chào " + tenGoi + ", đây là các hạng mục sửa chữa được giao cho bạn hôm nay.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initController() {
        // Điều hướng trên Left Sidebar
        view.getBtnMenuTrangChu().addActionListener(e -> loadData());
        view.getBtnMenuTienDo().addActionListener(e -> dieuHuongSangTienDo(null, false));

        // Nút Đăng xuất trên TopBar
        view.getBtnDangXuat().addActionListener(e -> {
            int cf = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                Window w = SwingUtilities.getWindowAncestor(view);
                if (w != null) w.dispose();
            }
        });

        // Tìm kiếm realtime
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                locBangTheoTuKhoa();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                locBangTheoTuKhoa();
            }
            @Override public void changedUpdate(DocumentEvent e) {
                locBangTheoTuKhoa();
            }
        });

        // Làm mới dữ liệu
        view.getBtnLamMoi().addActionListener(e -> {
            dangXemTatCa = false;
            loadData();
        });

        // Nút "Xem tất cả": Chỉ mở rộng bảng nếu có nhiều hơn 7 công việc; nếu ít hơn thì không làm gì cả
        view.getBtnXemTatCa().addActionListener(e -> {
            if (danhSachTimDuoc.size() <= SO_PHIEU_GAN_NHAT) {
                return;
            }
            dangXemTatCa = true;
            doDuLieuLenBang();
        });

        // Click vào cột "Thao tác" (cột 5) trên bảng
        view.getTblCongViec().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblCongViec().rowAtPoint(e.getPoint());
                int col = view.getTblCongViec().columnAtPoint(e.getPoint());
                if (row >= 0 && row < danhSachHienThi.size() && (col == 5 || e.getClickCount() == 2)) {
                    PhieuTiepNhanVaSuaChua phieu = danhSachHienThi.get(row);
                    boolean moYeuCauPT = phieu.getTrangThai() == TrangThaiPhieu.CHO_PHU_TUNG;
                    dieuHuongSangTienDo(phieu.getMaPhieuTNSC(), moYeuCauPT);
                }
            }
        });
    }

    public void loadData() {
        try {
            List<PhieuTiepNhanVaSuaChua> all = tienDoDao.searchPhieuĐượcGiao("", "", "Tất cả");

            int choSua = 0, dangSua = 0, choPhuTung = 0, hoanThanh = 0;
            for (PhieuTiepNhanVaSuaChua p : all) {
                switch (p.getTrangThai()) {
                    case CHO_KIEM_TRA: choSua++; break;
                    case DANG_SUA: dangSua++; break;
                    case CHO_PHU_TUNG: choPhuTung++; break;
                    case HOAN_THANH: hoanThanh++; break;
                    default: break;
                }
            }

            view.getLblSoChoSua().setText(String.format("%02d", choSua));
            view.getLblSoDangSua().setText(String.format("%02d", dangSua));
            view.getLblSoChoPhuTung().setText(String.format("%02d", choPhuTung));
            view.getLblSoHoanThanh().setText(String.format("%02d", hoanThanh));

            String gioHienTai = LocalTime.now().format(TIME_FMT);
            view.getLblSubHoanThanh().setText("Tính đến " + gioHienTai + " hôm nay");
            view.getLblCapNhatLuc().setText("Cập nhật lúc " + gioHienTai);

            locBangTheoTuKhoa();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu trang chủ KTV: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locBangTheoTuKhoa() {
        try {
            String kw = view.getTxtTimKiem().getText();
            danhSachTimDuoc = tienDoDao.searchPhieuĐượcGiao("", kw, "Tất cả");
            doDuLieuLenBang();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void doDuLieuLenBang() {
        view.getTableModel().setRowCount(0);

        int gioiHan = (dangXemTatCa || danhSachTimDuoc.size() <= SO_PHIEU_GAN_NHAT)
                ? danhSachTimDuoc.size()
                : SO_PHIEU_GAN_NHAT;

        danhSachHienThi = new ArrayList<>(danhSachTimDuoc.subList(0, gioiHan));

        for (PhieuTiepNhanVaSuaChua p : danhSachHienThi) {
            String tt = p.getTrangThaiHienThiKTV();
            String nhanTrangThai = tt;
            if (p.getTrangThai() == TrangThaiPhieu.DANG_SUA) {
                nhanTrangThai = "Đang sửa • " + TienDoSuaChua_Dao.phanTramTienDo(p) + "%";
            }

            String thaoTac;
            switch (p.getTrangThai()) {
                case CHO_KIEM_TRA: thaoTac = "Bắt đầu →"; break;
                case DANG_SUA: thaoTac = "Cập nhật tiến độ"; break;
                case CHO_PHU_TUNG: thaoTac = "Yêu cầu phụ tùng"; break;
                default: thaoTac = "Xem phiếu"; break;
            }

            view.getTableModel().addRow(new Object[]{
                    p.getMaPhieuTNSC(),
                    p.getXe().getBienSo(),
                    p.getThongTinXeNgan(),
                    p.getTomTatHangMuc(),
                    nhanTrangThai,
                    thaoTac
            });
        }

        if (dangXemTatCa && danhSachTimDuoc.size() > SO_PHIEU_GAN_NHAT) {
            view.getLblThongKeBang().setText("Tất cả " + danhSachHienThi.size() + " phiếu • Ngày " + LocalDate.now().format(DATE_FMT));
        } else {
            view.getLblThongKeBang().setText(danhSachHienThi.size() + " phiếu gần nhất • Ngày " + LocalDate.now().format(DATE_FMT));
        }
    }

    private void dieuHuongSangTienDo(String maPhieuTNSC, boolean moManHinhYeuCauPT) {
        Window w0 = SwingUtilities.getWindowAncestor(view);
        if (w0 instanceof gui.GiaoDienChinh) {
            ((gui.GiaoDienChinh) w0).moCard("QL_TIEN_DO");
            QLTienDoSuaChuaController ctrl = QLTienDoSuaChua.getActiveController();
            if (ctrl != null) {
                ctrl.chonPhieuVaMoManHinh(maPhieuTNSC, moManHinhYeuCauPT);
            }
            return;
        }
        Container parent = view.getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout) {
            ((CardLayout) parent.getLayout()).show(parent, "QL_TIEN_DO");
            QLTienDoSuaChuaController ctrl = QLTienDoSuaChua.getActiveController();
            if (ctrl != null) {
                ctrl.chonPhieuVaMoManHinh(maPhieuTNSC, moManHinhYeuCauPT);
            }
            return;
        }

        Window w = SwingUtilities.getWindowAncestor(view);
        if (w instanceof JFrame) {
            JFrame frame = (JFrame) w;
            JPanel pnlTienDo = QLTienDoSuaChua.createPanel();
            frame.setContentPane(pnlTienDo);
            frame.revalidate();
            frame.repaint();

            QLTienDoSuaChuaController ctrl = QLTienDoSuaChua.getActiveController();
            if (ctrl != null) {
                ctrl.chonPhieuVaMoManHinh(maPhieuTNSC, moManHinhYeuCauPT);
            }
        }
    }
}