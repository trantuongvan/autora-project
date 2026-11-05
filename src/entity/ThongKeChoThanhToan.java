package entity;

/**
 * Số việc chờ thanh toán gồm hóa đơn công nợ và phiếu đã hoàn thành chưa lập hóa đơn.
 * Tổng tiền chỉ tính công nợ vì phiếu chưa lập hóa đơn chưa có khuyến mãi và thuế nên chưa có số cuối cùng.
 */
public class ThongKeChoThanhToan {
    private final int soLuong;
    private final double tongTien;

    public ThongKeChoThanhToan(int soLuong, double tongTien) {
        this.soLuong = soLuong;
        this.tongTien = tongTien;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public double getTongTien() {
        return tongTien;
    }
}
