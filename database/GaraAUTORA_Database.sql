USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'GaraAUTORA')
BEGIN
    ALTER DATABASE GaraAUTORA SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE GaraAUTORA;
END
GO

CREATE DATABASE GaraAUTORA;
GO

USE GaraAUTORA;
GO

-- ==============================================================
-- 1. TẠO CÁC BẢNG DANH MỤC ĐỘC LẬP (BÊN 1)
-- ==============================================================

-- 1.1. Bảng Nhân viên
CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15) NOT NULL UNIQUE,
    diaChi NVARCHAR(255),
    viTriLamViec NVARCHAR(50) NOT NULL -- 'Chủ cửa hàng', 'Lễ tân', 'Kỹ thuật viên', 'Thủ kho'
);
GO

-- 1.2. Bảng Khách hàng
CREATE TABLE KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15) NOT NULL UNIQUE,
    diaChi NVARCHAR(255),
    email VARCHAR(100),
    maSoThue VARCHAR(50),
    loaiKhachHang NVARCHAR(50) DEFAULT N'Khách vãng lai', -- 'Khách vãng lai', 'Thành viên', 'VIP'
    duocGhiNo BIT DEFAULT 0 -- 1: Cho phép ghi nợ, 0: Không
);
GO

-- 1.3. Bảng Danh mục Dịch vụ
CREATE TABLE DichVu (
    maDichVu VARCHAR(20) PRIMARY KEY,
    tenDichVu NVARCHAR(150) NOT NULL,
    tienCong FLOAT NOT NULL DEFAULT 0
);
GO

-- 1.4. Bảng Danh mục Phụ tùng
CREATE TABLE PhuTung (
    maSKU VARCHAR(30) PRIMARY KEY,
    tenPhuTung NVARCHAR(150) NOT NULL,
    hangSanXuat NVARCHAR(100),
    viTriLuuKho NVARCHAR(50),
    giaNhap FLOAT NOT NULL DEFAULT 0,
    giaBanLe FLOAT NOT NULL DEFAULT 0,
    soLuongTon INT NOT NULL DEFAULT 0,
    nguongCanhBao INT NOT NULL DEFAULT 5
);
GO

-- 1.5. Bảng Nhà cung cấp
CREATE TABLE NhaCungCap (
    maNCC VARCHAR(20) PRIMARY KEY,
    tenNCC NVARCHAR(150) NOT NULL,
    soDienThoai VARCHAR(15),
    diaChi NVARCHAR(255)
);
GO

-- 1.6. Bảng Khuyến mãi
CREATE TABLE KhuyenMai (
    maKM VARCHAR(20) PRIMARY KEY,
    tenChuongTrinh NVARCHAR(150) NOT NULL,
    hinhThuc NVARCHAR(50) NOT NULL, -- 'Giảm %' hoặc 'Giảm tiền mặt'
    giaTri FLOAT NOT NULL DEFAULT 0,
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianKetThuc DATETIME NOT NULL,
    trangThai BIT DEFAULT 1
);
GO

-- 1.7. Bảng Thuế
CREATE TABLE Thue (
    maThue VARCHAR(20) PRIMARY KEY,
    tenLoaiThue NVARCHAR(100) NOT NULL,
    thueSuat FLOAT NOT NULL DEFAULT 8.0 -- % (Ví dụ: 8.0 hoặc 10.0)
);
GO

-- ==============================================================
-- 2. TẠO CÁC BẢNG CÓ QUAN HỆ KHÓA NGOẠI (BÊN N)
-- ==============================================================

-- 2.1. Bảng Tài khoản
CREATE TABLE TaiKhoan (
    tenDangNhap VARCHAR(50) PRIMARY KEY,
    matKhau VARCHAR(255) NOT NULL,
    vaiTro NVARCHAR(50) NOT NULL,
    trangThai BIT DEFAULT 1,
    maNV VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV) ON DELETE CASCADE
);
GO

