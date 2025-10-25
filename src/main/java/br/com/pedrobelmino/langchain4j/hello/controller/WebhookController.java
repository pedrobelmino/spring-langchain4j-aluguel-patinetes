
package br.com.pedrobelmino.langchain4j.hello.controller;

import br.com.pedrobelmino.langchain4j.hello.bot.MyTelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final MyTelegramBot myTelegramBot;
    private final String secretToken;

    public WebhookController(MyTelegramBot myTelegramBot, 
                             @Value("${telegram.bot.secret-token}") String secretToken) {
        this.myTelegramBot = myTelegramBot;
        this.secretToken = secretToken;
    }

    @PostMapping("${telegram.bot.path}")
    public ResponseEntity<BotApiMethod<?>> onUpdateReceived(
            @RequestBody Update update,
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String token) {
        
        // Verifica se o token recebido é válido
        if (!secretToken.equals(token)) {
            // Se o token for inválido, retorna um erro de acesso negado.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BotApiMethod<?> response = myTelegramBot.onWebhookUpdateReceived(update);
        return ResponseEntity.ok(response);
    }
}
