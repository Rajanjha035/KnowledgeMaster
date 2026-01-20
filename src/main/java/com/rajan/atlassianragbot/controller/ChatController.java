package com.rajan.atlassianragbot.controller;

import com.rajan.atlassianragbot.service.AnswerService;
import com.rajan.atlassianragbot.service.ImageService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AnswerService answerService;
    private final ImageService imageService;

    public ChatController(AnswerService answerService, ImageService imageService) {
        this.answerService = answerService;
        this.imageService = imageService;
    }

    @PostMapping("/submit")
    public Map<String, String> submit(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        if (answerService.isImageRequest(query)) {
            String imageUrl = imageService.generateImage(query);
            return Map.of("type", "image", "content", imageUrl);
        } else {
            String answer = answerService.answerQuestion(query);
            return Map.of("type", "text", "content", answer);
        }
    }
}
