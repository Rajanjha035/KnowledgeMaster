package com.rajan.atlassianragbot.service;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnswerService {

    private final AtlassianService atlassianService;
    private final ChatClient chatClient;
    private final List<Message> conversationHistory = new ArrayList<>();

    public AnswerService(AtlassianService atlassianService, ChatClient chatClient) {
        this.atlassianService = atlassianService;
        this.chatClient = chatClient;
    }

    public String answerQuestion(String userQuestion) {
        // 1. Retrieve Context
        String jiraData = atlassianService.searchJira(userQuestion);
        String confluenceData = atlassianService.searchConfluence(userQuestion);

        String combinedContext = "JIRA DATA:\n" + jiraData + "\n\nCONFLUENCE DATA:\n" + confluenceData;

        // 2. Construct Prompt
        String systemText = """
                You are a friendly and knowledgeable AI assistant.
                
                You have access to the following context from Jira and Confluence:
                CONTEXT:
                {context}
                
                Instructions:
                - If the user's question is related to the provided context, answer based on that information.
                - If the context is empty or irrelevant, answer the question using your general knowledge.
                - Engage in natural, human-like conversation.
                - If the user asks for a diagram, flowchart, draw.io, or Visio diagram, generate it using Mermaid.js syntax wrapped in ```mermaid ... ``` blocks.
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", combinedContext));
        
        UserMessage userMessage = new UserMessage(userQuestion);
        conversationHistory.add(userMessage);

        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(conversationHistory);

        // 3. Call AI
        Prompt prompt = new Prompt(messages);
        String response = chatClient.call(prompt).getResult().getOutput().getContent();
        conversationHistory.add(new AssistantMessage(response));
        return response;
    }

    public boolean isImageRequest(String userInput) {
        String promptText = """
                Analyze the following input. Does the user explicitly ask to generate a visual image, picture, or photograph using an image generation model?
                Note: Requests for diagrams, flowcharts, graphs, or mermaid diagrams should be classified as NO.
                Answer only YES or NO.
                Input: %s
                """.formatted(userInput);
        Prompt prompt = new Prompt(promptText);
        String response = chatClient.call(prompt).getResult().getOutput().getContent();
        return response.trim().toUpperCase().startsWith("YES");
    }
}
