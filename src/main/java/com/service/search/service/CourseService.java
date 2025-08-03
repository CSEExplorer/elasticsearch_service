package com.service.search.service;

import  com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.search.model.CourseDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "courses";

    @PostConstruct
    public void loadCoursesFromJson() {
        try {
            // Step 1: Load and deserialize JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.findAndRegisterModules();

            InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream();
            List<CourseDocument> courses = mapper.readValue(inputStream, new TypeReference<>() {});

            // Step 2: Convert to bulk operations
            List<BulkOperation> bulkOperations = courses.stream()
                .map(course -> BulkOperation.of(op -> op
                    .index(i -> i
                        .index(INDEX_NAME)
                        .id(course.getId())
                        .document(course)
                    )
                ))
                .collect(Collectors.toList());

            // Step 3: Execute bulk indexing
            elasticsearchClient.bulk(b -> b
                .index(INDEX_NAME)
                .operations(bulkOperations)
            );

            log.info("✅ Indexed {} courses into Elasticsearch", courses.size());

        } catch (Exception e) {
            log.error("❌ Failed to load and index courses", e);
        }
    }
}
