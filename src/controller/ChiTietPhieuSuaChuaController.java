package controller;

import com.itextpdf.text.DocumentException;
import dao.ChiTietSuaChua_DichVu_Dao;
import dao.DichVu_Dao;
import dao.NhanVien_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import entity.ChiTietSuaChua_DichVu;
import entity.DichVu;
import entity.NhanVien;
import util.PhanCongTuDong;
import util.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import entity.VaiTro;
import gui.ChiTietPhieuSuaChua;
import util.PdfExporter;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/** Điều phối chi tiết phiếu: dòng dịch vụ, chỉ định thợ, KTV phụ trách, trạng thái, hủy và in phiếu. */
public class ChiTietPhieuSuaChuaController {
    private final ChiTietPhieuSuaChua view;
    private final String maPhieu;
    private final DieuHuongPhieu dieuHuong;
    private final PhieuTiepNhanVaSuaChua_Dao phieuDao = new PhieuTiepNhanVaSuaChua_Dao();
    private final ChiTietSuaChua_DichVu_Dao chiTietDao = new ChiTietSuaChua_DichVu_Dao();
    private final DichVu_Dao dichVuDao = new DichVu_Dao();
    private final NhanVien_Dao nhanVienDao = new NhanVien_Dao();
    private final NumberFormat dinhDangTien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private List<ChiTietSuaChua_DichVu> danhSach = new ArrayList<>();
    private PhieuTiepNhanVaSuaChua phieu;

    public ChiTietPhieuSuaChuaController(ChiTietPhieuSuaChua view, String maPhieu, DieuHuongPhieu dieuHuong) {
        this.view = view;
        this.maPhieu = maPhieu;
        this.dieuHuong = dieuHuong;
        initController();
        taiDuLieu();
    }

    private void initController() {
        view.getBtnThemDichVu().addActionListener(e -> themDong());
        view.getBtnXoaDichVu().addActionListener(e -> xoaDong());
        view.getBtnTuDongPhanCong().addActionListener(e -> tuDongPhanCong());
        view.getBtnLuuPhanCong().addActionListener(e -> luuPhanCong());
        view.getBtnCapNhatTienDo().addActionListener(e -> capNhatTienDo());
        view.getBtnHuyPhieu().addActionListener(e -> huyPhieu());
        view.getBtnInPhieu().addActionListener(e -> inPhieu());
        view.getBtnQuayVe().addActionListener(e -> dieuHuong.moDanhSach());
        // Cảnh báo tự phân công cũ không còn đúng khi người dùng tự chọn thợ.
        view.getTblChiTiet().getColumnModel().getColumn(ChiTietPhieuSuaChua.COT_THO).getCellEditor()
                .addCellEditorListener(new CellEditorListener() {
                    @Override
                    public void editingStopped(ChangeEvent e) { view.hienLoi(null); }
                    @Override
                    public void editingCanceled(ChangeEvent e) { }
                });
    }

    // Thao tác đơn lẻ trên một phiếu nên gọi DAO trực tiếp trên EDT.
    // shortcut: giao diện đứng khi CSDL chậm, chuyển sang SwingWorker nếu kết nối qua mạng.
    private void taiDuLieu() {
        try {
            phieu = phieuDao.findById(maPhieu);
            if (phieu == null) {
                JOptionPane.showMessageDialog(view, "Phiếu không còn tồn tại.");
                dieuHuong.moDanhSach();
                return;
            }
            taiDanhMuc();
            view.getTxtBienSo().setText(phieu.getXe().getBienSo());
            view.getTxtXe().setText(phieu.getTenXe());
            view.getTxtKhachHang().setText(phieu.getXe().getKhachHang().getHoTen());
            view.getTxtSoDienThoai().setText(phieu.getXe().getKhachHang().getSoDienThoai());
            view.getTxtSoKm().setText(dinhDangTien.format(phieu.getSoKm()) + " km");
            view.getTxtPhuTungDuKien().setText(phieu.getPhuTungDuKien());
            view.getTxtGhiChuKyThuat().setText(phieu.getGhiChuKyThuat());
            if (phieu.getTrangThai() == TrangThaiPhieu.DA_HUY
                    && ((DefaultComboBoxModel<TrangThaiPhieu>) view.getCboTrangThai().getModel())
                            .getIndexOf(TrangThaiPhieu.DA_HUY) < 0) {
                view.getCboTrangThai().addItem(TrangThaiPhieu.DA_HUY);
            }
            view.getCboTrangThai().setSelectedItem(phieu.getTrangThai());
            view.getCboKtvPhuTrach().setSelectedItem(thoHienThi(phieu.getNhanVienPhuTrach()));
            view.getThanhTienTrinh().setHienTai(phieuDao.buocHienTai(maPhieu));
            hienDanhSach();
            view.setKhoa(phieuDao.isKhoa(maPhieu));
        } catch (SQLException ex) {
            hienLoiDuLieu(ex);
        }
    }

