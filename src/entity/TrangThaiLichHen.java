package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum TrangThaiLichHen {
    DANG_HEN("Đang hẹn"),
    DA_DEN("Đã đến"),
    DA_HUY("Đã hủy");

    private final String text;

    TrangThaiLichHen(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TrangThaiLichHen fromDb(String text) {
        for (TrangThaiLichHen giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("TrangThaiLichHen không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
