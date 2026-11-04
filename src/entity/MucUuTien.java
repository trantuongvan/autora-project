package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum MucUuTien {
    THUONG("Thường"),
    KHAN("Khẩn");

    private final String text;

    MucUuTien(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static MucUuTien fromDb(String text) {
        for (MucUuTien giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("MucUuTien không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