-- 2.2. Bảng Xe
CREATE TABLE Xe (
    bienSo VARCHAR(20) PRIMARY KEY,
    soVIN VARCHAR(50), -- Số khung / Số VIN (17 ký tự)
    hangXe NVARCHAR(50) NOT NULL,
    dongXe NVARCHAR(50),
    doiXe INT, -- Năm sản xuất (VD: 2022)
    mauSon NVARCHAR(50), -- Màu sơn xe
    maKH VARCHAR(20) NOT NULL,
    CONSTRAINT FK_Xe_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

-- ==============================================================
-- 3. TẠO CÁC BẢNG NGHIỆP VỤ CHÍNH
-- ==============================================================

-- 3.1. Bảng Phiếu Tiếp Nhận & Sửa Chữa
CREATE TABLE PhieuTiepNhanVaSuaChua (
    maPhieuTNSC VARCHAR(30) PRIMARY KEY,
    yeuCauBanDau NVARCHAR(500),
    soKm INT DEFAULT 0,
    mucNhienLieu FLOAT DEFAULT 50.0,
    trayXuoc NVARCHAR(255),
    taiSanTrenXe NVARCHAR(255),
    thoiGianTiepNhan DATETIME DEFAULT GETDATE(),
    thoiGianDuKien DATETIME,
    trangThai NVARCHAR(50) DEFAULT N'Chờ kiểm tra', -- 'Chờ kiểm tra', 'Đang sửa', 'Chờ phụ tùng', 'Hoàn thành'
    bienSo VARCHAR(20) NOT NULL,
    maNV_LapPhieu VARCHAR(20) NOT NULL,
    CONSTRAINT FK_PTNSC_Xe FOREIGN KEY (bienSo) REFERENCES Xe(bienSo),
    CONSTRAINT FK_PTNSC_NhanVien FOREIGN KEY (maNV_LapPhieu) REFERENCES NhanVien(maNV)
);
GO

-- 3.2. Bảng Phiếu Yêu Cầu Xuất Kho (KTV xin phụ tùng)
CREATE TABLE PhieuYeuCauXuatKho (
    maPhieuYC VARCHAR(30) PRIMARY KEY,
    ngayYeuCau DATETIME DEFAULT GETDATE(),
    ngayXuat DATETIME,
    trangThai NVARCHAR(50) DEFAULT N'Chờ xuất', -- 'Chờ xuất', 'Đã xuất', 'Từ chối'
    maPhieuTNSC VARCHAR(30) NOT NULL,
    maNV_KyThuat VARCHAR(20) NOT NULL,
    maNV_ThuKho VARCHAR(20),
    CONSTRAINT FK_PYC_PTNSC FOREIGN KEY (maPhieuTNSC) REFERENCES PhieuTiepNhanVaSuaChua(maPhieuTNSC),
    CONSTRAINT FK_PYC_KTV FOREIGN KEY (maNV_KyThuat) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PYC_ThuKho FOREIGN KEY (maNV_ThuKho) REFERENCES NhanVien(maNV)
);
GO

-- 3.3. Bảng Phiếu Nhập Kho
CREATE TABLE PhieuNhapKho (
    maPhieuNhap VARCHAR(30) PRIMARY KEY,
    ngayNhap DATETIME DEFAULT GETDATE(),
    tongTienNhap FLOAT DEFAULT 0,
    maNCC VARCHAR(20) NOT NULL,
    maNV_ThuKho VARCHAR(20) NOT NULL,
    CONSTRAINT FK_PNK_NCC FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC),
    CONSTRAINT FK_PNK_NhanVien FOREIGN KEY (maNV_ThuKho) REFERENCES NhanVien(maNV)
);
GO

