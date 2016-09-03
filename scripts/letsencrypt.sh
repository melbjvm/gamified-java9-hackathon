#!/usr/bin/env bash

# Rudimentary script to create a letsencrypt cert file for a spring boot app on Jelastic hosted alpine-linux docker image
# Modified from: https://gist.github.com/mihkels/6e30e8e21acc68a55482
# Uses certbot command which was recently renamed from letsencrypt-auto, plus adds non-interactive flags
# and works around PythonDisplayBug that occurs on alpine linux

domain_name=${domain_name:?Set domain_name env var of domain you wish to generate certs for}
agree_email=${agree_email:?Set agree_email env var for letsencrypt terms acceptance email}

print_logs() {
    echo "Printing /var/log/letsencrypt/letsencrypt.log"
    cat /var/log/letsencrypt/letsencrypt.log
}

create_letsencrypt_certs() {
    certbot certonly --text -vvvvvv --non-interactive --standalone -d ${domain_name} --agree-tos --email ${agree_email}
    worked=$?; echo $worked
    if [ ! $worked ]; then
        echo "Failure occured ($worked)."
        exit ${worked}
    else
        echo "Successfully created cert for $domain_name.  Printing cert dir & logs";
        echo "Contents of /var/log/letencrypt"
        ls -laRrt /etc/letsencrypt/
        print_logs
    fi
}

create_java_keystore() {
    cd /etc/letsencrypt/live/${domain_name}
    echo "Generating cert_and_key.p12.  You will be asked for password"
    openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out cert_and_key.p12 -name tomcat -CAfile chain.pem -caname root
    echo "\nGenerating java key store"
    keytool -importkeystore -deststorepass password -destkeypass password -destkeystore letsencrypt.jks \
            -srckeystore cert_and_key.p12 -srcstoretype PKCS12 -srcstorepass password -alias tomcat
    echo "\nAdding CA to keystore"
    keytool -import -trustcacerts -alias root -file chain.pem -keystore letsencrypt.jks
}

create_letsencrypt_certs && \
  create_java_keystore

