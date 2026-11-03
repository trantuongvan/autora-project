package controller;

import dao.DangNhap_Dao;
import entity.PhienDangNhap;
import gui.DangNhap;
import gui.GiaoDienChinh;

import javax.swing.*;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

/** Xác thực tài khoản, nhớ tên đăng nhập, đặt lại mật khẩu và mở màn hình chính theo vai trò. */
public class DangNhapController {
    static final int DO_DAI_MAT_KHAU_TOI_THIEU = 6;
    private static final String KHOA_TEN_DA_NHO = "tenDangNhap";

    private final DangNhap view;
    private final DangNhap_Dao dao = new DangNhap_Dao();
    private final Preferences caiDat = Preferences.userNodeForPackage(DangNhapController.class);

    public DangNhapController(DangNhap view) {
        this.view = view;
        initController();
    }

    private void initController() {
        view.getBtnDangNhap().addActionListener(e -> dangNhap());
        view.getBtnQuenMatKhau().addActionListener(e -> datLaiMatKhau());
        String tenDaNho = caiDat.get(KHOA_TEN_DA_NHO, "");
        if (!tenDaNho.isEmpty()) {
            view.getTxtTenDangNhap().setText(tenDaNho);
            view.getChkNhoDangNhap().setSelected(true);
        }
    }

    private void dangNhap() {
        String tenDangNhap = view.getTxtTenDangNhap().getText().trim();
        char[] kyTuMatKhau = view.getPwdMatKhau().getPassword();
        String matKhau = new String(kyTuMatKhau);
        Arrays.fill(kyTuMatKhau, '\0');
        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            view.hienLoi("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        view.hienLoi(null);
        view.getBtnDangNhap().setEnabled(false);
        new SwingWorker<PhienDangNhap, Void>() {
            @Override
            protected PhienDangNhap doInBackground() throws Exception {
                return dao.xacThuc(tenDangNhap, matKhau);
            }

            @Override
            protected void done() {
                view.getBtnDangNhap().setEnabled(true);
                try {
                    xuLyKetQua(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    view.hienLoi("Không kết nối được CSDL. Vui lòng kiểm tra SQL Server.");
                }
            }
        }.execute();
    }

    private void xuLyKetQua(PhienDangNhap phien) {
        if (phien == null) {
            // Không nói rõ sai tên hay sai mật khẩu để tránh dò tài khoản.
            view.hienLoi("Sai thông tin đăng nhập hoặc tài khoản đã bị khóa.");
            view.getPwdMatKhau().setText("");
            view.getPwdMatKhau().requestFocusInWindow();
            return;
        }
        // Chỉ nhớ tên đăng nhập, không bao giờ lưu mật khẩu xuống máy.
        if (view.getChkNhoDangNhap().isSelected()) {
            caiDat.put(KHOA_TEN_DA_NHO, phien.getTenDangNhap());
        } else {
            caiDat.remove(KHOA_TEN_DA_NHO);
        }
        PhienDangNhap.batDau(phien);
        view.dispose();
        GiaoDienChinh.hienThi();
    }

    /** Nhân viên không có email nên chủ cửa hàng là người xác nhận việc đặt lại mật khẩu. */
    private void datLaiMatKhau() {
        JTextField txtTaiKhoan = new JTextField(view.getTxtTenDangNhap().getText().trim());
        JPasswordField pwdMoi = new JPasswordField();
        JPasswordField pwdNhapLai = new JPasswordField();
        JTextField txtChu = new JTextField();
        JPasswordField pwdChu = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(new JLabel("Tài khoản cần đặt lại mật khẩu"));
        form.add(txtTaiKhoan);
        form.add(new JLabel("Mật khẩu mới (tối thiểu " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự)"));
        form.add(pwdMoi);
        form.add(new JLabel("Nhập lại mật khẩu mới"));
        form.add(pwdNhapLai);
        form.add(new JLabel("Tài khoản chủ cửa hàng xác nhận"));
        form.add(txtChu);
        form.add(new JLabel("Mật khẩu chủ cửa hàng"));
        form.add(pwdChu);

        int chon = JOptionPane.showConfirmDialog(view, form, "Đặt lại mật khẩu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (chon != JOptionPane.OK_OPTION) {
            return;
        }
        String taiKhoan = txtTaiKhoan.getText().trim();
        String matKhauMoi = new String(pwdMoi.getPassword());
        String loi = kiemTraMatKhauMoi(taiKhoan, matKhauMoi, new String(pwdNhapLai.getPassword()));
        if (loi != null) {
            JOptionPane.showMessageDialog(view, loi, "Đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            PhienDangNhap chu = dao.xacThuc(txtChu.getText().trim(), new String(pwdChu.getPassword()));
            if (chu == null || !PhienDangNhap.VAI_TRO_CHU_CUA_HANG.equals(chu.getVaiTro())) {
                JOptionPane.showMessageDialog(view, "Không xác nhận được tài khoản chủ cửa hàng.",
                        "Đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            } else if (!dao.datLaiMatKhau(taiKhoan, matKhauMoi, chu.getTenDangNhap())) {
                JOptionPane.showMessageDialog(view, "Không tìm thấy tài khoản " + taiKhoan + ".",
                        "Đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "Đã đặt lại mật khẩu cho " + taiKhoan + ".");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không kết nối được CSDL.\n" + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Trả về thông báo lỗi, hoặc null khi hợp lệ. */
    public static String kiemTraMatKhauMoi(String taiKhoan, String matKhauMoi, String nhapLai) {
        if (taiKhoan.isEmpty()) {
            return "Vui lòng nhập tài khoản cần đặt lại mật khẩu.";
        }
        if (matKhauMoi.length() < DO_DAI_MAT_KHAU_TOI_THIEU) {
            return "Mật khẩu mới phải có tối thiểu " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự.";
        }
        if (matKhauMoi.matches(".*\\s.*")) {
            return "Mật khẩu mới không được chứa khoảng trắng.";
        }
        return matKhauMoi.equals(nhapLai) ? null : "Hai mật khẩu mới không khớp.";
    }
}
