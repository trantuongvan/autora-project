# GIAI ĐOẠN 1: BÁO CÁO VI PHẠM KIẾN TRÚC

**Ngày phân tích:** 2026-11-07  
**Branch:** `refactor/giai-doan-1-chuan-bi`  
**Tổng số Entity đã kiểm tra:** 33/33

---

## 📊 TỔNG QUAN THỐNG KÊ

| Mức độ | Số lượng | Entity |
|--------|----------|--------|
| 🔴 **Nghiêm trọng** | 5 | HoaDon, PhieuTiepNhanVaSuaChua, KhuyenMai, PhuTung, PhieuNhapKho |
| 🟡 **Cần cải thiện** | 2 | Thue, KhachHang |
| ✅ **Đạt chuẩn** | 26 | Các entity còn lại |

### Thống kê dòng code

| Entity | Dòng | Methods | Mức độ phình to |
|--------|------|---------|-----------------|
| PhieuTiepNhanVaSuaChua | 387 | 64 | 🔴 Quá lớn |
| HoaDon | 254 | 28 | 🟡 Lớn |
| KhuyenMai | 230 | 32 | 🟡 Lớn |
| PhuTung | 167 | 28 | ✅ Chấp nhận được |
| KhachHang | 163 | 27 | ✅ Chấp nhận được |

---

## 🔴 VI PHẠM NGHIÊM TRỌNG

### 1. HoaDon.java (254 dòng)

**VI PHẠM:** Có setter cho 5 trường số tiền tính toán

```java
// ❌ VI PHẠM: Setter cho giá trị tính toán
private void setTongTienCong(double tongTienCong)      // Dòng 102
private void setTongTienPhuTung(double tongTienPhuTung) // Dòng 110
private void setTongTienGiam(double tongTienGiam)      // Dòng 118
private void setTongTienVAT(double tongTienVAT)        // Dòng 126
private void setTongThanhToan(double tongThanhToan)    // Dòng 134

// ❌ VI PHẠM: Logic nghiệp vụ phức tạp
public void tinhTongThanhToan(double tienCong, double tienPhuTung) // Dòng 204
    - Tính giảm giá
    - Tính VAT
    - Gọi khuyenMai.tinhTienGiam()
    - Gọi thue.tinhTienThue()
    - Set 5 giá trị

public boolean daThanhToanHopLe(KhachHang khachHang)   // Dòng 225
public boolean laCongNoHopLe(KhachHang khachHang)      // Dòng 229
static double phanTram(double coSo, double tyLe)       // Dòng 234
```

**DÙNG Ở ĐÂU:**
- Constructor dòng 35-39: Gọi các setter
- `HoaDon_Dao.java`: Map từ ResultSet
- `QLHoaDonController.java`: Tính toán và set

**MỨC ĐỘ ƯU TIÊN:** 🔴 **RẤT CAO** - Vi phạm trực tiếp tài liệu dòng 175

**GIẢI PHÁP:**
```java
// ✅ Bỏ tất cả setter, chỉ giữ getter
public double getTongTienCong() { return tongTienCong; }
// KHÔNG có setTongTienCong()

// ✅ Constructor nhận giá trị đã tính
public HoaDon(String maHD, double tongTienCong, double tongTienPhuTung, 
              double tongTienGiam, double tongTienVAT, double tongThanhToan) {
    this.maHD = maHD;
    this.tongTienCong = tongTienCong;     // Gán thẳng
    this.tongTienPhuTung = tongTienPhuTung;
    this.tongTienGiam = tongTienGiam;
    this.tongTienVAT = tongTienVAT;
    this.tongThanhToan = tongThanhToan;
}

// ✅ Logic tính toán chuyển sang Controller
// QLHoaDonController.tinhToanHoaDon(...)
```

---

### 2. PhieuTiepNhanVaSuaChua.java (387 dòng - QUÁ LỚN)

**VI PHẠM:** Chứa 10+ method logic nghiệp vụ phức tạp

