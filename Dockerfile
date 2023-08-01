FROM amazoncorretto:8

ENV SERVER_PORT=8051
ENV BASE_DIR=/var/run/bahmni-reports
ENV CONTEXT_PATH=/bahmnireports
ENV WAR_DIRECTORY=/var/run/bahmni-reports/bahmni-reports
ENV SERVER_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
ENV DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,address=8003,server=y,suspend=n"

RUN mkdir -p /var/log/bahmni-reports
RUN mkdir -p /home/bahmni/reports
RUN mkdir -p ${WAR_DIRECTORY}
RUN mkdir -p ${HOME}/.bahmni-reports/
RUN mkdir -p /var/log/bahmni-reports/
RUN yum install -y gettext unzip

ADD https://raw.githubusercontent.com/eficode/wait-for/v2.2.3/wait-for /etc/wait-for
RUN yum install -y nc
RUN chmod +x /etc/wait-for

COPY target/bahmnireports.war /etc/bahmni-reports/bahmnireports.war
RUN cd ${WAR_DIRECTORY} && jar xvf /etc/bahmni-reports/bahmnireports.war

ADD https://repo.mybahmni.org/packages/build/bahmni-embedded-tomcat-8.0.42.jar /opt/bahmni-reports/lib/bahmni-embedded-tomcat.jar
ADD https://github.com/daviemukungi/bahmni-Icd10extensions/raw/main/jarfile/extensions-1.0-SNAPSHOT.jar /opt/bahmni-reports/lib/extensions-1.0-SNAPSHOT.jar

COPY package/resources/log4j2.properties ${WAR_DIRECTORY}/WEB-INF/classes/
COPY package/resources/run-liquibase.sh /etc/bahmni-reports/
COPY package/docker/bahmni-reports/template/bahmni-reports.properties.template /etc/bahmni-reports/

COPY package/docker/bahmni-reports/start.sh start.sh
RUN chmod +x start.sh
CMD [ "./start.sh" ]
