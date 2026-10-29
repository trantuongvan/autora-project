package controller;

import dao.Xe_Dao;
import entity.Xe;
import gui.QLHoSoXe;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tìm kiếm và CRUD hồ sơ xe, danh sách chủ xe và biểu mẫu dữ liệu. */
public class QLHoSoXeController {
    private final QLHoSoXe view;
    private final Xe_Dao dao = new Xe_Dao();
    private String selectedPlate;
    private int loadVersion;
    private boolean loadErrorShown;

    public QLHoSoXeController(QLHoSoXe view) {
        this.view = view;
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { load(); }
            public void removeUpdate(DocumentEvent e) { load(); }
            public void changedUpdate(DocumentEvent e) { load(); }
        });
        view.getBtnLuu().addActionListener(e -> save());
        view.getBtnThemHoSo().addActionListener(e -> selectedPlate = null);
        view.getBtnHuy().addActionListener(e -> selectedPlate = null);
        view.getTblDanhSach().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("DELETE"), "xoaHoSoXe");
        view.getTblDanhSach().getActionMap().put("xoaHoSoXe", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { deleteSelected(); }
        });
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTable table = view.getTblDanhSach();
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewColumn = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewColumn < 0) return;
                int actionColumn = table.convertColumnIndexToView(QLHoSoXe.COT_THAO_TAC);
                if (viewColumn == actionColumn) {
                    Rectangle cell = table.getCellRect(viewRow, viewColumn, false);
                    int icon = util.UIComponents.viTriBieuTuong(table, e, actionColumn, 2);
                    // Chia toàn bộ ô thao tác thành hai vùng để icon vẫn dễ bấm.
                    if (icon < 0) icon = e.getX() - cell.x < cell.width / 2 ? QLHoSoXe.ICON_XEM : QLHoSoXe.ICON_SUA;
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    String plate = String.valueOf(view.getTableModel().getValueAt(modelRow, 0));
                    if (icon == QLHoSoXe.ICON_XEM) viewRecord(plate, modelRow);
                    else edit(plate, modelRow);
                } else if (e.getClickCount() > 1) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    edit(String.valueOf(view.getTableModel().getValueAt(modelRow, 0)), modelRow);
                }
            }
        });
        load();
    }

    private void load() {
        final int version = ++loadVersion;
        final String query = view.getTxtTimKiem().getText();
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                return new Object[]{dao.search(query), dao.findChuSoHuu()};
            }
            @Override protected void done() {
                if (version != loadVersion) return;
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<Object[]> rows = (List<Object[]>) result[0];
                    @SuppressWarnings("unchecked") List<String> owners = (List<String>) result[1];
                    view.setDanhSach(rows);
                    view.setThongKeBang(rows.size(), selectedPlate);
                    view.setDanhSachChuSoHuu(owners);
                    loadErrorShown = false;
                } catch (Exception ex) {
                    view.setDanhSach(Collections.emptyList());
                    view.setThongKeBang(0, null);
                    view.setDanhSachChuSoHuu(Collections.emptyList());
                    if (!loadErrorShown) {
                        loadErrorShown = true;
                        JOptionPane.showMessageDialog(view,
                                "Không thể tải hồ sơ xe từ CSDL.\n" + rootMessage(ex),
                                "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    private void edit(String plate, int modelRow) {
        try {
            Xe xe = dao.findById(plate);
            if (xe == null) throw new SQLException("Không tìm thấy hồ sơ xe " + plate + ".");
            selectedPlate = plate;
            view.dienForm(xe);
            view.setCheDoSua(true);
            view.setThongKeBang(view.getTableModel().getRowCount(), selectedPlate);
            showHistory(modelRow);
        } catch (SQLException ex) {
            showLoadError("Không thể tải hồ sơ xe", ex);
        }
    }

    private void viewRecord(String plate, int modelRow) {
        try {
            Xe xe = dao.findById(plate);
            if (xe == null) throw new SQLException("Không tìm thấy hồ sơ xe " + plate + ".");
            selectedPlate = null;
            view.dienForm(xe);
            view.setCheDoXem(true);
            showHistory(modelRow);
        } catch (SQLException ex) {
            showLoadError("Không thể tải hồ sơ xe", ex);
        }
    }

    private void showHistory(int modelRow) {
        try {
            String plate = String.valueOf(view.getTableModel().getValueAt(modelRow, 0));
            List<Object[]> history = dao.findLichSu(plate);
            String owner = String.valueOf(view.getTableModel().getValueAt(modelRow, 4));
            String info = String.valueOf(view.getTableModel().getValueAt(modelRow, 5)) + " • "
                    + view.getTableModel().getValueAt(modelRow, 1) + " " + view.getTableModel().getValueAt(modelRow, 2);
            boolean paid = history.stream().anyMatch(h -> String.valueOf(h[3]).contains("Đã thanh toán"));
            view.hienThiLichSu(plate, owner, info, history,
                    paid ? "Phiếu đã thanh toán: nội dung sửa chữa được khóa, không thể thay đổi." : null);
        } catch (SQLException ex) {
            showLoadError("Không thể tải lịch sử sửa chữa", ex);
        }
    }

    private void save() {
        String plate = view.getTxtBienSo().getText().trim();
        try {
            if (!Xe.bienSoHopLe(plate)) {
                view.hienLoiBienSo("Biển số xe không đúng định dạng.");
                return;
            }
            String selected = String.valueOf(view.getCboChuSoHuu().getSelectedItem());
            if (selected.startsWith("Chọn")) throw new IllegalArgumentException("Xe phải có chủ sở hữu. Vui lòng chọn trước khi lưu.");
            String year = view.getTxtNam().getText().trim();
            Integer y = year.isEmpty() ? null : Integer.valueOf(year);
            Xe xe = new Xe(plate, view.getTxtVIN().getText(), String.valueOf(view.getCboHang().getSelectedItem()),
                    String.valueOf(view.getCboDong().getSelectedItem()), y, view.getTxtMauSon().getText(), selected.split(" - ", 2)[0]);
            boolean update = selectedPlate != null && plate.equalsIgnoreCase(selectedPlate);
            if (!update && dao.findById(xe.getBienSo()) != null) {
                view.hienLoiBienSo("Biển số đã tồn tại trong hồ sơ.");
                return;
            }
            if (!(update ? dao.update(xe) : dao.insert(xe))) throw new SQLException("Không thể ghi hồ sơ xe.");
            selectedPlate = null;
            view.xoaTrangForm();
            view.setCheDoSua(false);
            view.anLichSu();
            load();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Năm sản xuất phải là số nguyên.", "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            if (!Xe.bienSoHopLe(plate)) view.hienLoiBienSo(ex.getMessage());
            else if (String.valueOf(view.getCboChuSoHuu().getSelectedItem()).startsWith("Chọn")) view.hienLoiChuSoHuu(ex.getMessage());
            else JOptionPane.showMessageDialog(view, ex.getMessage(), "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể lưu hồ sơ. Hãy kiểm tra dữ liệu hoặc xe đang được phiếu sửa chữa sử dụng.\n" + ex.getMessage(),
                    "Lỗi lưu dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = view.getTblDanhSach().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblDanhSach().convertRowIndexToModel(row);
        delete(String.valueOf(view.getTableModel().getValueAt(modelRow, 0)));
    }

    private void delete(String plate) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Xóa hồ sơ xe " + plate + "? Xe đã có phiếu sửa chữa sẽ không thể xóa.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (!dao.delete(plate)) throw new SQLException("Không tìm thấy hồ sơ xe.");
            if (plate.equals(selectedPlate)) selectedPlate = null;
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể xóa hồ sơ xe đang được phiếu tiếp nhận hoặc dữ liệu khác sử dụng.\n" + ex.getMessage(),
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showLoadError(String title, SQLException ex) {
        JOptionPane.showMessageDialog(view, title + ": " + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private static String rootMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() == null ? ex.getMessage() : cause.getMessage();
    }
}
