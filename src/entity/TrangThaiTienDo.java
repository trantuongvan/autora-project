package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum TrangThaiTienDo {
    CHO_SUA("Chờ sửa"),
    DANG_SUA("Đang sửa"),
    HOAN_THANH("Hoàn thành");

    private final String text;

    TrangThaiTienDo(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TrangThaiTienDo fromDb(String text) {
        for (TrangThaiTienDo giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("TrangThaiTienDo không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