-- 3.4. Bảng Hóa Đơn
CREATE TABLE HoaDon (
    maHD VARCHAR(30) PRIMARY KEY,
    tongTienCong FLOAT DEFAULT 0,
    tongTienPhuTung FLOAT DEFAULT 0,
    tongTienGiam FLOAT DEFAULT 0,
    tongTienVAT FLOAT DEFAULT 0,
    tongThanhToan FLOAT DEFAULT 0,
    hinhThucThanhToan NVARCHAR(50) DEFAULT N'Tiền mặt', -- 'Tiền mặt', 'Chuyển khoản', 'Công nợ'
    trangThaiThanhToan NVARCHAR(50) DEFAULT N'Đã thanh toán', -- 'Đã thanh toán', 'Công nợ', 'Đã hủy'
    lyDoHuy NVARCHAR(255),
    ngayLap DATETIME DEFAULT GETDATE(),
    maPhieuTNSC VARCHAR(30) NOT NULL UNIQUE,
    maNV_Lap VARCHAR(20) NOT NULL,
    maKM VARCHAR(20) NULL,
    maThue VARCHAR(20) NULL,
    CONSTRAINT FK_HoaDon_PTNSC FOREIGN KEY (maPhieuTNSC) REFERENCES PhieuTiepNhanVaSuaChua(maPhieuTNSC),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV_Lap) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HoaDon_Thue FOREIGN KEY (maThue) REFERENCES Thue(maThue)
);
GO

-- 3.5. Bảng Phiếu Bàn Giao Xe (Xuất Xưởng)
CREATE TABLE PhieuBanGiao (
    maPhieuBG VARCHAR(30) PRIMARY KEY,
    ngayBanGiao DATETIME DEFAULT GETDATE(),
    xacNhanHoanTat BIT DEFAULT 1,
    xacNhanThanhToan BIT DEFAULT 1,
    chuKyKhachHang NVARCHAR(255),
    maPhieuTNSC VARCHAR(30) NOT NULL UNIQUE,
    maNV_BanGiao VARCHAR(20) NOT NULL,
    CONSTRAINT FK_PBG_PTNSC FOREIGN KEY (maPhieuTNSC) REFERENCES PhieuTiepNhanVaSuaChua(maPhieuTNSC),
    CONSTRAINT FK_PBG_NhanVien FOREIGN KEY (maNV_BanGiao) REFERENCES NhanVien(maNV)
);
GO

-- ==============================================================
-- 4. TẠO CÁC BẢNG CHI TIẾT (QUAN HỆ NHIỀU - NHIỀU)
-- ==============================================================

-- 4.1. Chi tiết dịch vụ sửa chữa
CREATE TABLE ChiTietSuaChua_DichVu (
    maPhieuTNSC VARCHAR(30) NOT NULL,
    maDichVu VARCHAR(20) NOT NULL,
    maNV_KyThuat VARCHAR(20),
    trangThaiTienDo NVARCHAR(50) DEFAULT N'Chờ sửa', -- 'Chờ sửa', 'Đang sửa', 'Hoàn thành'
    tienCongThucTe FLOAT NOT NULL DEFAULT 0,
    PRIMARY KEY (maPhieuTNSC, maDichVu),
    CONSTRAINT FK_CTDV_PTNSC FOREIGN KEY (maPhieuTNSC) REFERENCES PhieuTiepNhanVaSuaChua(maPhieuTNSC) ON DELETE CASCADE,
    CONSTRAINT FK_CTDV_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT FK_CTDV_KTV FOREIGN KEY (maNV_KyThuat) REFERENCES NhanVien(maNV)
);
GO

-- 4.2. Chi tiết phụ tùng sửa chữa (Thực tế đã gắn vào xe)
CREATE TABLE ChiTietSuaChua_PhuTung (
    maPhieuTNSC VARCHAR(30) NOT NULL,
    maSKU VARCHAR(30) NOT NULL,
    soLuongSuDung INT NOT NULL DEFAULT 1,
    giaBanThucTe FLOAT NOT NULL DEFAULT 0,
    PRIMARY KEY (maPhieuTNSC, maSKU),
    CONSTRAINT FK_CTPT_PTNSC FOREIGN KEY (maPhieuTNSC) REFERENCES PhieuTiepNhanVaSuaChua(maPhieuTNSC) ON DELETE CASCADE,
    CONSTRAINT FK_CTPT_PhuTung FOREIGN KEY (maSKU) REFERENCES PhuTung(maSKU)
);
GO

