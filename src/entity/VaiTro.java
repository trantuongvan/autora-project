package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum VaiTro {
    CHU_CUA_HANG("Chủ cửa hàng"),
    LE_TAN("Lễ tân"),
    KY_THUAT_VIEN("Kỹ thuật viên"),
    THU_KHO("Thủ kho");

    private final String text;

    VaiTro(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static VaiTro fromDb(String text) {
        for (VaiTro giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("VaiTro không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
