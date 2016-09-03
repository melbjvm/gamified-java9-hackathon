FROM maven:alpine
ENV maintainer_email=kon@melbjvm.com
MAINTAINER Kon Soulianidis <${maintainer_email}>

RUN apk update
RUN apk add git certbot openssl
RUN git clone https://github.com/melbjvm/gamified-java9-hackathon.git
WORKDIR gamified-java9-hackathon
RUN mvn clean package
ENV HTTPPORT=80 HTTPSPORT=443
EXPOSE $HTTPPORT $HTTPSPORT
ENV domain_name=j9.melbjvm.com
ADD scripts/letsencrypt.sh letsencrypt.sh
ENV SPRING_APPLICATION_JSON='{"httpport": $HTTPPORT, \
                              "server": { "port": $HTTPSPORT , \
                                          "ssl": { \
                                                "key-store": "/gamified-java9-hackathon/letencrypt.jks", \
                                                "key-store-password": "password", \
                                                "key-password": "password" \
                                                } }'
ENTRYPOINT ["java","-jar","target/gamified-java9-hackathon-1.0.0-SNAPSHOT.jar"]
CMD ["--spring-application-json=$SPRING_APPLICATION_JSON" ]
