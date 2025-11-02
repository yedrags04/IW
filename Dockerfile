FROM eclipse-temurin:21-jdk

# Instala netcat para que wait-for-it.sh funcione
RUN apt-get update && apt-get install -y netcat-openbsd

WORKDIR /app

# Copia el wrapper y el proyecto
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn ./.mvn
COPY src ./src
COPY wait-for-it.sh /wait-for-it.sh

# Da permisos de ejecución
RUN chmod +x mvnw /wait-for-it.sh

# Compila el proyecto
RUN ./mvnw clean package -DskipTests

# Ejecuta la app solo cuando MySQL esté listo
CMD ["/wait-for-it.sh", "mysql", "java", "-jar", "target/vaadinweb-0.0.1-SNAPSHOT.jar"]
