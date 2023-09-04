#!/bin/bash -ex

PASSWORD=changeit
SERVER_KEYSTORE=server.keystore.jks
SERVER_TRUSTSTORE=server.truststore.jks
CLIENT_KEYSTORE=client.keystore.jks
CLIENT_TRUSTSTORE=client.truststore.jks
VALIDITY=10365
USER=myuser
BROKER_HOST=kafka-broker

# 1 Key
keytool -v -genkey -dname "CN=${BROKER_HOST}, \
	OU=${USER}, o=Load, L=Moscow, C=RU" \
	-alias ${BROKER_HOST} -storetype jks -keystore ${SERVER_KEYSTORE} \
	-validity ${VALIDITY} -keyalg RSA -keysize 2048 \
	-storepass ${PASSWORD} -keypass ${PASSWORD}
	
# 2 Cert center
openssl req -new -passout pass:${PASSWORD} -x509 -keyout ca-key -out ca-cert -days ${VALIDITY} -subj '/C=RU/ST=${USER}/L=Moscow/o=Load/CN=${BROKER_HOST}'

# 3 Import cert into store
keytool -keystore ${CLIENT_TRUSTSTORE} -storepass ${PASSWORD} -noprompt -alias CARoot -import -file ca-cert
keytool -keystore ${SERVER_TRUSTSTORE} -storepass ${PASSWORD} -noprompt -alias CARoot -import -file ca-cert

# 4 Cert req sign
keytool -keystore ${SERVER_KEYSTORE} -storepass ${PASSWORD} -noprompt -alias ${BROKER_HOST} -certreq -file cert-req

#5 Open center
openssl x509 -passin pass:${PASSWORD} -req -CA ca-cert -CAkey ca-key -in cert-req -out cert-signed -days ${VALIDITY} -CAcreateserial

# 6 Import CA into server truststore
keytool -keystore ${SERVER_KEYSTORE} -storepass ${PASSWORD} -noprompt -alias CARoot  -import -file ca-cert

# 7 Import signed cert into server truststore
keytool -keystore ${SERVER_KEYSTORE} -storepass ${PASSWORD} -noprompt -alias ${BROKER_HOST} -import -file cert-signed

# 8 Generate client keystore
keytool -keystore ${CLIENT_KEYSTORE} -alias gatling_test -validity ${VALIDITY} -genkey \
	-dname "CN=gatling_test, OU=${USER}, o=Load, L=Moscow, C=RU" \
	-keyalg RSA -keysize 2048 \
	-storepass ${PASSWORD} \
	-keypass ${PASSWORD}
	
# 9 Generate client cert
keytool -keystore ${CLIENT_KEYSTORE} -storepass ${PASSWORD} -alias gatling_test -certreq -file client-cert-file
openssl x509 -passin pass:${PASSWORD} -req -CA ca-cert -CAkey ca-key -in client-cert-file -out client-cert-signed -days ${VALIDITY}

# 10 Import CA into client cert
keytool -keystore ${CLIENT_KEYSTORE} -storepass ${PASSWORD} -noprompt -alias CARoot -import -file ca-cert

# 11 Import signed cert into keystore
keytool -keystore ${CLIENT_KEYSTORE} -storepass ${PASSWORD} -noprompt -alias gatling_test -import -file client-cert-signed