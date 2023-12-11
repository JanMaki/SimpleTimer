#コンパイル
FROM gradle AS compile

WORKDIR /app
COPY . .
RUN ./gradlew shadowJar -i

#Botを起動
FROM amazoncorretto:17 AS bot

WORKDIR /app

COPY --from=compile /app/build/libs .

CMD ["java", "-jar", "SimpleTimer-all.jar"]