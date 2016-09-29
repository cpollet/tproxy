FROM cpollet/oracle-jdk8

COPY tproxy.jar /opt/tproxy.jar

VOLUME /etc/tproxy

CMD java -jar /opt/tproxy.jar /etc/tproxy/tproxy.json