-- 4.3. Chi tiết phiếu yêu cầu xuất kho
CREATE TABLE ChiTietYeuCauXuatKho (
    maPhieuYC VARCHAR(30) NOT NULL,
    maSKU VARCHAR(30) NOT NULL,
    soLuong INT NOT NULL DEFAULT 1,
    PRIMARY KEY (maPhieuYC, maSKU),
    CONSTRAINT FK_CTYC_PYC FOREIGN KEY (maPhieuYC) REFERENCES PhieuYeuCauXuatKho(maPhieuYC) ON DELETE CASCADE,
    CONSTRAINT FK_CTYC_PhuTung FOREIGN KEY (maSKU) REFERENCES PhuTung(maSKU)
);
GO

-- 4.4. Chi tiết nhập kho
CREATE TABLE ChiTietNhapKho (
    maPhieuNhap VARCHAR(30) NOT NULL,
    maSKU VARCHAR(30) NOT NULL,
    soLuongNhap INT NOT NULL DEFAULT 1,
    giaNhapThucTe FLOAT NOT NULL DEFAULT 0,
    PRIMARY KEY (maPhieuNhap, maSKU),
    CONSTRAINT FK_CTNK_PNK FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhapKho(maPhieuNhap) ON DELETE CASCADE,
    CONSTRAINT FK_CTNK_PhuTung FOREIGN KEY (maSKU) REFERENCES PhuTung(maSKU)
);
GO

-- ==============================================================
-- 5. BỘ DỮ LIỆU MẪU ĐẦY ĐỦ ĐỂ TEST CHỨC NĂNG (SAMPLE DATA)
-- ==============================================================

-- 5.1. Thêm Nhân viên
INSERT INTO NhanVien (maNV, hoTen, soDienThoai, diaChi, viTriLamViec) VALUES
('NV0001', N'Trần Thị Tường Vân', '0901234567', N'Gò Vấp, TP.HCM', N'Chủ cửa hàng'),
('NV0002', N'Nguyễn Tuấn Vũ', '0912345678', N'Bình Thạnh, TP.HCM', N'Lễ tân'),
('NV0003', N'Nguyễn Xuân Việt Anh', '0923456789', N'Quận 12, TP.HCM', N'Lễ tân'),
('NV0004', N'Bùi Thái Vy', '0934567890', N'Thủ Đức, TP.HCM', N'Thủ kho'),
('NV0005', N'Lê Văn Thợ Máy', '0945678901', N'Tân Bình, TP.HCM', N'Kỹ thuật viên'),
('NV0006', N'Trần Văn Điện Lạnh', '0956789012', N'Phú Nhuận, TP.HCM', N'Kỹ thuật viên');
GO

-- 5.2. Thêm Tài khoản
INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThai, maNV) VALUES
('admin', 'admin123', N'Chủ cửa hàng', 1, 'NV0001'),
('letan01', '123456', N'Lễ tân', 1, 'NV0002'),
('letan02', '123456', N'Lễ tân', 1, 'NV0003'),
('thukho01', '123456', N'Thủ kho', 1, 'NV0004'),
('ktv01', '123456', N'Kỹ thuật viên', 1, 'NV0005'),
('ktv02', '123456', N'Kỹ thuật viên', 1, 'NV0006');
GO

-- 5.3. Thêm Khách hàng
INSERT INTO KhachHang (maKH, hoTen, soDienThoai, diaChi, email, maSoThue, loaiKhachHang, duocGhiNo) VALUES
('KH0001', N'Phạm Minh Hoàng', '0988111222', N'Quận 1, TP.HCM', 'hoang.pham@gmail.com', NULL, N'Thành viên', 0),
('KH0002', N'Công ty CP Vận Tải An Lạc', '0977222333', N'Bình Tân, TP.HCM', 'contact@anlac.vn', '0312456789', N'VIP', 1),
('KH0003', N'Lê Thị Thu Thảo', '0966333444', N'Quận 3, TP.HCM', 'thao.le@yahoo.com', NULL, N'Khách vãng lai', 0);
GO

