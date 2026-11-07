# KẾ HOẠCH REFACTOR KIẾN TRÚC - DỰ ÁN AUTORA

## 📋 Tổng quan vấn đề

Sau khi phân tích toàn bộ 127 file Java trong dự án, phát hiện **vi phạm nghiêm trọng nguyên tắc kiến trúc 3 lớp**:

- ✅ **Đúng**: Cấu trúc thư mục rõ ràng (entity, dao, controller, gui)
- ❌ **Sai**: Entity chứa quá nhiều logic nghiệp vụ phức tạp
- ❌ **Sai**: Setter cho các giá trị tính toán (tiền bạc)
- ❌ **Sai**: Thiếu lớp Service/Business Logic cho nghiệp vụ phức tạp

---

## ⚠️ VẤN ĐỀ 1: ENTITY CHỨA LOGIC NGHIỆP VỤ PHỨC TẠP

### 1.1. PhieuTiepNhanVaSuaChua.java - **VI PHẠM NGHIÊM TRỌNG**

**Các method không thuộc Entity:**
```java
// ❌ Logic tính toán phức tạp - phải chuyển sang Controller
public int phanTramHoanTat()
public boolean duDieuKienBanGiao()
public String lyDoChuaDuDieuKienBanGiao()
public boolean coTheHoanThanh()
public int phanTramTienDo()
public String getTrangThaiHoaDonHienThi()
public String getNhanHoaDonBanGiao()
public String getTrangThaiHienThiKTV()
```

**Nguyên nhân vi phạm:**
- Các method này chứa logic nghiệp vụ phức tạp: duyệt list, kiểm tra điều kiện nhiều lớp
- Làm Entity phình to 388 dòng
- Trộn lẫn data holder với business rules

**Giải pháp:**
```java
// ✅ Entity CHỈ giữ data và getter/setter
public class PhieuTiepNhanVaSuaChua {
    private String maPhieuTNSC;
    private Xe xe;
    private NhanVien nhanVienLap;
    private TrangThaiPhieu trangThai;
    // ... chỉ thuộc tính + getter/setter
}

// ✅ Logic chuyển sang Controller hoặc Service
public class PhieuSuaChuaService {
    public int tinhPhanTramHoanTat(PhieuTiepNhanVaSuaChua phieu) {
        // logic ở đây
    }
    
    public boolean kiemTraDuDieuKienBanGiao(PhieuTiepNhanVaSuaChua phieu, HoaDon hoaDon) {
        // logic ở đây
    }
}
```

---

### 1.2. HoaDon.java - **VI PHẠM NGHIÊM TRỌNG**

**Setter cho số tiền - VI PHẠM QUY CHUẨN:**
```java
// ❌ Tiền không được set, phải tính toán
private void setTongTienCong(double tongTienCong)
private void setTongTienPhuTung(double tongTienPhuTung)
private void setTongTienGiam(double tongTienGiam)
private void setTongTienVAT(double tongTienVAT)
private void setTongThanhToan(double tongThanhToan)

// ❌ Logic tính toán phức tạp trong Entity
public void tinhTongThanhToan(double tienCong, double tienPhuTung)
public boolean daThanhToanHopLe(KhachHang khachHang)
public boolean laCongNoHopLe(KhachHang khachHang)
static double phanTram(double coSo, double tyLe)
```

**Theo tài liệu kiến trúc (dòng 175):**
> "thứ 2 là chỉ có getter setter, nhưng không được set các số tiền vì nghiệp vụ nó không đúng, tiền thì không được set mà phải được tính toán ra"

