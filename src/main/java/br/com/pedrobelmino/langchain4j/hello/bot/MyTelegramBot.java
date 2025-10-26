
package br.com.pedrobelmino.langchain4j.hello.bot;

import br.com.pedrobelmino.langchain4j.hello.service.AssistantAiService;
import br.com.pedrobelmino.langchain4j.hello.service.AssistantTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MyTelegramBot extends TelegramWebhookBot {

    private String botUsername;
    private String botPath;

    @Autowired
    private GoogleAiGeminiChatModel model;

    @Autowired
    private AssistantTools assistantTools;

    @Autowired
    private GoogleAiGeminiChatModel chatLanguageModel;

    private final Map<Long, ChatMemory> memories = new ConcurrentHashMap<>();

    public MyTelegramBot(@Value("${telegram.bot.username}") String botUsername,
                         @Value("${telegram.bot.path}") String botPath) {
        this.botUsername = botUsername;
        this.botPath = botPath;
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return null;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
             String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            ChatMemory chatMemory = memories.computeIfAbsent(chatId, id -> MessageWindowChatMemory.withMaxMessages(10));

            AssistantAiService patineteChatService = AiServices.builder(AssistantAiService.class)
                    .chatModel(chatLanguageModel)
                    .tools(assistantTools)
                    .chatMemory(chatMemory)
                    .build();

            Result<String> responseText = patineteChatService.handleRequest(messageText);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(responseText.content());
            return message;
        }
        return null;
    }
}
