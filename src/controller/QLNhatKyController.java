package controller;

import dao.NhatKy_Dao;
import gui.QLNhatKy;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Tải và tìm kiếm nhật ký; màn hình chỉ đọc nên không có thao tác ghi. */
public class QLNhatKyController {
    private static final DateTimeFormatter DINH_DANG = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final QLNhatKy view;
    private final NhatKy_Dao dao = new NhatKy_Dao();
    private int phienBan;

    public QLNhatKyController(QLNhatKy view) {
        this.view = view;
        initController();
        taiDuLieu();
    }

    private void initController() {
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { taiDuLieu(); }
            @Override public void removeUpdate(DocumentEvent e) { taiDuLieu(); }
            @Override public void changedUpdate(DocumentEvent e) { taiDuLieu(); }
        });
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai");
        view.getActionMap().put("taiLai", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taiDuLieu();
            }
        });
    }

    private void taiDuLieu() {
        String tuKhoa = view.getTxtTimKiem().getText();
        int phien = ++phienBan;
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return dao.search(tuKhoa);
            }

            @Override
            protected void done() {
                if (phien != phienBan) {
                    return;
                }
                try {
                    List<Object[]> dong = new ArrayList<>();
                    for (Object[] r : get()) {
                        Object[] hienThi = r.clone();
                        hienThi[0] = ((LocalDateTime) r[0]).format(DINH_DANG);
                        dong.add(hienThi);
                    }
                    view.setDanhSach(dong);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Không thể tải nhật ký.\n" + ex.getMessage(), "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
