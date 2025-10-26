# Use uma imagem base com uma versão específica do Maven e JDK
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Crie um diretório de trabalho
WORKDIR /app

# Copie apenas o arquivo pom.xml primeiro para aproveitar o cache de dependências
COPY pom.xml .

# Baixe todas as dependências. Esta camada só será reconstruída se o pom.xml mudar.
RUN mvn dependency:go-offline

# Agora copie o resto do código-fonte
COPY src ./src

# Compile a aplicação e gere o JAR
RUN mvn package -DskipTests

# --- Estágio Final ---
# Use uma imagem base leve para a execução
FROM eclipse-temurin:21-jre-jammy

# Crie um diretório de trabalho
WORKDIR /app

# Copie o JAR do estágio de build para o estágio final
COPY --from=build /app/target/*.jar app.jar

# Exponha a porta que a aplicação usa
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java","-jar","app.jar"]
