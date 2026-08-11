FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac -cp .:postgresql-42.7.3.jar *.java
EXPOSE 8080
CMD ["java", "-cp", ".:postgresql-42.7.3.jar", "ServidorWeb"]