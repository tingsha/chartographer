FROM openjdk:11
EXPOSE 8080

COPY ./target/chartographer-1.0.0.jar /app/chartographer/chartographer.jar

WORKDIR /app/chartographer/

CMD java -jar chartographer.jar /data/