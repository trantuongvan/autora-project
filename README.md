# HỆ THỐNG QUẢN LÝ GARA Ô TÔ AUTORA
> **Đồ án môn học:** Phát triển ứng dụng (Application Development)  
> **Đơn vị:** Khoa Công nghệ Thông tin – Trường Đại học Công nghiệp TP.HCM (IUH)  
> **Nhóm thực hiện:** Nhóm 13

---

## 👥 Thành viên nhóm thực hiện & Phân chia màn hình

> 📖 **Xem hướng dẫn chi tiết:** Toàn bộ kiến trúc 3 lớp MVC, chuẩn đặt tên code (Coding Conventions) và mẫu triển khai từng tầng được quy định tại [HUONG_DAN_KIEN_TRUC_VA_CHUAN_CODE.md](HUONG_DAN_KIEN_TRUC_VA_CHUAN_CODE.md).

| STT | Họ và tên | MSSV | Vai trò | Màn hình chức năng phân công phụ trách |
| :---: | :--- | :---: | :---: | :--- |
| 1 | **Nguyễn Tuấn Vũ** | 24642961 | Thành viên | **Trang chủ Lễ tân**, **Quản lý hồ sơ xe**, **Quản lý bàn giao xe**, **In phiếu xuất xưởng**, **Quản lý nhân viên**. |
| 2 | **Trần Thị Tường Vân** | 24642841 | Thành viên | **Quản lý khách hàng**, **Quản lý hóa đơn**, **Chi tiết hóa đơn & Thanh toán**, **Tạo tài khoản và phân quyền**. |
| 3 | **Nguyễn Xuân Việt Anh** | 24725141 | Thành viên | **Đăng nhập**, **Trang chủ Quản lý (Khung sườn GiaoDienChinh)**, **Quản lý tiếp nhận & Lập phiếu sửa chữa**, **Chi tiết phiếu sửa chữa & Chỉ định thợ**, **Quản lý khuyến mãi**. |
| 4 | **Bùi Thái Vy** | 24636361 | Thành viên | **Thống kê báo cáo**, **Trang chủ Kỹ thuật viên / Thủ kho**, **Quản lý tiến độ sửa chữa & Phiếu yêu cầu phụ tùng**, **Quản lý phụ tùng & tồn kho**, **Quản lý nhập kho & Duyệt xuất kho**. |

---

## 📌 1. Giới thiệu tổng quan

**AUTORA** là giải pháp phần mềm quản lý nội bộ dành riêng cho chuỗi vận hành tại các gara ô tô. 

Trước đây, công tác quản lý tại các gara thường thực hiện thủ công bằng sổ sách, biểu mẫu giấy (thẻ hồ sơ xe, phiếu yêu cầu sửa chữa, phiếu lĩnh vật tư, phiếu nhập kho, hóa đơn Excel rời rạc) dẫn đến nhiều bất cập:
- Dễ thất lạc thông tin, tra cứu lịch sử sửa chữa/bảo hành chậm chạp.
- Khó kiểm soát tồn kho linh kiện phụ tùng theo thời gian thực.
- Dễ sai sót khi cộng dồn chi phí dịch vụ, áp mã khuyến mãi, tính thuế VAT.
- Chủ cửa hàng mất nhiều thời gian tổng hợp đối soát doanh thu cuối ngày.

**Mục tiêu của hệ thống AUTORA:**  
Số hóa toàn diện chu trình dịch vụ khép kín từ lúc **tiếp nhận xe -> chẩn đoán & phân công thợ -> yêu cầu xuất kho phụ tùng -> thực hiện sửa chữa -> lập hóa đơn & thanh toán -> bàn giao xe xuất xưởng**, đồng thời cung cấp hệ thống báo cáo thống kê trực quan hỗ trợ việc ra quyết định quản lý.

---

## 👤 2. Đối tượng & Phân quyền người dùng

Hệ thống được thiết kế phục vụ người dùng nội bộ của gara với 4 vai trò chính:

1. **Chủ cửa hàng (Quản lý):**  
   - Quản lý toàn diện hệ thống, nhân sự, tài khoản và phân quyền.
   - Thiết lập các chính sách khuyến mãi, quản lý danh mục dịch vụ/phụ tùng.
   - Xem báo cáo thống kê doanh thu, tồn kho và đánh giá năng suất kỹ thuật viên.
   - Giám sát nhật ký hoạt động (Audit Log) đối với các thao tác nhạy cảm.

2. **Nhân viên Lễ tân:**  
   - Quản lý hồ sơ khách hàng và phương tiện liên kết.
   - Tiếp nhận xe vào xưởng (Check-in), ghi nhận tình trạng vật lý và lập phiếu sửa chữa.
   - Phân công kỹ thuật viên phụ trách từng hạng mục dịch vụ.
   - Quản lý hóa đơn, áp dụng khuyến mãi, xử lý thanh toán (Tiền mặt, VietQR, Công nợ).
   - In phiếu xuất xưởng (Gate Pass) và bàn giao xe cho khách hàng (Check-out).

3. **Kỹ thuật viên:**  
   - Theo dõi danh sách công việc/hạng mục dịch vụ được phân công.
   - Cập nhật tiến độ sửa chữa theo thời gian thực (*Chờ sửa*, *Đang sửa*, *Chờ phụ tùng*, *Hoàn thành*).
   - Lập phiếu yêu cầu xuất kho phụ tùng gửi cho Thủ kho.

