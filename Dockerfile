# Use Maven to build the fat jar
FROM eclipse-temurin:21 as build
WORKDIR /app
COPY . /app
RUN ./mvnw -B -DskipTests package

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]