# 📘 TÀI LIỆU HƯỚNG DẪN KIẾN TRÚC VÀ QUY CHUẨN CODE (CODING STANDARDS)
## DỰ ÁN: HỆ THỐNG QUẢN LÝ GARA Ô TÔ AUTORA
> **Đơn vị:** Khoa Công nghệ Thông tin – Trường Đại học Công nghiệp TP.HCM (IUH)  
> **Nhóm thực hiện:** Nhóm 13 (AUTORA Team) | **Học phần:** Phát triển ứng dụng  
> **Nền tảng:** Java Swing Desktop Application | **Hệ quản trị CSDL:** Microsoft SQL Server  

---

## 📑 MỤC LỤC
1. [Kiến Trúc Tổng Thể Hệ Thống AUTORA (Mô Hình MVC 3 Lớp)](#1-kiến-trúc-tổng-thể-hệ-thống-autora-mô-hình-mvc-3-lớp)
2. [Cấu Trúc Và Nhiệm Vụ Của Từng Package (`src/`)](#2-cấu-trúc-và-nhiệm-vụ-của-từng-package-src)
3. [Quy Chuẩn Thiết Kế Giao Diện Chung (UI Standards)](#3-quy-chuẩn-thiết-kế-giao-diện-chung-ui-standards)
4. [Quy Chuẩn Lập Trình & Code Mẫu Chuẩn Cho Từng Tầng](#4-quy-chuẩn-lập-trình--code-mẫu-chuẩn-cho-từng-tầng)
   - [4.1. Quy tắc đặt tên (Naming Conventions)](#41-quy-tắc-đặt-tên-naming-conventions)
   - [4.2. Chuẩn mực tầng Entity (POJO & Validation)](#42-chuẩn-mực-tầng-entity-pojo--validation)
   - [4.3. Chuẩn mực tầng DAO (Data Access Object)](#43-chuẩn-mực-tầng-dao-data-access-object)
   - [4.4. Chuẩn mực tầng GUI (Swing Modern Flat UI)](#44-chuẩn-mực-tầng-gui-swing-modern-flat-ui)
   - [4.5. Chuẩn mực tầng Controller (Event Handling & MVC Bridge)](#45-chuẩn-mực-tầng-controller-event-handling--mvc-bridge)
5. [Cây Thư Mục File Mã Nguồn Toàn Diện](#5-cây-thư-mục-file-mã-nguồn-toàn-diện)
6. [Bảng Phân Công Công Việc 4 Thành Viên (WBS)](#6-bảng-phân-công-công-việc-4-thành-viên-wbs)
7. [Các Điểm Giao Thoa Dữ Liệu Giữa Các Thành Viên](#7-các-điểm-giao-thoa-dữ-liệu-giữa-các-thành-viên)
8. [Hướng Dẫn Tích Hợp Vào Màn Hình Chính (`GiaoDienChinh`)](#8-hướng-dẫn-tích-hợp-vào-màn-hình-chính-giaodienchinh)

---

## 1. KIẾN TRÚC TỔNG THỂ HỆ THỐNG AUTORA (MÔ HÌNH MVC 3 LỚP)

Hệ thống được thiết kế theo mô hình **MVC kết hợp Kiến trúc phân tầng 3 lớp (3-Tier Layered Architecture)**:

```
+---------------------------------------------------------------------------------+
|                       TẦNG GIAO DIỆN (VIEW - package gui)                       |
|                                                                                 |
|   +-------------------------------------------------------------------------+   |
|   |                  GiaoDienChinh.java (JFrame)                            |   |
|   |   - TopBar: Logo, Tên gara, Tên màn hình, Thông tin User (PhienDangNhap)|   |
|   |   - LeftSidebar: Menu điều hướng theo phân quyền                        |   |
|   |   - ContentPanel: CardLayout chứa các màn hình nghiệp vụ                 |   |
|   +------------------------------------+------------------------------------+   |
|                                        |                                        |
|         +------------------------------+------------------------------+         |
|         |                                                             |         |
|         v                                                             v         |
|   +-------------------------------+                             +-----------+   |
|   | Các màn hình quản lý (JPanel) |                             | JDialog   |   |
|   | - QLHoSoXe, QLKhachHang       |                             | - Popups  |   |
|   | - QLTiepNhanSuaChua, QLHoaDon |                             | - ChiTiet |   |
|   | - QLPhuTung, QLThongKe        |                             | - ThanhToan   |
|   +---------------+---------------+                             +-----+-----+   |
+-------------------|---------------------------------------------------|---------+
                    |                                                   |
                    | (View cung cấp public getters, đăng ký qua ctor)  |
                    v                                                   v
+---------------------------------------------------------------------------------+
|                   TẦNG ĐIỀU KHIỂN (CONTROLLER - package controller)             |
|                                                                                 |
|   - Nhận tham chiếu View trong Constructor: public QLXxxController(QLXxx view)  |
|   - Gán sự kiện (ActionListeners, DocumentListeners) thông qua Lambda           |
|   - Kiểm tra hợp lệ dữ liệu nhập (Validation form)                              |
|   - Gọi DAO để lấy hoặc lưu dữ liệu; hiển thị thông báo JOptionPane             |
|   - Đổ dữ liệu từ danh sách Entity lên JTable (fillTable)                       |
+------------------------------------+--------------------------------------------+
                                     |
                                     | (Gọi phương thức CRUD & Tìm kiếm)
                                     v
+---------------------------------------------------------------------------------+
|                    TẦNG DỮ LIỆU & THỰC THỂ (MODEL - package dao & entity)       |
|                                                                                 |
|   +------------------------------+            +-----------------------------+   |
|   |     package dao (Xxx_Dao)    |            |        package entity       |   |
|   |  - PreparedStatement JDBC    |<---------->|  - POJO đóng gói dữ liệu    |   |
|   |  - try-with-resources        |            |  - Validation nghiệp vụ     |   |
|   |  - CRUD & Truy vấn nâng cao  |            |  - toString, equals, hash   |   |
|   +--------------+---------------+            +-----------------------------+   |
+------------------|--------------------------------------------------------------+
                   |
                   | (Mở kết nối qua ConnectDB.getConnection())
                   v
+---------------------------------------------------------------------------------+
|                     TẦNG KẾT NỐI (package connection)                           |
|                                                                                 |
|   ConnectDB.java (Singleton Pattern) -> Microsoft SQL Server (GaraAUTORA)       |
+---------------------------------------------------------------------------------+
```

---

## 2. CẤU TRÚC VÀ NHIỆM VỤ CỦA TỪNG PACKAGE (`src/`)

| Package | Nhiệm vụ kỹ thuật |
| :--- | :--- |
| **`connection`** | Chứa duy nhất class `ConnectDB.java` hiện thực Singleton kết nối SQL Server qua JDBC. |
| **`entity`** | Chứa các POJO đại diện cho bảng CSDL, đóng gói dữ liệu, có validation nghiệp vụ trong setter. |
| **`dao`** | Chứa các lớp Data Access Object (hậu tố `_Dao.java`). Thực thi JDBC PreparedStatement trong `try-with-resources`. |
| **`controller`** | Chứa các Controller lắng nghe sự kiện từ View, validate form, điều phối giữa GUI và DAO. |
| **`gui`** | Chứa giao diện Java Swing (`GiaoDienChinh`, các `JPanel` nghiệp vụ, các `JDialog` popup). |
| **`util`** | Chứa tiện ích dùng chung: `UIConstants.java` (bảng màu, font), `FormatUtil.java`, `VietQRUtil.java`, `PdfExporter.java`. |
| **`test`** | Chứa các class kiểm thử kết nối CSDL và chạy thử nghiệm độc lập. |

---

## 3. QUY CHUẨN THIẾT KẾ GIAO DIỆN CHUNG (UI STANDARDS)

### 3.1. Bảng màu hệ thống (Color Palette)
Được định nghĩa tập trung trong class [`util.UIConstants`](file:///e:/PTUD/AUTORA/src/util/UIConstants.java):

| Nhóm Màu / Phân Hệ | Mã Màu Hex | Tên Hằng Số Trong `UIConstants` | Phạm Vi Áp Dụng |
| :--- | :---: | :--- | :--- |
| **Màu chủ đạo (Primary)** | `#1565C0`<br/>`#3498DB` | `UIConstants.PRIMARY`<br/>`UIConstants.PRIMARY_LIGHT` | Header chính, Topbar điều hướng, các nút hành động chính (Lưu, Tiếp nhận, Thanh toán). |
| **Phân hệ Chủ cửa hàng** | `#27AE60`<br/>`#76CA65` | `UIConstants.ROLE_MANAGER`<br/>`UIConstants.ROLE_MANAGER_LIGHT` | Nền nhãn/Card Quản lý Nhân viên, Khuyến mãi, Tài khoản & Báo cáo thống kê. |
| **Phân hệ Lễ tân** | `#C0392B`<br/>`#E74C3C` | `UIConstants.ROLE_RECEPTION`<br/>`UIConstants.ROLE_RECEPTION_LIGHT` | Nền nhãn/Card Quản lý Khách hàng, Hồ sơ xe, Lập phiếu sửa chữa & Lập hóa đơn. |
| **Phân hệ KTV & Thủ kho** | `#D35400`<br/>`#F39C12` | `UIConstants.ROLE_TECH_STOCK`<br/>`UIConstants.ROLE_TECH_STOCK_LIGHT` | Nền nhãn/Card Tiến độ sửa chữa, Yêu cầu phụ tùng, Xuất kho & Nhập kho. |
| **Trạng thái Thành công** | `#00B050` | `UIConstants.STATUS_SUCCESS` | Hồ sơ/Phiếu ở trạng thái `Đã hoàn thành`, `Đã thanh toán`, `Đã xuất kho`. |
| **Trạng thái Cảnh báo** | `#F1C40F` | `UIConstants.STATUS_WARNING` | Hồ sơ/Phiếu ở trạng thái `Chờ sửa`, `Chờ phụ tùng`, hoặc tồn kho chạm ngưỡng cảnh báo. |
| **Trạng thái Lỗi / Vi phạm** | `#FF5E5E` | `UIConstants.STATUS_DANGER` | Bôi đỏ khung viền ô nhập liệu vi phạm ràng buộc (SĐT sai, hết tồn kho, bỏ trống). |
| **Màu trung tính (Neutral)** | `#ECEFF1`<br/>`#FFFFFF` | `UIConstants.APP_BG`<br/>`UIConstants.CARD_BG` | `#ECEFF1` làm nền ứng dụng; `#FFFFFF` làm nền bảng dữ liệu, nền form card và nút đóng/hủy. |

### 3.2. Cấp bậc văn bản & Font chữ (Typography Hierarchy)
- **Font tiêu chuẩn:** `Roboto` (dự phòng: `Segoe UI`, `Sans-serif`).
- **Tiêu đề màn hình (Page Title):** 20pt - 24pt, In đậm (`Font.BOLD`), màu `UIConstants.PRIMARY`.
- **Tiêu đề nhóm / Card (Section Title):** 16pt - 18pt, In đậm (`Font.BOLD`), màu theo phân hệ tương ứng.
- **Tiêu đề cột bảng / Nhãn Form (Form Label):** 13pt - 14pt, In đậm (`Font.BOLD`), thêm dấu `*` đỏ nếu bắt buộc.
- **Nội dung văn bản / Ô nhập liệu (Body / Input):** 12pt - 13pt, Bình thường (`Font.PLAIN`).
- **Nhãn trạng thái / Ghi chú phụ (Badge / Caption):** 10pt - 11pt.

### 3.3. Cấu trúc 3 vùng bố cục cố định (Layout Grid System)
1. **Top Header Bar:** Logo AUTORA, tên gara, tên màn hình hiện tại, thông tin tài khoản đang đăng nhập (Tên NV + Vai trò) và nút Đăng xuất.
2. **Left Sidebar Menu:** Danh sách menu chức năng kèm icon. Tự động ẩn hoặc vô hiệu hóa các mục ngoài phân quyền người dùng.
3. **Main Workspace:** 
   - Phía trên: Thanh công cụ tìm kiếm / Bộ lọc nhanh (Filter Card).
   - Ở giữa: Bảng danh sách dữ liệu (`JTable` trong `JScrollPane`) và Form nhập liệu chi tiết.
   - Phía dưới: Cụm nút hành động (Thêm, Cập nhật, Xóa, Làm mới, Thanh toán, In phiếu).

### 3.4. Các quy chuẩn tương tác thông minh (Interactive Behaviors)
- **Tự động điền dữ liệu (Auto-fetch):** Khi nhập Biển số xe hoặc SĐT tại màn hình tiếp nhận, nếu dữ liệu đã có trong DB, tự động điền Tên chủ xe, Địa chỉ và thông số xe lên form.
- **Cảnh báo tồn kho tự động (Stock Alert):** Mặt hàng phụ tùng có `soLuongTon <= nguongCanhBao` tự động bôi đỏ dòng và hiện icon cảnh báo.
- **Modal xem trước & VietQR động:** Khi bấm thanh toán, popup hiển thị chi tiết tổng chi phí và tự động tạo mã QR chuyển khoản VietQR chứa chính xác số tiền cần thanh toán.
- **Khóa dữ liệu sau thanh toán (Data Lock):** Khi hóa đơn đã thanh toán thành công, hệ thống khóa toàn bộ phiếu sửa chữa liên quan, không cho phép sửa đổi nhằm bảo toàn tính toàn vẹn tài chính.

---

## 4. QUY CHUẨN LẬP TRÌNH & CODE MẪU CHUẨN CHO TỪNG TẦNG

### 4.1. Quy tắc đặt tên (Naming Conventions)
- **Tên Package:** Chữ thường toàn bộ: `connection`, `entity`, `dao`, `controller`, `gui`, `util`, `test`.
- **Tên Class Entity:** Danh từ PascalCase: `KhachHang`, `Xe`, `PhuTung`, `HoaDon`, `PhieuTiepNhanVaSuaChua`.
- **Tên Class DAO:** Luôn có hậu tố `_Dao`: `KhachHang_Dao`, `Xe_Dao`, `PhuTung_Dao`, `HoaDon_Dao`.
- **Tên Class Controller:** Luôn có hậu tố `Controller`: `QLKhachHangController`, `QLHoSoXeController`, `DangNhapController`.
- **Tên Class GUI:** 
  - Màn hình quản lý (JPanel): `QL[TênChứcNăng].java` (ví dụ `QLKhachHang.java`, `QLHoSoXe.java`).
  - Hộp thoại popup (JDialog): `[TênChứcNăng]Dialog.java` (ví dụ `ChiTietPhieuSuaChuaDialog.java`).
  - Dashboard: `TrangChuLeTan.java`, `TrangChuQuanLy.java`, `TrangChuKyThuatVien.java`, `TrangChuThuKho.java`.
- **Tiền tố bắt buộc cho biến UI Components:**
  - `JButton`: `btnThem`, `btnCapNhat`, `btnXoa`, `btnLamMoi`, `btnInPhieu`
  - `JTextField`: `txtMaKH`, `txtHoTen`, `txtBienSo`, `txtTimKiem`
  - `JPasswordField`: `pwdMatKhau`
  - `JComboBox`: `cboLoaiKH`, `cboHangXe`, `cboTrangThai`
  - `JTable`: `tblDanhSach`
  - `DefaultTableModel`: `tableModel`
  - `JLabel`: `lblTongTien`, `lblTieuDe`
  - `JCheckBox`: `chkXacNhan`
  - `JRadioButton`: `radTienMat`, `radChuyenKhoan`

---

### 4.2. Chuẩn mực tầng Entity (POJO & Validation)
1. Tất cả thuộc tính phải là `private`.
2. Có tối thiểu 3 Constructor: Rỗng mặc định, Constructor theo Khóa chính, và Constructor đầy đủ tham số.
3. Các setter phải validate đúng ràng buộc và ném `IllegalArgumentException` khi dữ liệu sai định dạng:
   - SĐT: đúng 10 số, bắt đầu bằng `0` (`^0[0-9]{9}$`).
   - Biển số xe: đúng định dạng biển số xe Việt Nam (`^[0-9]{2}[A-Z]{1,2}-[0-9]{4,5}(\\.[0-9]{2})?$`).
   - Tiền bạc, số lượng, % nhiên liệu: không được âm.
4. Ghi đè `equals()` và `hashCode()` theo Khóa chính.
5. Ghi đè `toString()`.

```java
// CODE MẪU CHUẨN ENTITY
package entity;

import java.util.Objects;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String email;
    private String maSoThue;
    private String loaiKhachHang;
    private boolean duocGhiNo;

    public KhachHang() {}

    public KhachHang(String maKH) { this.maKH = maKH; }

    public KhachHang(String maKH, String hoTen, String soDienThoai, String diaChi, String email, String maSoThue, String loaiKhachHang, boolean duocGhiNo) {
        setMaKH(maKH);
        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setDiaChi(diaChi);
        setEmail(email);
        setMaSoThue(maSoThue);
        setLoaiKhachHang(loaiKhachHang);
        setDuocGhiNo(duocGhiNo);
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được để trống!");
        }
        this.maKH = maKH.trim();
    }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên khách hàng không được để trống!");
        }
        this.hoTen = hoTen.trim();
    }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || !soDienThoai.matches("^0[0-9]{9}$")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0!");
        }
        this.soDienThoai = soDienThoai.trim();
    }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi == null ? "" : diaChi.trim(); }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ!");
        }
        this.email = email == null ? "" : email.trim();
    }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue == null ? "" : maSoThue.trim(); }

    public String getLoaiKhachHang() { return loaiKhachHang; }
    public void setLoaiKhachHang(String loaiKhachHang) { this.loaiKhachHang = loaiKhachHang == null ? "Khách vãng lai" : loaiKhachHang; }

    public boolean isDuocGhiNo() { return duocGhiNo; }
    public void setDuocGhiNo(boolean duocGhiNo) { this.duocGhiNo = duocGhiNo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KhachHang)) return false;
        KhachHang that = (KhachHang) o;
        return Objects.equals(maKH, that.maKH);
    }

    @Override
    public int hashCode() { return Objects.hash(maKH); }

    @Override
    public String toString() { return maKH + " - " + hoTen + " (" + soDienThoai + ")"; }
}
```

---

### 4.3. Chuẩn mực tầng DAO (Data Access Object)
1. Luôn dùng `ConnectDB.getConnection()`.
2. Luôn dùng `PreparedStatement` với tham số `?` chống SQL Injection.
3. Luôn dùng `try-with-resources` để tự động đóng Connection, Statement, ResultSet.
4. Có phương thức phụ `private Entity mapEntity(ResultSet rs)` để tái sử dụng mã nguồn.
5. Cung cấp bộ hàm chuẩn: `findAll()`, `findById()`, `insert()`, `update()`, `delete()` và các hàm tìm kiếm nghiệp vụ.

```java
// CODE MẪU CHUẨN DAO
package dao;

import connection.ConnectDB;
import entity.KhachHang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHang_Dao {

    public List<KhachHang> findAll() {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, duocGhiNo FROM KhachHang ORDER BY maKH ASC";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapKhachHang(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public KhachHang findById(String maKH) {
        String sql = "SELECT maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, duocGhiNo FROM KhachHang WHERE maKH = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapKhachHang(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KhachHang findBySdt(String sdt) {
        String sql = "SELECT maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, duocGhiNo FROM KhachHang WHERE soDienThoai = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapKhachHang(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, duocGhiNo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getHoTen());
            ps.setString(3, kh.getSoDienThoai());
            ps.setString(4, kh.getDiaChi());
            ps.setString(5, kh.getEmail());
            ps.setString(6, kh.getMaSoThue());
            ps.setString(7, kh.getLoaiKhachHang());
            ps.setBoolean(8, kh.isDuocGhiNo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(KhachHang kh) {
        String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, diaChi = ?, email = ?, maSoThue = ?, loaiKhachHang = ?, duocGhiNo = ? WHERE maKH = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSoDienThoai());
            ps.setString(3, kh.getDiaChi());
            ps.setString(4, kh.getEmail());
            ps.setString(5, kh.getMaSoThue());
            ps.setString(6, kh.getLoaiKhachHang());
            ps.setBoolean(7, kh.isDuocGhiNo());
            ps.setString(8, kh.getMaKH());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maKH) {
        String sql = "DELETE FROM KhachHang WHERE maKH = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private KhachHang mapKhachHang(ResultSet rs) throws SQLException {
        return new KhachHang(
            rs.getString("maKH"),
            rs.getString("hoTen"),
            rs.getString("soDienThoai"),
            rs.getString("diaChi"),
            rs.getString("email"),
            rs.getString("maSoThue"),
            rs.getString("loaiKhachHang"),
            rs.getBoolean("duocGhiNo")
        );
    }
}
```

---

### 4.4. Chuẩn mực tầng GUI (Swing Modern Flat UI)
1. Màn hình quản lý chính luôn kế thừa `JPanel`.
2. Có hàm tĩnh `createPanel()` khởi tạo View và gắn Controller:
   ```java
   public static JPanel createPanel() {
       QLKhachHang panel = new QLKhachHang();
       new QLKhachHangController(panel);
       return panel;
   }
   ```
3. Cung cấp đầy đủ các `public getter` cho mọi thành phần nhập liệu, nút bấm, bảng và table model để Controller thao tác.
4. Ứng dụng màu sắc chuẩn từ `UIConstants`: Nền app `UIConstants.APP_BG`, nền thẻ `UIConstants.CARD_BG`, nút chính `UIConstants.PRIMARY`.

---

### 4.5. Chuẩn mực tầng Controller (Event Handling & MVC Bridge)
1. Nhận View trong constructor, tự khởi tạo DAO:
   ```java
   public QLKhachHangController(QLKhachHang view) {
       this.view = view;
       this.khachHangDao = new KhachHang_Dao();
       initController();
       loadDataToTable();
   }
   ```
2. Gán sự kiện qua hàm `initController()` bằng biểu thức Lambda.
3. Bắt sự kiện click dòng trên bảng (`ListSelectionListener`) để đổ dữ liệu ngược lại form nhập liệu. Khóa trường Khóa chính (`setEditable(false)`).
4. Tìm kiếm realtime bằng `DocumentListener` trên `JTextField`.
5. Bắt ngoại lệ và hiển thị thông báo thân thiện bằng `JOptionPane`.

---

## 5. CÂY THƯ MỤC FILE MÃ NGUỒN TOÀN DIỆN

```
AUTORA/
├── database/
│   └── GaraAUTORA_Database.sql                     # Script SQL Server tạo DB và dữ liệu mẫu
│
└── src/
    ├── connection/
    │   └── ConnectDB.java                          # Singleton quản lý kết nối JDBC
    │
    ├── entity/
    │   ├── NhanVien.java
    │   ├── TaiKhoan.java
    │   ├── PhienDangNhap.java                      # Lưu session người dùng đăng nhập
    │   ├── KhachHang.java
    │   ├── Xe.java
    │   ├── DichVu.java
    │   ├── PhuTung.java
    │   ├── NhaCungCap.java
    │   ├── KhuyenMai.java
    │   ├── Thue.java
    │   ├── PhieuTiepNhanVaSuaChua.java
    │   ├── ChiTietSuaChua_DichVu.java
    │   ├── ChiTietSuaChua_PhuTung.java
    │   ├── PhieuYeuCauXuatKho.java
    │   ├── ChiTietYeuCauXuatKho.java
    │   ├── PhieuNhapKho.java
    │   ├── ChiTietNhapKho.java
    │   ├── HoaDon.java
    │   └── PhieuBanGiao.java
    │
    ├── dao/
    │   ├── NhanVien_Dao.java
    │   ├── TaiKhoan_Dao.java
    │   ├── KhachHang_Dao.java
    │   ├── Xe_Dao.java
    │   ├── DichVu_Dao.java
    │   ├── PhuTung_Dao.java
    │   ├── NhaCungCap_Dao.java
    │   ├── KhuyenMai_Dao.java
    │   ├── Thue_Dao.java
    │   ├── PhieuTiepNhanVaSuaChua_Dao.java
    │   ├── ChiTietSuaChua_DichVu_Dao.java
    │   ├── ChiTietSuaChua_PhuTung_Dao.java
    │   ├── PhieuYeuCauXuatKho_Dao.java
    │   ├── PhieuNhapKho_Dao.java
    │   ├── HoaDon_Dao.java
    │   ├── PhieuBanGiao_Dao.java
    │   └── ThongKe_Dao.java
    │
    ├── controller/
    │   ├── DangNhapController.java
    │   ├── GiaoDienChinhController.java
    │   ├── TrangChuLeTanController.java
    │   ├── TrangChuQuanLyController.java
    │   ├── TrangChuKyThuatVienController.java
    │   ├── TrangChuThuKhoController.java
    │   ├── QLHoSoXeController.java
    │   ├── QLKhachHangController.java
    │   ├── QLTiepNhanSuaChuaController.java
    │   ├── ChiTietPhieuSuaChuaController.java
    │   ├── QLTienDoSuaChuaController.java
    │   ├── QLHoaDonController.java
    │   ├── ThanhToanController.java
    │   ├── QLBanGiaoXeController.java
    │   ├── QLNhanVienController.java
    │   ├── QLTaiKhoanController.java
    │   ├── QLKhuyenMaiController.java
    │   ├── QLPhuTungController.java
    │   ├── QLNhapKhoController.java
    │   ├── QLDuyetXuatKhoController.java
    │   └── QLThongKeController.java
    │
    ├── gui/
    │   ├── Main.java                               # Điểm khởi chạy ứng dụng
    │   ├── DangNhap.java                           # JFrame Đăng nhập
    │   ├── GiaoDienChinh.java                      # JFrame Chính (Sidebar + CardLayout)
    │   ├── TrangChuLeTan.java                      # JPanel Dashboard Lễ tân
    │   ├── TrangChuQuanLy.java                     # JPanel Dashboard Quản lý
    │   ├── TrangChuKyThuatVien.java                # JPanel Dashboard KTV
    │   ├── TrangChuThuKho.java                     # JPanel Dashboard Thủ kho
    │   ├── QLKhachHang.java                        # JPanel Quản lý Khách hàng
    │   ├── QLHoSoXe.java                           # JPanel Quản lý Hồ sơ xe
    │   ├── QLTiepNhanSuaChua.java                  # JPanel Tiếp nhận & Lập phiếu
    │   ├── ChiTietPhieuSuaChuaDialog.java          # JDialog Chi tiết & Chỉ định thợ
    │   ├── QLTienDoSuaChua.java                    # JPanel Cập nhật tiến độ & Xin phụ tùng
    │   ├── QLHoaDon.java                           # JPanel Quản lý Hóa đơn
    │   ├── ChiTietHoaDonDialog.java                # JDialog Xem chi tiết hóa đơn
    │   ├── ThanhToanDialog.java                    # JDialog Thanh toán & Hiển thị VietQR
    │   ├── QLBanGiaoXe.java                        # JPanel Quản lý Bàn giao xe
    │   ├── InPhieuXuatXuongDialog.java             # JDialog Xem trước & In phiếu xuất xưởng
    │   ├── QLNhanVien.java                         # JPanel Quản lý Nhân sự
    │   ├── QLTaiKhoan.java                         # JPanel Cấp tài khoản & Phân quyền
    │   ├── QLKhuyenMai.java                        # JPanel Quản lý Khuyến mãi
    │   ├── QLPhuTung.java                          # JPanel Quản lý Phụ tùng & Tồn kho
    │   ├── QLNhapKho.java                          # JPanel Nhập kho từ NCC
    │   ├── QLDuyetXuatKho.java                     # JPanel Thủ kho duyệt xuất phụ tùng
    │   └── QLThongKe.java                          # JPanel Thống kê đồ họa (JFreeChart)
    │
    ├── util/
    │   ├── UIConstants.java                        # Hằng số màu sắc, font chữ hệ thống
    │   ├── FormatUtil.java                         # Format tiền tệ VNĐ, ngày tháng
    │   ├── VietQRUtil.java                         # Sinh link QR động chuyển khoản
    │   └── PdfExporter.java                        # Xuất file PDF (Hóa đơn, Phiếu xuất xưởng)
    │
    └── test/
        └── TestConnection.java                     # Chạy thử kiểm tra kết nối DB
```

---

## 6. BẢNG PHÂN CÔNG CÔNG VIỆC 4 THÀNH VIÊN (WBS)

### 6.1. Nguyễn Tuấn Vũ
- **Màn hình phụ trách:**
  1. `TrangChuLeTan` (JPanel)
  2. `QLHoSoXe` (JPanel)
  3. `QLBanGiaoXe` (JPanel)
  4. `InPhieuXuatXuongDialog` (JDialog)
  5. `QLNhanVien` (JPanel)
- **Controller:** `TrangChuLeTanController`, `QLHoSoXeController`, `QLBanGiaoXeController`, `QLNhanVienController`.
- **DAO & Entity:** `Xe_Dao`, `PhieuBanGiao_Dao`, `NhanVien_Dao` / `Xe`, `PhieuBanGiao`, `NhanVien`.
- **Màu sắc chủ đạo:** Màu đỏ Lễ tân (`UIConstants.ROLE_RECEPTION`: `#C0392B`).

### 6.2. Trần Thị Tường Vân
- **Màn hình phụ trách:**
  1. `QLKhachHang` (JPanel)
  2. `QLHoaDon` (JPanel)
  3. `ChiTietHoaDonDialog` (JDialog)
  4. `ThanhToanDialog` (JDialog)
  5. `QLTaiKhoan` (JPanel)
- **Controller:** `QLKhachHangController`, `QLHoaDonController`, `ThanhToanController`, `QLTaiKhoanController`.
- **DAO & Entity:** `KhachHang_Dao`, `HoaDon_Dao`, `TaiKhoan_Dao`, `Thue_Dao` / `KhachHang`, `HoaDon`, `TaiKhoan`, `Thue`.
- **Màu sắc chủ đạo:** Màu đỏ Lễ tân (`UIConstants.ROLE_RECEPTION`) cho Khách hàng & Hóa đơn; Màu xanh lá Quản lý (`UIConstants.ROLE_MANAGER`) cho Tài khoản.

### 6.3. Nguyễn Xuân Việt Anh
- **Màn hình phụ trách:**
  1. `DangNhap` (JFrame)
  2. `GiaoDienChinh` (JFrame - Khung sườn tổng thể của ứng dụng)
  3. `TrangChuQuanLy` (JPanel)
  4. `QLTiepNhanSuaChua` (JPanel)
  5. `ChiTietPhieuSuaChuaDialog` (JDialog)
  6. `QLKhuyenMai` (JPanel)
- **Controller:** `DangNhapController`, `GiaoDienChinhController`, `TrangChuQuanLyController`, `QLTiepNhanSuaChuaController`, `ChiTietPhieuSuaChuaController`, `QLKhuyenMaiController`.
- **DAO & Entity:** `PhieuTiepNhanVaSuaChua_Dao`, `DichVu_Dao`, `ChiTietSuaChua_DichVu_Dao`, `KhuyenMai_Dao` / `PhieuTiepNhanVaSuaChua`, `DichVu`, `ChiTietSuaChua_DichVu`, `KhuyenMai`, `PhienDangNhap`.
- **Màu sắc chủ đạo:** Màu xanh dương (`UIConstants.PRIMARY`: `#1565C0`) cho Đăng nhập & Khung chính; Màu xanh lá (`UIConstants.ROLE_MANAGER`) cho Khuyến mãi.

### 6.4. Bùi Thái Vy
- **Màn hình phụ trách:**
  1. `QLThongKe` (JPanel - Ứng dụng thư viện JFreeChart)
  2. `TrangChuKyThuatVien` (JPanel)
  3. `TrangChuThuKho` (JPanel)
  4. `QLTienDoSuaChua` (JPanel)
  5. `QLPhuTung` (JPanel)
  6. `QLNhapKho` (JPanel)
  7. `QLDuyetXuatKho` (JPanel)
- **Controller:** `QLThongKeController`, `TrangChuKyThuatVienController`, `TrangChuThuKhoController`, `QLTienDoSuaChuaController`, `QLPhuTungController`, `QLNhapKhoController`, `QLDuyetXuatKhoController`.
- **DAO & Entity:** `PhuTung_Dao`, `NhaCungCap_Dao`, `PhieuNhapKho_Dao`, `PhieuYeuCauXuatKho_Dao`, `ThongKe_Dao` / `PhuTung`, `ChiTietSuaChua_PhuTung`, `NhaCungCap`, `PhieuNhapKho`, `ChiTietNhapKho`, `PhieuYeuCauXuatKho`, `ChiTietYeuCauXuatKho`.
- **Màu sắc chủ đạo:** Màu cam KTV & Kho (`UIConstants.ROLE_TECH_STOCK`: `#D35400`); Cảnh báo đỏ (`UIConstants.STATUS_DANGER`) cho phụ tùng dưới ngưỡng tồn kho.

---

## 7. CÁC ĐIỂM GIAO THOA DỮ LIỆU GIỮA CÁC THÀNH VIÊN

1. **Tiếp nhận xe (Việt Anh):** Gọi `KhachHang_Dao.findBySdt()` (Tường Vân) để lấy chủ xe, gọi `Xe_Dao.findById()` (Tuấn Vũ) để lấy thông tin xe. Nếu xe chưa có, mở popup thêm nhanh xe của Tuấn Vũ.
2. **Chỉ định thợ (Việt Anh):** Gọi `NhanVien_Dao.findByViTri("Kỹ thuật viên")` (Tuấn Vũ) để đổ danh sách thợ vào ComboBox.
3. **Lập hóa đơn (Tường Vân):** Gọi `PhieuTiepNhanVaSuaChua_Dao` (Việt Anh) để lấy tiền công và `ChiTietSuaChua_PhuTung_Dao` (Thái Vy) để lấy tiền phụ tùng xuất dùng.
4. **Bàn giao xe (Tuấn Vũ):** Kiểm tra trạng thái phiếu sửa chữa đạt `"Hoàn thành"` (Việt Anh) và hóa đơn đạt `"Đã thanh toán"` (Tường Vân) mới kích hoạt nút Bàn giao xe và In phiếu xuất xưởng.

---

## 8. HƯỚNG DẪN TÍCH HỢP VÀO MÀN HÌNH CHÍNH (`GiaoDienChinh`)

Tại class `GiaoDienChinh.java`, vùng làm việc trung tâm `contentPanel` sử dụng `CardLayout`. Mỗi thành viên khi hoàn thành màn hình chỉ cần thêm 1 dòng gọi hàm `createPanel()`:

```text
// Đăng ký các màn hình vào CardLayout của GiaoDienChinh
contentPanel.add(QLKhachHang.createPanel(), "QL_KHACH_HANG");
contentPanel.add(QLHoSoXe.createPanel(), "QL_HO_SO_XE");
contentPanel.add(QLTiepNhanSuaChua.createPanel(), "QL_TIEP_NHAN");
contentPanel.add(QLHoaDon.createPanel(), "QL_HOA_DON");
contentPanel.add(QLPhuTung.createPanel(), "QL_PHU_TUNG");
contentPanel.add(QLTienDoSuaChua.createPanel(), "QL_TIEN_DO");
contentPanel.add(QLNhapKho.createPanel(), "QL_NHAP_KHO");
contentPanel.add(QLDuyetXuatKho.createPanel(), "QL_DUYET_XUAT");
contentPanel.add(QLNhanVien.createPanel(), "QL_NHAN_VIEN");
contentPanel.add(QLTaiKhoan.createPanel(), "QL_TAI_KHOAN");
contentPanel.add(QLKhuyenMai.createPanel(), "QL_KHUYEN_MAI");
contentPanel.add(QLThongKe.createPanel(), "QL_THONG_KE");

// Khi người dùng click nút trên Sidebar menu, chuyển Card:
cardLayout.show(contentPanel, "QL_HO_SO_XE");
```
