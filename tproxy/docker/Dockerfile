FROM cpollet/oracle-jdk8

COPY tproxy.jar /opt/tproxy.jar

EXPOSE 8080

VOLUME /etc/tproxy

CMD java -jar /opt/tproxy.jar /etc/tproxy/tproxy.json
