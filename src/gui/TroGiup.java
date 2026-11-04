package gui;

import util.UIComponents.CotDoc;
import util.UIComponents.RoundedPanel;
import util.UIComponents;
import util.UIComponents.Loai;
import util.UIConstants;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;

/** Trang trợ giúp sử dụng (phím F1); nội dung cố định nên không cần Controller. */
public class TroGiup extends JPanel {

    public TroGiup() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(the("Quy trình sửa xe", Loai.WRENCH, UIConstants.PRIMARY,
                "1. Lễ tân tiếp nhận xe và lập phiếu tại Tiếp nhận xe.",
                "2. Kỹ thuật viên cập nhật tiến độ và yêu cầu phụ tùng tại Tiến độ sửa chữa.",
                "3. Thủ kho duyệt xuất kho. Tồn kho chỉ bị trừ ở bước này.",
                "4. Khi phiếu Hoàn thành, lễ tân lập hóa đơn và thu tiền tại Hóa đơn & Thanh toán.",
                "5. Lễ tân bàn giao xe cho khách tại Bàn giao xe."), 0);
        noiDung.them(the("Thao tác trên danh sách", Loai.CLIPBOARD, UIConstants.PRIMARY,
                "Danh sách chỉ hiện dữ liệu khi bấm Tải dữ liệu, kể cả sau khi đổi ô tìm kiếm hoặc bộ lọc (nhấn Enter ở ô tìm kiếm cũng tải). Sau khi lưu hoặc xóa, danh sách đã tải sẽ tự cập nhật.",
                "Khách hàng, Hồ sơ xe, Nhân viên: bấm vào dòng để hiện dữ liệu lên biểu mẫu, sửa rồi bấm lưu.",
                "Hóa đơn, Phiếu sửa chữa: bấm đúp vào dòng để mở chi tiết.",
                "Biểu tượng thùng rác dùng để xóa. Xe đã có phiếu sửa chữa không thể xóa."), 16);
        noiDung.them(the("Chọn ngày", Loai.CALENDAR, UIConstants.PRIMARY,
                "Bấm vào ô ngày (hoặc nhấn Enter khi đang ở ô đó) để mở lịch và chọn ngày.",
                "Bấm biểu tượng xóa trên lịch để bỏ ngày đã chọn. Ô ngày không cho gõ tay."), 16);
        noiDung.them(the("Nhập kho từ Excel", Loai.DOWNLOAD, UIConstants.PRIMARY,
                "Tại Nhập kho, bấm Tạo phiếu mới rồi bấm Nhập từ Excel và chọn file .xlsx.",
                "Dòng 1 là tiêu đề. Cột A: mã SKU, cột B: số lượng, cột C: đơn giá.",
                "Dòng hợp lệ được đưa lên phiếu để kiểm tra trước khi lưu; mã đã có trên phiếu sẽ lấy số lượng và đơn giá theo file. Dòng sai được liệt kê và không được thêm."), 16);
        noiDung.them(the("Phím tắt", Loai.KEY, UIConstants.PRIMARY,
                "F1: mở trang Trợ giúp.",
                "F5: tải dữ liệu (giống nút Tải dữ liệu), Ctrl+F: tới ô tìm kiếm (trên các màn hình có ghi chú ở tiêu đề).",
                "Ctrl+1 đến Ctrl+9: mở mục menu theo thứ tự.",
                "Delete: xóa hồ sơ xe hoặc hủy phiếu đang chọn."), 16);
        noiDung.them(the("Tự động phân công", Loai.WRENCH, UIConstants.PRIMARY,
                "Khi lập phiếu, hệ thống tự chọn kỹ thuật viên có kỹ năng và xong sớm nhất cho từng hạng mục; lễ tân chỉ chọn tay khi có ngoại lệ tại Chi tiết phiếu.",
                "Chủ cửa hàng đặt ca làm việc và kỹ năng (dịch vụ làm được, số phút) của kỹ thuật viên tại Quản lý nhân viên."), 16);
        noiDung.them(the("Lịch hẹn", Loai.CALENDAR, UIConstants.PRIMARY,
                "Tại Khách hàng, chọn một khách rồi bấm Thêm lịch hẹn để hẹn ngày đến; chọn một lịch và bấm Hủy lịch hẹn để hủy. Lập phiếu cho xe sẽ tự chuyển lịch hẹn tới hạn sang Đã đến.",
                "Trang chủ Lễ tân liệt kê lịch hẹn hôm nay: bấm đúp vào dòng để mở Tiếp nhận xe với biển số hoặc SĐT đã điền sẵn."), 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        return new TroGiup();
    }

    private static RoundedPanel the(String tieuDe, Loai loai, Color mau, String... dong) {
        JTextArea chu = new JTextArea(String.join("\n", dong));
        chu.setEditable(false);
        chu.setOpaque(false);
        chu.setLineWrap(true);
        chu.setWrapStyleWord(true);
        chu.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        chu.setForeground(UIConstants.TEXT_DARK);
        chu.setBorder(new EmptyBorder(0, 20, 20, 20));
        RoundedPanel the = UIComponents.theMuc(tieuDe, loai, mau);
        the.add(chu, BorderLayout.CENTER);
        return the;
    }
}