**Giải pháp:**
```java
// ✅ Entity CHỈ có getter cho tiền, KHÔNG có setter
public class HoaDon {
    private String maHD;
    private PhieuTiepNhanVaSuaChua phieu;
    private HinhThucThanhToan hinhThucThanhToan;
    private TrangThaiHoaDon trangThaiThanhToan;
    
    // Các giá trị tính toán - CHỈ có getter
    private double tongTienCong;
    private double tongTienPhuTung;
    private double tongTienGiam;
    private double tongTienVAT;
    private double tongThanhToan;
    
    public double getTongTienCong() { return tongTienCong; }
    // ❌ KHÔNG có setTongTienCong()
}

// ✅ Tính toán ở Controller
public class QLHoaDonController {
    public void lapHoaDon(String maPhieu) {
        // Lấy dữ liệu
        double tienCong = tinhTongTienCong(maPhieu);
        double tienPhuTung = tinhTongTienPhuTung(maPhieu);
        
        // Tính toán
        double giam = khuyenMai == null ? 0 : tinhTienGiam(tienCong, tienPhuTung);
        double vat = thue == null ? 0 : tinhVAT(tienCong + tienPhuTung - giam);
        double tongTien = tienCong + tienPhuTung - giam + vat;
        
        // Lưu vào DB qua DAO
        dao.insert(maHD, tienCong, tienPhuTung, giam, vat, tongTien);
    }
}
```

---

### 1.3. PhuTung.java - **VI PHẠM VỪA PHẢI**

**Logic nghiệp vụ xuất nhập kho:**
```java
// ❌ Logic nghiệp vụ không nên ở Entity
public void xuatKho(int soLuong)
public void nhapKho(int soLuong, double giaNhapMoi)
public boolean giaNhapHopLe(double giaNhapMoi)
public int soLuongNhapDeXuat()
```

**Đánh giá:**
- Logic đơn giản hơn `PhieuTiepNhanVaSuaChua` và `HoaDon`
- Nhưng vẫn vi phạm: Entity không nên thay đổi state phức tạp
- Việc xuất/nhập kho nên là transaction ở DAO

**Giải pháp:**
```java
// ✅ Entity chỉ có data validation
public class PhuTung {
    private String maSKU;
    private int soLuongTon;
    
    public boolean duTon(int soLuong) {
        return soLuongTon >= soLuong;
    }
    
    public boolean kiemTraCanhBaoTon() {
        return soLuongTon <= nguongCanhBao;
    }
}

// ✅ Logic xuất nhập kho ở DAO (transaction)
public class PhuTung_Dao {
    public boolean xuatKho(String maSKU, int soLuong) throws SQLException {
        String sql = "UPDATE PhuTung SET soLuongTon = soLuongTon - ? WHERE maSKU = ? AND soLuongTon >= ?";
        // PreparedStatement...
    }
    
    public boolean nhapKho(String maSKU, int soLuong, double giaNhap) throws SQLException {
        String sql = "UPDATE PhuTung SET soLuongTon = soLuongTon + ?, giaNhap = ? WHERE maSKU = ?";
        // PreparedStatement...
    }
}
```

---

### 1.4. KhachHang.java - **VI PHẠM NHẸ**

**Logic kiểm tra nghiệp vụ:**
```java
// ⚠️ Logic đơn giản nhưng nên cân nhắc chuyển
public boolean duocThanhToanBang(HinhThucThanhToan hinhThuc) {
    return hinhThuc != HinhThucThanhToan.CONG_NO || duocGhiNo;
}
```

**Đánh giá:**
- Logic đơn giản, chỉ 1 dòng
- Có thể giữ lại HOẶC chuyển sang Controller
- Ưu tiên: **MỨC ĐỘ THẤP** (không bắt buộc)

---

## ⚠️ VẤN ĐỀ 2: KHAI BÁO THUỘC TÍNH KHÔNG THEO OOP

### 2.1. Các entity ĐÃ LÀM ĐÚNG

✅ **ChiTietSuaChua_DichVu.java** - Mẫu chuẩn:
```java
public class ChiTietSuaChua_DichVu {
    private PhieuTiepNhanVaSuaChua phieu;  // ✅ Dùng object reference
    private DichVu dichVu;                   // ✅ Dùng object reference
    private NhanVien kyThuatVien;           // ✅ Dùng object reference
    private TrangThaiTienDo trangThaiTienDo; // ✅ Dùng enum
}
```

✅ **DichVu.java, PhuTung.java** - Đã làm đúng OOP

### 2.2. Cần kiểm tra thêm

Cần đọc chi tiết tất cả 33 entity để tìm các trường hợp:
- ❌ Khai báo `private String maNhanVien` thay vì `private NhanVien nhanVien`
- ❌ Khai báo `private String maKhachHang` thay vì `private KhachHang khachHang`
- ❌ Khai báo `private String trangThai` thay vì `private TrangThaiEnum trangThai`

