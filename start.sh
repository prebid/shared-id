#!/bin/bash
#export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xmx1G -Dspring.profiles.active=local -Dlog.dir=target/logs -Djava.net.preferIPv4Stack=true"
export LOG_DIR="target/logs"
mvn spring-boot:run -Dspring-boot.run.profiles=local