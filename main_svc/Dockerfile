FROM amazoncorretto:11-alpine-jdk
COPY /target/*.jar main_svc.jar
ENTRYPOINT ["java","-jar","/main_svc.jar"]