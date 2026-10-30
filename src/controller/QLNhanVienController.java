package controller;

import dao.NhanVien_Dao;
import entity.NhanVien;
import gui.QLNhanVien;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
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
    private String selectedId;
    private int loadVersion;
    private boolean loadErrorShown;

    public QLNhanVienController(QLNhanVien view) {
        this.view = view;
        initController();
        load();
    }

    private void initController() {
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                load();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                load();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                load();
            }
        });
        view.getCboLocViTri().addActionListener(e -> load());
        view.getBtnLuu().addActionListener(e -> save());
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
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblDanhSach().rowAtPoint(e.getPoint());
                if (row < 0) {
                    return;
                }
                int icon = util.UIComponents.viTriBieuTuong(
                        view.getTblDanhSach(), e, QLNhanVien.COT_THAO_TAC, 3);
                if (icon >= 0) {
                    act(row, icon);
                } else if (e.getClickCount() == 2) {
                    edit(row);
                }
            }
        });
    }

    private void load() {
        final int version = ++loadVersion;
        final String keyword = view.getTxtTimKiem().getText();
        final String position = "Tất cả vị trí".equals(view.getCboLocViTri().getSelectedItem())
                ? "" : String.valueOf(view.getCboLocViTri().getSelectedItem());
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
                                employee.getTenDangNhap() == null ? "—" : employee.getTenDangNhap(), ""
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

    private void act(int row, int icon) {
        int modelRow = view.getTblDanhSach().convertRowIndexToModel(row);
        String id = String.valueOf(view.getTableModel().getValueAt(modelRow, 0));
        if (icon == QLNhanVien.ICON_XEM) {
            NhanVien employee = find(id);
            if (employee != null) {
                showEmployeeDetail(employee);
            }
        } else if (icon == QLNhanVien.ICON_SUA) {
            edit(row);
        } else {
            selectedId = id;
            view.hienXacNhanXoa(
                    view.getTableModel().getValueAt(modelRow, 1) + " • " + id,
                    String.valueOf(view.getTableModel().getValueAt(modelRow, 4)));
        }
    }

    private void showEmployeeDetail(NhanVien employee) {
        String[][] fields = {
                {"Mã nhân viên:", employee.getMaNV()},
                {"Họ tên:", employee.getHoTen()},
                {"Số điện thoại:", employee.getSoDienThoai()},
                {"Địa chỉ:", employee.getDiaChi()},
                {"Vị trí:", employee.getViTriLamViec()},
                {"Tài khoản:", employee.getTenDangNhap() == null
                        ? "Chưa liên kết" : employee.getTenDangNhap()}
        };
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        content.setPreferredSize(new Dimension(430, 210));
        Font font = UIManager.getFont("Label.font").deriveFont(15f);

        for (int i = 0; i < fields.length; i++) {
            GridBagConstraints labelCell = new GridBagConstraints();
            labelCell.gridx = 0;
            labelCell.gridy = i;
            labelCell.weightx = 0.34;
            labelCell.fill = GridBagConstraints.HORIZONTAL;
            labelCell.anchor = GridBagConstraints.NORTHWEST;
            labelCell.insets = new Insets(5, 2, 5, 16);
            JLabel label = new JLabel(fields[i][0]);
            label.setFont(font.deriveFont(Font.BOLD));
            content.add(label, labelCell);

            GridBagConstraints valueCell = new GridBagConstraints();
            valueCell.gridx = 1;
            valueCell.gridy = i;
            valueCell.weightx = 0.66;
            valueCell.fill = GridBagConstraints.HORIZONTAL;
            valueCell.anchor = GridBagConstraints.NORTHWEST;
            valueCell.insets = new Insets(5, 0, 5, 2);
            String text = fields[i][1] == null || fields[i][1].trim().isEmpty()
                    ? "—" : fields[i][1];
            JLabel value = new JLabel(text);
            value.setFont(font);
            content.add(value, valueCell);
        }
        JOptionPane.showMessageDialog(view, content,
                "Thông tin nhân viên", JOptionPane.INFORMATION_MESSAGE);
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
            view.setCheDoSua(true);
        }
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
                    String.valueOf(view.getCboViTri().getSelectedItem()));
            boolean saved = editing ? dao.update(employee) : dao.insert(employee);
            if (!saved) {
                throw new SQLException("Không tìm thấy nhân viên để cập nhật.");
            }
            view.anXacNhanXoa();
            view.xoaTrangForm();
            view.getTxtMaNV().setText("");
            view.setCheDoSua(false);
            selectedId = null;
            load();
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
            if (!dao.delete(selectedId)) {
                throw new SQLException("Không tìm thấy nhân viên.");
            }
            view.anXacNhanXoa();
            selectedId = null;
            load();
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
