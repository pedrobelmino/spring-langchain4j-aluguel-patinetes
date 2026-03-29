package br.com.pedrobelmino.langchain4j.hello.controller;

import br.com.pedrobelmino.langchain4j.hello.service.AssistantAiService;
import br.com.pedrobelmino.langchain4j.hello.service.AssistantTools;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final GoogleAiGeminiChatModel chatLanguageModel;
    private final AssistantTools assistantTools;

    private record SessionContext(ChatMemory memory, AssistantAiService assistant) {}

    private final Cache<String, SessionContext> sessions = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    public ChatController(GoogleAiGeminiChatModel chatLanguageModel, AssistantTools assistantTools) {
        this.chatLanguageModel = chatLanguageModel;
        this.assistantTools = assistantTools;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : "default";
        String message = request.message();

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Mensagem não pode ser vazia.", sessionId));
        }

        SessionContext ctx = sessions.get(sessionId, id -> {
            ChatMemory memory = MessageWindowChatMemory.withMaxMessages(20);
            AssistantAiService assistant = AiServices.builder(AssistantAiService.class)
                    .chatModel(chatLanguageModel)
                    .tools(assistantTools)
                    .chatMemory(memory)
                    .build();
            return new SessionContext(memory, assistant);
        });

        Result<String> result = ctx.assistant().handleRequest(message);

        return ResponseEntity.ok(new ChatResponse(result.content(), sessionId));
    }

    public record ChatRequest(String sessionId, String message) {}

    public record ChatResponse(String response, String sessionId) {}
}
