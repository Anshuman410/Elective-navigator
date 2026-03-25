# Stage 1: Build the Java application using Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Create the war file
RUN mvn clean package -DskipTests

# Stage 2: Deploy to Tomcat
FROM tomcat:9.0-jre17
# Remove default Tomcat apps to keep it clean (optional)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file from the build stage into Tomcat's webapps directory 
# Renaming it to ROOT.war so it serves at the root path (/) instead of /student-backend-1.0-SNAPSHOT
COPY --from=build /app/target/student-backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat's default port
EXPOSE 8080

CMD ["catalina.sh", "run"]
