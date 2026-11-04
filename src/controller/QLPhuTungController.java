package controller;

import dao.PhieuNhapKho_Dao;
import dao.PhieuYeuCauXuatKho_Dao;
import dao.PhuTung_Dao;
import entity.NhanVien;
import entity.PhuTung;
import entity.TrangThaiYeuCauXuat;
import gui.QLPhuTung;
import gui.TrangChuThuKho;
import gui.TrangChuThuKho.MenuThuKho;
import util.FormatUtil;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QLPhuTungController {

    private final QLPhuTung view;
    private final PhuTung_Dao phuTungDao;
    private final PhieuYeuCauXuatKho_Dao yeuCauDao;
    private final PhieuNhapKho_Dao nhapKhoDao;

    private List<PhuTung> danhSachHienThi = new ArrayList<>();
    private boolean cheDoSua = false;
    private boolean dangNapDuLieuForm = false;
    private boolean daTai = false;

    public QLPhuTungController(QLPhuTung view) {
        this.view = view;
        this.phuTungDao = new PhuTung_Dao();
        this.yeuCauDao = new PhieuYeuCauXuatKho_Dao();
        this.nhapKhoDao = new PhieuNhapKho_Dao();

        initHeaderAndFilters();
        initController();
        UIComponents.goiYChuaTai(view.getTblPhuTung(), true);
    }

    private void initHeaderAndFilters() {
        try {
            NhanVien tk = nhapKhoDao.findDefaultThuKho();
            if (tk != null) {
                TrangChuThuKho.capNhatHeaderThuKho(view.getLblAvatar(), view.getLblTenThuKhoHeader(), tk.getHoTen());
            }
            int soCho = yeuCauDao.countByTrangThai(TrangThaiYeuCauXuat.CHO_XUAT);
            view.getLblBadgeSidebarDuyetXuat().setText(String.valueOf(soCho));
            view.getLblBadgeSidebarDuyetXuat().setVisible(soCho > 0);

            List<String> dsHang = phuTungDao.findAllHangSanXuat();
            Object dangChon = view.getCboHangSanXuat().getSelectedItem();
            view.getCboHangSanXuat().removeAllItems();
            view.getCboHangSanXuat().addItem("Hãng sản xuất: Tất cả");
            for (String h : dsHang) {
                view.getCboHangSanXuat().addItem(h);
            }
            if (dangChon != null) {
                view.getCboHangSanXuat().setSelectedItem(dangChon);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initController() {
        // Điều hướng Sidebar
        view.getBtnMenuTrangChu().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.TRANG_CHU, null));
        view.getBtnMenuPhuTung().addActionListener(e -> loadData());
        view.getBtnMenuNhapKho().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.NHAP_KHO, null));
        view.getBtnMenuDuyetXuat().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.DUYET_XUAT, null));

        view.getBtnDangXuat().addActionListener(e -> {
            int cf = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn đăng xuất?",
                    "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                Window w = SwingUtilities.getWindowAncestor(view);
                if (w != null) w.dispose();
            }
        });

        // Bộ lọc chỉ áp dụng khi bấm Tải dữ liệu hoặc nhấn Enter ở ô tìm kiếm
        view.getBtnTaiDuLieu().addActionListener(e -> loadData());
        view.getTxtTimKiem().addActionListener(e -> loadData());

        // Mở / đóng Drawer
        view.getBtnMoThemPhuTung().addActionListener(e -> moDrawerThemMoi());
        view.getBtnDongDrawer().addActionListener(e -> view.hienThiDrawer(false));
        view.getBtnHuyDrawer().addActionListener(e -> view.hienThiDrawer(false));

        // Kiểm tra hợp lệ REALTIME riêng biệt cho từng ô nhập liệu
        view.getTxtMaSKU().getDocument().addDocumentListener(taoDocListener(this::validateMaSKURealtime));
        view.getTxtTenPhuTung().getDocument().addDocumentListener(taoDocListener(this::validateTenPhuTungRealtime));
        view.getTxtGiaNhap().getDocument().addDocumentListener(taoDocListener(this::validateGiaRealtime));
        view.getTxtGiaBanLe().getDocument().addDocumentListener(taoDocListener(this::validateGiaRealtime));
        view.getTxtSoLuongTon().getDocument().addDocumentListener(taoDocListener(this::validateSoLuongTonRealtime));
        view.getTxtNguongCanhBao().getDocument().addDocumentListener(taoDocListener(this::validateNguongCanhBaoRealtime));

        // Nút Lưu trên Drawer
        view.getBtnLuuDrawer().addActionListener(e -> xuLyLuuPhuTung());

        // Click vào cột "Thao tác" ("Sửa   Xóa" - cột 8) trên bảng
        view.getTblPhuTung().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblPhuTung().rowAtPoint(e.getPoint());
                int col = view.getTblPhuTung().columnAtPoint(e.getPoint());
                if (row < 0 || row >= danhSachHienThi.size()) return;

                PhuTung pt = danhSachHienThi.get(row);
                if (col == 8) {
                    Rectangle cellRect = view.getTblPhuTung().getCellRect(row, col, false);
                    int clickX = e.getX() - cellRect.x;
                    if (clickX < 48) {
                        moDrawerSua(pt);
                    } else {
                        xuLyXoaPhuTung(pt);
                    }
                } else if (e.getClickCount() == 2) {
                    moDrawerSua(pt);
                }
            }
        });
    }

    private DocumentListener taoDocListener(Runnable r) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                if (!dangNapDuLieuForm) r.run();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                if (!dangNapDuLieuForm) r.run();
            }
            @Override public void changedUpdate(DocumentEvent e) {
                if (!dangNapDuLieuForm) r.run();
            }
        };
    }

    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    private void taiLai() {
        if (daTai) {
            loadData();
        }
    }

    public void loadData() {
        daTai = true;
        UIComponents.goiYChuaTai(view.getTblPhuTung(), false);
        try {
            List<PhuTung> all = phuTungDao.findAll();
            int tongMatHang = all.size();
            int soDuoiNguong = phuTungDao.countDuoiNguongCanhBao();

            view.getLblMoTaSoMatHang().setText(
                    "<html>" + tongMatHang + " mặt hàng • <font color='#E53935'><b>"
                            + soDuoiNguong + " mặt hàng dưới ngưỡng cảnh báo</b></font></html>"
            );

            String kw = view.getTxtTimKiem().getText();
            String hang = (String) view.getCboHangSanXuat().getSelectedItem();
            String gia = (String) view.getCboKhoangGia().getSelectedItem();
            String ton = (String) view.getCboTinhTrangTon().getSelectedItem();

            danhSachHienThi = phuTungDao.search(kw, hang, gia, ton);

            view.getTableModel().setRowCount(0);
            for (PhuTung pt : danhSachHienThi) {
                view.getTableModel().addRow(new Object[]{
                        pt.getMaSKU(),
                        pt.getTenPhuTung(),
                        pt.getHangSanXuat(),
                        pt.getViTriLuuKho(),
                        FormatUtil.formatCurrency(pt.getGiaNhap()),
                        FormatUtil.formatCurrency(pt.getGiaBanLe()),
                        pt.getSoLuongTon(),
                        pt.getNguongCanhBao(),
                        "Sửa   Xóa"
                });
            }

            view.getLblFooterHienThi().setText("Hiển thị " + danhSachHienThi.size() + " / " + tongMatHang + " mặt hàng");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void xoaTrangThaiLoiDrawer() {
        UIComponents.setError(view.getTxtMaSKU(), false);
        QLPhuTung.showWrappedError(view.getLblLoiMaSKU(), null, 360);
        UIComponents.setError(view.getTxtTenPhuTung(), false);
        QLPhuTung.showWrappedError(view.getLblLoiTenPhuTung(), null, 360);
        UIComponents.setError(view.getTxtGiaNhap(), false);
        QLPhuTung.showWrappedError(view.getLblLoiGiaNhap(), null, 175);
        UIComponents.setError(view.getTxtGiaBanLe(), false);
        view.getLblGhiChuGia().setForeground(UIConstants.TEXT_MUTED);
        view.getLblGhiChuGia().setText("<html><div style='width:175px;'>Không được thấp hơn giá nhập</div></html>");
        UIComponents.setError(view.getTxtSoLuongTon(), false);
        QLPhuTung.showWrappedError(view.getLblLoiSoLuongTon(), null, 175);
        UIComponents.setError(view.getTxtNguongCanhBao(), false);
        QLPhuTung.showWrappedError(view.getLblLoiNguongCanhBao(), null, 175);
    }

    private void moDrawerThemMoi() {
        cheDoSua = false;
        dangNapDuLieuForm = true;
        xoaTrangThaiLoiDrawer();
        view.getLblTieuDeDrawer().setText("Thêm phụ tùng mới");
        view.getTxtMaSKU().setEditable(true);
        view.getTxtMaSKU().setBackground(UIConstants.CARD_BG);
        view.getTxtMaSKU().setText("");
        view.getTxtTenPhuTung().setText("");
        view.getCboDrawerHang().setSelectedItem("Toyota");
        view.getCboDrawerViTri().setSelectedItem("Kệ A-01");
        view.getTxtGiaNhap().setText("85000");
        view.getTxtGiaBanLe().setText("120000");
        view.getTxtSoLuongTon().setText("10");
        view.getTxtSoLuongTon().setEditable(true);
        view.getTxtSoLuongTon().setBackground(UIConstants.CARD_BG);
        view.getTxtSoLuongTon().setToolTipText(null);
        view.getTxtNguongCanhBao().setText("5");
        dangNapDuLieuForm = false;
        view.hienThiDrawer(true);
        view.getTxtMaSKU().requestFocusInWindow();
    }

    private void moDrawerSua(PhuTung pt) {
        cheDoSua = true;
        dangNapDuLieuForm = true;
        xoaTrangThaiLoiDrawer();
        view.getLblTieuDeDrawer().setText("Cập nhật phụ tùng");
        view.getTxtMaSKU().setText(pt.getMaSKU());
        view.getTxtMaSKU().setEditable(false);
        view.getTxtMaSKU().setBackground(UIComponents.tint(UIConstants.APP_BG, 0.45f));
        view.getTxtTenPhuTung().setText(pt.getTenPhuTung());
        view.getCboDrawerHang().setSelectedItem(pt.getHangSanXuat());
        view.getCboDrawerViTri().setSelectedItem(pt.getViTriLuuKho());
        view.getTxtGiaNhap().setText(String.valueOf(Math.round(pt.getGiaNhap())));
        view.getTxtGiaBanLe().setText(String.valueOf(Math.round(pt.getGiaBanLe())));
        view.getTxtSoLuongTon().setText(String.valueOf(pt.getSoLuongTon()));
        view.getTxtSoLuongTon().setEditable(false);
        view.getTxtSoLuongTon().setBackground(UIComponents.tint(UIConstants.APP_BG, 0.45f));
        view.getTxtSoLuongTon().setToolTipText("Tồn kho chỉ thay đổi qua nhập kho và duyệt xuất kho.");
        view.getTxtNguongCanhBao().setText(String.valueOf(pt.getNguongCanhBao()));
        dangNapDuLieuForm = false;
        view.hienThiDrawer(true);
    }

    // =========================================================================
    // CÁC HÀM KIỂM TRA HỢP LỆ REALTIME ĐỘC LẬP CHO TỪNG Ô
    // =========================================================================
    private boolean validateMaSKURealtime() {
        if (cheDoSua) return true;
        String ma = view.getTxtMaSKU().getText().trim();
        if (ma.isEmpty()) {
            UIComponents.setError(view.getTxtMaSKU(), true);
            QLPhuTung.showWrappedError(view.getLblLoiMaSKU(), "Mã SKU không được để trống", 360);
            return false;
        }
        try {
            if (phuTungDao.existsMaSKU(ma)) {
                UIComponents.setError(view.getTxtMaSKU(), true);
                QLPhuTung.showWrappedError(view.getLblLoiMaSKU(), "Mã SKU đã tồn tại trên hệ thống", 360);
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        UIComponents.setError(view.getTxtMaSKU(), false);
        QLPhuTung.showWrappedError(view.getLblLoiMaSKU(), null, 360);
        return true;
    }

    private boolean validateTenPhuTungRealtime() {
        String ten = view.getTxtTenPhuTung().getText().trim();
        if (ten.isEmpty()) {
            UIComponents.setError(view.getTxtTenPhuTung(), true);
            QLPhuTung.showWrappedError(view.getLblLoiTenPhuTung(), "Tên phụ tùng không được để trống", 360);
            return false;
        }
        UIComponents.setError(view.getTxtTenPhuTung(), false);
        QLPhuTung.showWrappedError(view.getLblLoiTenPhuTung(), null, 360);
        return true;
    }

    /** Giá gõ tay: dấu chấm hoặc dấu phẩy đều là ngăn cách hàng nghìn. */
    public static double docGia(String chuoi) {
        return FormatUtil.parseCurrency(chuoi.replace(',', '.'));
    }

    private boolean validateGiaRealtime() {
        boolean hopLe = true;
        double giaNhap = -1;
        try {
            giaNhap = docGia(view.getTxtGiaNhap().getText());
            if (giaNhap <= 0) throw new NumberFormatException();
            UIComponents.setError(view.getTxtGiaNhap(), false);
            QLPhuTung.showWrappedError(view.getLblLoiGiaNhap(), null, 175);
        } catch (NumberFormatException ex) {
            UIComponents.setError(view.getTxtGiaNhap(), true);
            QLPhuTung.showWrappedError(view.getLblLoiGiaNhap(), "Giá nhập phải là số > 0", 175);
            hopLe = false;
        }

        try {
            double giaBan = docGia(view.getTxtGiaBanLe().getText());
            if (giaBan <= 0) {
                UIComponents.setError(view.getTxtGiaBanLe(), true);
                view.getLblGhiChuGia().setForeground(UIConstants.STATUS_DANGER);
                view.getLblGhiChuGia().setText("<html><div style='width:175px;'>Giá bán lẻ phải là số > 0</div></html>");
                hopLe = false;
            } else if (giaNhap > 0 && giaBan < giaNhap) {
                UIComponents.setError(view.getTxtGiaBanLe(), true);
                view.getLblGhiChuGia().setForeground(UIConstants.STATUS_DANGER);
                view.getLblGhiChuGia().setText("<html><div style='width:175px;'>Giá bán lẻ không được thấp hơn giá nhập</div></html>");
                hopLe = false;
            } else {
                UIComponents.setError(view.getTxtGiaBanLe(), false);
                view.getLblGhiChuGia().setForeground(UIConstants.TEXT_MUTED);
                view.getLblGhiChuGia().setText("<html><div style='width:175px;'>Không được thấp hơn giá nhập</div></html>");
            }
        } catch (NumberFormatException ex) {
            UIComponents.setError(view.getTxtGiaBanLe(), true);
            view.getLblGhiChuGia().setForeground(UIConstants.STATUS_DANGER);
            view.getLblGhiChuGia().setText("<html><div style='width:175px;'>Giá bán lẻ phải là số hợp lệ</div></html>");
            hopLe = false;
        }
        return hopLe;
    }

    /** Kiểm tra riêng ô Số lượng tồn và hiện cảnh báo ngay dưới ô Số lượng tồn. */
    private boolean validateSoLuongTonRealtime() {
        String raw = view.getTxtSoLuongTon().getText().trim();
        try {
            if (raw.isEmpty()) throw new NumberFormatException();
            int ton = Integer.parseInt(raw);
            if (ton < 0) {
                UIComponents.setError(view.getTxtSoLuongTon(), true);
                QLPhuTung.showWrappedError(view.getLblLoiSoLuongTon(), "Số lượng tồn không được là số âm (>= 0)", 175);
                return false;
            }
            UIComponents.setError(view.getTxtSoLuongTon(), false);
            QLPhuTung.showWrappedError(view.getLblLoiSoLuongTon(), null, 175);
            return true;
        } catch (NumberFormatException ex) {
            UIComponents.setError(view.getTxtSoLuongTon(), true);
            QLPhuTung.showWrappedError(view.getLblLoiSoLuongTon(), "Số lượng tồn phải là số nguyên không âm", 175);
            return false;
        }
    }

    /** Kiểm tra riêng ô Ngưỡng cảnh báo và hiện cảnh báo ngay dưới ô Ngưỡng cảnh báo. */
    private boolean validateNguongCanhBaoRealtime() {
        String raw = view.getTxtNguongCanhBao().getText().trim();
        try {
            if (raw.isEmpty()) throw new NumberFormatException();
            int nguong = Integer.parseInt(raw);
            if (nguong < 0) {
                UIComponents.setError(view.getTxtNguongCanhBao(), true);
                QLPhuTung.showWrappedError(view.getLblLoiNguongCanhBao(), "Ngưỡng cảnh báo không được là số âm (>= 0)", 175);
                return false;
            }
            UIComponents.setError(view.getTxtNguongCanhBao(), false);
            QLPhuTung.showWrappedError(view.getLblLoiNguongCanhBao(), null, 175);
            return true;
        } catch (NumberFormatException ex) {
            UIComponents.setError(view.getTxtNguongCanhBao(), true);
            QLPhuTung.showWrappedError(view.getLblLoiNguongCanhBao(), "Ngưỡng cảnh báo phải là số nguyên không âm", 175);
            return false;
        }
    }

    private void xuLyLuuPhuTung() {
        boolean v1 = validateMaSKURealtime();
        boolean v2 = validateTenPhuTungRealtime();
        boolean v3 = validateGiaRealtime();
        boolean v4 = validateSoLuongTonRealtime();
        boolean v5 = validateNguongCanhBaoRealtime();
        if (!v1 || !v2 || !v3 || !v4 || !v5) return;

        String maSKU = view.getTxtMaSKU().getText().trim();
        String tenPT = view.getTxtTenPhuTung().getText().trim();
        String hang = String.valueOf(view.getCboDrawerHang().getEditor().getItem()).trim();
        String viTri = String.valueOf(view.getCboDrawerViTri().getEditor().getItem()).trim();
        double giaNhap = docGia(view.getTxtGiaNhap().getText());
        double giaBan = docGia(view.getTxtGiaBanLe().getText());
        int soLuongTon = Integer.parseInt(view.getTxtSoLuongTon().getText().trim());
        int nguong = Integer.parseInt(view.getTxtNguongCanhBao().getText().trim());

        try {
            PhuTung pt = new PhuTung(maSKU, tenPT, hang, viTri, giaNhap, giaBan, soLuongTon, nguong);
            if (cheDoSua) {
                phuTungDao.update(pt);
                JOptionPane.showMessageDialog(view, "Đã cập nhật phụ tùng " + maSKU + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                phuTungDao.insert(pt);
                JOptionPane.showMessageDialog(view, "Đã thêm phụ tùng mới " + maSKU + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
            view.hienThiDrawer(false);
            initHeaderAndFilters();
            taiLai();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi lưu phụ tùng: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaPhuTung(PhuTung pt) {
        try {
            if (phuTungDao.isUsedInTransactions(pt.getMaSKU())) {
                JOptionPane.showMessageDialog(view,
                        "Phụ tùng " + pt.getMaSKU() + " đã phát sinh trong phiếu nhập/xuất/sửa chữa nên không thể xóa!",
                        "Ràng buộc toàn vẹn dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int cf = JOptionPane.showConfirmDialog(view,
                    "Bạn có chắc chắn muốn xóa phụ tùng " + pt.getMaSKU() + " - " + pt.getTenPhuTung() + "?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                phuTungDao.delete(pt.getMaSKU());
                taiLai();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}