# Etapa de construcción (Build Stage)
FROM eclipse-temurin:17-jdk-jammy AS builder
# eclipse-temurin es una imagen de OpenJDK bien mantenida. Usamos JDK 17.

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el wrapper de Maven y el pom.xml para descargar dependencias y aprovechar el caché de Docker
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Descargar dependencias (esto se cacheará si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar el resto del código fuente de la aplicación
COPY src ./src

# Limpiar, compilar y empaquetar la aplicación, omitiendo los tests para una construcción más rápida de la imagen
# Los tests se ejecutarán en la pipeline de Jenkins o localmente antes.
RUN ./mvnw package -DskipTests -B

# Etapa de ejecución (Runtime Stage)
FROM eclipse-temurin:17-jre-jammy
# Usamos una imagen JRE más ligera para la ejecución

WORKDIR /app

# Copiar el JAR construido desde la etapa 'builder'
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto en el que corre la aplicación Spring Boot (usualmente 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "app.jar"]