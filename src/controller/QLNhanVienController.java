package controller;

import dao.DichVu_Dao;
import dao.NhanVien_Dao;
import entity.CaLamViec;
import entity.DichVu;
import entity.KyNang;
import entity.NhanVien;
import util.PhienDangNhap;
import entity.VaiTro;
import gui.QLNhanVien;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Điều phối CRUD nhân viên, tìm kiếm và lọc theo vị trí. */
public class QLNhanVienController {

    private final QLNhanVien view;
    private final NhanVien_Dao dao = new NhanVien_Dao();
    private List<DichVu> dichVu = new ArrayList<>();
    private final String tenDangNhap;
    private String selectedId;
    private int loadVersion;
    private boolean loadErrorShown;
    private boolean daTai;

    public QLNhanVienController(QLNhanVien view) {
        this(view, tenDangNhapHienTai());
    }

    public QLNhanVienController(QLNhanVien view, String tenDangNhap) {
        this.view = view;
        this.tenDangNhap = tenDangNhap == null || tenDangNhap.trim().isEmpty()
                ? "system" : tenDangNhap.trim();
        taiDanhMuc();
        initController();
        util.UIComponents.tuTaiLai(this::load, view.getTxtTimKiem(), view.getCboLocViTri());
        load();
    }

    private void taiDanhMuc() {
        try {
            view.napDanhSachCa(dao.findAllCa());
            dichVu = new DichVu_Dao().findAll();
            view.napDichVuKyNang(dichVu);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không thể tải ca làm việc và dịch vụ.\n" + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String tenDangNhapHienTai() {
        PhienDangNhap phien = PhienDangNhap.getHienTai();
        return phien == null ? "system" : phien.getTenDangNhap();
    }

    private void initController() {
        view.getTxtTimKiem().addActionListener(e -> load());
        view.getBtnTaiDuLieu().addActionListener(e -> load());
        view.getBtnLuu().addActionListener(e -> save());
        // Tick "Làm được" thì điền sẵn số phút chuẩn của dịch vụ.
        view.getKyNangModel().addTableModelListener(e -> {
            int dong = e.getFirstRow();
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 0
                    && dong >= 0 && dong < dichVu.size()
                    && Boolean.TRUE.equals(view.getKyNangModel().getValueAt(dong, 0))
                    && String.valueOf(view.getKyNangModel().getValueAt(dong, 2)).trim().isEmpty()) {
                view.getKyNangModel().setValueAt(dichVu.get(dong).getThoiGianChuan(), dong, 2);
            }
        });
        view.getBtnThemNhanVien().addActionListener(e -> {
            selectedId = null;
            view.getTxtMaNV().setText("");
        });
        view.getBtnXacNhanXoa().addActionListener(e -> delete());
        view.getBtnHuyXoa().addActionListener(e -> {
            selectedId = null;
            view.anXacNhanXoa();
        });
        view.getBtnDongXoa().addActionListener(e -> selectedId = null);
        view.getBtnHuy().addActionListener(e -> {
            selectedId = null;
            view.getTxtMaNV().setText("");
            view.setCheDoSua(false);
        });
        view.getTblDanhSach().getSelectionModel().addListSelectionListener(e -> {
            int row = view.getTblDanhSach().getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                edit(row);
            }
        });
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblDanhSach().rowAtPoint(e.getPoint());
                if (row >= 0 && util.UIComponents.viTriBieuTuong(view.getTblDanhSach(), e,
                        QLNhanVien.COT_THAO_TAC, 2) == QLNhanVien.ICON_XOA) {
                    hoiXoa(row);
                }
            }
        });
    }

    private void load() {
        daTai = true;
        util.UIComponents.goiYChuaTai(view.getTblDanhSach(), false);
        final int version = ++loadVersion;
        final String keyword = view.getTxtTimKiem().getText();
        final Object chonViTri = view.getCboLocViTri().getSelectedItem();
        final VaiTro position = chonViTri instanceof VaiTro ? (VaiTro) chonViTri : null;
        new SwingWorker<List<NhanVien>, Void>() {
            @Override
            protected List<NhanVien> doInBackground() throws Exception {
                return dao.search(keyword, position);
            }

            @Override
            protected void done() {
                if (version != loadVersion) {
                    return;
                }
                try {
                    List<NhanVien> employees = get();
                    ArrayList<Object[]> rows = new ArrayList<>();
                    for (NhanVien employee : employees) {
                        rows.add(new Object[]{
                                employee.getMaNV(), employee.getHoTen(), employee.getSoDienThoai(),
                                employee.getDiaChi(), employee.getViTriLamViec(),
                                employee.getTaiKhoan() == null ? "—" : employee.getTaiKhoan().getTenDangNhap(), ""
                        });
                    }
                    view.setDanhSach(rows);
                    loadErrorShown = false;
                } catch (Exception ex) {
                    view.setDanhSach(Collections.emptyList());
                    if (!loadErrorShown) {
                        loadErrorShown = true;
                        JOptionPane.showMessageDialog(view,
                                "Không thể tải danh sách nhân viên từ CSDL.\n" + rootMessage(ex),
                                "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    private void hoiXoa(int row) {
        int modelRow = view.getTblDanhSach().convertRowIndexToModel(row);
        String id = String.valueOf(view.getTableModel().getValueAt(modelRow, 0));
        selectedId = id;
        view.hienXacNhanXoa(
                view.getTableModel().getValueAt(modelRow, 1) + " • " + id,
                String.valueOf(view.getTableModel().getValueAt(modelRow, 4)));
    }

    private NhanVien find(String id) {
        try {
            return dao.findById(id);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không thể tải nhân viên: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void edit(int row) {
        int modelRow = view.getTblDanhSach().convertRowIndexToModel(row);
        selectedId = String.valueOf(view.getTableModel().getValueAt(modelRow, 0));
        NhanVien employee = find(selectedId);
        if (employee != null) {
            view.dienForm(employee);
            dienKyNang(selectedId);
            view.setCheDoSua(true);
        }
    }

    private void dienKyNang(String maNV) {
        try {
            view.hienLoiCa(null);
            view.hienLoiKyNang(null);
            javax.swing.table.DefaultTableModel model = view.getKyNangModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt("", i, 2);
                model.setValueAt(false, i, 0);
            }
            for (KyNang kyNang : dao.findKyNang(maNV)) {
                for (int i = 0; i < dichVu.size(); i++) {
                    if (dichVu.get(i).equals(kyNang.getDichVu())) {
                        // Điền số phút trước khi tick để không bị thay bằng số phút chuẩn.
                        model.setValueAt(kyNang.getThoiGianPhut(), i, 2);
                        model.setValueAt(true, i, 0);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không thể tải kỹ năng: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Đọc các dòng đã tick; trả về null và báo lỗi lên form khi số phút không hợp lệ. */
    private List<KyNang> docKyNang(NhanVien employee) {
        if (view.getTblKyNang().isEditing()) {
            view.getTblKyNang().getCellEditor().stopCellEditing();
        }
        List<KyNang> ketQua = new ArrayList<>();
        for (int i = 0; i < dichVu.size(); i++) {
            if (!Boolean.TRUE.equals(view.getKyNangModel().getValueAt(i, 0))) {
                continue;
            }
            try {
                ketQua.add(new KyNang(employee, dichVu.get(i),
                        Integer.parseInt(String.valueOf(view.getKyNangModel().getValueAt(i, 2)).trim())));
            } catch (IllegalArgumentException ex) {
                view.hienLoiKyNang("Số phút của \"" + dichVu.get(i).getTenDichVu() + "\" phải là số nguyên lớn hơn 0.");
                return null;
            }
        }
        return ketQua;
    }

    private void save() {
        boolean editing = view.getLblTieuDeFormText().startsWith("Sửa");
        String id = editing ? view.getTxtMaNV().getText().trim() : null;
        try {
            String name = view.getTxtHoTen().getText().trim();
            if (name.isEmpty()) {
                view.hienLoiHoTen("Họ tên không được để trống.");
                return;
            }
            String phone = view.getTxtSoDienThoai().getText().trim();
            if (!phone.matches("0[0-9]{9}")) {
                view.hienLoiSoDienThoai("SĐT phải gồm 10 chữ số và bắt đầu bằng 0.");
                return;
            }
            if (dao.existsSoDienThoai(phone, id)) {
                view.hienLoiSoDienThoai("Số điện thoại đã được nhân viên khác sử dụng.");
                return;
            }
            if (!editing) {
                id = dao.nextId();
            }
            NhanVien employee = new NhanVien(
                    id,
                    view.getTxtHoTen().getText(),
                    phone,
                    view.getTxtDiaChi().getText(),
                    (VaiTro) view.getCboViTri().getSelectedItem());
            Object chonCa = view.getCboCa().getSelectedItem();
            employee.setCaLamViec(chonCa instanceof CaLamViec ? (CaLamViec) chonCa : null);
            view.hienLoiCa(null);
            view.hienLoiKyNang(null);
            // Đổi khỏi Kỹ thuật viên thì không ghi kỹ năng nào, nên các dòng cũ bị xóa theo.
            if (employee.getViTriLamViec() == VaiTro.KY_THUAT_VIEN) {
                List<KyNang> kyNang = docKyNang(employee);
                if (kyNang == null) {
                    return;
                }
                if (!kyNang.isEmpty() && employee.getCaLamViec() == null) {
                    view.hienLoiCa("Kỹ thuật viên có kỹ năng phải được xếp ca làm việc.");
                    return;
                }
                employee.setDanhSachKyNang(kyNang);
            }
            boolean saved = editing
                    ? dao.update(employee, tenDangNhap)
                    : dao.insert(employee, tenDangNhap);
            if (!saved) {
                throw new SQLException("Không tìm thấy nhân viên để cập nhật.");
            }
            view.anXacNhanXoa();
            view.xoaTrangForm();
            view.getTxtMaNV().setText("");
            view.setCheDoSua(false);
            selectedId = null;
            if (daTai) load();
        } catch (IllegalArgumentException ex) {
            if (view.getTxtHoTen().getText().trim().isEmpty()) {
                view.hienLoiHoTen(ex.getMessage());
            } else {
                view.hienLoiSoDienThoai(ex.getMessage());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể lưu nhân viên. Hãy kiểm tra dữ liệu hoặc kết nối CSDL.\n"
                            + ex.getMessage(),
                    "Lỗi lưu dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void delete() {
        if (selectedId == null) {
            return;
        }
        try {
            if (!dao.delete(selectedId, PhienDangNhap.getHienTai().getTenDangNhap())) {
                throw new SQLException("Không tìm thấy nhân viên.");
            }
            view.anXacNhanXoa();
            selectedId = null;
            // Biểu mẫu đang hiện nhân viên vừa xóa: dọn đi để nút lưu không ghi vào mã đã mất.
            view.xoaTrangForm();
            view.getTxtMaNV().setText("");
            view.setCheDoSua(false);
            if (daTai) load();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể xóa nhân viên đang được tham chiếu bởi dữ liệu khác.\n"
                            + ex.getMessage(),
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static String rootMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null ? ex.getMessage() : message;
    }
}
