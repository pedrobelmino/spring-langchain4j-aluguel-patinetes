
package br.com.pedrobelmino.langchain4j.hello.bot;

import br.com.pedrobelmino.langchain4j.hello.service.AssistantAiService;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MyTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;

    @Autowired
    @Qualifier("assistantAiService")
    private AssistantAiService patineteChatService;

    public MyTelegramBot(@Value("${telegram.bot.username}") String botUsername,
                         @Value("${telegram.bot.path}") String botPath) {
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.botToken = null;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
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

            Result<String> responseText = patineteChatService.handleRequest(messageText);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(responseText.content());
            return message;
        }
        return null;
    }
}
