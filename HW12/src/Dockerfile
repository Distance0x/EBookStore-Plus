FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /app
COPY . .
RUN chmod +x mvnw || true \
	&& ./mvnw -q -DskipTests package
EXPOSE 8080
# 用通配符启动生成的 Spring Boot jar 文件
ENTRYPOINT ["sh","-c","java -jar target/*.jar"]
