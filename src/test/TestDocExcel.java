package test;

import util.XuatFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Không cần CSDL. Kiểm tra đọc file .xlsx do AUTORA ghi và file có cấu trúc như Excel lưu. */
public class TestDocExcel {
    private static int soLoi;

    public static void main(String[] args) throws Exception {
        kiemTraDocLaiFileTuGhi();
        kiemTraFileNhuExcelLuu();
        kiemTraFileSai();
        System.out.println(soLoi == 0 ? "TAT CA DAT" : soLoi + " LOI");
        if (soLoi > 0) {
            System.exit(1);
        }
    }

    private static void kiemTraDocLaiFileTuGhi() throws IOException {
        File tep = File.createTempFile("autora", ".xlsx");
        try {
            XuatFile.ghiXlsx(tep, new String[] {"Mã SKU", "Số lượng", "Đơn giá"},
                    List.of(new Object[] {"SKU_A", 10, "150.000"}, new Object[] {"SKU_Đ", 2, 85000}));
            List<String[]> d = XuatFile.docXlsx(tep, 3);
            kiem("Đủ số dòng", d.size() == 3);
            kiem("Tiêu đề", "Mã SKU".equals(d.get(0)[0]));
            kiem("Chuỗi và số", Arrays.equals(d.get(1), new String[] {"SKU_A", "10", "150.000"}));
            kiem("Tiếng Việt và số", Arrays.equals(d.get(2), new String[] {"SKU_Đ", "2", "85000"}));
        } finally {
            tep.delete();
        }
    }

    private static void kiemTraFileNhuExcelLuu() throws IOException {
        File tep = File.createTempFile("autora", ".xlsx");
        try (OutputStream ra = Files.newOutputStream(tep.toPath()); ZipOutputStream zip = new ZipOutputStream(ra)) {
            String ns = "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"";
            ghi(zip, "xl/workbook.xml", "<workbook " + ns + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"B\" sheetId=\"2\" r:id=\"rId9\"/><sheet name=\"A\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            ghi(zip, "xl/_rels/workbook.xml.rels", "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId9\" Target=\"/xl/worksheets/sheet2.xml\"/></Relationships>");
            ghi(zip, "xl/sharedStrings.xml", "<sst " + ns + "><si><t>SKU_X</t></si>"
                    + "<si><r><t>Ghép </t></r><r><t>đoạn</t></r><rPh sb=\"0\" eb=\"1\"><t>bỏ</t></rPh></si></sst>");
            ghi(zip, "xl/worksheets/sheet1.xml", "<worksheet " + ns + "><sheetData><row r=\"1\"><c r=\"A1\"><v>1</v></c></row></sheetData></worksheet>");
            ghi(zip, "xl/worksheets/sheet2.xml", "<worksheet " + ns + "><sheetData>"
                    + "<row r=\"1\"><c r=\"A1\" t=\"s\"><v>1</v></c></row>"
                    // Dòng 2 trống không được lưu; ô B4 trống; ô D ngoài phạm vi.
                    + "<row r=\"3\"><c r=\"A3\" t=\"s\"><v>0</v></c><c r=\"B3\" s=\"1\"><v>5</v></c><c r=\"C3\"><v>1.5E5</v></c><c r=\"D3\"><v>9</v></c></row>"
                    + "<row r=\"4\"><c r=\"A4\" t=\"str\"><v>SKU_Y</v></c><c r=\"C4\"><v>150.125</v></c></row>"
                    + "</sheetData></worksheet>");
        }
        try {
            List<String[]> d = XuatFile.docXlsx(tep, 3);
            kiem("Lấy đúng trang đầu theo thứ tự", d.size() == 4);
            kiem("Chuỗi chung bỏ phiên âm", "Ghép đoạn".equals(d.get(0)[0]));
            kiem("Giữ vị trí dòng trống", Arrays.equals(d.get(1), new String[] {"", "", ""}));
            kiem("Số dạng khoa học", Arrays.equals(d.get(2), new String[] {"SKU_X", "5", "150000"}));
            kiem("Bỏ ô trống và số lẻ không thành hàng nghìn", Arrays.equals(d.get(3), new String[] {"SKU_Y", "", "150,125"}));
        } finally {
            tep.delete();
        }
    }

    private static void kiemTraFileSai() throws IOException {
        File tep = File.createTempFile("autora", ".xlsx");
        try {
            Files.write(tep.toPath(), "khong phai zip".getBytes(StandardCharsets.UTF_8));
            biTuChoi("File không phải zip", tep);
            try (OutputStream ra = Files.newOutputStream(tep.toPath()); ZipOutputStream zip = new ZipOutputStream(ra)) {
                ghi(zip, "khac.txt", "x");
            }
            biTuChoi("Zip không phải Excel", tep);
            try (OutputStream ra = Files.newOutputStream(tep.toPath()); ZipOutputStream zip = new ZipOutputStream(ra)) {
                ghi(zip, "xl/workbook.xml", "<!DOCTYPE a [<!ENTITY x SYSTEM \"file:///c:/windows/win.ini\">]><a>&x;</a>");
            }
            biTuChoi("XML có DOCTYPE", tep);
        } finally {
            tep.delete();
        }
    }

    private static void biTuChoi(String ten, File tep) {
        try {
            XuatFile.docXlsx(tep, 3);
            kiem(ten, false);
        } catch (IOException ex) {
            kiem(ten, true);
        }
    }

    private static void ghi(ZipOutputStream zip, String ten, String noiDung) throws IOException {
        zip.putNextEntry(new ZipEntry(ten));
        zip.write(noiDung.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void kiem(String ten, boolean dat) {
        if (!dat) {
            soLoi++;
            System.out.println("SAI: " + ten);
        }
    }
}
