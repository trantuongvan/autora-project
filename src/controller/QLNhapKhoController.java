package controller;

import dao.NhaCungCap_Dao;
import dao.PhieuNhapKho_Dao;
import dao.PhieuYeuCauXuatKho_Dao;
import dao.PhuTung_Dao;
import entity.ChiTietNhapKho;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhieuNhapKho;
import entity.PhuTung;
import entity.TrangThaiYeuCauXuat;
import gui.QLNhapKho;
import gui.TrangChuThuKho;
import gui.TrangChuThuKho.MenuThuKho;
import util.FormatUtil;
import util.UIComponents;
import util.UIConstants;
import util.XuatFile;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QLNhapKhoController {

    private final QLNhapKho view;
    private final PhieuNhapKho_Dao nhapKhoDao;
    private final NhaCungCap_Dao nccDao;
    private final PhuTung_Dao phuTungDao;
    private final PhieuYeuCauXuatKho_Dao yeuCauDao;

    private NhanVien thuKhoHienTai;
    private List<PhieuNhapKho> danhSachPhieuNhap = new ArrayList<>();
    private boolean daTai;

    private final List<ChiTietNhapKho> dsChiTietModal = new ArrayList<>();
    private final List<JTextField> dsInputSoLuong = new ArrayList<>();
    private final List<JTextField> dsInputDonGia = new ArrayList<>();
    private final List<JLabel> dsLabelThanhTien = new ArrayList<>();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_PARSE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /** Thủ kho đã tự chọn nhà cung cấp trên phiếu đang mở: ngừng tự gợi ý theo lịch sử nhập. */
    private boolean nccDoNguoiChon;
    private boolean dangTuChonNcc;

    private void goiYNhaCungCap() {
        if (nccDoNguoiChon || dsChiTietModal.isEmpty()) {
            return;
        }
        try {
            NhaCungCap ncc = nhapKhoDao.findNhaCungCapGanNhat(dsChiTietModal.get(0).getPhuTung().getMaSKU());
            if (ncc != null) {
                dangTuChonNcc = true;
                view.getCboModalNCC().setSelectedItem(ncc);
                dangTuChonNcc = false;
            }
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }

    public QLNhapKhoController(QLNhapKho view) {
        this.view = view;
        this.nhapKhoDao = new PhieuNhapKho_Dao();
        this.nccDao = new NhaCungCap_Dao();
        this.phuTungDao = new PhuTung_Dao();
        this.yeuCauDao = new PhieuYeuCauXuatKho_Dao();

        initHeaderAndNCC();
        initController();
        UIComponents.tuNgayGanDay(view.getTxtTuNgay());
        UIComponents.boLocMacDinhKhiTim(view.getTxtTimPhieu(), () -> view.getTxtTuNgay().setText(""));
        UIComponents.tuTaiLai(this::loadData, view.getTxtTimPhieu(), view.getCboLocNCC(), view.getTxtTuNgay(),
                view.getTxtDenNgay());
        loadData();
    }

    private void initHeaderAndNCC() {
        try {
            thuKhoHienTai = nhapKhoDao.findDefaultThuKho();
            if (thuKhoHienTai != null) {
                TrangChuThuKho.capNhatHeaderThuKho(view.getLblAvatar(), view.getLblTenThuKhoHeader(), thuKhoHienTai.getHoTen());
                view.getLblThuKhoModal().setText(thuKhoHienTai.getHoTen());
            }

            int soCho = yeuCauDao.countByTrangThai(TrangThaiYeuCauXuat.CHO_XUAT);
            view.getLblBadgeSidebarDuyetXuat().setText(String.valueOf(soCho));
            view.getLblBadgeSidebarDuyetXuat().setVisible(soCho > 0);

            List<NhaCungCap> dsNCC = nccDao.findAll();
            view.getCboLocNCC().removeAllItems();
            view.getCboLocNCC().addItem("Nhà cung cấp: Tất cả");
            view.getCboModalNCC().removeAllItems();
            for (NhaCungCap ncc : dsNCC) {
                view.getCboLocNCC().addItem(ncc.getTenNCC());
                view.getCboModalNCC().addItem(ncc);
            }
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void initController() {
        view.getCboModalNCC().addActionListener(e -> {
            if (!dangTuChonNcc) {
                nccDoNguoiChon = true;
            }
        });
        view.getBtnMenuTrangChu().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.TRANG_CHU, null));
        view.getBtnMenuPhuTung().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.PHU_TUNG, null));
        view.getBtnMenuNhapKho().addActionListener(e -> loadData());
        view.getBtnMenuDuyetXuat().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.DUYET_XUAT, null));

        view.getBtnDangXuat().addActionListener(e -> {
            int cf = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                Window w = SwingUtilities.getWindowAncestor(view);
                if (w != null) w.dispose();
            }
        });

        view.getTxtTimPhieu().addActionListener(e -> loadData());
        view.getBtnTaiDuLieu().addActionListener(e -> loadData());

        view.getBtnTaoPhieuMoi().addActionListener(e -> moPopupLapPhieuNhap(null));
        view.getBtnNhapExcel().addActionListener(e -> nhapTuExcel());
        view.getBtnDongModal().addActionListener(e -> view.hienThiModalLapPhieu(false));
        view.getBtnHuyModal().addActionListener(e -> view.hienThiModalLapPhieu(false));

        view.getTxtTimPhuTungModal().addActionListener(e -> chonPhuTungThemVaoPhieuNhap(view.getTxtTimPhuTungModal().getText()));
        view.getBtnLuuPhieuNhap().addActionListener(e -> xuLyLuuPhieuNhap());

        view.getTblPhieuNhap().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblPhieuNhap().rowAtPoint(e.getPoint());
                int col = view.getTblPhieuNhap().columnAtPoint(e.getPoint());
                if (row >= 0 && row < danhSachPhieuNhap.size() && (col == 6 || e.getClickCount() == 2)) {
                    xemChiTietPhieuNhap(danhSachPhieuNhap.get(row));
                }
            }
        });
    }

    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    private void taiLai() {
        if (daTai) {
            loadData();
        }
    }

    public void loadData() {
        daTai = true;
        UIComponents.goiYChuaTai(view.getTblPhieuNhap(), false);
        try {
            List<PhieuNhapKho> all = nhapKhoDao.search("", "Tất cả", null, null);
            String kw = view.getTxtTimPhieu().getText();
            String ncc = (String) view.getCboLocNCC().getSelectedItem();
            if (ncc != null && ncc.startsWith("Nhà cung cấp:")) ncc = "Tất cả";

            LocalDate tuNgay = parseNgay(view.getTxtTuNgay().getText());
            LocalDate denNgay = parseNgay(view.getTxtDenNgay().getText());

            danhSachPhieuNhap = nhapKhoDao.search(kw, ncc, tuNgay, denNgay);

            view.getTableModel().setRowCount(0);
            for (PhieuNhapKho pn : danhSachPhieuNhap) {
                view.getTableModel().addRow(new Object[]{
                        pn.getMaPhieuNhap(),
                        pn.getNgayNhap().format(DT_FMT),
                        pn.getNhaCungCap().getTenNCC(),
                        pn.getSoMatHang(),
                        FormatUtil.formatCurrency(pn.getTongTienNhap()),
                        pn.getThuKho().getHoTen(),
                        "Xem chi tiết →"
                });
            }

            view.getLblFooterSoPhieu().setText("Hiển thị " + danhSachPhieuNhap.size() + " / " + all.size() + " phiếu");
            view.getLblCapNhatLuc().setText("Cập nhật lúc " + LocalTime.now().format(TIME_FMT));
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private LocalDate parseNgay(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim(), DATE_PARSE_FMT);
        } catch (Exception ex) {
            return null;
        }
    }

    public void moPopupLapPhieuNhap(String maSKUMacDinh) {
        try {
            String maPN = nhapKhoDao.nextId();
            view.getLblMaPhieuNhapModal().setText(maPN);
        } catch (SQLException ex) {
            view.getLblMaPhieuNhapModal().setText("PN031120260001");
        }
        view.getLblNgayNhapModal().setText(LocalDateTime.now().format(DT_FMT));
        view.getTxtTimPhuTungModal().setText("");
        UIComponents.showError(view.getLblLoiModal(), null);

        dsChiTietModal.clear();
        nccDoNguoiChon = false;
        try {
            String maPhieu = view.getLblMaPhieuNhapModal().getText();
            if (maSKUMacDinh != null && !maSKUMacDinh.isEmpty()) {
                PhuTung pt = phuTungDao.findById(maSKUMacDinh);
                if (pt != null) {
                    dsChiTietModal.add(new ChiTietNhapKho(new PhieuNhapKho(maPhieu), pt, NghiepVuKho.soLuongNhapDeXuat(pt), pt.getGiaNhap()));
                }
            } else {
                List<PhuTung> dsCanNhap = phuTungDao.findDuoiNguongCanhBao("");
                if (dsCanNhap.isEmpty()) dsCanNhap = phuTungDao.findAll();
                int count = 0;
                for (PhuTung pt : dsCanNhap) {
                    if (count >= 2) break;
                    dsChiTietModal.add(new ChiTietNhapKho(new PhieuNhapKho(maPhieu), pt, NghiepVuKho.soLuongNhapDeXuat(pt), pt.getGiaNhap()));
                    count++;
                }
            }
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }

        veBangChiTietModal();
        tinhVaKiemTraChiTietModal();
        view.hienThiModalLapPhieu(true);
    }

    private void veBangChiTietModal() {
        JPanel container = view.getPnlDanhSachChiTietModal();
        container.removeAll();
        dsInputSoLuong.clear();
        dsInputDonGia.clear();
        dsLabelThanhTien.clear();

        if (dsChiTietModal.isEmpty()) {
            JLabel lblEmpty = UIComponents.label(
                    "Chưa có phụ tùng nào. Gõ mã SKU hoặc tên phụ tùng vào ô tìm kiếm phía trên và nhấn Enter.",
                    UIConstants.FONT_BODY, UIConstants.TEXT_MUTED
            );
            lblEmpty.setBorder(new EmptyBorder(20, 14, 20, 14));
            container.add(lblEmpty);
        } else {
            for (int i = 0; i < dsChiTietModal.size(); i++) {
                final int idx = i;
                ChiTietNhapKho ct = dsChiTietModal.get(i);
                container.add(taoDongChiTietModal(ct, idx));
            }
        }
        container.revalidate();
        container.repaint();
        goiYNhaCungCap();
    }

    private JPanel taoDongChiTietModal(ChiTietNhapKho ct, int idx) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(UIConstants.CARD_BG);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIComponents.tint(UIConstants.BORDER_COLOR, 0.5f)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        row.add(UIComponents.label(ct.getPhuTung().getMaSKU(), UIConstants.FONT_FORM_LABEL.deriveFont(13f), UIConstants.TEXT_DARK),
                QLNhapKho.cotModal(0, 0.16));
        row.add(UIComponents.label(ct.getPhuTung().getTenPhuTung(), UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_DARK),
                QLNhapKho.cotModal(1, 0.34));

        JTextField txtSL = new JTextField(String.valueOf(ct.getSoLuongNhap()), 4);
        UIComponents.styleInput(txtSL);
        txtSL.setPreferredSize(new Dimension(64, 32));
        dsInputSoLuong.add(txtSL);
        row.add(txtSL, QLNhapKho.cotModal(2, 0.14));

        JTextField txtGia = new JTextField(FormatUtil.formatCurrency(ct.getGiaNhapThucTe()), 7);
        UIComponents.styleInput(txtGia);
        txtGia.setPreferredSize(new Dimension(88, 32));
        dsInputDonGia.add(txtGia);
        row.add(txtGia, QLNhapKho.cotModal(3, 0.14));

        JLabel lblThanhTien = UIComponents.label(FormatUtil.formatCurrency(NghiepVuKho.thanhTien(ct)), UIConstants.FONT_FORM_LABEL.deriveFont(13f), UIConstants.TEXT_DARK);
        dsLabelThanhTien.add(lblThanhTien);
        row.add(lblThanhTien, QLNhapKho.cotModal(4, 0.16));

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                tinhVaKiemTraChiTietModal();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                tinhVaKiemTraChiTietModal();
            }
            @Override public void changedUpdate(DocumentEvent e) {
                tinhVaKiemTraChiTietModal();
            }
        };
        txtSL.getDocument().addDocumentListener(dl);
        txtGia.getDocument().addDocumentListener(dl);

        JButton btnXoa = UIComponents.nutIcon(new UIComponents.LineIcon(UIComponents.Loai.TRASH, UIConstants.TEXT_MUTED, 16));
        btnXoa.addActionListener(e -> {
            dsChiTietModal.remove(idx);
            veBangChiTietModal();
            tinhVaKiemTraChiTietModal();
        });
        row.add(btnXoa, QLNhapKho.cotModal(5, 0.06));
        return row;
    }

    private boolean tinhVaKiemTraChiTietModal() {
        String thongBaoLoi = null;
        double tongTien = 0;

        for (int i = 0; i < dsChiTietModal.size() && i < dsInputSoLuong.size(); i++) {
            ChiTietNhapKho ct = dsChiTietModal.get(i);
            JTextField txtSL = dsInputSoLuong.get(i);
            JTextField txtGia = dsInputDonGia.get(i);
            JLabel lblTT = dsLabelThanhTien.get(i);

            boolean loiSL = false;
            boolean loiGia = false;
            int sl = 0;
            double gia = 0;

            try {
                sl = Integer.parseInt(txtSL.getText().trim());
                if (sl <= 0) {
                    loiSL = true;
                    if (thongBaoLoi == null) thongBaoLoi = "Số lượng nhập phải là số nguyên dương";
                }
            } catch (NumberFormatException ex) {
                loiSL = true;
                if (thongBaoLoi == null) thongBaoLoi = "Số lượng nhập phải là số nguyên dương";
            }

            try {
                gia = FormatUtil.parseCurrency(txtGia.getText());
                if (gia <= 0) throw new NumberFormatException();
                if (!NghiepVuKho.giaNhapHopLe(ct.getPhuTung(), gia)) {
                    loiGia = true;
                    if (thongBaoLoi == null) {
                        thongBaoLoi = "Đơn giá nhập của mã " + ct.getPhuTung().getMaSKU()
                                + " cao hơn giá bán lẻ. Hãy tăng giá bán lẻ ở Phụ tùng & Kho trước khi nhập.";
                    }
                }
            } catch (NumberFormatException ex) {
                loiGia = true;
                if (thongBaoLoi == null) thongBaoLoi = "Đơn giá nhập phải lớn hơn 0";
            }

            txtSL.setForeground(loiSL ? UIConstants.STATUS_DANGER : UIConstants.TEXT_DARK);
            txtSL.setFont(loiSL ? UIConstants.FONT_FORM_LABEL.deriveFont(13f) : UIConstants.FONT_BODY.deriveFont(13f));
            UIComponents.setError(txtSL, loiSL);
            UIComponents.setError(txtGia, loiGia);

            if (!loiSL && !loiGia) {
                ct.setSoLuongNhap(sl);
                ct.setGiaNhapThucTe(gia);
                double tt = NghiepVuKho.thanhTien(ct);
                tongTien += tt;
                lblTT.setText(FormatUtil.formatCurrency(tt));
            } else {
                lblTT.setText("—");
            }
        }

        view.getLblTongTienModal().setText("Tổng tiền nhập   " + FormatUtil.formatCurrency(tongTien));
        UIComponents.showError(view.getLblLoiModal(), thongBaoLoi);
        return thongBaoLoi == null;
    }

    private void chonPhuTungThemVaoPhieuNhap(String tuKhoa) {
        try {
            String kw = tuKhoa == null ? "" : tuKhoa.trim();
            List<PhuTung> ds;

            if (kw.isEmpty()) {
                ds = phuTungDao.findAll();
            } else {
                ds = phuTungDao.search(kw);
                if (ds.isEmpty()) {
                    UIComponents.showError(view.getLblLoiModal(),
                            "Không tìm thấy phụ tùng nào khớp với từ khóa: '" + kw + "'");
                    return;
                }
            }

            if (ds.isEmpty()) {
                UIComponents.showError(view.getLblLoiModal(), "Danh mục phụ tùng trong kho đang trống!");
                return;
            }

            PhuTung ptChon = null;

            if (!kw.isEmpty() && ds.size() == 1) {
                ptChon = ds.get(0);
            } else {
                DefaultTableModel modelChon = UIComponents.modelKhongSua(
                        "Mã SKU", "Tên phụ tùng", "Hãng", "Vị trí kho", "Tồn hiện tại", "Giá nhập chuẩn"
                );
                for (PhuTung pt : ds) {
                    modelChon.addRow(new Object[]{
                            pt.getMaSKU(),
                            pt.getTenPhuTung(),
                            pt.getHangSanXuat(),
                            pt.getViTriLuuKho(),
                            pt.getSoLuongTon(),
                            FormatUtil.formatCurrency(pt.getGiaNhap())
                    });
                }

                JTable tblChon = new JTable(modelChon);
                UIComponents.styleTable(tblChon);
                tblChon.setRowHeight(42);
                UIComponents.datDoRong(tblChon, 100, 230, 100, 90, 90, 120);
                if (tblChon.getRowCount() > 0) {
                    tblChon.setRowSelectionInterval(0, 0);
                }

                JScrollPane sp = UIComponents.cuonBang(tblChon);
                sp.setPreferredSize(new Dimension(730, 280));
                sp.setBorder(new javax.swing.border.LineBorder(UIConstants.BORDER_COLOR, 1, true));

                JPanel pnlDialog = new JPanel(new BorderLayout(0, 10));
                pnlDialog.add(UIComponents.label(
                        "Chọn phụ tùng cần thêm vào phiếu nhập kho:",
                        UIConstants.FONT_FORM_LABEL.deriveFont(14f),
                        UIConstants.TEXT_DARK
                ), BorderLayout.NORTH);
                pnlDialog.add(sp, BorderLayout.CENTER);

                int confirm = JOptionPane.showConfirmDialog(
                        view,
                        pnlDialog,
                        "Thêm phụ tùng nhập kho",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (confirm == JOptionPane.OK_OPTION) {
                    int selectedRow = tblChon.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < ds.size()) {
                        ptChon = ds.get(selectedRow);
                    }
                }
            }

            if (ptChon != null) {
                UIComponents.showError(view.getLblLoiModal(), null);

                for (int i = 0; i < dsChiTietModal.size(); i++) {
                    ChiTietNhapKho existing = dsChiTietModal.get(i);
                    if (existing.getPhuTung().getMaSKU().equalsIgnoreCase(ptChon.getMaSKU())) {
                        int slMoi = existing.getSoLuongNhap() + 5;
                        dsInputSoLuong.get(i).setText(String.valueOf(slMoi));
                        tinhVaKiemTraChiTietModal();
                        view.getTxtTimPhuTungModal().setText("");
                        return;
                    }
                }

                dsChiTietModal.add(new ChiTietNhapKho(
                        new PhieuNhapKho(view.getLblMaPhieuNhapModal().getText()),
                        ptChon,
                        NghiepVuKho.soLuongNhapDeXuat(ptChon),
                        ptChon.getGiaNhap()
                ));
                veBangChiTietModal();
                tinhVaKiemTraChiTietModal();
                view.getTxtTimPhuTungModal().setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi truy vấn phụ tùng: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Đưa các dòng hợp lệ lên bảng để thủ kho xem lại trước khi lưu; dòng sai được liệt kê và không thêm vào. */
    private void nhapTuExcel() {
        JFileChooser chon = new JFileChooser();
        chon.setFileFilter(new FileNameExtensionFilter("Excel (.xlsx)", "xlsx"));
        if (chon.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        List<String[]> dong;
        try {
            dong = XuatFile.docXlsx(chon.getSelectedFile(), 3);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view, "Không đọc được file. Hãy chọn file Excel .xlsx.\n" + ex.getMessage(),
                    "Lỗi nhập Excel", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<String> loi = new ArrayList<>();
        Set<String> maTrongFile = new HashSet<>();
        int soDongThem = 0;
        try {
            // Dòng 1 là tiêu đề.
            for (int i = 1; i < dong.size(); i++) {
                String[] o = dong.get(i);
                if (o[0].trim().isEmpty() && o[1].trim().isEmpty() && o[2].trim().isEmpty()) {
                    continue;
                }
                String loiDong = themDongExcel(o[0].trim(), o[1].trim(), o[2].trim(), maTrongFile);
                if (loiDong == null) {
                    soDongThem++;
                } else {
                    loi.add("Dòng " + (i + 1) + ": " + loiDong);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Lỗi truy vấn phụ tùng: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
        veBangChiTietModal();
        tinhVaKiemTraChiTietModal();
        String thongBao = "Đã đưa " + soDongThem + " dòng từ file vào phiếu. Hãy kiểm tra lại trước khi lưu.";
        if (loi.isEmpty()) {
            JOptionPane.showMessageDialog(view, thongBao, "Nhập Excel", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextArea chiTiet = new JTextArea(thongBao + "\n\n" + loi.size() + " dòng không được thêm:\n" + String.join("\n", loi));
        chiTiet.setEditable(false);
        chiTiet.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        JScrollPane cuon = new JScrollPane(chiTiet);
        cuon.setPreferredSize(new Dimension(520, 240));
        JOptionPane.showMessageDialog(view, cuon, "Nhập Excel", JOptionPane.WARNING_MESSAGE);
    }

    /** Trả về lý do nếu dòng sai, null nếu đã đưa vào phiếu. Mã đã có trên phiếu thì lấy số lượng và đơn giá theo file. */
    private String themDongExcel(String maSKU, String soLuong, String donGia, Set<String> maTrongFile) throws SQLException {
        PhuTung pt = maSKU.isEmpty() ? null : phuTungDao.findById(maSKU);
        if (pt == null) {
            return "không có phụ tùng mã \"" + maSKU + "\".";
        }
        if (maTrongFile.contains(pt.getMaSKU())) {
            return "mã " + pt.getMaSKU() + " bị lặp trong file.";
        }
        int sl;
        try {
            sl = Integer.parseInt(soLuong);
        } catch (NumberFormatException ex) {
            sl = 0;
        }
        if (sl <= 0) {
            return "số lượng \"" + soLuong + "\" phải là số nguyên dương.";
        }
        double gia;
        try {
            gia = FormatUtil.parseCurrency(donGia);
        } catch (NumberFormatException ex) {
            gia = 0;
        }
        if (gia <= 0) {
            return "đơn giá \"" + donGia + "\" không hợp lệ.";
        }
        if (!NghiepVuKho.giaNhapHopLe(pt, gia)) {
            return "đơn giá \"" + donGia + "\" cao hơn giá bán lẻ của mã " + pt.getMaSKU() + ".";
        }
        maTrongFile.add(pt.getMaSKU());
        for (ChiTietNhapKho daCo : dsChiTietModal) {
            if (daCo.getPhuTung().getMaSKU().equalsIgnoreCase(pt.getMaSKU())) {
                daCo.setSoLuongNhap(sl);
                daCo.setGiaNhapThucTe(gia);
                return null;
            }
        }
        dsChiTietModal.add(new ChiTietNhapKho(new PhieuNhapKho(view.getLblMaPhieuNhapModal().getText()), pt, sl, gia));
        return null;
    }

    private void xuLyLuuPhieuNhap() {
        NhaCungCap ncc = (NhaCungCap) view.getCboModalNCC().getSelectedItem();
        if (ncc == null || dsChiTietModal.isEmpty()) {
            UIComponents.showError(view.getLblLoiModal(), "Vui lòng nhập đầy đủ thông tin bắt buộc trước khi lưu");
            return;
        }
        if (!tinhVaKiemTraChiTietModal()) {
            return;
        }

        double tongTien = 0;
        for (ChiTietNhapKho ct : dsChiTietModal) {
            tongTien += NghiepVuKho.thanhTien(ct);
        }

        try {
            String maPN = nhapKhoDao.nextId();
            NhanVien thuKho = thuKhoHienTai != null ? thuKhoHienTai : new NhanVien(util.PhienDangNhap.getHienTai().getMaNV());
            PhieuNhapKho phieu = new PhieuNhapKho(maPN, LocalDateTime.now(), tongTien, ncc, thuKho);

            nhapKhoDao.insertWithDetails(phieu, dsChiTietModal);
            JOptionPane.showMessageDialog(view,
                    "Nhập kho thành công! Mã phiếu: " + maPN + "\nSố lượng tồn kho của các phụ tùng đã được cộng dồn tự động.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            view.hienThiModalLapPhieu(false);
            taiLai();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi lưu phiếu nhập kho: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xemChiTietPhieuNhap(PhieuNhapKho pn) {
        try {
            List<ChiTietNhapKho> dsCT = nhapKhoDao.findChiTietByMaPhieu(pn.getMaPhieuNhap());
            DefaultTableModel modelCT = UIComponents.modelKhongSua(
                    "Mã SKU", "Tên phụ tùng", "Số lượng nhập", "Đơn giá nhập", "Thành tiền"
            );
            for (ChiTietNhapKho ct : dsCT) {
                modelCT.addRow(new Object[]{
                        ct.getPhuTung().getMaSKU(),
                        ct.getPhuTung().getTenPhuTung(),
                        ct.getSoLuongNhap(),
                        FormatUtil.formatCurrency(ct.getGiaNhapThucTe()),
                        FormatUtil.formatCurrency(NghiepVuKho.thanhTien(ct))
                });
            }
            JTable tblCT = new JTable(modelCT);
            UIComponents.styleTable(tblCT);
            tblCT.setRowHeight(40);
            JScrollPane sp = UIComponents.cuonBang(tblCT);
            sp.setPreferredSize(new Dimension(620, 220));

            JPanel pnl = new JPanel(new BorderLayout(0, 10));
            pnl.add(UIComponents.label(
                    "Phiếu nhập: " + pn.getMaPhieuNhap() + "  •  NCC: " + pn.getNhaCungCap().getTenNCC()
                            + "  •  Tổng tiền: " + FormatUtil.formatCurrency(pn.getTongTienNhap()),
                    UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK
            ), BorderLayout.NORTH);
            pnl.add(sp, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(view, pnl, "Chi tiết phiếu nhập kho " + pn.getMaPhieuNhap(), JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            util.UIComponents.baoLoiCsdl(view, ex);
        }
    }
}