---

## 📊 PHÂN LOẠI MỨC ĐỘ ƯU TIÊN

### 🔴 NGHIÊM TRỌNG - BẮT BUỘC SỬA NGAY

| Entity | Vấn đề | Số dòng | Độ phức tạp |
|--------|--------|---------|-------------|
| `HoaDon.java` | Setter cho số tiền tính toán | 254 | Cao |
| `PhieuTiepNhanVaSuaChua.java` | 10+ method logic nghiệp vụ | 388 | Rất cao |
| `PhuTung.java` | Logic xuất nhập kho | 168 | Trung bình |

### 🟡 CẦN CẢI THIỆN

| Entity | Vấn đề | Độ ưu tiên |
|--------|--------|-----------|
| `KhachHang.java` | Logic kiểm tra thanh toán đơn giản | Thấp |
| Các entity khác | Cần review để tìm vi phạm | Trung bình |

---

## 🛠️ KẾ HOẠCH REFACTOR CHI TIẾT

### GIAI ĐOẠN 1: CHUẨN BỊ (1-2 giờ)

**Bước 1.1: Review toàn bộ 33 Entity**
```bash
# Tìm các setter cho tiền bạc
grep -r "setTong\|setTien\|setGia" src/entity/

# Tìm các method có logic phức tạp (có if, for, while)
grep -r "public.*{" src/entity/ | grep -E "if|for|while"
```

**Bước 1.2: Tạo danh sách vi phạm**
- Lập bảng Excel: File | Method | Loại vi phạm | Giải pháp

**Bước 1.3: Backup code**
```bash
git checkout -b refactor/entity-cleanup
git commit -m "Backup before entity refactoring"
```

---

### GIAI ĐOẠN 2: REFACTOR HoaDon (3-4 giờ)

**Công việc:**

1. **Bỏ setter cho các số tiền**
```java
// XÓA các method này
- private void setTongTienCong(double tongTienCong)
- private void setTongTienPhuTung(double tongTienPhuTung)
- private void setTongTienGiam(double tongTienGiam)
- private void setTongTienVAT(double tongTienVAT)
- private void setTongThanhToan(double tongThanhToan)
```

2. **Chuyển logic tính toán sang Controller**
```java
// Di chuyển từ HoaDon.tinhTongThanhToan() 
// sang QLHoaDonController.tinhToanHoaDon()
```

3. **Sửa tất cả nơi gọi HoaDon**
- Tìm: `hoaDon.setTongTienCong(`
- Thay: Tính toán ở Controller, truyền vào constructor hoặc qua DAO

4. **Sửa Constructor**
```java
// ✅ Constructor nhận đầy đủ giá trị đã tính
public HoaDon(String maHD, double tongTienCong, double tongTienPhuTung, 
              double tongTienGiam, double tongTienVAT, double tongThanhToan) {
    this.maHD = maHD;
    this.tongTienCong = tongTienCong;  // Gán thẳng, không qua setter
    // ...
}
```

5. **Sửa HoaDon_Dao**
```java
// DAO INSERT nhận các giá trị đã tính từ Controller
public boolean insert(String maHD, String maPhieu, double tienCong, 
                     double tienPhuTung, double tienGiam, double vat, double tongTien)
```

**File cần sửa:**
- `entity/HoaDon.java`
- `dao/HoaDon_Dao.java`
- `controller/QLHoaDonController.java`
- `controller/ChiTietHoaDonController.java`
- `controller/ThanhToanController.java` (nếu có)

---

### GIAI ĐOẠN 3: REFACTOR PhieuTiepNhanVaSuaChua (4-5 giờ)

**Công việc:**

