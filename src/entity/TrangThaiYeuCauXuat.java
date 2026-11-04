package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum TrangThaiYeuCauXuat {
    CHO_XUAT("Chờ xuất"),
    DA_XUAT("Đã xuất"),
    TU_CHOI("Từ chối");

    private final String text;

    TrangThaiYeuCauXuat(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TrangThaiYeuCauXuat fromDb(String text) {
        for (TrangThaiYeuCauXuat giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("TrangThaiYeuCauXuat không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
