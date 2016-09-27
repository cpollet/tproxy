# Compile & Run
```
$ mvn clean package
$ java -jar tproxy/target/tproxy.jar
```
or
```
$ mvn clean package
$ cd tproxy
$ mvn docker:build
$ docker run -it --rm -p 8080:8080 -v `pwd`/../config/:/etc/tproxy cpollet/tproxy:VERSION
```

visit http://localhost:8080
