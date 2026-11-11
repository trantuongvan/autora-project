package controller;

import dao.DichVu_Dao;
import dao.NhaCungCap_Dao;
import entity.DichVu;
import entity.NhaCungCap;
import gui.QLDanhMuc;
import gui.QLDanhMuc.TheDanhMuc;
import util.FormatUtil;
import util.PhienDangNhap;
import util.UIComponents;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Thêm, sửa, xóa dịch vụ và nhà cung cấp. Mỗi thao tác được ghi nhật ký trong DAO. */
public class QLDanhMucController {
    private final QLDanhMuc view;
    private final DichVu_Dao dichVuDao = new DichVu_Dao();
    private final NhaCungCap_Dao nhaCungCapDao = new NhaCungCap_Dao();
    private List<DichVu> dsDichVu = new ArrayList<>();
    private List<NhaCungCap> dsNhaCungCap = new ArrayList<>();

    public QLDanhMucController(QLDanhMuc view) {
        this.view = view;
        ganSuKien(view.getDichVu(), this::taiDichVu, this::chonDichVu, this::luuDichVu, this::xoaDichVu);
        ganSuKien(view.getNhaCungCap(), this::taiNhaCungCap, this::chonNhaCungCap, this::luuNhaCungCap, this::xoaNhaCungCap);
        taiDichVu();
        taiNhaCungCap();
    }

    private void ganSuKien(TheDanhMuc the, Runnable tai, java.util.function.IntConsumer chon, Runnable luu, Runnable xoa) {
        the.getBtnTaiDuLieu().addActionListener(e -> tai.run());
        the.getTxtTimKiem().addActionListener(e -> tai.run());
        UIComponents.tuTaiLai(tai, the.getTxtTimKiem());
        the.getBtnThemMoi().addActionListener(e -> the.datCheDoThem());
        the.getBtnLuu().addActionListener(e -> luu.run());
        the.getBtnXoa().addActionListener(e -> xoa.run());
        the.getTblDanhSach().getSelectionModel().addListSelectionListener(e -> {
            int dong = the.getTblDanhSach().getSelectedRow();
            if (!e.getValueIsAdjusting() && dong >= 0) {
                chon.accept(the.getTblDanhSach().convertRowIndexToModel(dong));
            }
        });
    }

    // ===== Dịch vụ =====

