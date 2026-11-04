package controller;

import com.itextpdf.text.DocumentException;
import dao.ChiTietSuaChua_DichVu_Dao;
import dao.DichVu_Dao;
import dao.NhanVien_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import entity.ChiTietSuaChua_DichVu;
import entity.DichVu;
import entity.NhanVien;
import entity.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import entity.VaiTro;
import gui.ChiTietPhieuSuaChua;
import util.PdfExporter;

import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
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

    /** Phiếu chỉ được đánh dấu hoàn thành khi có dịch vụ và mọi dịch vụ đã xong. */
    public static boolean coTheHoanThanh(List<ChiTietSuaChua_DichVu> dong) {
        if (dong.isEmpty()) {
            return false;
        }
        for (ChiTietSuaChua_DichVu d : dong) {
            if (d.getTrangThaiTienDo() != TrangThaiTienDo.HOAN_THANH) {
                return false;
            }
        }
        return true;
    }

    private void initController() {
        view.getBtnThemDichVu().addActionListener(e -> themDong());
        view.getBtnXoaDichVu().addActionListener(e -> xoaDong());
        view.getBtnLuuPhanCong().addActionListener(e -> luuPhanCong());
        view.getBtnCapNhatTienDo().addActionListener(e -> capNhatTienDo());
        view.getBtnHuyPhieu().addActionListener(e -> huyPhieu());
        view.getBtnInPhieu().addActionListener(e -> inPhieu());
        view.getBtnQuayVe().addActionListener(e -> dieuHuong.moDanhSach());
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
            view.getTxtBienSo().setText(phieu.getBienSo());
            view.getTxtXe().setText(phieu.getThongTinXe());
            view.getTxtKhachHang().setText(phieu.getTenKhachHang());
            view.getTxtSoDienThoai().setText(phieu.getSdtKhachHang());
            view.getTxtSoKm().setText(dinhDangTien.format(phieu.getSoKm()) + " km");
            view.getTxtPhuTungDuKien().setText(phieu.getPhuTungDuKien());
            view.getTxtGhiChuKyThuat().setText(phieu.getGhiChuKyThuat());
            if (phieu.getTrangThai() == TrangThaiPhieu.DA_HUY
                    && ((DefaultComboBoxModel<TrangThaiPhieu>) view.getCboTrangThai().getModel())
                            .getIndexOf(TrangThaiPhieu.DA_HUY) < 0) {
                view.getCboTrangThai().addItem(TrangThaiPhieu.DA_HUY);
            }
            view.getCboTrangThai().setSelectedItem(phieu.getTrangThai());
            view.getCboKtvPhuTrach().setSelectedItem(thoHienThi(phieu.getMaNVPhuTrach()));
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
    private Object thoHienThi(String maNV) {
        if (maNV != null) {
            JComboBox<Object> cbo = view.getCboThoTrongBang();
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Object muc = cbo.getItemAt(i);
                if (muc instanceof NhanVien && maNV.equals(((NhanVien) muc).getMaNV())) {
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
            view.getTableModel().addRow(new Object[] {stt++, d.getTenDichVu(), thoHienThi(d.getMaNVKyThuat()),
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
            if (d.getMaDichVu().equals(dv.getMaDichVu())) {
                view.hienLoi("Dịch vụ này đã có trong phiếu.");
                return;
            }
        }
        ChiTietSuaChua_DichVu moi = new ChiTietSuaChua_DichVu();
        moi.setMaPhieuTNSC(maPhieu);
        moi.setMaDichVu(dv.getMaDichVu());
        moi.setTrangThaiTienDo(TrangThaiTienDo.CHO_SUA);
        moi.setTienCongThucTe(dv.getTienCong());
        ghi(() -> chiTietDao.insert(moi));
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
        ghi(() -> chiTietDao.delete(maPhieu, dang.getMaDichVu()));
    }

    private void luuPhanCong() {
        if (view.getTblChiTiet().isEditing()) {
            view.getTblChiTiet().getCellEditor().stopCellEditing();
        }
        ghi(() -> {
            for (int i = 0; i < danhSach.size(); i++) {
                ChiTietSuaChua_DichVu d = danhSach.get(i);
                Object tho = view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_THO);
                d.setMaNVKyThuat(tho instanceof NhanVien ? ((NhanVien) tho).getMaNV() : null);
                d.setTienCongThucTe(Double.parseDouble(
                        String.valueOf(view.getTableModel().getValueAt(i, ChiTietPhieuSuaChua.COT_TIEN_CONG))
                                .replace(".", "").trim()));
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
        if (!PhieuTiepNhanVaSuaChua.coTheChuyen(hienTai, trangThai)) {
            view.hienLoi("Không thể chuyển trạng thái từ " + hienTai + " sang " + trangThai + ".");
            return;
        }
        if (trangThai == TrangThaiPhieu.HOAN_THANH && !coTheHoanThanh(danhSach)) {
            view.hienLoi("Chỉ hoàn thành được phiếu khi mọi dịch vụ đã hoàn thành.");
            return;
        }
        ghi(() -> phieuDao.capNhatTrangThai(maPhieu, hienTai, trangThai));
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

    /** DAO trả về false khi phiếu vừa bị khóa bởi hóa đơn lập ở máy khác. */
    private void ghi(ThaoTacGhi thaoTac) {
        try {
            view.hienLoi(null);
            if (!thaoTac.chay()) {
                view.hienLoi("Không thực hiện được: phiếu đã có hóa đơn hoặc dữ liệu đã thay đổi.");
            }
            taiDuLieu();
        } catch (NumberFormatException ex) {
            view.hienLoi("Tiền công phải là số.");
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
