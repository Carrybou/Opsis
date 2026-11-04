# Construction : les tests ne sont pas relancés ici, GitHub Actions s'en charge à chaque push.
FROM eclipse-temurin:25-jdk AS construction
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN sh mvnw -B -q package -DskipTests

# Exécution : une image légère, sans les sources, avec un utilisateur sans droits particuliers.
FROM eclipse-temurin:25-jre
# Le code lit l'heure locale : sans ce réglage, l'ancienneté des incidents serait décalée par rapport aux dates saisies en France.
ENV TZ=Europe/Paris
RUN useradd --system opsis
USER opsis
WORKDIR /app
COPY --from=construction /app/target/Opsis-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
