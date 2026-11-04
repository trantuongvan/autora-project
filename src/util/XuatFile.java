package util;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Đọc và xuất file Excel .xlsx. Định dạng này là XML nén nên chỉ cần thư viện chuẩn của JDK. */
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

    /** Đọc các cột đầu của trang tính đầu tiên. Phần tử i là dòng i + 1 của Excel, ô trống là chuỗi rỗng. */
    public static List<String[]> docXlsx(File tep, int soCot) throws IOException {
        try (ZipFile zip = new ZipFile(tep)) {
            List<String> chuoiChung = new ArrayList<>();
            if (zip.getEntry("xl/sharedStrings.xml") != null) {
                NodeList si = docXml(zip, "xl/sharedStrings.xml").getElementsByTagNameNS("*", "si");
                for (int i = 0; i < si.getLength(); i++) {
                    chuoiChung.add(vanBan((Element) si.item(i)));
                }
            }
            List<String[]> ketQua = new ArrayList<>();
            NodeList dong = docXml(zip, duongDanTrangDau(zip)).getElementsByTagNameNS("*", "row");
            for (int i = 0; i < dong.getLength(); i++) {
                Element row = (Element) dong.item(i);
                int soDong = row.hasAttribute("r") ? Integer.parseInt(row.getAttribute("r")) : ketQua.size() + 1;
                while (ketQua.size() < soDong) {
                    String[] trong = new String[soCot];
                    Arrays.fill(trong, "");
                    ketQua.add(trong);
                }
                NodeList o = row.getElementsByTagNameNS("*", "c");
                for (int j = 0; j < o.getLength(); j++) {
                    Element c = (Element) o.item(j);
                    int cot = c.hasAttribute("r") ? chiSoCot(c.getAttribute("r")) : j;
                    if (cot < soCot) {
                        ketQua.get(soDong - 1)[cot] = giaTriO(c, chuoiChung);
                    }
                }
            }
            return ketQua;
        } catch (ParserConfigurationException | SAXException | RuntimeException ex) {
            throw new IOException("File không đúng định dạng .xlsx.", ex);
        }
    }

    private static Document docXml(ZipFile zip, String ten) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory nhaMay = DocumentBuilderFactory.newInstance();
        nhaMay.setNamespaceAware(true);
        // File do người dùng chọn: chặn DOCTYPE để XML không đọc được tệp khác trên máy.
        nhaMay.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream vao = zip.getInputStream(zip.getEntry(ten))) {
            DocumentBuilder boDoc = nhaMay.newDocumentBuilder();
            // File sai đã được báo cho người dùng qua ngoại lệ, không in thêm ra console.
            boDoc.setErrorHandler(null);
            return boDoc.parse(vao);
        }
    }

    private static String duongDanTrangDau(ZipFile zip) throws IOException, SAXException, ParserConfigurationException {
        Element trangDau = (Element) docXml(zip, "xl/workbook.xml").getElementsByTagNameNS("*", "sheet").item(0);
        String id = trangDau.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
        NodeList lienKet = docXml(zip, "xl/_rels/workbook.xml.rels").getElementsByTagNameNS("*", "Relationship");
        for (int i = 0; i < lienKet.getLength(); i++) {
            Element e = (Element) lienKet.item(i);
            if (id.equals(e.getAttribute("Id"))) {
                String dich = e.getAttribute("Target");
                return dich.startsWith("/") ? dich.substring(1) : "xl/" + dich;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trang tính đầu tiên.");
    }

    private static String giaTriO(Element c, List<String> chuoiChung) {
        String loai = c.getAttribute("t");
        if ("inlineStr".equals(loai)) {
            return vanBan(c);
        }
        Node v = c.getElementsByTagNameNS("*", "v").item(0);
        if (v == null) {
            return "";
        }
        String giaTri = v.getTextContent().trim();
        if ("s".equals(loai)) {
            return chuoiChung.get(Integer.parseInt(giaTri));
        }
        if (loai.isEmpty() || "n".equals(loai)) {
            // Số lẻ đổi dấu chấm thành phẩy: trong AUTORA dấu chấm là ngăn cách hàng nghìn, 150.125 không phải 150125.
            return new BigDecimal(giaTri).stripTrailingZeros().toPlainString().replace('.', ',');
        }
        return giaTri;
    }

    /** Ghép các đoạn chữ của một ô, bỏ phần phiên âm (rPh) Excel có thể lưu kèm. */
    private static String vanBan(Element o) {
        StringBuilder chu = new StringBuilder();
        NodeList t = o.getElementsByTagNameNS("*", "t");
        for (int i = 0; i < t.getLength(); i++) {
            if (!"rPh".equals(t.item(i).getParentNode().getLocalName())) {
                chu.append(t.item(i).getTextContent());
            }
        }
        return chu.toString();
    }

    private static int chiSoCot(String thamChieu) {
        int cot = 0;
        for (char ky : thamChieu.toCharArray()) {
            if (!Character.isLetter(ky)) {
                break;
            }
            cot = cot * 26 + (ky - 'A' + 1);
        }
        return cot - 1;
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
