package gui;

import util.UIComponents.CotDoc;
import util.UIComponents.RoundedPanel;
import util.UIComponents;
import util.UIComponents.Loai;
import util.UIConstants;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/** Trang giới thiệu hệ thống và nhóm thực hiện; nội dung cố định nên không cần Controller. */
public class GioiThieu extends JPanel {

    public GioiThieu() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc thongTin = new CotDoc();
        thongTin.setBorder(new EmptyBorder(0, 20, 20, 20));
        thongTin.them(UIComponents.logoAutora(), 0);
        thongTin.them(dong("Hệ thống quản lý gara ô tô AUTORA: tiếp nhận xe, sửa chữa, kho phụ tùng, hóa đơn và bàn giao xe."), 12);
        thongTin.them(dong("Báo cáo môn Phát triển ứng dụng (Application Development)."), 6);
        thongTin.them(dong("Khoa Công nghệ Thông tin, Trường Đại học Công nghiệp TP.HCM."), 6);
        thongTin.them(dong("Công nghệ: Java Swing, SQL Server."), 6);
        thongTin.ketThuc();
        RoundedPanel theHeThong = UIComponents.theMuc("Giới thiệu", Loai.INFO, UIConstants.PRIMARY);
        theHeThong.add(thongTin, BorderLayout.CENTER);

        DefaultTableModel model = UIComponents.modelKhongSua("STT", "Họ tên", "MSSV");
        model.addRow(new Object[] {1, "Trần Thị Tường Vân", "24642841"});
        model.addRow(new Object[] {2, "Bùi Thái Vy", "24636361"});
        model.addRow(new Object[] {3, "Nguyễn Xuân Việt Anh", "24725141"});
        model.addRow(new Object[] {4, "Nguyễn Tuấn Vũ", "24642961"});
        JTable bang = new JTable(model);
        UIComponents.styleTable(bang);
        UIComponents.datDoRong(bang, 70, 360, 160);
        UIComponents.canChieuCaoBang(bang, 4, 4);
        RoundedPanel theNhom = UIComponents.theMuc("Nhóm 13", Loai.USERS, UIConstants.PRIMARY);
        theNhom.add(UIComponents.cuonBang(bang), BorderLayout.CENTER);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(theHeThong, 0);
        noiDung.them(theNhom, 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        return new GioiThieu();
    }

    private static javax.swing.JLabel dong(String chu) {
        return UIComponents.label(chu, UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_DARK);
    }
}
