package controller;

import dao.KhuyenMai_Dao;
import entity.HinhThucKhuyenMai;
import entity.KhuyenMai;
import gui.KhuyenMaiFormDialog;
import gui.QLKhuyenMai;
import util.UIComponents;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QLKhuyenMaiController {

    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter NGAY_HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int SO_NGAY_SAP_HET_HAN = 7;

    private final QLKhuyenMai view;
    private final KhuyenMai_Dao dao = new KhuyenMai_Dao();

    private List<KhuyenMai> ketQuaTimKiem = new ArrayList<>();
    // Đánh số phiên tải để bỏ qua kết quả của lần tìm kiếm đã cũ
    private int phienBan = 0;
    private boolean daTai;

    public QLKhuyenMaiController(QLKhuyenMai view) {
        this.view = view;
        initController();
        UIComponents.tuTaiLai(this::taiDuLieu, view.getTxtTimKiem(), view.getCboLocTinhTrang(), view.getCboLocHinhThuc());
        taiDuLieu();
    }

    private void initController() {
        view.getBtnTaoMoi().addActionListener(e -> moFormThem());
        view.getBtnXuatExcel().addActionListener(e -> xuatExcel());
        view.getBtnTaiDuLieu().addActionListener(e -> taiDuLieu());
        view.getTxtTimKiem().addActionListener(e -> taiDuLieu());
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                xuLyBieuTuong(e);
            }
        });

        JComponent goc = view;
        goc.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai");
        goc.getActionMap().put("taiLai", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taiDuLieu();
            }
        });
        goc.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "taoMoi");
        goc.getActionMap().put("taoMoi", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moFormThem();
            }
        });
        goc.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "timKiem");
        goc.getActionMap().put("timKiem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.getTxtTimKiem().requestFocusInWindow();
            }
        });

        JTable bang = view.getTblDanhSach();
        bang.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "xoaDong");
        bang.getActionMap().put("xoaDong", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xoaDongDangChon();
            }
        });
    }

    // ==========================================================
    // Tải dữ liệu
    // ==========================================================

    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    private void taiLai() {
        if (daTai) {
            taiDuLieu();
        }
    }

    private void taiDuLieu() {
        daTai = true;
        UIComponents.goiYChuaTai(view.getTblDanhSach(), false);
        final int phien = ++phienBan;
        final String tuKhoa = view.getTxtTimKiem().getText().trim();
        final Object chonHinhThuc = view.getCboLocHinhThuc().getSelectedItem();
        final HinhThucKhuyenMai hinhThuc = chonHinhThuc instanceof HinhThucKhuyenMai ? (HinhThucKhuyenMai) chonHinhThuc : null;

        new SwingWorker<List<KhuyenMai>[], Void>() {
            @Override
            protected List<KhuyenMai>[] doInBackground() throws Exception {
                List<KhuyenMai> timKiem = dao.search(tuKhoa, hinhThuc);
                List<KhuyenMai> tatCa = dao.search("", null);
                @SuppressWarnings("unchecked")
                List<KhuyenMai>[] ketQua = new List[]{timKiem, tatCa};
                return ketQua;
            }

            @Override
            protected void done() {
                if (phien != phienBan) {
                    return;
                }
                try {
                    List<KhuyenMai>[] ketQua = get();
                    ketQuaTimKiem = ketQua[0];
                    capNhatThongKe(ketQua[1]);
                    hienThiBang();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    hienLoiSql(ex);
                }
            }
        }.execute();
    }

    private void capNhatThongKe(List<KhuyenMai> tatCa) {
        LocalDate homNay = LocalDate.now();
        int dangDienRa = 0;
        int sapHetHan = 0;
        for (KhuyenMai km : tatCa) {
            String tinhTrang = tinhTrangKhuyenMai(km, homNay);
            if ("Đang áp dụng".equals(tinhTrang)) {
                dangDienRa++;
                if (!km.getThoiGianKetThuc().isAfter(homNay.plusDays(SO_NGAY_SAP_HET_HAN))) {
                    sapHetHan++;
                }
            }
        }
        view.setThongKe(tatCa.size(), dangDienRa, sapHetHan);
    }

    /**
     * Tính trạng thái khuyến mãi tại một ngày cụ thể.
     * @param km Khuyến mãi
     * @param homNay Ngày cần kiểm tra
     * @return "Đang áp dụng", "Sắp diễn ra", "Hết hạn", hoặc "Ngừng áp dụng"
     */
    public static String tinhTrangKhuyenMai(KhuyenMai km, LocalDate homNay) {
        if (!km.isTrangThai()) {
            return "Ngừng áp dụng";
        }
        if (homNay == null) {
            throw new IllegalArgumentException("Ngày xét tình trạng không được để trống.");
        }
        km.kiemTraHopLe();
        if (homNay.isBefore(km.getThoiGianBatDau())) {
            return "Sắp diễn ra";
        }
        if (homNay.isAfter(km.getThoiGianKetThuc())) {
            return "Hết hạn";
        }
        return "Đang áp dụng";
    }

    private void hienThiBang() {
        String locTinhTrang = String.valueOf(view.getCboLocTinhTrang().getSelectedItem());
        LocalDate homNay = LocalDate.now();
        List<Object[]> dong = new ArrayList<>();
        for (KhuyenMai km : ketQuaTimKiem) {
            String tinhTrang = tinhTrangKhuyenMai(km, homNay);
            if (!"Tất cả".equals(locTinhTrang) && !locTinhTrang.equals(tinhTrang)) {
                continue;
            }
            String thoiGian = km.getThoiGianBatDau().format(NGAY_HIEN_THI)
                    + " - " + km.getThoiGianKetThuc().format(NGAY_HIEN_THI);
            String luotDung = km.getSoLuotDaDung() + "/"
                    + (km.getSoLuotToiDa() == null ? "∞" : String.valueOf(km.getSoLuotToiDa()));
            dong.add(new Object[]{
                    km.getMaKM(), km.getTenChuongTrinh(), km.getHinhThuc(), km.getPhamViApDung(),
                    thoiGian, luotDung, tinhTrang, ""
            });
        }
        view.setDanhSach(dong);
    }

    // ==========================================================
    // Thao tác trên bảng
    // ==========================================================

    private void xuLyBieuTuong(MouseEvent e) {
        JTable bang = view.getTblDanhSach();
        int viTri = UIComponents.viTriBieuTuong(bang, e, QLKhuyenMai.COT_THAO_TAC, 4);
        if (viTri < 0) {
            return;
        }
        int hang = bang.rowAtPoint(e.getPoint());
        if (hang < 0) {
            return;
        }
        KhuyenMai km = timTheoMa(String.valueOf(bang.getValueAt(hang, 0)));
        if (km == null) {
            return;
        }
        switch (viTri) {
            case QLKhuyenMai.ICON_SUA:
                moFormSua(km);
                break;
            case QLKhuyenMai.ICON_KICH_HOAT:
                doiTrangThai(km);
                break;
            case QLKhuyenMai.ICON_KET_THUC:
                ketThucSom(km);
                break;
            case QLKhuyenMai.ICON_XOA:
                xoa(km);
                break;
            default:
                break;
        }
    }

    private void xoaDongDangChon() {
        int hang = view.getTblDanhSach().getSelectedRow();
        if (hang < 0) {
            return;
        }
        KhuyenMai km = timTheoMa(String.valueOf(view.getTblDanhSach().getValueAt(hang, 0)));
        if (km != null) {
            xoa(km);
        }
    }

    private KhuyenMai timTheoMa(String maKM) {
        for (KhuyenMai km : ketQuaTimKiem) {
            if (km.getMaKM().equals(maKM)) {
                return km;
            }
        }
        return null;
    }

    private void doiTrangThai(KhuyenMai km) {
        boolean kichHoat = !km.isTrangThai();
        String cauHoi = kichHoat
                ? "Kích hoạt lại chương trình \"" + km.getTenChuongTrinh() + "\"?"
                : "Tạm ngưng chương trình \"" + km.getTenChuongTrinh() + "\"?";
        int chon = JOptionPane.showConfirmDialog(view, cauHoi, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        km.setTrangThai(kichHoat);
        luuCapNhat(km);
    }

    /** Kết thúc hẳn: đóng ngày kết thúc về hôm nay, khác với Tạm ngưng có thể kích hoạt lại đúng thời hạn cũ. */
    private void ketThucSom(KhuyenMai km) {
        LocalDate homNay = LocalDate.now();
        if (!"Đang áp dụng".equals(tinhTrangKhuyenMai(km, homNay))) {
            JOptionPane.showMessageDialog(view, "Chỉ kết thúc sớm được chương trình đang áp dụng.");
            return;
        }
        int chon = JOptionPane.showConfirmDialog(view, "Kết thúc sớm chương trình \"" + km.getTenChuongTrinh() + "\" từ hôm nay?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        km.setThoiGianKetThuc(homNay);
        km.setTrangThai(false);
        luuCapNhat(km);
    }

    private void xoa(KhuyenMai km) {
        try {
            if (dao.isDangDuocDung(km.getMaKM())) {
                int chon = JOptionPane.showConfirmDialog(view,
                        "Chương trình đã được sử dụng trong hóa đơn nên không thể xóa.\n"
                        + "Bạn có muốn ngừng áp dụng chương trình này không?",
                        "Không thể xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (chon == JOptionPane.YES_OPTION) {
                    km.setTrangThai(false);
                    luuCapNhat(km);
                }
                return;
            }
        } catch (SQLException ex) {
            hienLoiSql(ex);
            return;
        }
        int chon = JOptionPane.showConfirmDialog(view,
                "Xóa chương trình \"" + km.getTenChuongTrinh() + "\"? Thao tác không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dao.delete(km.getMaKM());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(view, "Thành công.");
                    taiLai();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    hienLoiSql(ex);
                }
            }
        }.execute();
    }

    private void luuCapNhat(KhuyenMai km) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dao.update(km);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(view, "Thành công.");
                    taiLai();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    hienLoiSql(ex);
                }
            }
        }.execute();
    }

    // ==========================================================
    // Tạo / Sửa
    // ==========================================================

    private void moFormThem() {
        KhuyenMaiFormDialog hopThoai = new KhuyenMaiFormDialog(SwingUtilities.getWindowAncestor(view));
        hopThoai.setTieuDe("Tạo chương trình khuyến mãi mới");
        hopThoai.getBtnLuu().addActionListener(e -> luuMoi(hopThoai));
        hopThoai.setVisible(true);
    }

    private void luuMoi(KhuyenMaiFormDialog hopThoai) {
        KhuyenMai km;
        try {
            // Mã tạm, mã thật được sinh từ CSDL ngay trước khi ghi
            km = taoTuForm(hopThoai, "KM0000");
        } catch (IllegalArgumentException ex) {
            hopThoai.hienLoi(ex.getMessage());
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                km.setMaKM(dao.nextId());
                dao.insert(km);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    hopThoai.dispose();
                    JOptionPane.showMessageDialog(view, "Lưu chương trình khuyến mãi thành công.");
                    taiLai();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    hienLoiSql(ex);
                }
            }
        }.execute();
    }

    private void moFormSua(KhuyenMai km) {
        KhuyenMaiFormDialog hopThoai = new KhuyenMaiFormDialog(SwingUtilities.getWindowAncestor(view));
        hopThoai.setTieuDe("Sửa chương trình khuyến mãi");
        hopThoai.dien(km);
        hopThoai.getBtnLuu().addActionListener(e -> luuSua(hopThoai, km));
        hopThoai.setVisible(true);
    }

    private void luuSua(KhuyenMaiFormDialog hopThoai, KhuyenMai goc) {
        KhuyenMai km;
        try {
            km = taoTuForm(hopThoai, goc.getMaKM());
        } catch (IllegalArgumentException ex) {
            hopThoai.hienLoi(ex.getMessage());
            return;
        }
        if (km.getSoLuotToiDa() != null && km.getSoLuotToiDa() < goc.getSoLuotDaDung()) {
            hopThoai.hienLoi("Số lượt tối đa không được nhỏ hơn số lượt đã sử dụng ("
                    + goc.getSoLuotDaDung() + ").");
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dao.update(km);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    hopThoai.dispose();
                    JOptionPane.showMessageDialog(view, "Lưu chương trình khuyến mãi thành công.");
                    taiLai();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    hienLoiSql(ex);
                }
            }
        }.execute();
    }

    private KhuyenMai taoTuForm(KhuyenMaiFormDialog hopThoai, String maKM) {
        String ten = hopThoai.getTxtTen().getText().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên chương trình không được để trống.");
        }
        HinhThucKhuyenMai hinhThuc = hopThoai.getRadTienMat().isSelected()
                ? HinhThucKhuyenMai.TIEN_MAT : HinhThucKhuyenMai.PHAN_TRAM;
        double giaTri = docGiaTri(hopThoai.getTxtGiaTri().getText(), hinhThuc);
        LocalDate batDau = docNgay(hopThoai.getTxtBatDau().getText(), "Thời gian bắt đầu");
        LocalDate ketThuc = docNgay(hopThoai.getTxtKetThuc().getText(), "Thời gian kết thúc");
        if (ketThuc.isBefore(batDau)) {
            throw new IllegalArgumentException("Thời gian kết thúc không được trước thời gian bắt đầu.");
        }
        Integer soLuot = docSoLuot(hopThoai.getTxtSoLuotToiDa().getText());

        KhuyenMai km = new KhuyenMai(maKM, ten, hinhThuc, giaTri, batDau, ketThuc,
                hopThoai.getChkKichHoat().isSelected());
        km.setPhamViApDung(hopThoai.getPhamViDangChon());
        km.setSoLuotToiDa(soLuot);
        km.setGhiChu(hopThoai.getTxtGhiChu().getText());
        return km;
    }

    // ==========================================================
    // Hàm phân tích dùng chung (public để kiểm thử)
    // ==========================================================

    public static double docGiaTri(String nhap, HinhThucKhuyenMai hinhThuc) {
        String s = nhap == null ? "" : nhap.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Giá trị giảm không được để trống.");
        }
        try {
            if (hinhThuc == HinhThucKhuyenMai.TIEN_MAT) {
                double giaTri = Double.parseDouble(s.replace(".", "").replace(" ", ""));
                if (giaTri <= 0) {
                    throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
                }
                return giaTri;
            }
            double phanTram = Double.parseDouble(s.replace(',', '.'));
            if (phanTram <= 0) {
                throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
            }
            if (phanTram > 100) {
                throw new IllegalArgumentException("Phần trăm giảm không được vượt quá 100.");
            }
            return phanTram;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá trị giảm không hợp lệ.");
        }
    }

    public static Integer docSoLuot(String nhap) {
        String s = nhap == null ? "" : nhap.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            int soLuot = Integer.parseInt(s);
            if (soLuot <= 0) {
                throw new IllegalArgumentException("Số lượt tối đa phải là số nguyên lớn hơn 0.");
            }
            return soLuot;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số lượt tối đa không hợp lệ.");
        }
    }

    public static LocalDate docNgay(String nhap, String tenTruong) {
        String s = nhap == null ? "" : nhap.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống.");
        }
        try {
            return LocalDate.parse(s, DINH_DANG_NGAY);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(tenTruong + " phải có định dạng dd/MM/yyyy.");
        }
    }

    // ==========================================================
    // Xuất file
    // ==========================================================

    private void xuatExcel() {
        DefaultTableModel mo = view.getTableModel();
        int soCot = QLKhuyenMai.COT_THAO_TAC;
        String[] cot = new String[soCot];
        for (int i = 0; i < soCot; i++) {
            cot[i] = mo.getColumnName(i);
        }
        List<Object[]> dong = new ArrayList<>();
        for (int i = 0; i < mo.getRowCount(); i++) {
            Object[] r = new Object[soCot];
            for (int j = 0; j < soCot; j++) {
                r[j] = mo.getValueAt(i, j);
            }
            dong.add(r);
        }
        util.XuatFile.xuatExcel(view, "khuyen-mai", cot, dong);
    }

    private void hienLoiSql(Throwable ex) {
        Throwable nguyenNhan = ex.getCause() == null ? ex : ex.getCause();
        String thongDiep = nguyenNhan.getMessage() == null ? nguyenNhan.toString() : nguyenNhan.getMessage();
        JOptionPane.showMessageDialog(view, "Lỗi cơ sở dữ liệu: " + thongDiep,
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