```java
// ❌ VI PHẠM: Logic nghiệp vụ có vòng lặp, điều kiện phức tạp
public int phanTramHoanTat()                         // Dòng 274
    - Tính toán: tongSoHangMuc == 0 ? 0 : ...

public boolean duDieuKienBanGiao()                   // Dòng 279
    - Gọi phanTramHoanTat()
    - Kiểm tra hoaDon
    - Gọi hoaDon.daThanhToanHopLe()

public String getTrangThaiHoaDonHienThi()            // Dòng 283
public String getNhanHoaDonBanGiao()                 // Dòng 287

public String lyDoChuaDuDieuKienBanGiao()            // Dòng 291
    - 20+ dòng logic if-else
    - Kiểm tra nhiều điều kiện
    - Format chuỗi phức tạp

public boolean coTheHoanThanh()                      // Dòng 311
    - Duyệt danhSachDichVu
    - Kiểm tra từng phần tử

public int phanTramTienDo()                          // Dòng 324
    - Logic if-else nhiều nhánh
    - Tính toán phần trăm

public String getTrangThaiHienThiKTV()               // Dòng 367

// ⚠️ CÓ THỂ GIỮ: Helper format đơn giản
public String getTenXe()                             // Dòng 347
public String getThongTinXeNgan()                    // Dòng 357
public String getThongTinXeDayDu()                   // Dòng 362
```

**MỨC ĐỘ ƯU TIÊN:** 🔴 **RẤT CAO** - Entity quá lớn, logic phức tạp

**GIẢI PHÁP:**
```java
// ✅ Entity chỉ giữ data
public class PhieuTiepNhanVaSuaChua {
    // Chỉ thuộc tính + getter/setter
    // Giữ lại: getTenXe(), getThongTinXeNgan() (helper đơn giản)
}

// ✅ Logic chuyển sang Controller
public class QLBanGiaoXeController {
    public boolean kiemTraDuDieuKienBanGiao(String maPhieu) {
        PhieuTiepNhanVaSuaChua phieu = dao.findById(maPhieu);
        HoaDon hoaDon = hoaDonDao.findByPhieu(maPhieu);
        
        // Logic ở đây
        int phanTramHoanTat = tinhPhanTramHoanTat(phieu);
        boolean daTra = hoaDon != null && hoaDon.daThanhToan();
        
        return phanTramHoanTat == 100 && daTra;
    }
    
    private int tinhPhanTramHoanTat(PhieuTiepNhanVaSuaChua phieu) {
        return phieu.getTongSoHangMuc() == 0 ? 0 
            : phieu.getSoHangMucHoanThanh() * 100 / phieu.getTongSoHangMuc();
    }
}
```

---

### 3. KhuyenMai.java (230 dòng)

**VI PHẠM:** Logic tính toán và vòng lặp trong Entity

```java
// ❌ VI PHẠM: Logic tính toán phức tạp
public double tinhTienGiam(double tienCong, double tienPhuTung) // Dòng 173
    - Gọi phamViApDung.coSoGiam()
    - Gọi hinhThuc.tinhTienGiam()
    - Dùng Math.min()

// ❌ VI PHẠM: Static method có vòng lặp for
public static KhuyenMai totNhat(List<KhuyenMai> hopLe, 
                                double tienCong, double tienPhuTung) // Dòng 179
    - Vòng lặp for qua List
    - So sánh giamMax
    - Tìm khuyến mãi tốt nhất

// ❌ VI PHẠM: Logic kiểm tra phức tạp
public String getTinhTrang(LocalDate homNay)                    // Dòng 192
    - Nhiều if-else
    - So sánh ngày
```

**MỨC ĐỘ ƯU TIÊN:** 🔴 **CAO** - Logic nên ở Controller

**GIẢI PHÁP:**
```java
// ✅ Entity chỉ giữ data + validation
public class KhuyenMai {
    // Chỉ thuộc tính + getter/setter
    public void kiemTraHopLe() { ... } // Giữ lại validation
}

// ✅ Logic chuyển sang Controller hoặc Service
public class QLHoaDonController {
    public double tinhTienGiam(KhuyenMai km, double tienCong, double tienPhuTung) {
        double coSo = km.getPhamViApDung().coSoGiam(tienCong, tienPhuTung);
        return Math.min(coSo, km.getHinhThuc().tinhTienGiam(coSo, km.getGiaTri()));
    }
    
    public KhuyenMai chonKhuyenMaiTotNhat(List<KhuyenMai> danhSach, 
                                          double tienCong, double tienPhuTung) {
        KhuyenMai tot = null;
        double giamMax = 0;
        for (KhuyenMai km : danhSach) {
            double giam = tinhTienGiam(km, tienCong, tienPhuTung);
            if (giam > giamMax) {
                giamMax = giam;
                tot = km;
            }
        }
        return tot;
    }
}
```