1. **Tạo Service class mới** (nếu cần)
```java
public class PhieuSuaChuaService {
    private PhieuTiepNhanVaSuaChua_Dao phieuDao;
    private ChiTietSuaChua_DichVu_Dao chiTietDao;
    
    public int tinhPhanTramHoanTat(String maPhieu) {
        // Logic từ PhieuTiepNhanVaSuaChua.phanTramHoanTat()
    }
    
    public boolean kiemTraDuDieuKienBanGiao(String maPhieu) {
        // Logic từ PhieuTiepNhanVaSuaChua.duDieuKienBanGiao()
    }
    
    public String layLyDoKhongBanGiao(String maPhieu) {
        // Logic từ PhieuTiepNhanVaSuaChua.lyDoChuaDuDieuKienBanGiao()
    }
}
```

2. **Di chuyển logic ra khỏi Entity**
```java
// XÓA khỏi PhieuTiepNhanVaSuaChua.java
- public int phanTramHoanTat()
- public boolean duDieuKienBanGiao()
- public String lyDoChuaDuDieuKienBanGiao()
- public boolean coTheHoanThanh()
- public int phanTramTienDo()
```

3. **Giữ lại các helper đơn giản**
```java
// ✅ CÓ THỂ GIỮ - logic format/display đơn giản
public String getTenXe()
public String getThongTinXeNgan()
public String getThongTinXeDayDu()
```

4. **Sửa tất cả Controller**
- `QLBanGiaoXeController`: Gọi Service thay vì phieu.duDieuKienBanGiao()
- `QLPhieuSuaChuaController`: Gọi Service thay vì phieu.phanTramHoanTat()

**File cần sửa:**
- `entity/PhieuTiepNhanVaSuaChua.java`
- NEW: `service/PhieuSuaChuaService.java` (hoặc logic vào Controller)
- `controller/QLBanGiaoXeController.java`
- `controller/ChiTietPhieuSuaChuaController.java`
- `controller/QLTienDoSuaChuaController.java`

---

### GIAI ĐOẠN 4: REFACTOR PhuTung (2-3 giờ)

**Công việc:**

1. **Chuyển xuatKho() sang DAO**
```java
// XÓA khỏi PhuTung.java
- public void xuatKho(int soLuong)
- public void nhapKho(int soLuong, double giaNhapMoi)

// THÊM vào PhuTung_Dao.java
public boolean xuatKho(Connection conn, String maSKU, int soLuong) throws SQLException
public boolean nhapKho(Connection conn, String maSKU, int soLuong, double giaNhap) throws SQLException
```

2. **Giữ lại validation logic**
```java
// ✅ CÓ THỂ GIỮ - validation đơn giản
public boolean duTon(int soLuong)
public boolean kiemTraCanhBaoTon()
```

**File cần sửa:**
- `entity/PhuTung.java`
- `dao/PhuTung_Dao.java`
- `controller/QLDuyetXuatKhoController.java`
- `controller/QLNhapKhoController.java`

---

### GIAI ĐOẠN 5: REVIEW 30 ENTITY CÒN LẠI (3-4 giờ)

**Checklist cho mỗi Entity:**

```
□ Chỉ có thuộc tính private
□ Dùng object reference thay vì String (maNhanVien → NhanVien)
□ Có getter cho mọi thuộc tính
□ Có setter với validation (trừ số tiền tính toán)
□ KHÔNG có setter cho: tongTien, tongGiam, tongVAT, tienThanhToan, etc.
□ KHÔNG có method logic nghiệp vụ phức tạp (có if/for/while nhiều tầng)
□ CÓ THỂ có: format/display helper, validation đơn giản
□ Có equals(), hashCode(), toString()
□ Constructor rỗng, constructor theo khóa, constructor đầy đủ
```

**Ưu tiên review:**
1. `Xe.java`, `NhanVien.java`, `TaiKhoan.java`
2. `PhieuYeuCauXuatKho.java`, `PhieuNhapKho.java`
3. `KhuyenMai.java`, `Thue.java`
4. Các ChiTietXxx.java

---

### GIAI ĐOẠN 6: KIỂM THỬ (2-3 giờ)

**Test cases:**

1. **Test Entity thuần túy**
```java
@Test
public void testHoaDonKhongChoSetTien() {
    HoaDon hd = new HoaDon();
    // Không còn method setTongTienCong() nữa
    // Chỉ có thể set qua constructor
}
```

2. **Test Controller tính toán**
```java
@Test
public void testTinhToanHoaDon() {
    // Test logic đã chuyển sang Controller
}
```

