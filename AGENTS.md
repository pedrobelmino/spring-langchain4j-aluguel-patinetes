# AGENTS.md — spring-langchain4j-aluguel-patinetes

Chatbot de Telegram que atua como assistente de uma empresa de aluguel de patinetes elétricos.
Recebe o update por webhook protegido, interpreta com LangChain4j + Google Gemini, chama tools
(preço, reserva, e-mail) e grava reservas no Firestore. Diagrama em `arq-v1.png`.

## Stack (lida de pom.xml / Dockerfile)

- Java 21 (`<java.version>21</java.version>`)
- Spring Boot 3.2.2 (parent)
- LangChain4j 1.7.1-beta14 (`langchain4j-spring-boot-starter`, `langchain4j-google-ai-gemini-spring-boot-starter`)
- `telegrambots-spring-boot-starter` 6.9.0
- `mailjet-client` 5.2.5
- `spring-cloud-gcp-starter-data-firestore` 5.1.0
- Caffeine (versão via BOM do Spring Boot)
- JUnit 4.13.2 + `spring-boot-starter-test`
- Build Docker: `maven:3.9.6-eclipse-temurin-21` -> `eclipse-temurin:21-jre-jammy`
- Coordenadas: groupId `br.com.pedro.langchain4j.hello`, artifactId `git`, version `0.1.0-SNAPSHOT`

## Comandos

- build: `mvn clean package`  (jar em `target/git-0.1.0-SNAPSHOT.jar`)
- build imagem: `docker build -t aluguel-patinetes .`
- test: `mvn test`  (3 classes: `PricingConfigTest`, `BookingServiceTest`, `ScooterServiceTest`)
- run: `mvn spring-boot:run`  (porta 8080)
- lint: não existe no projeto
- wrapper: não existe `mvnw` — usar o `mvn` do sistema

## Estrutura

- `src/main/java/br/com/pedrobelmino/langchain4j/hello/` — `Application` (main)
- `.../bot/` — `MyTelegramBot` (extends `TelegramWebhookBot`)
- `.../config/` — `LangChainConfig` (bean do Gemini), `PricingConfig` (tabela de preços por cidade)
- `.../controller/` — `WebhookController` (POST `${telegram.bot.path}`), `ChatController` (`/api/chat`)
- `.../model/` — `Booking`, `RentalData` (doc do Firestore), `Scooter`
- `.../repository/` — `RentalRepository` (`FirestoreReactiveRepository`)
- `.../service/` — `AssistantAiService`, `AssistantTools`, `BookingService`, `EmailService`, `ScooterService`
- `src/main/resources/application.properties` — única config lida no boot
- `src/test/java/...` — `config/` e `service/`
- `.github/workflows/` — `google-cloudrun-docker.yml`, `deploy-manual.yml`
- `application.properties` na raiz — cópia fora do classpath, não é lida pelo Spring
## Convenções observadas

- Pacote raiz `br.com.pedrobelmino.langchain4j.hello`; o groupId do pom é outro
  (`br.com.pedro.langchain4j.hello`) — divergência antiga, não propagar em código novo.
- Injeção por construtor na maioria das classes; `@Autowired` em campo só em `MyTelegramBot`.
- Dinheiro em `BigDecimal`; cidade normalizada com `toLowerCase().trim()` antes de buscar preço.
- Erros de domínio: `IllegalArgumentException` (não encontrado) e `IllegalStateException`
  (indisponível). Não há `@ControllerAdvice` — a exceção sobe crua.
- Estado em memória: `ConcurrentHashMap` (`BookingService`, memória de chat do bot);
  Caffeine com 1000 sessões e 30 min em `ChatController`.
- DTOs como `record` aninhados (`ChatRequest`, `ChatResponse`).

## Regras

- Segredo só por variável de ambiente: `GEMINI_API_KEY`, `TELEGRAM_SECRET_TOKEN`,
  `MAILJET_API_KEY`, `MAILJET_SECRET_KEY`, `GCP_PROJECT_ID`, `EMAIL_USER`.
  O `application.properties` só referencia `${...}` — nunca escrever valor ali.
- `.gitignore` NÃO cobre `.env` — não criar `.env` neste repo; exportar no shell.
- Não alterar contrato de endpoint sem atualizar teste: o webhook exige o header
  `X-Telegram-Bot-Api-Secret-Token` (403 se faltar); `/api/chat` recebe `{sessionId, message}`.
- Mexer em preço/fórmula de cobrança -> ajustar `PricingConfigTest` junto.

## Pegadinhas reais

- Sem `GEMINI_API_KEY` (e credencial GCP) a aplicação não sobe: os beans são criados no boot.
- Firestore não usa só a var `GCP_PROJECT_ID`; precisa de credencial (ADC / service account).
- O webhook precisa de URL HTTPS pública: local só com túnel + `setWebhook` no Telegram.
- `artifactId` é `git`, então o jar é `git-0.1.0-SNAPSHOT.jar`. O Dockerfile copia
  `target/*.jar`, o que funciona — mas `mvn install` publica um artefato chamado `git`.
- Modelo fixo em `gemini.model` (`gemini-3.1-flash-lite`); trocar exige reiniciar.
