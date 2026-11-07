package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum TrangThaiHoaDon {
    DA_LAP("Đã lập"),
    DA_THANH_TOAN("Đã thanh toán"),
    CONG_NO("Công nợ"),
    DA_HUY("Đã hủy");

    private final String text;

    TrangThaiHoaDon(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TrangThaiHoaDon fromDb(String text) {
        for (TrangThaiHoaDon giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("TrangThaiHoaDon không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
