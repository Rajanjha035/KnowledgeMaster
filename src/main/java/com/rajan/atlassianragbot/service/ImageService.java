package com.rajan.atlassianragbot.service;

import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private final ImageClient imageClient;

    public ImageService(ImageClient imageClient) {
        this.imageClient = imageClient;
    }

    public String generateImage(String promptText) {
        ImageResponse response = imageClient.call(new ImagePrompt(promptText));
        return response.getResult().getOutput().getUrl();
    }
}