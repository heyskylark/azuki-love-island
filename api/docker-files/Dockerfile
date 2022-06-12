FROM tomcat:9-jdk11-adoptopenjdk-hotspot

COPY ../server.xml /usr/local/tomcat/conf/server.xml
COPY ../rds-combined-ca-bundle.pem

RUN mkdir -p /opt/provision
RUN echo "local" > /opt/provision/on_disk_version

RUN rm -rf /usr/local/tomcat/webapps/*

EXPOSE 8080

ENTRYPOINT ["catalina.sh", "run"]
