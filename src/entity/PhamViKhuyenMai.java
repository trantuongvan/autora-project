package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum PhamViKhuyenMai {
    TONG_HOA_DON("Tổng hóa đơn"),
    NHOM_DICH_VU("Nhóm dịch vụ"),
    PHU_TUNG("Phụ tùng");

    private final String text;

    PhamViKhuyenMai(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }


    public static PhamViKhuyenMai fromDb(String text) {
        for (PhamViKhuyenMai giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("PhamViKhuyenMai không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
