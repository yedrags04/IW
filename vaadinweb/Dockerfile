FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copia el wrapper completo
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn ./.mvn
COPY src ./src

# Da permisos de ejecución
RUN chmod +x mvnw

# Compila el proyecto
RUN ./mvnw clean package -DskipTests

# Ejecuta el .jar generado
CMD ["java", "-jar", "target/vaadinweb-0.0.1-SNAPSHOT.jar"]