3. **Test tích hợp**
- Lập hóa đơn → Kiểm tra số tiền đúng
- Xuất kho → Kiểm tra tồn kho giảm
- Bàn giao xe → Kiểm tra điều kiện

4. **Test UI**
- Mở màn hình Hóa đơn → Không bị crash
- Lập hóa đơn mới → Hiển thị đúng
- Thanh toán → Cập nhật trạng thái

---

## 📏 QUY CHUẨN ENTITY SAU REFACTOR

### ✅ CẤU TRÚC CHUẨN

```java
package entity;

import java.util.Objects;

/**
 * Entity POJO theo chuẩn kiến trúc 3 lớp.
 * - CHỈ chứa dữ liệu + getter/setter + validation
 * - KHÔNG chứa logic nghiệp vụ phức tạp
 * - KHÔNG có setter cho giá trị tính toán (tiền bạc)
 */
public class TenEntity {
    
    // 1. THUỘC TÍNH PRIVATE
    private String ma;
    private String ten;
    private EntityKhac entityKhac;  // ✅ Dùng object reference, không dùng String
    private EnumTrangThai trangThai; // ✅ Dùng enum, không dùng String
    
    // Giá trị tính toán - CHỈ có getter
    private double tongTien;  // ❌ KHÔNG có setTongTien()
    
    // 2. CONSTRUCTOR
    public TenEntity() {}
    
    public TenEntity(String ma) {
        setMa(ma);
    }
    
    public TenEntity(String ma, String ten, EntityKhac entityKhac) {
        setMa(ma);
        setTen(ten);
        setEntityKhac(entityKhac);
    }
    
    // 3. GETTER/SETTER VỚI VALIDATION
    public String getMa() {
        return ma;
    }
    
    public void setMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã không được trống");
        }
        this.ma = ma.trim();
    }
    
    // 4. GETTER CHO GIÁ TRỊ TÍNH TOÁN (KHÔNG CÓ SETTER)
    public double getTongTien() {
        return tongTien;
    }
    // ❌ KHÔNG có setTongTien() - giá trị này tính ở Controller
    
    // 5. HELPER ĐƠN GIẢN (CÓ THỂ CÓ)
    /** Format hiển thị trên UI */
    public String getHienThi() {
        return ma + " - " + ten;
    }
    
    /** Validation đơn giản không cần DB */
    public boolean hopLe() {
        return ma != null && ten != null;
    }
    
    // 6. EQUALS, HASHCODE, TOSTRING
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenEntity)) return false;
        TenEntity that = (TenEntity) o;
        return Objects.equals(ma, that.ma);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ma);
    }
    
    @Override
    public String toString() {
        return "TenEntity{ma='" + ma + "', ten='" + ten + "'}";
    }
}
```

### ❌ KHÔNG ĐƯỢC LÀM

```java
// ❌ 1. Setter cho giá trị tính toán
public void setTongTien(double tongTien) { ... }
public void setTongGiam(double tongGiam) { ... }

// ❌ 2. Logic nghiệp vụ phức tạp
public double tinhTongThanhToan() {
    double tong = 0;
    for (ChiTiet ct : danhSach) {
        if (ct.getTrangThai() == ...) {
            tong += ct.getTien() * (1 - ct.getGiam()/100);
        }
    }
    return tong;
}

// ❌ 3. Gọi DAO trong Entity
public void luuVaoDB() {
    new SomeDao().insert(this);
}

// ❌ 4. Khai báo String thay vì object reference
private String maNhanVien;  // ❌ SAI
private NhanVien nhanVien;  // ✅ ĐÚNG
```

---

## 🎯 TIÊU CHÍ HOÀN THÀNH

### Mỗi Entity phải đạt:
- [ ] Dưới 150 dòng (trừ Entity phức tạp có lý do)
- [ ] Không có method nghiệp vụ phức tạp (>5 dòng logic)
- [ ] Không có setter cho số tiền tính toán
- [ ] Dùng object reference thay vì String cho FK
- [ ] Có đầy đủ: constructor, getter/setter, equals, hashCode, toString

