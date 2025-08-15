#!/bin/bash
echo "=== Dọn dẹp dự án Android ==="

# Xóa thư mục build
echo "Xóa thư mục build..."
rm -rf app/build

# Xóa thư mục build Gradle
echo "Xóa cache Gradle..."
rm -rf .gradle
rm -rf build

# Xóa file log nếu có
echo "Xóa file log..."
find . -name "*.log" -type f -delete

# Xóa file tạm của hệ thống
echo "Xóa file tạm..."
find . -name "*~" -type f -delete

# Xóa output APK cũ
echo "Xóa APK cũ..."
rm -rf app/release
rm -rf app/debug

echo "✅ Dọn dẹp hoàn tất!"
