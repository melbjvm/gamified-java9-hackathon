FROM maven:alpine
MAINTAINER Kon Soulianidis <kon@melbjvm.com>

RUN apk update
RUN apk add git certbot openssl
RUN git clone https://github.com/melbjvm/gamified-java9-hackathon.git
WORKDIR gamified-java9-hackathon
RUN mvn clean package
ENV HTTPPORT=80 HTTPSPORT=443
EXPOSE $HTTPPORT $HTTPSPORT
ENV domain_name=j9.melbjvm.com
ADD scripts/letsencrypt.sh letsencrypt.sh
ENV SPRING_APPLICATION_JSON='{"httpport":80, "server": { "port":443, "ssl": { "key-store": "/gamified-java9-hackathon/letsencrypt.jks", "key-store-password": "password", "key-password": "password" } } }'
ENTRYPOINT ["java","-jar","target/gamified-java9-hackathon-1.0.0-SNAPSHOT.jar"]
CMD ["--spring-application-json=\'$SPRING_APPLICATION_JSON\'" ]