4. **Thủ kho:**  
   - Quản lý danh mục phụ tùng, đơn giá nhập/bán, ngưỡng cảnh báo tồn kho tối thiểu.
   - Lập phiếu nhập kho từ nhà cung cấp và cộng dồn số lượng tồn.
   - Duyệt và xử lý các yêu cầu xuất kho phụ tùng từ kỹ thuật viên.

---

## 🔄 3. Quy trình nghiệp vụ cốt lõi

```
[Khách đến Gara]
       │
       ▼
1. Tiếp nhận & Kiểm tra tình trạng xe (Lễ tân)
       │
       ▼
2. Lập Phiếu tiếp nhận & Phân công kỹ thuật viên (Lễ tân)
       │
       ▼
3. Chẩn đoán & Lập yêu cầu xuất kho phụ tùng (Kỹ thuật viên)
       │
       ▼
4. Duyệt xuất kho phụ tùng (Thủ kho)
       │
       ▼
5. Tiến hành sửa chữa & Cập nhật tiến độ hoàn thành (Kỹ thuật viên)
       │
       ▼
6. Tổng hợp chi phí, áp khuyến mãi & Lập hóa đơn (Lễ tân)
       │
       ▼
7. Xử lý thanh toán (Tiền mặt / VietQR / Công nợ)
       │
       ▼
8. Kiểm tra điều kiện xuất xưởng & In phiếu bàn giao xe (Lễ tân)
```

---

## ⚙️ 4. Các phân hệ chức năng chính

- **Xác thực & Phân quyền:** Đăng nhập an toàn, phân luồng giao diện theo vai trò, quản lý tài khoản và ghi vết Audit Trail (Who - When - Data Before/After).
- **Quản lý khách hàng & Hồ sơ xe:** Quản lý thông tin chủ xe (cá nhân/doanh nghiệp), định danh bằng số điện thoại/biển số, lưu trữ số VIN, hãng xe, đời xe và truy xuất toàn bộ lịch sử dịch vụ.
- **Vận hành tiếp nhận & Sửa chữa:** Ghi nhận số km, mức nhiên liệu, vết trầy xước, tài sản kèm theo; chỉ định danh mục tiền công & phụ tùng; gán thợ chịu trách nhiệm; theo dõi trạng thái luồng sửa chữa.
- **Quản lý phụ tùng & Kho vật tư:** Quản lý mã SKU, phân loại vị trí kho, quản lý nhà cung cấp, lập phiếu nhập kho, duyệt phiếu xuất, tự động trừ tồn kho và cảnh báo khi chạm ngưỡng tối thiểu.
- **Quản lý khuyến mãi:** Cấu hình giảm giá theo % hoặc tiền mặt cố định, áp dụng theo khoảng thời gian và phạm vi dịch vụ/tổng hóa đơn.
- **Hóa đơn & Thanh toán:** Tự động tổng hợp tiền công và phụ tùng, tính thuế VAT, sinh mã động VietQR chuyển khoản hoặc quản lý khách hàng ghi nhận công nợ.
- **Quản lý bàn giao xe:** Kiểm soát chặt chẽ điều kiện xe xong 100% và đã hoàn tất thanh toán trước khi in phiếu xuất xưởng (Gate Pass).
- **Báo cáo & Thống kê:** Biểu đồ doanh thu theo ngày/tuần/tháng, tỷ trọng tiền công vs tiền phụ tùng, báo cáo nhập - xuất - tồn, thống kê năng suất thợ; hỗ trợ xuất file Excel và PDF.

---

## 🛠️ 5. Định hướng công nghệ & Môi trường dự kiến

*(Thông tin sơ bộ ban đầu, sẽ được cập nhật cụ thể sau khi hoàn thiện source code)*

- **Nền tảng ứng dụng:** Java Desktop Application (Java Swing / Java AWT)
- **Kiến trúc mã nguồn:** Mô hình phân lớp DAO Pattern (`entity`, `dao`, `controller`, `gui`)
- **Hệ quản trị CSDL:** Microsoft SQL Server
- **Thư viện tích hợp dự kiến:**
  - `sqljdbc42`: Kết nối CSDL SQL Server qua JDBC.
  - `jfreechart`: Vẽ biểu đồ trực quan hóa dữ liệu thống kê doanh thu và báo cáo.
  - `itextpdf`: Tạo và xuất hóa đơn, phiếu tiếp nhận, phiếu xuất xưởng định dạng PDF.
  - `jdatepicker`: Giao diện chọn ngày tháng thuận tiện cho người dùng.

---

## 📅 6. Trạng thái dự án & Lưu ý

- **Trạng thái hiện tại:** Đã hoàn thiện tài liệu đặc tả yêu cầu, khảo sát nghiệp vụ, phân tích Use Case, thiết kế giao diện (UI Mockup) và thiết kế CSDL (EER / Class Diagram).
- **Lưu ý:** Bản README này là phần mô tả sơ bộ nghiệp vụ đề tài. Khi hoàn thành toàn bộ source code của dự án, README sẽ được bổ sung chi tiết về:
  - Sơ đồ kiến trúc hạ tầng hệ thống.
  - Hướng dẫn cấu hình môi trường phát triển (JDK, IDE, SQL Server Script).
  - Hướng dẫn cài đặt, build và chạy ứng dụng.
  - Tài khoản đăng nhập mẫu theo từng phân quyền.

---
*AUTORA Garage Management System © 2026 - Nhóm 13 (IUH)*