### Toàn bộ dự án:
- [ ] Tất cả 33 Entity đều tuân thủ chuẩn
- [ ] Không có warning khi compile
- [ ] Tất cả test case passed
- [ ] UI chạy bình thường, không crash
- [ ] Commit và push lên branch refactor

---

## ⏱️ THỜI GIAN ƯỚC TÍNH

| Giai đoạn | Thời gian | Độ khó |
|-----------|-----------|---------|
| 1. Chuẩn bị & Review | 1-2 giờ | Dễ |
| 2. Refactor HoaDon | 3-4 giờ | Khó |
| 3. Refactor PhieuTiepNhanVaSuaChua | 4-5 giờ | Rất khó |
| 4. Refactor PhuTung | 2-3 giờ | Trung bình |
| 5. Review 30 Entity còn lại | 3-4 giờ | Trung bình |
| 6. Kiểm thử | 2-3 giờ | Trung bình |
| **TỔNG** | **15-21 giờ** | **~ 2-3 ngày** |

---

## 📌 LƯU Ý QUAN TRỌNG

### 1. Không refactor tất cả cùng lúc
- Ưu tiên: `HoaDon` → `PhieuTiepNhanVaSuaChua` → `PhuTung` → Còn lại
- Commit sau mỗi Entity xong
- Test ngay sau mỗi Entity

### 2. Giữ lại các helper hợp lý
```java
// ✅ CÓ THỂ GIỮ trong Entity
- Format/Display: getTenXe(), getHienThi()
- Validation đơn giản: hopLe(), duocPhepXuat()
- Getter phái sinh đơn giản: getTenDayDu() = ho + " " + ten
```

### 3. Tạo Service nếu logic quá phức tạp
- Nếu method có >10 dòng logic nghiệp vụ
- Nếu phải gọi nhiều DAO khác nhau
- Nếu có transaction phức tạp

### 4. Không sửa Database
- Refactor này KHÔNG đụng DB
- Chỉ sửa code Java
- DAO vẫn dùng SQL như cũ

---

## 🔍 SO SÁNH VỚI PROJECT KHÁCH SẠN

Đã scan qua project `E:\PTUD_AnhTuong\N02\N02_PTUD_QuanLyKhachSan`:

**Điểm tương đồng:**
- Cấu trúc thư mục giống nhau (entity, dao, controller, gui)
- Cũng có vấn đề khai báo String thay vì object reference

**Điểm khác:**
- Project khách sạn Entity đơn giản hơn (ít logic nghiệp vụ)
- AUTORA phức tạp hơn nhiều (gara có nhiều nghiệp vụ liên quan)

**Bài học:**
- Cần áp dụng nghiêm ngặt hơn project khách sạn
- AUTORA không thể "gọn gàng" nếu không tách logic ra khỏi Entity

---

## 🚀 CÂU HỎI TRƯỚC KHI BẮT ĐẦU

1. **Có cần tạo package `service`?**
   - Có: Nếu logic phức tạp (recommend)
   - Không: Logic vào Controller (đơn giản hơn)

2. **Có sửa tất cả Entity hay chỉ ưu tiên?**
   - Recommend: Ưu tiên 3 Entity nghiêm trọng trước
   - Sau đó review từng Entity còn lại

3. **Có cần viết Unit Test?**
   - Recommend: Có (ít nhất test Controller)
   - Minimum: Test thủ công qua UI

4. **Bao giờ merge vào main?**
   - Sau khi tất cả test pass
   - Sau khi cả nhóm review code
   - Backup DB trước khi merge

---

## ✅ BƯỚC TIẾP THEO

**Bạn cần quyết định:**

1. Có đồng ý với kế hoạch này?
2. Có muốn tôi bắt đầu refactor ngay?
3. Bắt đầu với Entity nào? (recommend: `HoaDon`)
4. Có muốn tạo package `service` không?

**Ra lệnh để bắt đầu:**
```
"Đồng ý, bắt đầu refactor HoaDon.java"
hoặc
"Chờ, tôi cần review thêm"
```

---

*Tài liệu này được tạo tự động bởi Claude Code sau khi phân tích 127 file Java trong dự án AUTORA.*
