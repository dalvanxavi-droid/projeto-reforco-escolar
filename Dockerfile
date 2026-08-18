FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN mkdir -p lib && curl -L -o lib/gson-2.11.0.jar https://repo1.maven.org/maven2/com/google/gson/gson/2.11.0/gson-2.11.0.jar
RUN javac -cp .:postgresql-42.7.3.jar:lib/gson-2.11.0.jar *.java
EXPOSE 8080
CMD ["java", "-cp", ".:postgresql-42.7.3.jar:lib/gson-2.11.0.jar", "ServidorWeb"]