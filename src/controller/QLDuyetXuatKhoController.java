package controller;

import dao.PhieuNhapKho_Dao;
import dao.PhieuYeuCauXuatKho_Dao;
import dao.PhuTung_Dao;
import util.PhienDangNhap;
import entity.ChiTietYeuCauXuatKho;
import entity.NhanVien;
import entity.PhieuYeuCauXuatKho;
import entity.PhuTung;
import entity.TrangThaiYeuCauXuat;
import gui.QLDuyetXuatKho;
import gui.TrangChuThuKho;
import gui.TrangChuThuKho.MenuThuKho;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class QLDuyetXuatKhoController {

    private final QLDuyetXuatKho view;
    private final PhieuYeuCauXuatKho_Dao yeuCauDao;
    private final PhuTung_Dao phuTungDao;
    private final PhieuNhapKho_Dao nhapKhoDao;

    private NhanVien thuKhoHienTai;
    private TrangThaiYeuCauXuat tabDangChon = TrangThaiYeuCauXuat.CHO_XUAT;
    private List<PhieuYeuCauXuatKho> danhSachHienTai = new ArrayList<>();
    private PhieuYeuCauXuatKho yeuCauDangChon;
    private List<ChiTietYeuCauXuatKho> danhSachChiTiet = new ArrayList<>();
    private boolean duTonKho = true;
    private boolean daTai;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public QLDuyetXuatKhoController(QLDuyetXuatKho view) {
        this.view = view;
        this.yeuCauDao = new PhieuYeuCauXuatKho_Dao();
        this.phuTungDao = new PhuTung_Dao();
        this.nhapKhoDao = new PhieuNhapKho_Dao();

        initThuKhoInfo();
        initController();
        UIComponents.tuTaiLai(() -> loadDanhSach(null), view.getTxtTimKiem());
        UIComponents.tuTaiLai(() -> loadDanhSach(null), view.getBtnTabs());
        loadDanhSach(null);
    }

    private void initThuKhoInfo() {
        try {
            thuKhoHienTai = nhapKhoDao.findDefaultThuKho();
            if (thuKhoHienTai != null) {
                TrangChuThuKho.capNhatHeaderThuKho(view.getLblAvatar(), view.getLblTenThuKhoHeader(), thuKhoHienTai.getHoTen());
            }
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void initController() {
        // Điều hướng Sidebar
        view.getBtnMenuTrangChu().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.TRANG_CHU, null));
        view.getBtnMenuPhuTung().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.PHU_TUNG, null));
        view.getBtnMenuNhapKho().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.NHAP_KHO, null));
        view.getBtnMenuDuyetXuat().addActionListener(e -> loadDanhSach(yeuCauDangChon != null ? yeuCauDangChon.getMaPhieuYC() : null));

        view.getBtnDangXuat().addActionListener(e -> {
            int cf = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                Window w = SwingUtilities.getWindowAncestor(view);
                if (w != null) w.dispose();
            }
        });

        view.getBtnTaiDuLieu().addActionListener(e -> loadDanhSach(null));
        view.getTxtTimKiem().addActionListener(e -> loadDanhSach(null));

        JButton[] tabs = view.getBtnTabs();
        TrangThaiYeuCauXuat[] tenTabs = view.getCacTab();
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].addActionListener(e -> {
                tabDangChon = tenTabs[idx];
                for (int j = 0; j < tabs.length; j++) {
                    view.capNhatStyleTab(tabs[j], j == idx);
                }
            });
        }

        cauhinhRenderBangTonKho();

        view.getBtnXacNhanXuat().addActionListener(e -> xacNhanXuatKho());
        view.getBtnTuChoi().addActionListener(e -> tuChoiYeuCau());
    }

    private void cauhinhRenderBangTonKho() {
        Color bgInsufficient = Color.decode("#FFF4F4");

        // Renderer cho các cột thường (0, 2, 3, 4)
        DefaultTableCellRenderer rendererThuong = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));

                boolean thieuTon = r < danhSachChiTiet.size()
                        && !NghiepVuKho.duTon(danhSachChiTiet.get(r).getPhuTung(), danhSachChiTiet.get(r).getSoLuong());

                if (c == 0 || c == 4) {
                    setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f));
                } else {
                    setFont(UIConstants.FONT_BODY.deriveFont(13.5f));
                }

                if (c == 4 && thieuTon) {
                    setForeground(UIConstants.STATUS_DANGER);
                } else {
                    setForeground(UIConstants.TEXT_DARK);
                }

                setBackground(sel ? t.getSelectionBackground() : (thieuTon ? bgInsufficient : UIConstants.CARD_BG));
                return this;
            }
        };

        TableCellRenderer wrapRenderer = new TableCellRenderer() {
            private final JTextArea area = new JTextArea();

            {
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setOpaque(true);
                area.setBorder(new EmptyBorder(10, 12, 10, 12));
                area.setFont(UIConstants.FONT_BODY.deriveFont(13.5f));
                area.setForeground(UIConstants.TEXT_DARK);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                boolean thieuTon = r < danhSachChiTiet.size()
                        && !NghiepVuKho.duTon(danhSachChiTiet.get(r).getPhuTung(), danhSachChiTiet.get(r).getSoLuong());

                area.setText(v == null ? "" : v.toString());
                area.setBackground(sel ? t.getSelectionBackground() : (thieuTon ? bgInsufficient : UIConstants.CARD_BG));

                int colWidth = t.getColumnModel().getColumn(c).getWidth();
                if (colWidth <= 0) colWidth = 235;
                area.setSize(new Dimension(colWidth, Short.MAX_VALUE));
                int prefH = Math.max(46, area.getPreferredSize().height + 4);
                if (t.getRowHeight(r) != prefH) {
                    t.setRowHeight(r, prefH);
                }
                return area;
            }
        };

        JTable tbl = view.getTblKiemTraTon();
        tbl.getColumnModel().getColumn(0).setCellRenderer(rendererThuong);
        tbl.getColumnModel().getColumn(1).setCellRenderer(wrapRenderer);
        tbl.getColumnModel().getColumn(2).setCellRenderer(rendererThuong);
        tbl.getColumnModel().getColumn(3).setCellRenderer(rendererThuong);
        tbl.getColumnModel().getColumn(4).setCellRenderer(rendererThuong);
    }

    public void chonYeuCau(String maPhieuYC) {
        tabDangChon = TrangThaiYeuCauXuat.CHO_XUAT;
        JButton[] tabs = view.getBtnTabs();
        for (int i = 0; i < tabs.length; i++) {
            view.capNhatStyleTab(tabs[i], i == 0);
        }
        loadDanhSach(maPhieuYC);
    }


    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    private void taiLai() {
        if (daTai) {
            loadDanhSach(null);
        }
    }

    public void loadDanhSach(String maYCCanChon) {
        daTai = true;
        try {
            String kw = view.getTxtTimKiem().getText();
            danhSachHienTai = yeuCauDao.searchYeuCauThongMinh(kw, tabDangChon);

            int countCho = yeuCauDao.countByTrangThai(TrangThaiYeuCauXuat.CHO_XUAT);
            view.getBadgeSoYeuCauCho().set(countCho + " yêu cầu chờ duyệt", UIConstants.ROLE_RECEPTION_LIGHT);
            view.getLblBadgeSidebarDuyetXuat().setText(String.valueOf(countCho));
            view.getLblBadgeSidebarDuyetXuat().setVisible(countCho > 0);
            view.getBtnTabs()[0].setText("Chờ xuất (" + countCho + ")");
            view.getLblDemYeuCauTrai().setText(danhSachHienTai.size() + " yêu cầu");
            hienDanhSach(maYCCanChon);
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }

    /** Vẽ lại danh sách đã tải và chọn một yêu cầu, không truy vấn lại theo bộ lọc mới chưa áp dụng. */
    private void hienDanhSach(String maYCCanChon) {
        JPanel container = view.getPnlDanhSachYeuCau();
        container.removeAll();
        yeuCauDangChon = null;

        for (PhieuYeuCauXuatKho yc : danhSachHienTai) {
            if (maYCCanChon != null && yc.getMaPhieuYC().equalsIgnoreCase(maYCCanChon)) {
                yeuCauDangChon = yc;
                break;
            }
        }
        if (yeuCauDangChon == null && !danhSachHienTai.isEmpty()) {
            yeuCauDangChon = danhSachHienTai.get(0);
        }

        if (danhSachHienTai.isEmpty()) {
            JLabel lblEmpty = UIComponents.label("Không có yêu cầu xuất kho nào.", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
            lblEmpty.setBorder(new EmptyBorder(20, 12, 20, 12));
            container.add(lblEmpty);
        } else {
            for (PhieuYeuCauXuatKho yc : danhSachHienTai) {
                boolean isSel = yeuCauDangChon != null && yc.getMaPhieuYC().equals(yeuCauDangChon.getMaPhieuYC());
                String tenKTV = yc.getTenKyThuatVien();
                JPanel item = view.taoTheYeuCau(yc, tenKTV, isSel);
                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        hienDanhSach(yc.getMaPhieuYC());
                    }
                });
                container.add(item);
                container.add(Box.createVerticalStrut(10));
            }
        }
        container.revalidate();
        container.repaint();

        loadChiTiet();
    }

    private void loadChiTiet() {
        view.getTableModel().setRowCount(0);
        duTonKho = true;

        if (yeuCauDangChon == null) {
            view.getLblMaYeuCau().setText("Chưa chọn yêu cầu");
            view.getLblThongTinPhu().setText("---");
            view.getLblValPhieuSC().setText("---");
            view.getLblValXe().setText("---");
            view.getLblValKTV().setText("---");
            view.getLblValNgayYC().setText("---");
            view.getBadgeTrangThai().set("Trống", UIConstants.TEXT_MUTED);
            view.getPnlHopLoiTonKho().setVisible(false);
            view.getBtnTuChoi().setEnabled(false);
            view.getBtnXacNhanXuat().setEnabled(false);
            view.capNhatChieuCaoBangTon();
            return;
        }

        try {
            Color cTrangThai = yeuCauDangChon.getTrangThai() == TrangThaiYeuCauXuat.CHO_XUAT ? UIConstants.STATUS_WARNING
                    : (yeuCauDangChon.getTrangThai() == TrangThaiYeuCauXuat.DA_XUAT ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER);
            view.getBadgeTrangThai().set(yeuCauDangChon.getTrangThai().getText(), cTrangThai);

            String thongTinXe = yeuCauDao.getThongTinXe(yeuCauDangChon.getPhieu().getMaPhieuTNSC());
            view.getLblMaYeuCau().setText(yeuCauDangChon.getMaPhieuYC());
            view.getLblThongTinPhu().setText("<html><div style='width:420px;'>Phiếu sửa chữa "
                    + yeuCauDangChon.getPhieu().getMaPhieuTNSC() + " • Xe " + thongTinXe + "</div></html>");

            view.getLblValPhieuSC().setText("<html><div style='width:320px;'>" + yeuCauDangChon.getPhieu().getMaPhieuTNSC() + "</div></html>");
            view.getLblValXe().setText("<html><div style='width:320px;'>" + thongTinXe + "</div></html>");
            view.getLblValKTV().setText("<html><div style='width:320px;'>" + yeuCauDangChon.getTenKyThuatVien() + "</div></html>");
            view.getLblValNgayYC().setText("<html><div style='width:320px;'>" + yeuCauDangChon.getNgayYeuCau().format(DT_FMT) + "</div></html>");

            danhSachChiTiet = yeuCauDao.getChiTietByMaPhieuYC(yeuCauDangChon.getMaPhieuYC());
            for (ChiTietYeuCauXuatKho ct : danhSachChiTiet) {
                PhuTung pt = ct.getPhuTung();
                if (!NghiepVuKho.duTon(pt, ct.getSoLuong())) {
                    duTonKho = false;
                }

                view.getTableModel().addRow(new Object[]{
                        pt.getMaSKU(),
                        pt.getTenPhuTung(),
                        pt.getViTriLuuKho(),
                        ct.getSoLuong(),
                        pt.getSoLuongTon()
                });
            }

            boolean isChoXuat = yeuCauDangChon.getTrangThai() == TrangThaiYeuCauXuat.CHO_XUAT;
            view.getPnlHopLoiTonKho().setVisible(!duTonKho && isChoXuat);
            view.getBtnTuChoi().setEnabled(isChoXuat);
            view.getBtnXacNhanXuat().setEnabled(isChoXuat && duTonKho);

            SwingUtilities.invokeLater(view::capNhatChieuCaoBangTon);
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void xacNhanXuatKho() {
        if (yeuCauDangChon == null || !duTonKho) return;
        try {
            // [VÁ LỖI THẤT THOÁT] Kiểm tra phiếu sửa chữa đã bị chốt Hóa đơn chưa
            boolean daCoHoaDon = yeuCauDao.kiemTraTonTaiHoaDonHopLe(yeuCauDangChon.getPhieu().getMaPhieuTNSC());
            if (daCoHoaDon) {
                JOptionPane.showMessageDialog(view,
                        "Không thể xuất kho!\nPhiếu sửa chữa " + yeuCauDangChon.getPhieu().getMaPhieuTNSC()
                                + " đã được Lễ tân lập Hóa đơn thanh toán.\n"
                                + "Để xuất thêm phụ tùng, Lễ tân cần Hủy hóa đơn hiện tại trước.",
                        "Chặn xuất kho", JOptionPane.ERROR_MESSAGE);
                return; // Chặn ngay lập tức, không thực hiện trừ kho
            }

            // Nếu chưa có hóa đơn, tiến hành trừ kho bình thường
            String maThuKho = thuKhoHienTai != null ? thuKhoHienTai.getMaNV() : util.PhienDangNhap.getHienTai().getMaNV();
            yeuCauDao.duyetXuatKho(
                    yeuCauDangChon.getMaPhieuYC(),
                    yeuCauDangChon.getPhieu().getMaPhieuTNSC(),
                    maThuKho,
                    danhSachChiTiet,
                    PhienDangNhap.getHienTai().getTenDangNhap()
            );
            JOptionPane.showMessageDialog(view,
                    "Xuất kho thành công cho phiếu " + yeuCauDangChon.getMaPhieuYC() + "!\nSố lượng tồn kho đã được trừ tự động.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            taiLai();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi khi duyệt xuất kho: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tuChoiYeuCau() {
        if (yeuCauDangChon == null) return;
        int cf = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc chắn muốn từ chối yêu cầu xuất kho " + yeuCauDangChon.getMaPhieuYC() + "?",
                "Xác nhận từ chối", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            try {
                String maThuKho = thuKhoHienTai != null ? thuKhoHienTai.getMaNV() : util.PhienDangNhap.getHienTai().getMaNV();
                if (!yeuCauDao.tuChoiYeuCau(yeuCauDangChon.getMaPhieuYC(), maThuKho,
                        PhienDangNhap.getHienTai().getTenDangNhap())) {
                    JOptionPane.showMessageDialog(view, "Yêu cầu " + yeuCauDangChon.getMaPhieuYC()
                            + " không còn chờ xuất nên không thể từ chối.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                }
                taiLai();
            } catch (SQLException ex) {
                util.UIComponents.baoLoiCsdl(view, ex);
            }
        }
    }
}