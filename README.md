# Assistente de Aluguel de Patinetes para Telegram

Este projeto é um chatbot para Telegram que atua como um assistente de uma empresa de aluguel de patinetes elétricos. O bot é construído com Java 21, Spring Boot, LangChain4j e a API do Google Gemini. A aplicação é projetada para ser implantada de forma segura e escalável no Google Cloud Run.

## Arquitetura da Solução

A interação do usuário começa no Telegram, passa por um webhook seguro, é processada pelo serviço no Cloud Run que utiliza o Gemini para gerar respostas, e retorna ao usuário.

![Arquitetura da Solução](arq-v1.png)

## Tecnologias Utilizadas

Este projeto integra um conjunto de tecnologias modernas para criar uma solução de IA robusta e escalável:

- **Spring Boot 3.2**: Framework principal para a criação da aplicação web, fornecendo um ecossistema completo com injeção de dependências, configuração automática e um servidor web embarcado (Tomcat).
- **LangChain4j 1.7**: O coração da lógica de IA. Este framework facilita a orquestração de chamadas para grandes modelos de linguagem (LLMs), gerenciamento de memória de chat e a criação de ferramentas personalizadas.
  - `langchain4j-spring-boot-starter`: Simplifica a integração do LangChain4j com o Spring, permitindo a configuração de assistentes e ferramentas através de anotações.
  - `langchain4j-google-ai-gemini-spring-boot-starter`: Provê a integração específica com a família de modelos Gemini do Google AI.
- **TelegramBots Spring Boot Starter 6.9**: Biblioteca que facilita a comunicação com a API do Telegram, permitindo a criação de bots baseados em Webhook de forma simples e integrada ao Spring.
- **Java 21**: Utiliza a versão mais recente do Java, aproveitando suas melhorias de performance e recursos de linguagem.
- **Maven**: Gerenciador de dependências e ferramenta de build do projeto.
- **Docker**: Utilizado para containerizar a aplicação, garantindo um ambiente de execução consistente e facilitando o deploy.
- **Google Cloud Run & Artifact Registry**: Plataforma serverless para a execução do contêiner e registro seguro para o armazenamento da imagem Docker.

## Funcionalidades

- **Integração com Telegram**: Responde a mensagens de usuários diretamente no Telegram através de um webhook.
- **Segurança**: O endpoint do webhook é protegido usando um token secreto (`X-Telegram-Bot-Api-Secret-Token`) para garantir que apenas o Telegram possa enviar requisições.
- **Assistente Especializado com IA**: Utiliza o LangChain4j e o Google Gemini para interpretar as mensagens dos usuários e fornecer respostas inteligentes e contextuais.
- **Gerenciamento de Credenciais**: As credenciais e tokens são gerenciados de forma segura através de variáveis de ambiente no Google Cloud Run, não sendo expostos no código-fonte.
- **Deploy Automatizado**: Inclui um script (`deploy.sh`) para automatizar o build da imagem Docker, o envio para o Artifact Registry e o deploy no Cloud Run com as configurações de ambiente necessárias.

## Configuração e Deploy

O deploy é feito em poucos passos utilizando o script `deploy.sh`.

### Passo 1: Configurar o Script `deploy.sh`

O arquivo `deploy.sh` contém as credenciais necessárias para a aplicação. **Este arquivo é ignorado pelo Git (`.gitignore`) por segurança.**

Abra o arquivo `deploy.sh` e preencha as seguintes variáveis com seus valores reais:

```sh
# --- CREDENCIAIS E SEGREDOS ---
GEMINI_API_KEY="SUA_GEMINI_API_KEY_AQUI"
TELEGRAM_SECRET_TOKEN="SEU_SECRET_TOKEN_AQUI"
```

### Passo 2: Executar o Deploy

Torne o script executável e rode-o.

```bash
chmod +x deploy.sh
./deploy.sh
```

O script irá construir a imagem, enviá-la para o Artifact Registry e implantar o serviço no Cloud Run. Ao final, ele exibirá a URL do serviço.

### Passo 3: Configurar o Webhook do Telegram

Após o deploy, você precisa informar ao Telegram para onde enviar as mensagens. Use o comando `curl` abaixo, substituindo os placeholders:

```bash
curl -F "url=URL_DO_SEU_SERVICO/webhook" \
     -F "secret_token=SEU_SECRET_TOKEN_AQUI" \
     https://api.telegram.org/botSEU_TOKEN_DO_BOT_AQUI/setWebhook
```

## Agradecimentos Especiais

Este projeto foi fortemente inspirado e utilizou conhecimentos dos seguintes recursos da comunidade:

- **Michelli Brito**: Pelo excelente conteúdo e projetos sobre LangChain4j.
  - [Repositório no GitHub](https://github.com/MichelliBrito/langchain4j)
- **Dev Soutinho (Mario Souto)**: Pelo vídeo inspirador sobre a criação de um bot com IA.
  - [Vídeo no YouTube](https://www.youtube.com/watch?v=A5i7D7RAPA4)

## Referências Oficiais

- **Documentação do LangChain4j**: [https://docs.langchain4j.dev/](https://docs.langchain4j.dev/)
