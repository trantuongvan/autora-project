package gui;

import connection.ConnectDB;
import util.UIComponents;
import util.UIConstants;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;

/** Màn hình chờ khi mở ứng dụng: kiểm tra kết nối CSDL rồi mở đăng nhập. Phụ trách: Nguyễn Xuân Việt Anh. */
public class ManHinhCho extends JWindow {

    private ManHinhCho() {
        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setBackground(UIConstants.CARD_BG);
        noiDung.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_COLOR), new EmptyBorder(28, 32, 24, 32)));
        noiDung.add(UIComponents.logoAutora(), BorderLayout.NORTH);
        noiDung.add(UIComponents.label("Đang kết nối cơ sở dữ liệu...", UIConstants.FONT_BODY.deriveFont(14f),
                UIConstants.TEXT_MUTED), BorderLayout.CENTER);
        JProgressBar tienTrinh = new JProgressBar();
        tienTrinh.setIndeterminate(true);
        tienTrinh.setForeground(UIConstants.PRIMARY);
        noiDung.add(tienTrinh, BorderLayout.SOUTH);
        add(noiDung);
        setSize(420, 170);
        setLocationRelativeTo(null);
    }

    public static void hienThi() {
        ManHinhCho cho = new ManHinhCho();
        cho.setVisible(true);
        cho.kiemTraKetNoi();
    }

    private void kiemTraKetNoi() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection ketNoi = ConnectDB.getConnection()) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    dispose();
                    DangNhap.hienThi();
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void baoLoi(String chiTiet) {
        setVisible(false);
        Object[] luaChon = {"Thử lại", "Thoát"};
        int chon = JOptionPane.showOptionDialog(null,
                "Không kết nối được cơ sở dữ liệu GaraAUTORA.\n"
                        + "Hãy kiểm tra SQL Server đã chạy và đã chạy file database/GaraAUTORA_Database.sql.\n\n"
                        + "Chi tiết: " + chiTiet,
                "AUTORA - Lỗi kết nối", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, luaChon, luaChon[0]);
        if (chon == 0) {
            setVisible(true);
            kiemTraKetNoi();
        } else {
            dispose();
            System.exit(1);
        }
    }
}
