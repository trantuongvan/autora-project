package util;

import org.jdatepicker.ComponentTextDefaults;
import org.jdatepicker.ComponentTextDefaults.Key;
import org.jdatepicker.JDatePanel;
import org.jdatepicker.LocalDateModel;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/** Biến ô ngày thành ô chỉ chọn bằng lịch; Controller vẫn đọc và ghi chuỗi như ô nhập thường. */
public final class ChonNgay {
    public static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static {
        ComponentTextDefaults chu = ComponentTextDefaults.getInstance();
        chu.setText(Key.TODAY, "Hôm nay");
        chu.setText(Key.CLEAR, "Xóa");
        chu.setText(Key.MONTH, "Tháng");
        chu.setText(Key.YEAR, "Năm");
        Key[] thang = {Key.JANUARY, Key.FEBRUARY, Key.MARCH, Key.APRIL, Key.MAY, Key.JUNE,
                Key.JULY, Key.AUGUST, Key.SEPTEMBER, Key.OCTOBER, Key.NOVEMBER, Key.DECEMBER};
        for (int i = 0; i < thang.length; i++) {
            chu.setText(thang[i], "Th " + (i + 1));
        }
        Key[] thu = {Key.SUN, Key.MON, Key.TUE, Key.WED, Key.THU, Key.FRI, Key.SAT};
        String[] tenThu = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (int i = 0; i < thu.length; i++) {
            chu.setText(thu[i], tenThu[i]);
        }
    }

    private ChonNgay() {
    }

    public static void ganNgay(JTextField... oNgay) {
        for (JTextField o : oNgay) {
            gan(o, false);
        }
    }

    public static void ganNgayGio(JTextField o) {
        gan(o, true);
    }

    private static void gan(JTextField o, boolean coGio) {
        o.setEditable(false);
        o.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        o.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mo(o, coGio);
            }
        });
        o.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int phim = e.getKeyCode();
                if (phim == KeyEvent.VK_ENTER || phim == KeyEvent.VK_SPACE || phim == KeyEvent.VK_DOWN) {
                    mo(o, coGio);
                }
            }
        });
    }

    private static void mo(JTextField o, boolean coGio) {
        if (!o.isEnabled()) {
            return;
        }
        LocalDateTime dangChon = doc(o.getText(), coGio);
        LocalDateModel model = new LocalDateModel(dangChon.toLocalDate(), true);
        JDatePanel lich = new JDatePanel(model);
        lich.setShowYearButtons(true);
        JPopupMenu popup = new JPopupMenu();
        JPanel noiDung = new JPanel(new BorderLayout(0, 6));
        noiDung.add(lich, BorderLayout.CENTER);

        JSpinner gio = new JSpinner(new SpinnerDateModel());
        gio.setEditor(new JSpinner.DateEditor(gio, "HH:mm"));
        gio.setValue(Date.from(dangChon.atZone(ZoneId.systemDefault()).toInstant()));
        if (coGio) {
            noiDung.add(gio, BorderLayout.SOUTH);
        }
        Runnable ghi = () -> {
            LocalDate ngay = model.isSelected() ? model.getValue() : null;
            if (ngay == null) {
                o.setText("");
            } else if (coGio) {
                LocalTime thoiGian = ((Date) gio.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                o.setText(ngay.atTime(thoiGian).format(NGAY_GIO));
            } else {
                o.setText(ngay.format(NGAY));
            }
            o.postActionEvent();
        };
        lich.addActionListener(e -> {
            ghi.run();
            // Ô có giờ để mở lịch cho người dùng chọn tiếp giờ.
            if (!coGio || !model.isSelected()) {
                popup.setVisible(false);
            }
        });
        gio.addChangeListener(e -> {
            if (model.isSelected()) {
                ghi.run();
            }
        });
        popup.add(noiDung);
        popup.show(o, 0, o.getHeight());
    }

    private static LocalDateTime doc(String chuoi, boolean coGio) {
        String s = chuoi == null ? "" : chuoi.trim();
        try {
            return coGio ? LocalDateTime.parse(s, NGAY_GIO) : LocalDate.parse(s, NGAY).atStartOfDay();
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }
}
