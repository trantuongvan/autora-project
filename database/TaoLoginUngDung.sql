-- Chạy một lần trên mỗi máy chủ SQL Server bằng tài khoản quản trị, sau GaraAUTORA_Database.sql.
-- Đổi mật khẩu ở dòng DECLARE (đừng lưu mật khẩu thật vào git), rồi ghi cùng mật khẩu đó vào database.properties.
USE master;
GO

DECLARE @matKhau NVARCHAR(128) = N'DOI_MAT_KHAU_NAY';

IF @matKhau = N'DOI' + N'_MAT_KHAU_NAY'
    THROW 50000, N'Hãy đổi mật khẩu ở dòng DECLARE trước khi chạy script.', 1;

DECLARE @lenh NVARCHAR(400) = CASE WHEN SUSER_ID(N'autora_app') IS NULL THEN N'CREATE' ELSE N'ALTER' END
    + N' LOGIN autora_app WITH PASSWORD = ' + QUOTENAME(@matKhau, '''');
EXEC (@lenh);
GO

USE GaraAUTORA;
GO

IF USER_ID(N'autora_app') IS NULL
    CREATE USER autora_app FOR LOGIN autora_app;
ALTER ROLE db_datareader ADD MEMBER autora_app;
ALTER ROLE db_datawriter ADD MEMBER autora_app;
GO
