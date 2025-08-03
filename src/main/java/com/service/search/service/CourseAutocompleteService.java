package com.service.search.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.service.search.model.CourseDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseAutocompleteService {

    private final ElasticsearchClient elasticsearchClient;

    public List<String> getAutocompleteSuggestions(String field, String prefix) {
        try {
            SearchResponse<CourseDocument> response = elasticsearchClient.search(s -> s
                    .index("courses")
                    .size(10)
                    .query(q -> q
                        .matchPhrasePrefix(mpp -> mpp
                            .field(field)
                            .query(prefix)
                        )
                    ), CourseDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(doc -> getFieldValue(doc, field))
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(10)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Autocomplete failed", e);
        }
    }

    private String getFieldValue(CourseDocument doc, String field) {
        return switch (field) {
            case "title" -> doc.getTitle();
            case "category" -> doc.getCategory();
            case "description" -> doc.getDescription();
            default -> null;
        };
    }
}
