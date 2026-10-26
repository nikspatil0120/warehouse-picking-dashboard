
# Use a minimal base image with regular security updates
FROM tomcat:11.0-jdk21-temurin-noble

# Set security labels
LABEL security.scan="true"
LABEL description="Picking Dashboard - Warehouse Management"

# Remove unnecessary files to reduce attack surface
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
    && rm -rf /usr/local/tomcat/webapps/docs \
    && rm -rf /usr/local/tomcat/webapps/examples \
    && rm -rf /usr/local/tomcat/webapps/host-manager \
    && rm -rf /usr/local/tomcat/webapps/manager

# Copy application WAR
COPY target/picking-dashboard-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Set non-root user (optional but recommended)
# USER tomcat

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
