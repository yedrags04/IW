FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y netcat-openbsd

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn ./.mvn
COPY src ./src
COPY wait-for-it.sh /wait-for-it.sh

RUN chmod +x mvnw /wait-for-it.sh

ENV MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dhttps.protocols=TLSv1.2"

RUN ./mvnw clean package -DskipTests

CMD ["/wait-for-it.sh", "mysql", "java", "-jar", "target/vaadinweb-0.0.1-SNAPSHOT.jar"]