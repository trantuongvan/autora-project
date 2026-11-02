package util;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Xuất bảng ra file Excel .xlsx. Định dạng này là XML nén nên chỉ cần thư viện chuẩn của JDK. */
public final class XuatFile {
    private XuatFile() {
    }

    /** Hỏi nơi lưu rồi ghi file; hủy chọn thì không làm gì. */
    public static void xuatExcel(Component cha, String tenGoiY, String[] cot, List<Object[]> dong) {
        JFileChooser chon = new JFileChooser();
        chon.setSelectedFile(new File(tenGoiY + ".xlsx"));
        chon.setFileFilter(new FileNameExtensionFilter("Excel (.xlsx)", "xlsx"));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File tep = chon.getSelectedFile();
        if (!tep.getName().toLowerCase().endsWith(".xlsx")) {
            tep = new File(tep.getParentFile(), tep.getName() + ".xlsx");
        }
        try {
            ghiXlsx(tep, cot, dong);
            JOptionPane.showMessageDialog(cha, "Đã xuất file " + tep.getName() + ".");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(cha, "Không thể ghi file.\n" + ex.getMessage(), "Lỗi xuất file",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void ghiXlsx(File tep, String[] cot, List<Object[]> dong) throws IOException {
        StringBuilder sheet = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        themDong(sheet, cot);
        for (Object[] d : dong) {
            themDong(sheet, d);
        }
        sheet.append("</sheetData></worksheet>");

        try (OutputStream ra = Files.newOutputStream(tep.toPath()); ZipOutputStream zip = new ZipOutputStream(ra)) {
            ghi(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "</Types>");
            ghi(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            ghi(zip, "xl/workbook.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""
                    + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"AUTORA\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            ghi(zip, "xl/_rels/workbook.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "</Relationships>");
            ghi(zip, "xl/worksheets/sheet1.xml", sheet.toString());
        }
    }

    private static void themDong(StringBuilder sheet, Object[] o) {
        sheet.append("<row>");
        for (Object giaTri : o) {
            if (giaTri instanceof Number) {
                sheet.append("<c><v>").append(giaTri).append("</v></c>");
            } else {
                // Luôn ghi dạng chuỗi nên dữ liệu người dùng nhập như "=SUM(A1)" không thành công thức.
                sheet.append("<c t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(thoatXml(giaTri == null ? "" : String.valueOf(giaTri)))
                        .append("</t></is></c>");
            }
        }
        sheet.append("</row>");
    }

    private static void ghi(ZipOutputStream zip, String ten, String noiDung) throws IOException {
        zip.putNextEntry(new ZipEntry(ten));
        zip.write(noiDung.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String thoatXml(String s) {
        return s.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
