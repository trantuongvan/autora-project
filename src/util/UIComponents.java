package util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.function.Function;

/**
 * Bộ thành phần giao diện dùng chung (Card bo góc, Badge trạng thái, Button, Input, Table, Icon vẽ tay).
 * Tất cả màu sắc và font đều lấy từ {@link UIConstants}, không hard-code.
 */
public final class UIComponents {

    private UIComponents() {}

    // ==========================================================
    // 1. MÀU DẪN XUẤT TỪ UIConstants
    // ==========================================================

    /** Trộn màu với trắng (ratio 0..1) để tạo nền nhạt cho badge/hộp thông báo. */
    public static Color tint(Color c, float ratio) {
        return new Color(
                Math.round(c.getRed() + (255 - c.getRed()) * ratio),
                Math.round(c.getGreen() + (255 - c.getGreen()) * ratio),
                Math.round(c.getBlue() + (255 - c.getBlue()) * ratio));
    }

    /** Làm tối màu (ratio < 1) để tạo màu chữ đọc rõ trên nền nhạt. */
    public static Color shade(Color c, float ratio) {
        return new Color(Math.round(c.getRed() * ratio), Math.round(c.getGreen() * ratio), Math.round(c.getBlue() * ratio));
    }

    // ==========================================================
    // 2. PANEL / CARD
    // ==========================================================

    public static class RoundedPanel extends JPanel {
        private final int arc;
        private Color borderColor;

