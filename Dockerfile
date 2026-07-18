# 1. Chọn bản Java 17 để chạy
FROM eclipse-temurin:17-jdk-alpine

# 2. Tạo một thư mục làm việc bên trong "cái hộp"
WORKDIR /app

# 3. Copy file .jar mà Maven đã build ra vào trong hộp
# Lưu ý: Tên file .jar thường là tên-dự-án-0.0.1-SNAPSHOT.jar nằm trong thư mục target
COPY target/*.jar app.jar

# 4. Lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]