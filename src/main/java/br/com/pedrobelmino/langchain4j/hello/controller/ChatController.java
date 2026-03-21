package br.com.pedrobelmino.langchain4j.hello.controller;

import br.com.pedrobelmino.langchain4j.hello.service.AssistantAiService;
import br.com.pedrobelmino.langchain4j.hello.service.AssistantTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private GoogleAiGeminiChatModel chatLanguageModel;

    @Autowired
    private AssistantTools assistantTools;

    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : "default";
        String message = request.message();

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Mensagem não pode ser vazia.", sessionId));
        }

        ChatMemory chatMemory = memories.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(20));

        AssistantAiService assistant = AiServices.builder(AssistantAiService.class)
                .chatModel(chatLanguageModel)
                .tools(assistantTools)
                .chatMemory(chatMemory)
                .build();

        Result<String> result = assistant.handleRequest(message);

        return ResponseEntity.ok(new ChatResponse(result.content(), sessionId));
    }

    public record ChatRequest(String sessionId, String message) {}

    public record ChatResponse(String response, String sessionId) {}
}
