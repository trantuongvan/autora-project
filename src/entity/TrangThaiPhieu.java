package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum TrangThaiPhieu {
    CHO_KIEM_TRA("Chờ kiểm tra"),
    DANG_SUA("Đang sửa"),
    CHO_PHU_TUNG("Chờ phụ tùng"),
    HOAN_THANH("Hoàn thành"),
    DA_HUY("Đã hủy");

    private final String text;

    TrangThaiPhieu(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TrangThaiPhieu fromDb(String text) {
        for (TrangThaiPhieu giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("TrangThaiPhieu không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