        public RoundedPanel(int arc, Color bg, Color borderColor) {
            this.arc = arc;
            this.borderColor = borderColor;
            setOpaque(false);
            setBackground(bg);
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Card trắng viền xám bo góc. */
    public static RoundedPanel card() {
        return new RoundedPanel(12, UIConstants.CARD_BG, UIConstants.BORDER_COLOR);
    }

    /** Hộp thông báo / ô tô màu không viền. */
    public static RoundedPanel box(Color bg) {
        return new RoundedPanel(8, bg, null);
    }

    /** JPanel trong suốt. */
    public static JPanel trong(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    /** Xếp các thành phần theo chiều dọc, mỗi thành phần chiếm hết chiều ngang, tự co giãn theo khung cuộn. */
    public static class CotDoc extends JPanel implements Scrollable {
        private int hang = 0;

        public CotDoc() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        public void them(Component c, int khoangTren) {
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = hang++;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.insets = new Insets(khoangTren, 0, 0, 0);
            add(c, gc);
        }

        /** Gọi sau thành phần cuối để đẩy mọi thứ lên trên. */
        public void ketThuc() {
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = hang++;
            gc.weighty = 1;
            gc.fill = GridBagConstraints.BOTH;
            add(Box.createGlue(), gc);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(r.height - 32, 16); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public static JScrollPane cuonTrang(JComponent view) {
        JScrollPane sp = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.APP_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static GridBagConstraints gbc(int x, int y, int w, double wx, int top, int right) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = w;
        gc.weightx = wx;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(top, 0, 0, right);
        return gc;
    }

    /** Tiêu đề trang: tên màn hình (màu theo phân hệ) + mô tả, nút hành động bên phải (có thể null). */
    public static JPanel tieuDeTrang(String tieuDe, JLabel lblMoTa, Color mau, Component phai) {
        JPanel p = trong(new BorderLayout(16, 0));
        JPanel trai = trong(null);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        trai.add(label(tieuDe, UIConstants.FONT_PAGE_TITLE.deriveFont(24f), mau));
        trai.add(Box.createVerticalStrut(4));
        lblMoTa.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        lblMoTa.setForeground(UIConstants.TEXT_MUTED);
        trai.add(lblMoTa);
        p.add(trai, BorderLayout.CENTER);
        if (phai != null) {
            JPanel e = trong(new GridBagLayout());
            e.add(phai);
            p.add(e, BorderLayout.EAST);
        }
        return p;
    }

    public static JPanel tieuDeTrang(String tieuDe, String moTa, Color mau, Component phai) {
        return tieuDeTrang(tieuDe, new JLabel(moTa), mau, phai);
    }

    /** Thanh tiêu đề của card (có đường kẻ phía dưới). */
    public static JPanel tieuDeCard(JLabel lblTieuDe, Component phai) {
        JPanel p = trong(new BorderLayout(12, 0));
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(14, 22, 14, 22)));
        lblTieuDe.setFont(UIConstants.FONT_SECTION_TITLE.deriveFont(18f));
        lblTieuDe.setForeground(UIConstants.TEXT_DARK);
        p.add(lblTieuDe, BorderLayout.CENTER);
        if (phai != null) p.add(phai, BorderLayout.EAST);
        return p;
    }

    // ==========================================================
    // 3. LABEL / BADGE / BUTTON / INPUT
    // ==========================================================

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** Nhãn form (đậm), thêm dấu * đỏ nếu bắt buộc. */
    public static JLabel formLabel(String text, boolean batBuoc) {
        String html = batBuoc
                ? String.format("<html>%s <font color='#%06X'>*</font></html>", text, UIConstants.STATUS_DANGER.getRGB() & 0xFFFFFF)
                : text;
        return label(html, UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK);
    }

    /** Nhãn báo lỗi dưới ô nhập (mặc định ẩn). */
    public static JLabel errorLabel() {
        JLabel l = label("", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.STATUS_DANGER);
        l.setVisible(false);
        return l;
    }

    /** msg = null thì ẩn lỗi. */
    public static void showError(JLabel l, String msg) {
        l.setText(msg == null ? "" : msg);
        l.setVisible(msg != null);
    }

    public static class Badge extends JLabel {
        private Color bg = UIConstants.APP_BG;

        public Badge() {
            setFont(UIConstants.FONT_CAPTION.deriveFont(Font.BOLD, 12f));
            setBorder(new EmptyBorder(4, 10, 4, 10));
            setOpaque(false);
        }

        public Badge(String text, Color base) {
            this();
            set(text, base);
        }

        public void set(String text, Color base) {
            this.bg = tint(base, 0.85f);
            setForeground(shade(base, 0.7f));
            setText("\u25CF " + text);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedButton extends JButton {
        private final Color base;
        private final boolean outline;

        public RoundedButton(String text, Color base, boolean outline) {
            super(text);
            this.base = base;
            this.outline = outline;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(UIConstants.FONT_FORM_LABEL.deriveFont(14f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color fill;
            Color fg;
            boolean hover = getModel().isRollover();
            if (!isEnabled()) {
                fill = UIConstants.BORDER_COLOR;
                fg = UIConstants.TEXT_MUTED;
            } else if (outline) {
                fill = hover ? UIConstants.APP_BG : UIConstants.CARD_BG;
                fg = UIConstants.TEXT_DARK;
            } else {
                fill = hover ? shade(base, 0.9f) : base;
                fg = Color.WHITE;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            if (outline && isEnabled()) {
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(fg);
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /** Nút hành động chính (nền màu). */
    public static JButton nutChinh(String text, Color mau) {
        return new RoundedButton(text, mau, false);
    }

    /** Nút phụ (Hủy, Đóng): nền trắng viền xám. */
    public static JButton nutPhu(String text) {
        return new RoundedButton(text, UIConstants.CARD_BG, true);
    }

    /** Nút chỉ có icon, không viền (đóng popup, làm mới...). */
    public static JButton nutIcon(Icon icon) {
        JButton b = new JButton(icon);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(4, 4, 4, 4));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static Border vienInput(boolean loi) {
        return new CompoundBorder(
                new LineBorder(loi ? UIConstants.STATUS_DANGER : UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10));
    }

    public static void styleInput(JTextField c) {
        c.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        c.setForeground(UIConstants.TEXT_DARK);
        c.setBackground(UIConstants.CARD_BG);
        c.setBorder(vienInput(false));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 40));
    }

    public static <T> void styleCombo(JComboBox<T> c) {
        c.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        c.setForeground(UIConstants.TEXT_DARK);
        c.setBackground(UIConstants.CARD_BG);
        c.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 40));
    }

    /** Bôi đỏ / bỏ bôi đỏ viền ô nhập liệu vi phạm ràng buộc. */
    public static void setError(JComponent c, boolean loi) {
        if (c instanceof JComboBox) {
            c.setBorder(new LineBorder(loi ? UIConstants.STATUS_DANGER : UIConstants.BORDER_COLOR, 1, true));
        } else {
            c.setBorder(vienInput(loi));
        }
    }

    public static class PlaceholderField extends JTextField {
        private final String hint;

        public PlaceholderField(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && hint != null && !hint.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Ô nhập có icon (ô tìm kiếm, ô khóa có biểu tượng ổ khóa). */
    public static class IconField extends RoundedPanel {
        private final JTextField txt;

        public IconField(String hint, Icon icon, boolean iconBenTrai) {
            super(8, UIConstants.CARD_BG, UIConstants.BORDER_COLOR);
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(0, 12, 0, 12));
            txt = new PlaceholderField(hint);
            txt.setBorder(null);
            txt.setOpaque(false);
            txt.setFont(UIConstants.FONT_BODY.deriveFont(14f));
            txt.setForeground(UIConstants.TEXT_DARK);
            add(txt, BorderLayout.CENTER);
            add(new JLabel(icon), iconBenTrai ? BorderLayout.WEST : BorderLayout.EAST);
            setPreferredSize(new Dimension(300, 40));
        }

        public JTextField getTextField() {
            return txt;
        }

        public void setEditable(boolean b) {
            txt.setEditable(b);
            setBackground(b ? UIConstants.CARD_BG : tint(UIConstants.APP_BG, 0.5f));
        }
    }

    // ==========================================================
    // 4. BẢNG DỮ LIỆU (JTable)
    // ==========================================================

    public static DefaultTableModel modelKhongSua(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(52);
        t.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        t.setForeground(UIConstants.TEXT_DARK);
        t.setBackground(UIConstants.CARD_BG);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(tint(UIConstants.BORDER_COLOR, 0.5f));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(tint(UIConstants.PRIMARY_LIGHT, 0.88f));
        t.setSelectionForeground(UIConstants.TEXT_DARK);
        t.setFillsViewportHeight(true);
        t.setBorder(null);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultRenderer(Object.class, new CellRenderer(UIConstants.FONT_BODY.deriveFont(14f), null));
        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new Dimension(0, 44));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new HeaderRenderer());
    }

    /** Bọc bảng vào JScrollPane không viền, nền trắng (đặt trong card). */
    public static JScrollPane cuonBang(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.CARD_BG);
        return sp;
    }

    public static void datDoRong(JTable t, int... w) {
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
    }

    /** Đặt chiều cao khung cuộn theo số dòng thực tế (tối thiểu..tối đa dòng). */
    public static void canChieuCaoBang(JTable t, int toiThieu, int toiDa) {
        int n = Math.max(toiThieu, Math.min(toiDa, t.getRowCount()));
        Container vp = t.getParent();
        if (vp != null && vp.getParent() instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) vp.getParent();
            sp.setPreferredSize(new Dimension(100, 44 + n * t.getRowHeight() + 2));
            sp.revalidate();
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setBackground(tint(UIConstants.APP_BG, 0.5f));
            setForeground(UIConstants.TEXT_MUTED);
            setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13f));
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR), new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    /** Ô văn bản thường; có thể đổi font và màu chữ theo nội dung. */
    public static class CellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Function<String, Color> mau;

        public CellRenderer(Font font, Function<String, Color> mau) {
            this.font = font;
            this.mau = mau;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            String s = v == null ? "" : v.toString();
            Color m = mau == null ? null : mau.apply(s);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(font);
            setForeground(m != null ? m : UIConstants.TEXT_DARK);
            setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return this;
        }
    }

    /** Ô hiển thị nhãn trạng thái (badge) với màu theo nội dung. */
    public static class BadgeRenderer implements TableCellRenderer {
        private final Function<String, Color> mau;
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final Badge badge = new Badge();

        public BadgeRenderer(Function<String, Color> mau) {
            this.mau = mau;
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            gc.insets = new Insets(0, 12, 0, 0);
            panel.add(badge, gc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String s = v == null ? "" : v.toString();
            Color m = mau.apply(s);
            badge.set(s, m != null ? m : UIConstants.TEXT_MUTED);
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    /** Cột thao tác gồm các biểu tượng (xem / sửa / xóa). Dùng {@link #viTriBieuTuong} để biết icon nào được bấm. */
    public static class ActionRenderer implements TableCellRenderer {
        public static final int LE_TRAI = 12;
        public static final int O_ICON = 32;
        private final JPanel panel = new JPanel(new GridBagLayout());

        public ActionRenderer(Icon... icons) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            row.setOpaque(false);
            for (Icon ic : icons) {
                JLabel l = new JLabel(ic);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setPreferredSize(new Dimension(O_ICON, 18));
                row.add(l);
            }
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.weightx = 1;
            gc.insets = new Insets(0, LE_TRAI, 0, 0);
            panel.add(row, gc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            panel.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return panel;
        }
    }

    /** Trả về chỉ số icon (0, 1, 2...) được click trong cột thao tác, hoặc -1 nếu click ngoài icon. */
    public static int viTriBieuTuong(JTable t, MouseEvent e, int cot, int soIcon) {
        Point p = e.getPoint();
        int row = t.rowAtPoint(p);
        int col = t.columnAtPoint(p);
        if (row < 0 || col != cot) return -1;
        Rectangle r = t.getCellRect(row, col, false);
        int x = p.x - r.x - ActionRenderer.LE_TRAI;
        if (x < 0) return -1;
        int idx = x / ActionRenderer.O_ICON;
        if (idx >= soIcon || x % ActionRenderer.O_ICON > 20) return -1;
        return idx;
    }

    // ==========================================================
    // 5. ICON VẼ BẰNG JAVA2D (không cần file ảnh)
    // ==========================================================

    public enum Loai { EYE, PENCIL, TRASH, SEARCH, LOCK, CLOSE, CHECK, ALERT, CLIPBOARD, WRENCH, KEY, REFRESH }

    public static class LineIcon implements Icon {
        private final Loai loai;
        private final Color color;
        private final int size;

        public LineIcon(Loai loai, Color color) {
            this(loai, color, 18);
        }

        public LineIcon(Loai loai, Color color, int size) {
            this.loai = loai;
            this.color = color;
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.scale(size / 18.0, size / 18.0);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (loai) {
                case EYE:
                    g2.draw(new QuadCurve2D.Float(1, 9, 9, 1, 17, 9));
                    g2.draw(new QuadCurve2D.Float(1, 9, 9, 17, 17, 9));
                    g2.draw(new Ellipse2D.Float(6.5f, 6.5f, 5, 5));
                    break;
                case PENCIL:
                    Path2D p = new Path2D.Float();
                    p.moveTo(3, 15);
                    p.lineTo(4, 11);
                    p.lineTo(12, 3);
                    p.lineTo(15, 6);
                    p.lineTo(7, 14);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Float(10, 5, 13, 8));
                    break;
                case TRASH:
                    g2.draw(new RoundRectangle2D.Float(4, 6, 10, 10, 2, 2));
                    g2.draw(new Line2D.Float(2, 5, 16, 5));
                    g2.draw(new Line2D.Float(7, 2.5f, 11, 2.5f));
                    g2.draw(new Line2D.Float(7.5f, 8.5f, 7.5f, 13.5f));
                    g2.draw(new Line2D.Float(10.5f, 8.5f, 10.5f, 13.5f));
                    break;
                case SEARCH:
                    g2.draw(new Ellipse2D.Float(2, 2, 10, 10));
                    g2.draw(new Line2D.Float(10, 10, 16, 16));
                    break;
                case LOCK:
                    g2.draw(new RoundRectangle2D.Float(3, 8, 12, 8, 2, 2));
                    g2.draw(new Arc2D.Float(5.5f, 2, 7, 9, 0, 180, Arc2D.OPEN));
                    g2.fill(new Ellipse2D.Float(8.2f, 11, 1.6f, 1.6f));
                    break;
                case CLOSE:
                    g2.draw(new Line2D.Float(4, 4, 14, 14));
                    g2.draw(new Line2D.Float(14, 4, 4, 14));
                    break;
                case CHECK:
                    g2.draw(new Ellipse2D.Float(1.5f, 1.5f, 15, 15));
                    Path2D t = new Path2D.Float();
                    t.moveTo(5.5, 9.5);
                    t.lineTo(8, 12);
                    t.lineTo(12.5, 6);
                    g2.draw(t);
                    break;
                case ALERT:
                    g2.draw(new Ellipse2D.Float(1.5f, 1.5f, 15, 15));
                    g2.draw(new Line2D.Float(9, 5.5f, 9, 10));
                    g2.fill(new Ellipse2D.Float(8.2f, 12, 1.6f, 1.6f));
                    break;
                case CLIPBOARD:
                    g2.draw(new RoundRectangle2D.Float(3.5f, 3.5f, 11, 12.5f, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(6.5f, 2, 5, 3, 1, 1));
                    g2.draw(new Line2D.Float(6, 9, 12, 9));
                    g2.draw(new Line2D.Float(6, 12, 12, 12));
                    break;
                case WRENCH:
                    g2.draw(new Line2D.Float(3.5f, 14.5f, 10.5f, 7.5f));
                    g2.draw(new Ellipse2D.Float(10, 2, 5.5f, 5.5f));
                    break;
                case KEY:
                    g2.draw(new Ellipse2D.Float(2, 9, 6, 6));
                    g2.draw(new Line2D.Float(7.5f, 10, 15, 3));
                    g2.draw(new Line2D.Float(12.5f, 5.5f, 14.5f, 7.5f));
                    break;
                case REFRESH:
                    g2.draw(new Arc2D.Float(3, 3, 12, 12, 40, 280, Arc2D.OPEN));
                    g2.draw(new Line2D.Float(13.6f, 12.9f, 16f, 10f));
                    g2.draw(new Line2D.Float(13.6f, 12.9f, 10.5f, 14f));
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }
}
