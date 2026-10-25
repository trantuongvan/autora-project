package util;

import java.awt.Color;
import java.awt.Font;

/**
 * QUY CHUẨN THIẾT KẾ GIAO DIỆN HỆ THỐNG AUTORA (UI DESIGN STANDARDS)
 * Định nghĩa bảng màu, font chữ và hằng số phong cách dùng chung cho toàn bộ dự án.
 */
public class UIConstants {

    // ==========================================
    // 1. BẢNG MÀU HỆ THỐNG (COLOR PALETTE)
    // ==========================================

    // Màu chủ đạo (Primary) - Dùng cho Header, Topbar, nút hành động chính (Lưu, Tiếp nhận, Thanh toán)
    public static final Color PRIMARY = Color.decode("#1565C0");
    public static final Color PRIMARY_LIGHT = Color.decode("#3498DB");

    // Phân hệ Chủ cửa hàng - Nền nhãn/Card Quản lý Nhân viên, Khuyến mãi, Tài khoản, Báo cáo
    public static final Color ROLE_MANAGER = Color.decode("#27AE60");
    public static final Color ROLE_MANAGER_LIGHT = Color.decode("#76CA65");

    // Phân hệ Lễ tân - Nền nhãn/Card Khách hàng, Hồ sơ xe, Lập phiếu sửa chữa, Hóa đơn
    public static final Color ROLE_RECEPTION = Color.decode("#C0392B");
    public static final Color ROLE_RECEPTION_LIGHT = Color.decode("#E74C3C");

    // Phân hệ Kỹ thuật viên & Thủ kho - Tiến độ sửa chữa, Xin phụ tùng, Nhập/Xuất kho
    public static final Color ROLE_TECH_STOCK = Color.decode("#D35400");
    public static final Color ROLE_TECH_STOCK_LIGHT = Color.decode("#F39C12");

    // Trạng thái xử lý dữ liệu
    public static final Color STATUS_SUCCESS = Color.decode("#00B050"); // 'Đã hoàn thành', 'Đã thanh toán', 'Đã xuất kho'
    public static final Color STATUS_WARNING = Color.decode("#F1C40F"); // 'Chờ sửa', 'Chờ phụ tùng', cảnh báo tồn kho
    public static final Color STATUS_DANGER = Color.decode("#FF5E5E");  // Báo lỗi, vi phạm ràng buộc dữ liệu

    // Màu trung tính (Neutral)
    public static final Color APP_BG = Color.decode("#ECEFF1");        // Nền ứng dụng
    public static final Color CARD_BG = Color.decode("#FFFFFF");       // Nền bảng, nền Card form
    public static final Color TEXT_DARK = Color.decode("#263238");     // Màu chữ tiêu đề, nội dung chính
    public static final Color TEXT_MUTED = Color.decode("#78909C");    // Màu chữ phụ, chú thích
    public static final Color BORDER_COLOR = Color.decode("#CFD8DC");  // Màu đường viền

    // ==========================================
    // 2. FONT CHỮ TIÊU CHUẨN (TYPOGRAPHY)
    // ==========================================
    public static final String FONT_FAMILY = "Roboto";
    public static final String FONT_FALLBACK = "Segoe UI";

    // Tiêu đề màn hình chính (Page Title): 20pt - 24pt Bold
    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);

    // Tiêu đề nhóm / Khung nhập liệu (Section Title): 16pt - 18pt Bold
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 16);

    // Tiêu đề bảng / Nhãn Form nhập liệu (Form Label): 13pt - 14pt Semi-bold / Bold
    public static final Font FONT_FORM_LABEL = new Font(FONT_FAMILY, Font.BOLD, 13);

    // Nội dung văn bản / Ô nhập liệu (Body / Input): 12pt - 13pt Regular
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);

    // Nhãn trạng thái / Ghi chú phụ (Badge / Caption): 10pt - 11pt Medium
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 11);
}
