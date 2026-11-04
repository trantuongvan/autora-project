package util;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích định dạng tiền tệ và ngày tháng dùng chung cho hệ thống AUTORA.
 */
public class FormatUtil {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###.##");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtil() {
        // Prevent instantiation
    }

    /**
     * Định dạng số tiền thành chuỗi tiền tệ VNĐ (Ví dụ: 1,500,000đ).
     */
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount) + "đ";
    }

    /**
     * Định dạng số tiền thành chuỗi tiền tệ VNĐ, cho phép tùy chọn hiển thị ký hiệu 'đ'.
     */
    public static String formatCurrency(double amount, boolean showSymbol) {
        String formatted = CURRENCY_FORMAT.format(amount);
        return showSymbol ? formatted + "đ" : formatted;
    }

    /**
     * Đọc số tiền nguyên (ví dụ 150000, 150.000, 1.500.000đ); dấu chấm chỉ là ngăn cách hàng nghìn.
     */
    public static double parseCurrency(String chuoi) {
        String s = chuoi == null ? "" : chuoi.trim().replaceFirst("đ$", "").trim();
        if (!s.matches("\\d{1,15}|\\d{1,3}(\\.\\d{3}){1,4}")) {
            throw new NumberFormatException("Số tiền không hợp lệ: " + chuoi);
        }
        return Double.parseDouble(s.replace(".", ""));
    }

    /**
     * Định dạng LocalDateTime thành chuỗi theo chuẩn "dd/MM/yyyy HH:mm".
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMAT);
    }

    /**
     * Định dạng LocalDateTime thành chuỗi ngày theo chuẩn "dd/MM/yyyy".
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMAT);
    }
}