---

### 4. PhuTung.java (167 dòng)

**VI PHẠM:** Method thay đổi state và logic nghiệp vụ

```java
// ❌ VI PHẠM: Thay đổi state, nên ở DAO (transaction)
public void xuatKho(int soLuong)                     // Dòng 112
    - Kiểm tra tồn kho
    - Trừ soLuongTon

public void nhapKho(int soLuong, double giaNhapMoi)  // Dòng 128
    - Kiểm tra giá
    - Set giaNhap
    - Cộng soLuongTon

// ❌ VI PHẠM: Logic nghiệp vụ
public boolean giaNhapHopLe(double giaNhapMoi)       // Dòng 123
public int soLuongNhapDeXuat()                       // Dòng 141
    - Math.max()
    - Tính toán phức tạp

// ✅ CÓ THỂ GIỮ: Validation đơn giản
public boolean duTon(int soLuong)                    // Dòng 107
public boolean kiemTraCanhBaoTon()                   // Dòng 145
```

**MỨC ĐỘ ƯU TIÊN:** 🔴 **CAO** - Xuất nhập kho phải là transaction

**GIẢI PHÁP:**
```java
// ✅ Entity chỉ validation
public class PhuTung {
    // Bỏ xuatKho(), nhapKho()
    // Giữ: duTon(), kiemTraCanhBaoTon()
}

// ✅ Logic chuyển sang DAO (transaction an toàn)
public class PhuTung_Dao {
    public boolean xuatKho(Connection conn, String maSKU, int soLuong) 
        throws SQLException {
        String sql = "UPDATE PhuTung SET soLuongTon = soLuongTon - ? " +
                     "WHERE maSKU = ? AND soLuongTon >= ?";
        // PreparedStatement...
        return ps.executeUpdate() > 0;
    }
    
    public boolean nhapKho(Connection conn, String maSKU, int soLuong, 
                          double giaNhap) throws SQLException {
        // Kiểm tra giá trước
        PhuTung pt = findById(conn, maSKU);
        if (giaNhap > pt.getGiaBanLe()) {
            throw new SQLException("Giá nhập cao hơn giá bán");
        }
        
        String sql = "UPDATE PhuTung SET soLuongTon = soLuongTon + ?, " +
                     "giaNhap = ? WHERE maSKU = ?";
        // PreparedStatement...
    }
}
```

---

### 5. PhieuNhapKho.java (119 dòng)

**VI PHẠM:** Setter cho tổng tiền tính toán

```java
// ❌ VI PHẠM: Setter cho tổng tiền (nên tính từ chi tiết)
public void setTongTienNhap(double tongTienNhap)     // Dòng 59
    - Cho phép set tùy ý
    - Không đảm bảo = sum(chiTiet.thanhTien())
```

**DÙNG Ở ĐÂU:**
- Constructor dòng 31
- `PhieuNhapKho_Dao`: Map từ DB

**MỨC ĐỘ ƯU TIÊN:** 🔴 **TRUNG BÌNH** - Nên tính từ chi tiết

**GIẢI PHÁP:**
```java
// ✅ Bỏ setter, tính từ chi tiết
public class PhieuNhapKho {
    private double tongTienNhap;
    
    public double getTongTienNhap() { 
        // Tính từ danhSachChiTiet nếu có
        if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
            return danhSachChiTiet.stream()
                .mapToDouble(ChiTietNhapKho::thanhTien)
                .sum();
        }
        return tongTienNhap; // Fallback cho dữ liệu từ DB
    }
    
    // ❌ Bỏ setTongTienNhap()
}

// ✅ DAO tính tổng khi insert
public class PhieuNhapKho_Dao {
    public boolean insert(PhieuNhapKho phieu, List<ChiTietNhapKho> chiTiet) {
        double tongTien = chiTiet.stream()
            .mapToDouble(ct -> ct.getSoLuongNhap() * ct.getGiaNhapThucTe())
            .sum();
        
        String sql = "INSERT INTO PhieuNhapKho(..., tongTienNhap) VALUES (..., ?)";
        ps.setDouble(x, tongTien);
    }
}
```