-- 5.4. Thêm Xe
INSERT INTO Xe (bienSo, soVIN, hangXe, dongXe, doiXe, mauSon, maKH) VALUES
('51H-123.45', '1HGCR2F83HA001234', 'Honda', 'Civic', 2021, N'Trắng Ngọc Trai', 'KH0001'),
('51K-888.88', 'WBA3A5C50DF123456', 'BMW', '320i', 2023, N'Đen Kim Cương', 'KH0001'),
('50E-999.12', 'KMHD35LF2JU654321', 'Hyundai', 'SantaFe', 2022, N'Đỏ Mận', 'KH0002'),
('60A-456.78', 'JTDKN3DU0E0987654', 'Toyota', 'Cross', 2020, N'Xám Bạc', 'KH0003');
GO

-- 5.5. Thêm Danh mục Dịch vụ
INSERT INTO DichVu (maDichVu, tenDichVu, tienCong) VALUES
('DV0001', N'Bảo dưỡng định kỳ cấp 1 (5.000 km)', 250000),
('DV0002', N'Thay dầu động cơ & lọc nhớt', 150000),
('DV0003', N'Vệ sinh kim phun buồng đốt', 450000),
('DV0004', N'Cân chỉnh thước lái góc đặt bánh xe', 500000),
('DV0005', N'Bảo dưỡng hệ thống phanh 4 bánh', 350000),
('DV0006', N'Nạp gas & bảo dưỡng điều hòa', 400000);
GO

-- 5.6. Thêm Danh mục Phụ tùng
INSERT INTO PhuTung (maSKU, tenPhuTung, hangSanXuat, viTriLuuKho, giaNhap, giaBanLe, soLuongTon, nguongCanhBao) VALUES
('SKU_OIL01', N'Dầu nhớt Castrol Magnatec 5W-30 (4L)', 'Castrol', N'Kệ A-01', 450000, 650000, 25, 5),
('SKU_OIL02', N'Dầu nhớt Mobil 1 Advanced Fuel 0W-20', 'Mobil', N'Kệ A-02', 650000, 950000, 18, 5),
('SKU_FLT01', N'Lọc dầu động cơ Honda Civic', 'Honda OEM', N'Kệ B-01', 80000, 150000, 30, 8),
('SKU_FLT02', N'Lọc gió điều hòa than hoạt tính', 'Denso', N'Kệ B-02', 120000, 250000, 15, 5),
('SKU_BRK01', N'Bộ má phanh trước Brembo Civic', 'Brembo', N'Kệ C-01', 800000, 1350000, 10, 3),
('SKU_SPK01', N'Bugi Iridium NGK Laser', 'NGK', N'Kệ D-01', 150000, 280000, 4, 6); -- Đang dưới ngưỡng cảnh báo để test cảnh báo đỏ
GO

-- 5.7. Thêm Nhà cung cấp
INSERT INTO NhaCungCap (maNCC, tenNCC, soDienThoai, diaChi) VALUES
('NCC0001', N'Công ty TNHH Phụ Tùng Ô Tô An Thái', '02838991234', N'Quận 5, TP.HCM'),
('NCC0002', N'Tổng Đại Lý Dầu Nhớt Castrol Miền Nam', '02837654321', N'KCX Tân Thuận, Q7, TP.HCM'),
('NCC0003', N'Phụ Tùng Chính Hãng Denso Việt Nam', '02839998888', N'Bình Dương');
GO

-- 5.8. Thêm Khuyến mãi & Thuế
INSERT INTO KhuyenMai (maKM, tenChuongTrinh, hinhThuc, giaTri, thoiGianBatDau, thoiGianKetThuc, trangThai) VALUES
('KM0001', N'Tri ân khách hàng - Giảm 10% tổng hóa đơn', N'Giảm %', 10.0, '2026-01-01', '2026-12-31', 1),
('KM0002', N'Ưu đãi bảo dưỡng mùa mưa - Tặng 100.000đ', N'Giảm tiền mặt', 100000, '2026-06-01', '2026-11-30', 1);
GO

