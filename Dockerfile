#コンパイル
FROM gradle AS compile

WORKDIR /app
COPY . .
RUN ./gradlew shadowJar -i

#Botを起動
FROM ubuntu AS bot

RUN apt update && apt install -y openjdk-17-jdk-headless

ENV SIMPLETIMER_TOKEN=""
ENV SIMPLETIMER_SHARDS_COUNT=1
ENV SIMPLETIMER_DB_ADDRESS=""
ENV SIMPLETIMER_DB_SCHEME=""
ENV SIMPLETIMER_DB_USER=""
ENV SIMPLETIMER_DB_PASS=""

WORKDIR /app

COPY --from=compile /app/build/libs .

CMD ["java", "-jar", "SimpleTimer-all.jar"]