---

## 🟡 CẦN CẢI THIỆN (Không bắt buộc ngay)

### 6. Thue.java (75 dòng)

**VẤN ĐỀ:** Logic tính toán trong Entity

```java
// ⚠️ Logic tính toán đơn giản
public double tinhTienThue(double tongTienCoSo)      // Dòng 57
    - Gọi HoaDon.phanTram()
    - 1 dòng code
```

**MỨC ĐỘ ƯU TIÊN:** 🟡 **THẤP** - Đơn giản, có thể giữ hoặc chuyển

---

### 7. KhachHang.java (163 dòng)

**VẤN ĐỀ:** Logic kiểm tra đơn giản

```java
// ⚠️ Logic đơn giản 1 dòng
public boolean duocThanhToanBang(HinhThucThanhToan hinhThuc) // Dòng 145
    - return hinhThuc != CONG_NO || duocGhiNo;
```

**MỨC ĐỘ ƯU TIÊN:** 🟡 **RẤT THẤP** - Có thể giữ lại

---

## ✅ LÀM ĐÚNG - KHÔNG CẦN SỬA

### Các Entity đạt chuẩn:

1. **ChiTietSuaChua_DichVu.java** (117 dòng) - ⭐ MẪU CHUẨN
   - Chỉ thuộc tính + getter/setter
   - Dùng object reference: PhieuTiepNhanVaSuaChua, DichVu, NhanVien
   - Dùng enum: TrangThaiTienDo
   - Helper đơn giản: getThoHienThi()

2. **DichVu.java** (90 dòng) - ✅ ĐÚNG
   - setTienCong() là ĐƯỢCphép vì đây là giá dịch vụ, không phải tổng tiền tính toán

3. **ChiTietSuaChua_PhuTung.java** (99 dòng) - ✅ ĐÚNG
   - setGiaBanThucTe() là ĐƯỢCphép (giá đơn vị)
   - thanhTien() = soLuong * gia - helper đơn giản, CÓ THỂ GIỮ

4. **ChiTietNhapKho.java** (77 dòng) - ✅ ĐÚNG
   - setGiaNhapThucTe() là ĐƯỢC phép (giá đơn vị)
   - thanhTien() = soLuong * gia - helper đơn giản, CÓ THỂ GIỮ

5. **Các Entity enum** - ✅ ĐÚNG HOÀN TOÀN
   - TrangThaiPhieu, TrangThaiHoaDon, TrangThaiTienDo
   - HinhThucThanhToan, HinhThucKhuyenMai
   - VaiTro, LoaiKhachHang, MucUuTien, etc.

6. **Các Entity đơn giản** - ✅ ĐÚNG
   - NhanVien, Xe, TaiKhoan, NhaCungCap
   - CaLamViec, LichHen, NhatKyHoatDong
   - ChiTietYeuCauXuatKho, PhieuYeuCauXuatKho, PhieuBanGiao
   - KyNang

---

## 🎯 KẾT LUẬN GIAI ĐOẠN 1

### Tổng kết số liệu

| Loại vi phạm | Số lượng | Entity |
|--------------|----------|--------|
| **Setter cho số tiền tính toán** | 2 | HoaDon (5 setter), PhieuNhapKho (1 setter) |
| **Logic nghiệp vụ phức tạp** | 3 | PhieuTiepNhanVaSuaChua, KhuyenMai, PhuTung |
| **Logic đơn giản có thể giữ** | 2 | Thue, KhachHang |
| **Đạt chuẩn hoàn toàn** | 26 | Còn lại |

### Phân loại theo mức độ ưu tiên

#### 🔴 BẮT BUỘC SỬA NGAY (5 entity)

