package controller;

import dao.NhatKy_Dao;
import entity.NhatKyHoatDong;
import gui.QLNhatKy;
import util.ChonNgay;
import util.UIComponents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
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
        UIComponents.tuNgayGanDay(view.getTxtTuNgay());
        UIComponents.tuTaiLai(this::taiDuLieu, view.getTxtTimKiem(), view.getTxtTuNgay(), view.getTxtDenNgay());
        taiDuLieu();
    }

    private void initController() {
        view.getBtnTaiDuLieu().addActionListener(e -> taiDuLieu());
        view.getBtnSaoLuu().addActionListener(e -> saoLuu());
        view.getTxtTimKiem().addActionListener(e -> taiDuLieu());
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai");
        view.getActionMap().put("taiLai", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taiDuLieu();
            }
        });
    }

    /** Sao lưu toàn bộ CSDL; tệp nằm trên máy chạy SQL Server. */
    private void saoLuu() {
        try {
            String tep = new dao.SaoLuu_Dao().saoLuu(util.PhienDangNhap.getHienTai().getTenDangNhap());
            JOptionPane.showMessageDialog(view, "Đã sao lưu dữ liệu vào tệp:\n" + tep, "Sao lưu",
                    JOptionPane.INFORMATION_MESSAGE);
            taiDuLieu();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không sao lưu được.\n" + ex.getMessage()
                    + "\n\nTài khoản CSDL của ứng dụng cần quyền db_backupoperator: chạy lại database/TaoLoginUngDung.sql.",
                    "Lỗi sao lưu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taiDuLieu() {
        UIComponents.goiYChuaTai(view.getTblDanhSach(), false);
        String tuKhoa = view.getTxtTimKiem().getText();
        LocalDate tuNgay = docNgay(view.getTxtTuNgay().getText());
        LocalDate denNgay = docNgay(view.getTxtDenNgay().getText());
        int phien = ++phienBan;
        new SwingWorker<List<NhatKyHoatDong>, Void>() {
            @Override
            protected List<NhatKyHoatDong> doInBackground() throws Exception {
                return dao.search(tuKhoa, tuNgay, denNgay);
            }

            @Override
            protected void done() {
                if (phien != phienBan) {
                    return;
                }
                try {
                    List<Object[]> dong = new ArrayList<>();
                    for (NhatKyHoatDong r : get()) {
                        dong.add(new Object[] {r.getThoiGian().format(DINH_DANG), r.getTenDangNhap(), r.getHanhDong(),
                            r.getDoiTuong(), r.getGiaTriCu(), r.getGiaTriMoi()});
                    }
                    view.setDanhSach(dong);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Không thể tải nhật ký.\n" + ex.getMessage(), "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static LocalDate docNgay(String chuoi) {
        return chuoi.isEmpty() ? null : LocalDate.parse(chuoi, ChonNgay.NGAY);
    }
}
