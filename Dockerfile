# Estágio 1: Build da aplicação com Maven
FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia o pom.xml para baixar as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do código-fonte e compila o JAR
COPY src ./src
RUN mvn package -DskipTests

# Estágio 2: Criação da imagem final a partir de uma JRE leve
FROM eclipse-temurin:21-jre-jammy

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que a aplicação vai usar (o Cloud Run usa a variável PORT)
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
