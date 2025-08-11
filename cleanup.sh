#!/bin/bash
# Script dọn project Android
# Dùng trong thư mục gốc của project

echo "=== DỌN DỰ ÁN ANDROID ==="

# 1. Xóa thư mục build (Gradle sẽ tự tạo lại khi build)
echo "Xóa thư mục build/..."
rm -rf build
rm -rf app/build

# 2. Xóa file/tệp tin thừa
echo "Xóa file báo cáo và liệt kê thư mục..."
rm -f tree.txt
rm -f build/reports/problems/problems-report.html

# 3. Xóa thư mục code thử nghiệm hoặc lỗi (nếu có)
echo "Xóa thư mục cd, {, Compilation, Run, Task, touch..."
rm -rf app/src/main/cd
rm -rf app/src/main/java/com/dung/clipboard/{
rm -rf app/src/main/java/com/dung/clipboard/Compilation
rm -rf app/src/main/java/com/dung/clipboard/Run
rm -rf app/src/main/java/com/dung/clipboard/Task
rm -rf app/src/main/java/com/dung/clipboard/touch

# 4. Xóa cache Gradle nếu muốn build sạch hoàn toàn
echo "Xóa cache Gradle (tùy chọn)..."
rm -rf ~/.gradle/caches

echo "=== HOÀN TẤT DỌN DỰ ÁN ==="
