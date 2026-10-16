
FROM tomcat:10.1-jdk17-temurin
COPY target/picking-dashboard-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
