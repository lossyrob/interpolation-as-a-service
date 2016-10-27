FROM java:8

EXPOSE 7070

COPY target/scala-2.11/interpolation-as-a-service-assembly-0.1.0.jar /opt/app/app.jar

CMD [ "java", "-jar", "/opt/app/app.jar", "com.azavea.iaas.Main"]
