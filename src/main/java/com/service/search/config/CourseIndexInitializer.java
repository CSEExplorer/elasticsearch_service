package com.service.search.config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;

import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class CourseIndexInitializer {
    
    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "courses";
    
    @PostConstruct
    public void setupIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(INDEX_NAME))
                    .value();
                    
            if (exists) {
                log.info("Index '{}' already exists. Skipping creation.", INDEX_NAME);
                return;
            }
            
            CreateIndexResponse response = elasticsearchClient.indices().create(c -> c
                .index(INDEX_NAME)
                .settings(s -> s
                    .numberOfShards("1")
                    .numberOfReplicas("1")
                )
                .mappings(m -> m
                    .properties("id", Property.of(p -> p.keyword(k -> k)))
                    .properties("title", Property.of(p -> p.text(t -> t
                        .fields("keyword", Property.of(f -> f.keyword(k -> k)))
                    )))
                    .properties("description", Property.of(p -> p.text(t -> t)))
                    .properties("category", Property.of(p -> p.keyword(k -> k)))
                    .properties("type", Property.of(p -> p.keyword(k -> k)))
                    .properties("gradeRange", Property.of(p -> p.keyword(k -> k)))
                    .properties("minAge", Property.of(p -> p.integer(i -> i)))
                    .properties("maxAge", Property.of(p -> p.integer(i -> i)))
                    .properties("price", Property.of(p -> p.double_(d -> d)))
                    .properties("nextSessionDate", Property.of(p -> p.date(d -> d
                        .format("strict_date_optional_time||epoch_millis")
                    )))
                )
            );
            
            log.info("Index '{}' created successfully: {}", INDEX_NAME, response.acknowledged());
            
        } catch (IOException e) {
            log.error("Failed to create index '{}': {}", INDEX_NAME, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Elasticsearch index", e);
        } catch (Exception e) {
            log.error("Unexpected error while setting up index '{}': {}", INDEX_NAME, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Elasticsearch index", e);
        }
    }
}