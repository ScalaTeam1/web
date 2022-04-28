# Before you build the image, remember to copy the jar file flight-price-core_2.12-0.1.jar under dir ./lib
sbt clean dist

docker build . -f docker/Dockerfile -t csye7200-web:1.0

docker run -d -p 7759:7759 -v ${CUSTOMIZED_APPLICATION_PATH}:/svc/conf/application.conf csye7200-web:1.0