1. **HoaDon.java** - Bỏ 5 setter, chuyển logic sang Controller
2. **PhieuTiepNhanVaSuaChua.java** - Chuyển 10+ method sang Controller
3. **KhuyenMai.java** - Chuyển tinhTienGiam(), totNhat() sang Controller
4. **PhuTung.java** - Chuyển xuatKho(), nhapKho() sang DAO
5. **PhieuNhapKho.java** - Bỏ setTongTienNhap(), tính từ chi tiết

#### 🟡 CÓ THỂ SỬA SAU (2 entity)

6. **Thue.java** - tinhTienThue() có thể giữ (logic đơn giản)
7. **KhachHang.java** - duocThanhToanBang() có thể giữ (1 dòng)

#### ✅ KHÔNG CẦN SỬA (26 entity)

Các entity còn lại đã làm đúng chuẩn kiến trúc 3 lớp.

---

## 📋 DANH SÁCH CÔNG VIỆC TIẾP THEO

Chuyển sang **GIAI ĐOẠN 2: REFACTOR HoaDon.java**

### Checklist

- [x] Phân tích toàn bộ 33 entity
- [x] Tìm tất cả setter cho số tiền
- [x] Tìm tất cả logic nghiệp vụ phức tạp
- [x] Phân loại mức độ vi phạm
- [x] Lập kế hoạch ưu tiên
- [ ] Backup code (commit)
- [ ] Bắt đầu refactor HoaDon.java

---

## 📌 LƯU Ý

### Các setter ĐƯỢC PHÉP (không vi phạm)

```java
// ✅ ĐÚNG: Setter cho giá đơn vị (không phải tổng tiền tính toán)
DichVu.setTienCong(double)              // Giá dịch vụ
PhuTung.setGiaNhap(double)              // Giá nhập 1 phụ tùng
PhuTung.setGiaBanLe(double)             // Giá bán 1 phụ tùng
KhuyenMai.setGiaTri(double)             // Giá trị khuyến mãi

ChiTietSuaChua_DichVu.setTienCongThucTe(double)    // Giá thực tế 1 dịch vụ
ChiTietSuaChua_PhuTung.setGiaBanThucTe(double)     // Giá bán thực tế 1 phụ tùng
ChiTietNhapKho.setGiaNhapThucTe(double)            // Giá nhập thực tế 1 phụ tùng
```

### Các setter VI PHẠM (phải bỏ)

```java
// ❌ SAI: Setter cho số tiền tính toán từ nhiều nguồn
HoaDon.setTongTienCong(double)          // = SUM(ChiTiet.tienCong)
HoaDon.setTongTienPhuTung(double)       // = SUM(ChiTiet.tienPhuTung)
HoaDon.setTongTienGiam(double)          // = Tính từ khuyến mãi
HoaDon.setTongTienVAT(double)           // = Tính từ thuế suất
HoaDon.setTongThanhToan(double)         // = Tổng - Giảm + VAT

PhieuNhapKho.setTongTienNhap(double)    // = SUM(ChiTiet.thanhTien)
```

### Quy tắc phân biệt

**Setter ĐƯỢC PHÉP khi:**
- Là giá đơn vị của 1 sản phẩm/dịch vụ
- Người dùng nhập trực tiếp
- Không phụ thuộc vào dữ liệu khác

**Setter VI PHẠM khi:**
- Là tổng tiền từ nhiều dòng chi tiết
- Phải tính toán từ nhiều nguồn
- Phụ thuộc vào công thức phức tạp

---

**Người phân tích:** Claude Code  
**Thời gian:** 2026-11-07 07:24 UTC  
**Branch:** `refactor/giai-doan-1-chuan-bi`

---

## 🚀 BƯỚC TIẾP THEO

Commit giai đoạn 1 và chuyển sang giai đoạn 2:

```bash
git add .
git commit -m "docs: Giai đoạn 1 - Phân tích và báo cáo vi phạm kiến trúc

- Phân tích 33 entity files
- Phát hiện 5 vi phạm nghiêm trọng
- Lập kế hoạch refactor chi tiết
- Tạo báo cáo đầy đủ"

git checkout -b refactor/giai-doan-2-hoa-don
```
