FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# Compila incluindo o driver do Postgres no classpath
RUN javac -cp .:postgresql-42.7.3.jar *.java

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/*.class /app/
COPY --from=build /app/postgresql-42.7.3.jar /app/

EXPOSE 8080
CMD ["java", "-cp", ".:postgresql-42.7.3.jar", "ServidorWeb"]