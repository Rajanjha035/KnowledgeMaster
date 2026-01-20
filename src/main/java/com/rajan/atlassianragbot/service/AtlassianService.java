package com.rajan.atlassianragbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Service
public class AtlassianService {

    private final RestClient restClient;

    public AtlassianService(
            @Value("${atlassian.domain}") String domain,
            @Value("${atlassian.email}") String email,
            @Value("${atlassian.api-token}") String apiToken) {
        // Ensure domain does not have a trailing slash to avoid double slashes in URLs
        String cleanDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((email + ":" + apiToken).getBytes());

        this.restClient = RestClient.builder()
                .baseUrl(cleanDomain)
                .defaultHeader("Authorization", authHeader)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String searchJira(String query) {
        // Simple JQL search
        String jql = "text ~ \"" + query + "\" OR summary ~ \"" + query + "\"";
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/api/3/search")
                            .queryParam("jql", jql)
                            .queryParam("maxResults", 5)
                            .queryParam("fields", "summary,description")
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error searching Jira: " + e.getMessage();
        }
    }

    public String searchConfluence(String query) {
        // Simple CQL search
        String cql = "text ~ \"" + query + "\"";

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/wiki/rest/api/content/search")
                            .queryParam("cql", cql)
                            .queryParam("limit", 5)
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error searching Confluence: " + e.getMessage();
        }
    }
}
