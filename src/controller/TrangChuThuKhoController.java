package controller;

import dao.PhieuNhapKho_Dao;
import dao.PhieuYeuCauXuatKho_Dao;
import dao.PhuTung_Dao;
import entity.NhanVien;
import entity.PhieuYeuCauXuatKho;
import entity.PhuTung;
import entity.TrangThaiYeuCauXuat;
import gui.TrangChuThuKho;
import gui.TrangChuThuKho.MenuThuKho;

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

public class TrangChuThuKhoController {

    private static final int SO_DONG_MAC_DINH = 5;

    private final TrangChuThuKho view;
    private final PhuTung_Dao phuTungDao;
    private final PhieuYeuCauXuatKho_Dao yeuCauDao;
    private final PhieuNhapKho_Dao nhapKhoDao;

    private List<PhieuYeuCauXuatKho> dsYeuCauTimDuoc = new ArrayList<>();
    private List<PhieuYeuCauXuatKho> dsYeuCauHienThi = new ArrayList<>();
    private List<PhuTung> dsCanhBaoHienThi = new ArrayList<>();
    private boolean dangXemTatCaYeuCau = false;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public TrangChuThuKhoController(TrangChuThuKho view) {
        this.view = view;
        this.phuTungDao = new PhuTung_Dao();
        this.yeuCauDao = new PhieuYeuCauXuatKho_Dao();
        this.nhapKhoDao = new PhieuNhapKho_Dao();

        initThuKhoInfo();
        initController();
        loadData();
    }

    private void initThuKhoInfo() {
        try {
            NhanVien tk = nhapKhoDao.findDefaultThuKho();
            if (tk != null) {
                TrangChuThuKho.capNhatHeaderThuKho(view.getLblAvatar(), view.getLblTenThuKhoHeader(), tk.getHoTen());
                String ten = tk.getHoTen();
                String tenGoi = ten.contains(" ") ? ten.substring(ten.lastIndexOf(' ') + 1) : ten;
                view.getLblMoTaChao().setText("Xin chào " + tenGoi + ", đây là tình hình tồn kho và các yêu cầu xuất kho hôm nay.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initController() {
        view.getBtnMenuTrangChu().addActionListener(e -> loadData());
        view.getBtnMenuPhuTung().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.PHU_TUNG, null));
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

        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                locDuLieuTheoTuKhoa();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                locDuLieuTheoTuKhoa();
            }
            @Override public void changedUpdate(DocumentEvent e) {
                locDuLieuTheoTuKhoa();
            }
        });

        view.getBtnLamMoi().addActionListener(e -> {
            dangXemTatCaYeuCau = false;
            loadData();
        });

        view.getBtnXemTatCaYeuCau().addActionListener(e -> {
            if (dsYeuCauTimDuoc.size() <= SO_DONG_MAC_DINH) return;
            dangXemTatCaYeuCau = true;
            doBangYeuCau();
        });

        view.getBtnXemKho().addActionListener(e -> TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.PHU_TUNG, null));

        view.getTblYeuCau().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblYeuCau().rowAtPoint(e.getPoint());
                int col = view.getTblYeuCau().columnAtPoint(e.getPoint());
                if (row >= 0 && row < dsYeuCauHienThi.size() && (col == 5 || e.getClickCount() == 2)) {
                    PhieuYeuCauXuatKho yc = dsYeuCauHienThi.get(row);
                    TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.DUYET_XUAT, yc.getMaPhieuYC());
                }
            }
        });

        view.getTblCanhBao().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblCanhBao().rowAtPoint(e.getPoint());
                int col = view.getTblCanhBao().columnAtPoint(e.getPoint());
                if (row >= 0 && row < dsCanhBaoHienThi.size() && (col == 5 || e.getClickCount() == 2)) {
                    PhuTung pt = dsCanhBaoHienThi.get(row);
                    TrangChuThuKho.chuyenManHinhThuKho(view, MenuThuKho.NHAP_KHO, pt.getMaSKU());
                }
            }
        });
    }

    public void loadData() {
        try {
            int soCanhBao = phuTungDao.countDuoiNguongCanhBao();
            int soChoXuat = yeuCauDao.countByTrangThai(TrangThaiYeuCauXuat.CHO_XUAT);
            int soPhieuNhap = nhapKhoDao.countPhieuNhapHomNay();
            int soDaXuat = yeuCauDao.countDaXuatHomNay();

            view.getLblSoCanhBaoTon().setText(String.format("%02d", soCanhBao));
            view.getLblSoYeuCauCho().setText(String.format("%02d", soChoXuat));
            view.getLblSoPhieuNhapHomNay().setText(String.format("%02d", soPhieuNhap));
            view.getLblSoDaXuatHomNay().setText(String.format("%02d", soDaXuat));
            view.getLblBadgeSidebarDuyetXuat().setText(String.valueOf(soChoXuat));
            view.getLblBadgeSidebarDuyetXuat().setVisible(soChoXuat > 0);

            String gio = LocalTime.now().format(TIME_FMT);
            view.getLblSubDaXuat().setText("Tính đến " + gio + " hôm nay");
            view.getLblCapNhatLuc().setText("Cập nhật lúc " + gio);

            locDuLieuTheoTuKhoa();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void locDuLieuTheoTuKhoa() {
        try {
            String kw = view.getTxtTimKiem().getText();
            dsYeuCauTimDuoc = yeuCauDao.searchYeuCauThongMinh(kw, null);
            doBangYeuCau();

            dsCanhBaoHienThi = phuTungDao.findDuoiNguongCanhBao(kw);
            view.getModelCanhBao().setRowCount(0);
            for (PhuTung pt : dsCanhBaoHienThi) {
                view.getModelCanhBao().addRow(new Object[]{
                        pt.getMaSKU(),
                        pt.getTenPhuTung(),
                        pt.getHangSanXuat(),
                        pt.getViTriLuuKho(),
                        pt.getSoLuongTon() + " / " + pt.getNguongCanhBao(),
                        "Nhập kho →"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void doBangYeuCau() {
        view.getModelYeuCau().setRowCount(0);
        int limit = (dangXemTatCaYeuCau || dsYeuCauTimDuoc.size() <= SO_DONG_MAC_DINH)
                ? dsYeuCauTimDuoc.size()
                : SO_DONG_MAC_DINH;

        dsYeuCauHienThi = new ArrayList<>(dsYeuCauTimDuoc.subList(0, limit));
        try {
            for (PhieuYeuCauXuatKho yc : dsYeuCauHienThi) {
                String tenKTV = yeuCauDao.getTenKTV(yc.getKyThuatVien().getMaNV());
                String tomTatPT = yeuCauDao.getTomTatPhuTungYeuCau(yc.getMaPhieuYC());
                String thaoTac;
                switch (yc.getTrangThai()) {
                    case CHO_XUAT: thaoTac = "Duyệt xuất →"; break;
                    case TU_CHOI: thaoTac = "Xem lý do"; break;
                    default: thaoTac = "Xem phiếu"; break;
                }
                view.getModelYeuCau().addRow(new Object[]{
                        yc.getMaPhieuYC(),
                        yc.getPhieu().getMaPhieuTNSC(),
                        tenKTV,
                        tomTatPT,
                        yc.getTrangThai(),
                        thaoTac
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        view.getLblThongKeYeuCau().setText(dsYeuCauHienThi.size() + " yêu cầu • Ngày " + LocalDate.now().format(DATE_FMT));
    }
}