INSERT INTO Thue (maThue, tenLoaiThue, thueSuat) VALUES
('VAT08', N'Thuế giá trị gia tăng 8%', 8.0),
('VAT10', N'Thuế giá trị gia tăng 10%', 10.0);
GO

-- 5.9. Thêm Phiếu tiếp nhận mẫu & Chi tiết
INSERT INTO PhieuTiepNhanVaSuaChua (maPhieuTNSC, bienSo, maNV_LapPhieu, yeuCauBanDau, soKm, mucNhienLieu, trayXuoc, taiSanTrenXe, thoiGianTiepNhan, thoiGianDuKien, trangThai) VALUES
('PTNSC00001', '51H-123.45', 'NV0002', N'Bảo dưỡng định kỳ mốc 4 vạn km, thay nhớt lọc và kiểm tra phanh', 40250, 65.0, N'Trầy nhẹ cản sau', N'Ví tiền lẻ trong hộc, 1 cặp kính râm', '2026-10-20 08:30:00', '2026-10-20 16:30:00', N'Hoàn thành'),
('PTNSC00002', '50E-999.12', 'NV0003', N'Xe có tiếng kêu lục cục ở bánh trước khi qua gờ giảm tốc', 68000, 45.0, N'Không trầy xước', N'Không gửi tài sản', '2026-10-23 09:15:00', '2026-10-23 17:00:00', N'Đang sửa');
GO

INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, maNV_KyThuat, tienCongThucTe, trangThaiTienDo) VALUES
('PTNSC00001', 'DV0001', 'NV0005', 250000, N'Hoàn thành'),
('PTNSC00001', 'DV0002', 'NV0005', 150000, N'Hoàn thành'),
('PTNSC00001', 'DV0005', 'NV0006', 350000, N'Hoàn thành'),
('PTNSC00002', 'DV0004', 'NV0005', 500000, N'Đang sửa');
GO

INSERT INTO ChiTietSuaChua_PhuTung (maPhieuTNSC, maSKU, soLuongSuDung, giaBanThucTe) VALUES
('PTNSC00001', 'SKU_OIL01', 1, 650000),
('PTNSC00001', 'SKU_FLT01', 1, 150000);
GO

-- 5.10. Thêm Hóa đơn mẫu (cho PTNSC00001)
-- Tiền công: 250k + 150k + 350k = 750k
-- Tiền phụ tùng: 650k + 150k = 800k
-- Tổng chưa giảm: 1.550.000đ
-- Giảm 10% (KM0001): 155.000đ -> Còn 1.395.000đ
-- VAT 8%: 111.600đ
-- Tổng thanh toán: 1.506.600đ
INSERT INTO HoaDon (maHD, maPhieuTNSC, maNV_Lap, maKM, maThue, ngayLap, tongTienCong, tongTienPhuTung, tongTienGiam, tongTienVAT, tongThanhToan, hinhThucThanhToan, trangThaiThanhToan) VALUES
('HD000001', 'PTNSC00001', 'NV0002', 'KM0001', 'VAT08', '2026-10-20 16:45:00', 750000, 800000, 155000, 111600, 1506600, N'Chuyển khoản', N'Đã thanh toán');
GO

-- 5.11. Thêm Phiếu bàn giao xe mẫu (cho PTNSC00001)
INSERT INTO PhieuBanGiao (maPhieuBG, maPhieuTNSC, maNV_BanGiao, ngayBanGiao, xacNhanHoanTat, xacNhanThanhToan, chuKyKhachHang) VALUES
('PBG00001', 'PTNSC00001', 'NV0002', '2026-10-20 17:00:00', 1, 1, N'Phạm Minh Hoàng');
GO

PRINT N'==============================================================';
PRINT N'ĐÃ TẠO DATABASE GaraAUTORA VÀ DỮ LIỆU MẪU THÀNH CÔNG!';
PRINT N'==============================================================';
GO