    private void taiDichVu() {
        try {
            dsDichVu = dichVuDao.search(view.getDichVu().getTxtTimKiem().getText());
            List<Object[]> dong = new ArrayList<>();
            for (DichVu dv : dsDichVu) {
                dong.add(new Object[] {dv.getMaDichVu(), dv.getTenDichVu(), FormatUtil.formatCurrency(dv.getTienCong()),
                        dv.getThoiGianChuan() + " phút"});
            }
            view.getDichVu().setDanhSach(dong);
        } catch (SQLException ex) {
            UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void chonDichVu(int dong) {
        DichVu dv = dsDichVu.get(dong);
        view.getDichVu().datCheDoSua(new String[] {dv.getMaDichVu(), dv.getTenDichVu(),
                FormatUtil.formatCurrency(dv.getTienCong(), false), String.valueOf(dv.getThoiGianChuan())});
    }

    private void luuDichVu() {
        TheDanhMuc the = view.getDichVu();
        try {
            DichVu dv = new DichVu();
            String ma = the.getMaDangSua();
            if (ma != null) {
                dv.setMaDichVu(ma);
            }
            dv.setTenDichVu(the.giaTri(1));
            dv.setTienCong(FormatUtil.parseCurrency(the.giaTri(2)));
            dv.setThoiGianChuan(docSoPhut(the.giaTri(3)));
            String daLuu = dichVuDao.luu(dv, PhienDangNhap.getHienTai().getTenDangNhap());
            taiDichVu();
            the.datCheDoThem();
            JOptionPane.showMessageDialog(view, "Đã lưu dịch vụ " + daLuu + ".");
        } catch (NumberFormatException ex) {
            the.hienLoi("Tiền công phải là số nguyên đồng, ví dụ 150000 hoặc 150.000.");
        } catch (IllegalArgumentException ex) {
            the.hienLoi(ex.getMessage());
        } catch (SQLException ex) {
            UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void xoaDichVu() {
        String ma = view.getDichVu().getMaDangSua();
        if (ma == null || !xacNhanXoa("dịch vụ " + ma)) {
            return;
        }
        try {
            if (!dichVuDao.xoa(ma, PhienDangNhap.getHienTai().getTenDangNhap())) {
                view.getDichVu().hienLoi("Không xóa được: dịch vụ đã dùng trên phiếu sửa chữa hoặc là kỹ năng của kỹ thuật viên.");
                return;
            }
            taiDichVu();
            view.getDichVu().datCheDoThem();
        } catch (SQLException ex) {
            UIComponents.baoLoiCsdl(view, ex);
        }
    }

    /** Số phút nguyên dương; báo lỗi bằng tiếng Việt thay vì NumberFormatException. */
    public static int docSoPhut(String nhap) {
        try {
            return Integer.parseInt(nhap.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Thời gian chuẩn phải là số phút nguyên, ví dụ 60.");
        }
    }

    // ===== Nhà cung cấp =====

    private void taiNhaCungCap() {
        try {
            dsNhaCungCap = nhaCungCapDao.search(view.getNhaCungCap().getTxtTimKiem().getText());
            List<Object[]> dong = new ArrayList<>();
            for (NhaCungCap ncc : dsNhaCungCap) {
                dong.add(new Object[] {ncc.getMaNCC(), ncc.getTenNCC(), ncc.getSoDienThoai(), ncc.getDiaChi()});
            }
            view.getNhaCungCap().setDanhSach(dong);
        } catch (SQLException ex) {
            UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void chonNhaCungCap(int dong) {
        NhaCungCap ncc = dsNhaCungCap.get(dong);
        view.getNhaCungCap().datCheDoSua(new String[] {ncc.getMaNCC(), ncc.getTenNCC(),
                ncc.getSoDienThoai() == null ? "" : ncc.getSoDienThoai(), ncc.getDiaChi() == null ? "" : ncc.getDiaChi()});
    }

    private void luuNhaCungCap() {
        TheDanhMuc the = view.getNhaCungCap();
        try {
            NhaCungCap ncc = new NhaCungCap();
            String ma = the.getMaDangSua();
            if (ma != null) {
                ncc.setMaNCC(ma);
            }
            ncc.setTenNCC(the.giaTri(1));
            ncc.setSoDienThoai(the.giaTri(2));
            ncc.setDiaChi(the.giaTri(3));
            String daLuu = nhaCungCapDao.luu(ncc, PhienDangNhap.getHienTai().getTenDangNhap());
            taiNhaCungCap();
            the.datCheDoThem();
            JOptionPane.showMessageDialog(view, "Đã lưu nhà cung cấp " + daLuu + ".");
        } catch (IllegalArgumentException ex) {
            the.hienLoi(ex.getMessage());
        } catch (SQLException ex) {
            UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private void xoaNhaCungCap() {
        String ma = view.getNhaCungCap().getMaDangSua();
        if (ma == null || !xacNhanXoa("nhà cung cấp " + ma)) {
            return;
        }
        try {
            if (!nhaCungCapDao.xoa(ma, PhienDangNhap.getHienTai().getTenDangNhap())) {
                view.getNhaCungCap().hienLoi("Không xóa được: nhà cung cấp đã có phiếu nhập kho.");
                return;
            }
            taiNhaCungCap();
            view.getNhaCungCap().datCheDoThem();
        } catch (SQLException ex) {
            UIComponents.baoLoiCsdl(view, ex);
        }
    }

    private boolean xacNhanXoa(String doiTuong) {
        return JOptionPane.showConfirmDialog(view, "Xóa " + doiTuong + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
