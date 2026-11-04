package controller;

import dao.PhieuYeuCauXuatKho_Dao;
import dao.PhuTung_Dao;
import dao.TienDoSuaChua_Dao;
import entity.*;
import gui.QLTienDoSuaChua;
import gui.TrangChuKyThuatVien;
import util.FormatUtil;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class QLTienDoSuaChuaController {

    private final QLTienDoSuaChua view;
    private final TienDoSuaChua_Dao tienDoDao;
    private final PhuTung_Dao phuTungDao;
    private final PhieuYeuCauXuatKho_Dao yeuCauDao;

    private NhanVien kyThuatVienHienTai;
    private static final String NHAN_CHO_SUA = "Chờ sửa"; // nhãn hiển thị của phiếu "Chờ kiểm tra"
    private String tabTrangThaiDangChon = "Tất cả";
    private List<PhieuTiepNhanVaSuaChua> danhSachPhieu = new ArrayList<>();
    private PhieuTiepNhanVaSuaChua phieuDangChon;
    private boolean daTai;

    private final List<ChiTietYeuCauXuatKho> danhSachPhuTungYeuCau = new ArrayList<>();
    private final List<JTextField> danhSachOInputSoLuong = new ArrayList<>();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat KM_FMT = new DecimalFormat("#,###");

    public QLTienDoSuaChuaController(QLTienDoSuaChua view) {
        this.view = view;
        this.tienDoDao = new TienDoSuaChua_Dao();
        this.phuTungDao = new PhuTung_Dao();
        this.yeuCauDao = new PhieuYeuCauXuatKho_Dao();

        initKyThuatVien();
        initController();
        hienChuaTai();
    }

    private void initKyThuatVien() {
        try {
            kyThuatVienHienTai = tienDoDao.findDefaultKyThuatVien();
            if (kyThuatVienHienTai != null) {
                view.capNhatThongTinKTVHeader(kyThuatVienHienTai.getHoTen());
                NhanVien dayDu = new dao.NhanVien_Dao().findById(kyThuatVienHienTai.getMaNV());
                view.setCaLamViec(dayDu == null ? null : dayDu.getCaLamViec());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initController() {
        view.getBtnMenuTrangChu().addActionListener(e -> dieuHuongVeTrangChu());
        view.getBtnMenuTienDo().addActionListener(e -> {
            view.hienThiManHinh(QLTienDoSuaChua.CARD_TIEN_DO);
            taiLai(phieuDangChon != null ? phieuDangChon.getMaPhieuTNSC() : null);
        });

        view.getBtnDangXuat().addActionListener(e -> {
            int cf = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                Window w = SwingUtilities.getWindowAncestor(view);
                if (w != null) w.dispose();
            }
        });

        // 1. Tìm kiếm phiếu sửa chữa: chỉ áp dụng khi bấm Tải dữ liệu hoặc nhấn Enter
        view.getBtnTaiDuLieu().addActionListener(e -> loadDanhSachPhieu(phieuDangChon != null ? phieuDangChon.getMaPhieuTNSC() : null));
        view.getTxtTimPhieu().addActionListener(e -> loadDanhSachPhieu(phieuDangChon != null ? phieuDangChon.getMaPhieuTNSC() : null));

        // 2. Các nút pill lọc trạng thái
        JButton[] pills = view.getBtnLocTrangThai();
        String[] tenTabs = view.getCacTabLoc();
        for (int i = 0; i < pills.length; i++) {
            final int idx = i;
            pills[i].addActionListener(e -> {
                tabTrangThaiDangChon = tenTabs[idx];
                for (int j = 0; j < pills.length; j++) {
                    view.capNhatStyleNutPill(pills[j], j == idx);
                }
            });
        }

        // 3. Khi đổi lựa chọn trong ComboBox trạng thái mới -> ẩn hộp báo lỗi cũ
        view.getCboTrangThaiMoi().addActionListener(e -> view.getPnlHopLoiTrangThai().setVisible(false));

        // 4. Click đúp vào dòng hạng mục dịch vụ để đổi nhanh trạng thái hạng mục
        view.getTblHangMuc().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && phieuDangChon != null) {
                    int row = view.getTblHangMuc().getSelectedRow();
                    if (row >= 0 && row < phieuDangChon.getDanhSachDichVu().size()) {
                        xuLyDoiTrangThaiHangMuc(phieuDangChon.getDanhSachDichVu().get(row));
                    }
                }
            }
        });

        // 5. Nút "Cập nhật trạng thái"
        view.getBtnCapNhatTrangThai().addActionListener(e -> xuLyCapNhatTrangThaiPhieu());

        // 6. Nút "Yêu cầu phụ tùng" -> Chuyển sang màn hình Lập phiếu yêu cầu phụ tùng
        view.getBtnMoYeuCauPhuTung().addActionListener(e -> moManHinhLapPhieuYeuCau());

        // 7. Nút "← Quay lại" từ màn hình Lập phiếu yêu cầu phụ tùng về Quản lý tiến độ
        view.getBtnQuayLai().addActionListener(e -> {
            view.hienThiManHinh(QLTienDoSuaChua.CARD_TIEN_DO);
            taiLai(phieuDangChon != null ? phieuDangChon.getMaPhieuTNSC() : null);
        });

        // 8. Nút "+ Thêm phụ tùng" & nhấn Enter trên ô tìm kiếm phụ tùng
        view.getBtnThemPhuTung().addActionListener(e -> moPopupChonPhuTung(view.getTxtTimPhuTung().getText()));
        view.getTxtTimPhuTung().addActionListener(e -> moPopupChonPhuTung(view.getTxtTimPhuTung().getText()));

        // 9. Nút "Gửi yêu cầu"
        view.getBtnGuiYeuCau().addActionListener(e -> xuLyGuiYeuCauPhuTung());
    }

    private void dieuHuongVeTrangChu() {
        Window w0 = SwingUtilities.getWindowAncestor(view);
        if (w0 instanceof gui.GiaoDienChinh) {
            ((gui.GiaoDienChinh) w0).moCard("TRANG_CHU_KTV");
            return;
        }
        Container parent = view.getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout) {
            ((CardLayout) parent.getLayout()).show(parent, "TRANG_CHU_KTV");
            return;
        }
        Window w = SwingUtilities.getWindowAncestor(view);
        if (w instanceof JFrame) {
            JFrame frame = (JFrame) w;
            frame.setContentPane(TrangChuKyThuatVien.createPanel());
            frame.revalidate();
            frame.repaint();
        }
    }

    /** Chưa bấm Tải dữ liệu: danh sách trống kèm gợi ý, khung chi tiết ở trạng thái chưa chọn. */
    private void hienChuaTai() {
        JPanel container = view.getPnlDanhSachPhieu();
        container.removeAll();
        JLabel lblGoiY = UIComponents.label(UIComponents.GOI_Y_TAI, UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
        lblGoiY.setBorder(new EmptyBorder(20, 12, 20, 12));
        container.add(lblGoiY);
        phieuDangChon = null;
        hienThiChiTietPhieu(null);
    }

    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    private void taiLai(String maPhieuCanChon) {
        if (daTai) {
            loadDanhSachPhieu(maPhieuCanChon);
        }
    }

    public void loadDanhSachPhieu(String maPhieuCanChon) {
        daTai = true;
        try {
            String kw = view.getTxtTimPhieu().getText();
            danhSachPhieu = tienDoDao.searchPhieuĐượcGiao("", kw, tabTrangThaiDangChon);

            PhieuTiepNhanVaSuaChua chon = null;
            if (maPhieuCanChon != null) {
                for (PhieuTiepNhanVaSuaChua p : danhSachPhieu) {
                    if (p.getMaPhieuTNSC().equalsIgnoreCase(maPhieuCanChon)) {
                        chon = p;
                        break;
                    }
                }
            }
            if (chon == null && !danhSachPhieu.isEmpty()) {
                chon = danhSachPhieu.get(0);
            }
            phieuDangChon = chon;
            veDanhSachThePhieu();
            hienThiChiTietPhieu(phieuDangChon);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải danh sách phiếu sửa chữa: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void veDanhSachThePhieu() {
        JPanel container = view.getPnlDanhSachPhieu();
        container.removeAll();

        if (danhSachPhieu.isEmpty()) {
            JLabel lblTrong = UIComponents.label("Không có phiếu sửa chữa phù hợp.", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
            lblTrong.setBorder(new EmptyBorder(20, 12, 20, 12));
            container.add(lblTrong);
        } else {
            for (PhieuTiepNhanVaSuaChua p : danhSachPhieu) {
                boolean isSelected = phieuDangChon != null && p.getMaPhieuTNSC().equals(phieuDangChon.getMaPhieuTNSC());
                JPanel cardItem = view.taoThePhieuItem(p, isSelected);
                cardItem.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        phieuDangChon = p;
                        view.getPnlHopLoiTrangThai().setVisible(false);
                        veDanhSachThePhieu();
                        hienThiChiTietPhieu(p);
                    }
                });
                container.add(cardItem);
                container.add(Box.createVerticalStrut(10));
            }
        }
        container.revalidate();
        container.repaint();
    }

    private void hienThiChiTietPhieu(PhieuTiepNhanVaSuaChua p) {
        view.getModelHangMuc().setRowCount(0);
        view.getPnlHopLoiTrangThai().setVisible(false);

        if (p == null) {
            view.getLblMaPhieuChiTiet().setText("Chưa chọn phiếu");
            view.getBadgeTrangThaiPhieu().set("Trống", UIConstants.TEXT_MUTED);
            view.getThanhTienDo().setPercent(0);
            view.getLblValBienSo().setText("---");
            view.getLblValXe().setText("---");
            view.getLblValKhachHang().setText("---");
            view.getLblValTiepNhan().setText("---");
            view.getLblValDuKien().setText("---");
            view.getLblValSoKm().setText("---");
            view.getLblValNhienLieu().setText("---");
            view.getLblValYeuCau().setText("---");
            view.getBtnCapNhatTrangThai().setEnabled(false);
            view.getBtnMoYeuCauPhuTung().setEnabled(false);
            return;
        }

        view.getLblMaPhieuChiTiet().setText(p.getMaPhieuTNSC());

        String tt = p.getTrangThaiHienThiKTV();
        int pct = TienDoSuaChua_Dao.phanTramTienDo(p);
        String badgeText = p.getTrangThai() == TrangThaiPhieu.DANG_SUA ? (tt + " • " + pct + "%") : tt;
        view.getBadgeTrangThaiPhieu().set(badgeText, view.mauTrangThai(tt));
        view.getThanhTienDo().setPercent(pct);

        view.getLblValBienSo().setText(p.getXe().getBienSo());
        view.getLblValXe().setText(p.getThongTinXeNgan());
        view.getLblValKhachHang().setText(p.getXe().getKhachHang().getHoTen());
        view.getLblValTiepNhan().setText(p.getThoiGianTiepNhan() != null ? p.getThoiGianTiepNhan().format(DT_FMT) : "---");
        view.getLblValDuKien().setText(p.getThoiGianDuKien() != null ? p.getThoiGianDuKien().format(DT_FMT) : "---");
        view.getLblValSoKm().setText(KM_FMT.format(p.getSoKm()) + " km");
        view.getLblValNhienLieu().setText(Math.round(p.getMucNhienLieu()) + "%");
        view.getLblValYeuCau().setText(p.getYeuCauBanDau().isEmpty() ? "Kiểm tra tổng quát" : p.getYeuCauBanDau());

        for (ChiTietSuaChua_DichVu ct : p.getDanhSachDichVu()) {
            view.getModelHangMuc().addRow(new Object[]{
                    ct.getDichVu().getTenDichVu(),
                    ct.getTrangThaiTienDo(),
                    ct.getTrangThaiTienDo()
            });
        }

        view.getCboTrangThaiMoi().setSelectedItem("Trạng thái mới: " + tt);

        boolean daThanhToan = false;
        try {
            daThanhToan = tienDoDao.isPhieuDaThanhToan(p.getMaPhieuTNSC());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        view.getBtnCapNhatTrangThai().setEnabled(!daThanhToan);
        view.getBtnMoYeuCauPhuTung().setEnabled(!daThanhToan && p.getTrangThai() != TrangThaiPhieu.HOAN_THANH);
    }

    private void xuLyCapNhatTrangThaiPhieu() {
        if (phieuDangChon == null) return;

        String rawSelected = (String) view.getCboTrangThaiMoi().getSelectedItem();
        if (rawSelected == null) return;
        String trangThaiMoi = rawSelected.replace("Trạng thái mới:", "").trim();
        String trangThaiHienTai = phieuDangChon.getTrangThaiHienThiKTV();

        if (!kiemTraLuongTrangThaiHopLe(trangThaiHienTai, trangThaiMoi)) {
            view.getLblThongBaoLoiTrangThai().setText(
                    "Trạng thái không hợp lệ: không thể chuyển từ '" + trangThaiHienTai + "' sang '" + trangThaiMoi + "'. Vui lòng chọn lại."
            );
            view.getPnlHopLoiTrangThai().setVisible(true);
            view.revalidate();
            return;
        }

        view.getPnlHopLoiTrangThai().setVisible(false);
        try {
            tienDoDao.capNhatTrangThaiPhieu(phieuDangChon.getMaPhieuTNSC(), trangThaiMoi, "");
            taiLai(phieuDangChon.getMaPhieuTNSC());
            JOptionPane.showMessageDialog(view,
                    "Đã cập nhật trạng thái phiếu " + phieuDangChon.getMaPhieuTNSC() + " sang '" + trangThaiMoi + "'.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi cập nhật trạng thái: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean kiemTraLuongTrangThaiHopLe(String hienTai, String moi) {
        if (hienTai.equalsIgnoreCase(moi)) return true;
        String hoanThanh = TrangThaiPhieu.HOAN_THANH.getText();
        if (NHAN_CHO_SUA.equalsIgnoreCase(hienTai) && hoanThanh.equalsIgnoreCase(moi)) {
            return false;
        }
        if (hoanThanh.equalsIgnoreCase(hienTai) && NHAN_CHO_SUA.equalsIgnoreCase(moi)) {
            return false;
        }
        return true;
    }

    private void xuLyDoiTrangThaiHangMuc(ChiTietSuaChua_DichVu ct) {
        try {
            if (tienDoDao.isPhieuDaThanhToan(ct.getPhieu().getMaPhieuTNSC())) {
                JOptionPane.showMessageDialog(view, "Phiếu sửa chữa này đã thanh toán và bị khóa dữ liệu!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TrangThaiTienDo moi = (TrangThaiTienDo) JOptionPane.showInputDialog(
                    view,
                    "Chọn trạng thái tiến độ mới cho hạng mục:\n" + ct.getDichVu().getTenDichVu(),
                    "Cập nhật tiến độ hạng mục",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    TrangThaiTienDo.values(),
                    ct.getTrangThaiTienDo()
            );
            if (moi != null && moi != ct.getTrangThaiTienDo()) {
                if (ct.getTrangThaiTienDo() == TrangThaiTienDo.CHO_SUA && moi == TrangThaiTienDo.HOAN_THANH) {
                    view.getLblThongBaoLoiTrangThai().setText(
                            "Trạng thái không hợp lệ: không thể chuyển từ 'Chờ sửa' sang 'Hoàn thành'. Vui lòng chọn lại."
                    );
                    view.getPnlHopLoiTrangThai().setVisible(true);
                    return;
                }
                tienDoDao.capNhatTienDoHangMuc(ct.getPhieu().getMaPhieuTNSC(), ct.getDichVu().getMaDichVu(), moi);
                taiLai(ct.getPhieu().getMaPhieuTNSC());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void chonPhieuVaMoManHinh(String maPhieuTNSC, boolean moYeuCauPhuTung) {
        if (maPhieuTNSC != null) {
            tabTrangThaiDangChon = "Tất cả";
            JButton[] pills = view.getBtnLocTrangThai();
            for (int i = 0; i < pills.length; i++) {
                view.capNhatStyleNutPill(pills[i], i == 0);
            }
            loadDanhSachPhieu(maPhieuTNSC);
        }
        if (moYeuCauPhuTung && phieuDangChon != null) {
            moManHinhLapPhieuYeuCau();
        } else {
            view.hienThiManHinh(QLTienDoSuaChua.CARD_TIEN_DO);
        }
    }

    private void moManHinhLapPhieuYeuCau() {
        if (phieuDangChon == null) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một phiếu sửa chữa trước khi lập yêu cầu phụ tùng!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String maPYC = yeuCauDao.nextId();
            view.getTxtMaYeuCau().setText(maPYC);
        } catch (SQLException ex) {
            view.getTxtMaYeuCau().setText("PYC00001");
        }

        view.getTxtPhieuSuaChuaYC().setText(phieuDangChon.getMaPhieuTNSC());
        view.getTxtXeYC().setText(phieuDangChon.getThongTinXeDayDu());

        String tenKTV = kyThuatVienHienTai != null ? kyThuatVienHienTai.getHoTen() : "Lê Văn Thợ Máy";
        if (!phieuDangChon.getDanhSachDichVu().isEmpty()
                && phieuDangChon.getDanhSachDichVu().get(0).getKyThuatVien() != null) {
            tenKTV = phieuDangChon.getDanhSachDichVu().get(0).getKyThuatVien().getHoTen();
        }
        view.getTxtKyThuatVienYC().setText(tenKTV);
        view.getTxtNgayYeuCau().setText(LocalDateTime.now().format(DT_FMT));
        view.getBadgeTrangThaiYC().set("Chờ xuất", UIConstants.STATUS_WARNING);

        // Bắt đầu rỗng: kỹ thuật viên tự tìm và thêm đúng phụ tùng cần cho phiếu này.
        danhSachPhuTungYeuCau.clear();

        UIComponents.showError(view.getLblLoiSoLuongPT(), null);
        veDanhSachPhuTungYeuCau();
        view.hienThiManHinh(QLTienDoSuaChua.CARD_YEU_CAU_PT);
    }

    private void veDanhSachPhuTungYeuCau() {
        JPanel container = view.getPnlDanhSachPhuTungChon();
        container.removeAll();
        danhSachOInputSoLuong.clear();

        if (danhSachPhuTungYeuCau.isEmpty()) {
            JLabel lblEmpty = UIComponents.label(
                    "Chưa có phụ tùng nào trong phiếu. Nhấn '+ Thêm phụ tùng' để chọn từ kho.",
                    UIConstants.FONT_BODY, UIConstants.TEXT_MUTED
            );
            lblEmpty.setBorder(new EmptyBorder(24, 16, 24, 16));
            container.add(lblEmpty);
        } else {
            for (int i = 0; i < danhSachPhuTungYeuCau.size(); i++) {
                final int index = i;
                ChiTietYeuCauXuatKho item = danhSachPhuTungYeuCau.get(i);
                container.add(taoDongPhuTungYeuCau(item, index));
            }
        }

        container.revalidate();
        container.repaint();
    }

    private JPanel taoDongPhuTungYeuCau(ChiTietYeuCauXuatKho item, int index) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(UIConstants.CARD_BG);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIComponents.tint(UIConstants.BORDER_COLOR, 0.5f)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        row.add(UIComponents.label(item.getPhuTung().getMaSKU(), UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK),
                QLTienDoSuaChua.cotGrid(0, 0.16));
        row.add(UIComponents.label(item.getPhuTung().getTenPhuTung(), UIConstants.FONT_BODY.deriveFont(13.5f), UIConstants.TEXT_DARK),
                QLTienDoSuaChua.cotGrid(1, 0.34));
        row.add(UIComponents.label(item.getPhuTung().getHangSanXuat(), UIConstants.FONT_BODY.deriveFont(13.5f), UIConstants.TEXT_DARK),
                QLTienDoSuaChua.cotGrid(2, 0.14));

        JPanel pnlTon = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel lblTon = UIComponents.label(String.valueOf(item.getPhuTung().getSoLuongTon()), UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
        pnlTon.add(lblTon);
        if (item.getPhuTung().getSoLuongTon() <= item.getPhuTung().getNguongCanhBao()) {
            pnlTon.add(new JLabel(new UIComponents.LineIcon(UIComponents.Loai.ALERT, UIConstants.STATUS_WARNING, 16)));
        }
        row.add(pnlTon, QLTienDoSuaChua.cotGrid(3, 0.12));

        JPanel pnlStepper = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton btnTru = taoNutStepper("−");
        JTextField txtSL = new JTextField(String.valueOf(item.getSoLuong()), 3);
        txtSL.setHorizontalAlignment(SwingConstants.CENTER);
        UIComponents.styleInput(txtSL);
        txtSL.setPreferredSize(new Dimension(48, 34));
        JButton btnCong = taoNutStepper("+");

        danhSachOInputSoLuong.add(txtSL);

        btnTru.addActionListener(e -> {
            int val = docSoLuong(txtSL.getText()) - 1;
            txtSL.setText(String.valueOf(val));
            kiemTraHopLeTatCaSoLuong();
        });

        btnCong.addActionListener(e -> {
            int val = docSoLuong(txtSL.getText()) + 1;
            txtSL.setText(String.valueOf(val));
            kiemTraHopLeTatCaSoLuong();
        });

        txtSL.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                kiemTraHopLeTatCaSoLuong();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                kiemTraHopLeTatCaSoLuong();
            }
            @Override public void changedUpdate(DocumentEvent e) {
                kiemTraHopLeTatCaSoLuong();
            }
        });

        pnlStepper.add(btnTru);
        pnlStepper.add(txtSL);
        pnlStepper.add(btnCong);
        row.add(pnlStepper, QLTienDoSuaChua.cotGrid(4, 0.18));

        JButton btnXoa = UIComponents.nutIcon(new UIComponents.LineIcon(UIComponents.Loai.TRASH, UIConstants.TEXT_MUTED, 17));
        btnXoa.addActionListener(e -> {
            danhSachPhuTungYeuCau.remove(index);
            veDanhSachPhuTungYeuCau();
            kiemTraHopLeTatCaSoLuong();
        });
        JPanel pnlXoa = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlXoa.add(btnXoa);
        row.add(pnlXoa, QLTienDoSuaChua.cotGrid(5, 0.06));

        return row;
    }

    private JButton taoNutStepper(String kyHieu) {
        JButton b = UIComponents.nutPhu(kyHieu);
        b.setBorder(new EmptyBorder(4, 10, 4, 10));
        b.setPreferredSize(new Dimension(32, 34));
        return b;
    }

    private int docSoLuong(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean kiemTraHopLeTatCaSoLuong() {
        String thongBaoLoi = null;
        for (int i = 0; i < danhSachPhuTungYeuCau.size() && i < danhSachOInputSoLuong.size(); i++) {
            JTextField txt = danhSachOInputSoLuong.get(i);
            ChiTietYeuCauXuatKho ct = danhSachPhuTungYeuCau.get(i);
            String raw = txt.getText().trim();

            boolean loiDong = false;
            try {
                int sl = Integer.parseInt(raw);
                if (sl <= 0) {
                    loiDong = true;
                    if (thongBaoLoi == null) thongBaoLoi = "Số lượng phải là số nguyên dương";
                } else if (sl > ct.getPhuTung().getSoLuongTon()) {
                    loiDong = true;
                    if (thongBaoLoi == null) {
                        thongBaoLoi = "Mã " + ct.getPhuTung().getMaSKU() + ": Tồn kho trên hệ thống không đủ (Tồn hiện tại: " + ct.getPhuTung().getSoLuongTon() + ")";
                    }
                } else {
                    ct.setSoLuong(sl);
                }
            } catch (NumberFormatException ex) {
                loiDong = true;
                if (thongBaoLoi == null) thongBaoLoi = "Số lượng phải là số nguyên dương";
            }

            UIComponents.setError(txt, loiDong);
            txt.setForeground(loiDong ? UIConstants.STATUS_DANGER : UIConstants.TEXT_DARK);
        }

        UIComponents.showError(view.getLblLoiSoLuongPT(), thongBaoLoi);
        return thongBaoLoi == null;
    }

    private void moPopupChonPhuTung(String tuKhoaBanDau) {
        try {
            List<PhuTung> dsPhuTung = phuTungDao.search(tuKhoaBanDau);
            if (dsPhuTung.isEmpty()) {
                dsPhuTung = phuTungDao.findAll();
            }
            if (dsPhuTung.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Danh mục phụ tùng trong kho đang trống!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DefaultTableModel modelChon = UIComponents.modelKhongSua(
                    "Mã SKU", "Tên phụ tùng", "Hãng", "Vị trí", "Tồn kho", "Giá bán lẻ"
            );
            for (PhuTung pt : dsPhuTung) {
                modelChon.addRow(new Object[]{
                        pt.getMaSKU(),
                        pt.getTenPhuTung(),
                        pt.getHangSanXuat(),
                        pt.getViTriLuuKho(),
                        pt.getSoLuongTon(),
                        FormatUtil.formatCurrency(pt.getGiaBanLe())
                });
            }

            JTable tblChon = new JTable(modelChon);
            UIComponents.styleTable(tblChon);
            tblChon.setRowHeight(40);
            UIComponents.datDoRong(tblChon, 100, 220, 100, 80, 70, 110);
            if (tblChon.getRowCount() > 0) tblChon.setRowSelectionInterval(0, 0);

            JScrollPane sp = UIComponents.cuonBang(tblChon);
            sp.setPreferredSize(new Dimension(680, 280));

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    sp,
                    "Chọn phụ tùng từ kho vật tư AUTORA",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (confirm == JOptionPane.OK_OPTION) {
                int selectedRow = tblChon.getSelectedRow();
                if (selectedRow >= 0) {
                    PhuTung ptChon = dsPhuTung.get(selectedRow);
                    themHoacTangPhuTung(ptChon);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi truy vấn danh mục phụ tùng: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themHoacTangPhuTung(PhuTung pt) {
        for (int i = 0; i < danhSachPhuTungYeuCau.size(); i++) {
            ChiTietYeuCauXuatKho ct = danhSachPhuTungYeuCau.get(i);
            if (ct.getPhuTung().getMaSKU().equalsIgnoreCase(pt.getMaSKU())) {
                int slMoi = ct.getSoLuong() + 1;
                if (i < danhSachOInputSoLuong.size()) {
                    danhSachOInputSoLuong.get(i).setText(String.valueOf(slMoi));
                }
                kiemTraHopLeTatCaSoLuong();
                return;
            }
        }

        danhSachPhuTungYeuCau.add(new ChiTietYeuCauXuatKho(
                new PhieuYeuCauXuatKho(view.getTxtMaYeuCau().getText()), pt, 1));
        veDanhSachPhuTungYeuCau();
        kiemTraHopLeTatCaSoLuong();
    }

    private void xuLyGuiYeuCauPhuTung() {
        if (danhSachPhuTungYeuCau.isEmpty()) {
            UIComponents.showError(view.getLblLoiSoLuongPT(), "Vui lòng chọn ít nhất một phụ tùng cần xuất kho!");
            return;
        }
        if (!kiemTraHopLeTatCaSoLuong()) {
            return;
        }

        try {
            String maPYC = yeuCauDao.nextId();
            view.getTxtMaYeuCau().setText(maPYC);

            String maKTV = kyThuatVienHienTai != null ? kyThuatVienHienTai.getMaNV() : "NV0005";
            PhieuYeuCauXuatKho phieu = new PhieuYeuCauXuatKho(
                    maPYC,
                    LocalDateTime.now(),
                    null,
                    TrangThaiYeuCauXuat.CHO_XUAT,
                    new PhieuTiepNhanVaSuaChua(phieuDangChon.getMaPhieuTNSC()),
                    new NhanVien(maKTV),
                    null
            );

            yeuCauDao.insertWithDetails(phieu, danhSachPhuTungYeuCau);
            JOptionPane.showMessageDialog(view,
                    "Đã gửi phiếu yêu cầu xuất kho " + maPYC + " thành công!\nPhiếu sửa chữa đã chuyển sang trạng thái 'Chờ phụ tùng'.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            view.hienThiManHinh(QLTienDoSuaChua.CARD_TIEN_DO);
            taiLai(phieuDangChon.getMaPhieuTNSC());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi lưu phiếu yêu cầu phụ tùng: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }
}