    private void taiDanhMuc() throws SQLException {
        if (view.getCboDichVu().getItemCount() > 0) {
            return;
        }
        for (DichVu dv : dichVuDao.findAll()) {
            view.getCboDichVu().addItem(dv);
        }
        view.getCboKtvPhuTrach().addItem(ChiTietPhieuSuaChua.CHO_PHAN_CONG);
        view.getCboThoTrongBang().addItem(ChiTietPhieuSuaChua.CHO_PHAN_CONG);
        for (NhanVien nv : nhanVienDao.findByViTri(VaiTro.KY_THUAT_VIEN)) {
            view.getCboKtvPhuTrach().addItem(nv);
            view.getCboThoTrongBang().addItem(nv);
        }
    }

    /** Tìm mục đúng trong danh sách thợ để bảng hiển thị họ tên thay vì chỉ mã. */
    private Object thoHienThi(NhanVien tho) {
        if (tho != null) {
            JComboBox<Object> cbo = view.getCboThoTrongBang();
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Object muc = cbo.getItemAt(i);
                if (muc instanceof NhanVien && tho.getMaNV().equals(((NhanVien) muc).getMaNV())) {
                    return muc;
                }
            }
        }
        return ChiTietPhieuSuaChua.CHO_PHAN_CONG;
    }

    private void hienDanhSach() throws SQLException {
        danhSach = chiTietDao.findByPhieu(maPhieu);
        view.getTableModel().setRowCount(0);
        double tong = 0;
        int stt = 1;
        for (ChiTietSuaChua_DichVu d : danhSach) {
            view.getTableModel().addRow(new Object[] {stt++, d.getDichVu().getTenDichVu(), thoHienThi(d.getKyThuatVien()),
                d.getTrangThaiTienDo(), dinhDangTien.format(d.getTienCongThucTe())});
            tong += d.getTienCongThucTe();
        }
        view.setTongTienCong(dinhDangTien.format(tong) + " đ");
        view.canChieuCaoBang();
    }

    private void themDong() {
        DichVu dv = (DichVu) view.getCboDichVu().getSelectedItem();
        if (dv == null) {
            view.hienLoi("Vui lòng chọn dịch vụ.");
            return;
        }
        for (ChiTietSuaChua_DichVu d : danhSach) {
            if (d.getDichVu().equals(dv)) {
                view.hienLoi("Dịch vụ này đã có trong phiếu.");
                return;
            }
        }
        ChiTietSuaChua_DichVu moi = new ChiTietSuaChua_DichVu();
        moi.setPhieu(phieu);
        moi.setDichVu(dv);
        moi.setTrangThaiTienDo(TrangThaiTienDo.CHO_SUA);
        moi.setTienCongThucTe(dv.getTienCong());
        ghiGiuThayDoi(() -> chiTietDao.insert(moi));
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getDichVu().equals(dv)) {
                tuChonTho(Collections.singletonList(i), false);
            }
        }
    }

    /** Điền thợ vào các dòng chưa có thợ (và KTV phụ trách nếu còn trống); chỉ điền lên bảng, chưa lưu. */
    private void tuDongPhanCong() {
        if (view.getTblChiTiet().isEditing()) {
            view.getTblChiTiet().getCellEditor().stopCellEditing();
        }
        List<Integer> dongTrong = new ArrayList<>();
        for (int i = 0; i < danhSach.size(); i++) {
            if (!(view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_THO) instanceof NhanVien)) {
                dongTrong.add(i);
            }
        }
        if (dongTrong.isEmpty()) {
            view.hienLoi("Mọi dịch vụ đã có thợ phụ trách.");
            return;
        }
        tuChonTho(dongTrong, true);
    }

    private void tuChonTho(List<Integer> dong, boolean dienPhuTrach) {
        List<DichVu> dichVu = new ArrayList<>();
        for (int i : dong) {
            dichVu.add(danhSach.get(i).getDichVu());
        }
        try {
            PhanCongTuDong.KetQua ketQua = NghiepVuPhieu.chonKyThuatVien(LocalDateTime.now(), dichVu);
            for (int i : dong) {
                NhanVien tho = ketQua.getPhanCong().get(danhSach.get(i).getDichVu());
                if (tho != null) {
                    view.getTableModel().setValueAt(thoHienThi(tho), i, ChiTietPhieuSuaChua.COT_THO);
                }
            }
            if (dienPhuTrach && ketQua.getPhuTrach() != null
                    && !(view.getCboKtvPhuTrach().getSelectedItem() instanceof NhanVien)) {
                view.getCboKtvPhuTrach().setSelectedItem(thoHienThi(ketQua.getPhuTrach()));
            }
            view.hienLoi(ketQua.getGhiChu().isEmpty() ? null : String.join(" ", ketQua.getGhiChu()));
        } catch (SQLException ex) {
            hienLoiDuLieu(ex);
        }
    }

    private void xoaDong() {
        int dong = view.getTblChiTiet().getSelectedRow();
        if (dong < 0) {
            view.hienLoi("Vui lòng chọn dòng cần xóa.");
            return;
        }
        ChiTietSuaChua_DichVu dang = danhSach.get(view.getTblChiTiet().convertRowIndexToModel(dong));
        if (dang.getTrangThaiTienDo() == TrangThaiTienDo.HOAN_THANH) {
            view.hienLoi("Không thể xóa dịch vụ đã hoàn thành.");
            return;
        }
        ghiGiuThayDoi(() -> chiTietDao.delete(maPhieu, dang.getDichVu().getMaDichVu()));
    }

    /** Thêm/xóa dòng tải lại bảng từ CSDL; giữ lại thợ, tiền công, KTV phụ trách và ghi chú chưa lưu của các dòng khác. */
    private void ghiGiuThayDoi(ThaoTacGhi thaoTac) {
        if (view.getTblChiTiet().isEditing()) {
            view.getTblChiTiet().getCellEditor().stopCellEditing();
        }
        Map<DichVu, Object[]> dangSua = new HashMap<>();
        for (int i = 0; i < danhSach.size(); i++) {
            dangSua.put(danhSach.get(i).getDichVu(), new Object[] {
                view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_THO),
                view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_TIEN_CONG)});
        }
        Object ktv = view.getCboKtvPhuTrach().getSelectedItem();
        String ghiChu = view.getTxtGhiChuKyThuat().getText();
        ghi(thaoTac);
        for (int i = 0; i < danhSach.size(); i++) {
            Object[] cu = dangSua.get(danhSach.get(i).getDichVu());
            if (cu != null) {
                view.getTableModel().setValueAt(cu[0], i, ChiTietPhieuSuaChua.COT_THO);
                view.getTableModel().setValueAt(cu[1], i, ChiTietPhieuSuaChua.COT_TIEN_CONG);
            }
        }
        view.getCboKtvPhuTrach().setSelectedItem(ktv);
        view.getTxtGhiChuKyThuat().setText(ghiChu);
    }

    private void luuPhanCong() {
        if (view.getTblChiTiet().isEditing()) {
            view.getTblChiTiet().getCellEditor().stopCellEditing();
        }
        ghi(() -> {
            for (int i = 0; i < danhSach.size(); i++) {
                ChiTietSuaChua_DichVu d = danhSach.get(i);
                Object tho = view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_THO);
                d.setKyThuatVien(tho instanceof NhanVien ? (NhanVien) tho : null);
                d.setTienCongThucTe(util.FormatUtil.parseCurrency(
                        String.valueOf(view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_TIEN_CONG))));
            }
            Object ktv = view.getCboKtvPhuTrach().getSelectedItem();
            return chiTietDao.luuPhanCong(maPhieu, danhSach, ktv instanceof NhanVien ? ((NhanVien) ktv).getMaNV() : null,
                    view.getTxtGhiChuKyThuat().getText().trim(), PhienDangNhap.getHienTai().getTenDangNhap());
        });
    }

    private void capNhatTienDo() {
        TrangThaiPhieu trangThai = (TrangThaiPhieu) view.getCboTrangThai().getSelectedItem();
        TrangThaiPhieu hienTai = phieu.getTrangThai();
        if (trangThai == hienTai) {
            view.hienLoi(null);
            return;
        }
        if (!NghiepVuPhieu.coTheChuyenSang(hienTai, trangThai)) {
            view.hienLoi("Không thể chuyển trạng thái từ " + hienTai + " sang " + trangThai + ".");
            return;
        }
        phieu.setDanhSachDichVu(danhSach);
        if (trangThai == TrangThaiPhieu.HOAN_THANH && !NghiepVuPhieu.coTheHoanThanh(phieu)) {
            view.hienLoi("Chỉ hoàn thành được phiếu khi mọi dịch vụ đã hoàn thành.");
            return;
        }
        ghi(() -> NghiepVuPhieu.doiTrangThai(maPhieu, hienTai, trangThai, PhienDangNhap.getHienTai().getTenDangNhap()));
    }

    private void huyPhieu() {
        int chon = JOptionPane.showConfirmDialog(view, "Hủy phiếu " + maPhieu + "? Phiếu vẫn được lưu để đối soát.", "Xác nhận hủy phiếu",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (phieuDao.huy(maPhieu, PhienDangNhap.getHienTai().getTenDangNhap())) {
                dieuHuong.moDanhSach();
            } else {
                view.hienLoi("Không thể hủy phiếu đã có hóa đơn, phiếu bàn giao hoặc yêu cầu xuất kho.");
            }
        } catch (SQLException ex) {
            hienLoiDuLieu(ex);
        }
    }

    private void inPhieu() {
        JFileChooser chon = new JFileChooser();
        chon.setSelectedFile(new File("phieu-" + maPhieu + ".pdf"));
        if (chon.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            File tep = chon.getSelectedFile();
            PdfExporter.xuatPhieuSuaChua(tep, phieu, danhSach);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tep);
            }
        } catch (IOException | DocumentException ex) {
            JOptionPane.showMessageDialog(view, "Không thể xuất phiếu.\n" + ex.getMessage(), "Lỗi xuất PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private interface ThaoTacGhi {
        boolean chay() throws SQLException;
    }

    /** DAO trả về false khi phiếu đã bị khóa bởi hóa đơn. */
    private void ghi(ThaoTacGhi thaoTac) {
        try {
            view.hienLoi(null);
            if (!thaoTac.chay()) {
                view.hienLoi("Không thực hiện được: phiếu đã hoàn thành, đã có hóa đơn hoặc dữ liệu đã thay đổi.");
            }
            taiDuLieu();
        } catch (NumberFormatException ex) {
            view.hienLoi("Tiền công phải là số nguyên đồng, ví dụ 150000 hoặc 150.000.");
        } catch (IllegalArgumentException ex) {
            view.hienLoi(ex.getMessage());
        } catch (SQLException ex) {
            hienLoiDuLieu(ex);
        }
    }

    private void hienLoiDuLieu(SQLException ex) {
        JOptionPane.showMessageDialog(view, "Lỗi dữ liệu:\n" + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
