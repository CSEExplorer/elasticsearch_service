package com.service.search.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.service.search.DTO.CourseSearchRequest;
import com.service.search.DTO.CourseSearchResponse;
import com.service.search.model.CourseDocument;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSearchService {
    
    private final ElasticsearchClient elasticsearchClient;
    
    /**
     * Main search method that executes the complete search workflow
     */
    public CourseSearchResponse searchCourses(CourseSearchRequest searchRequest) {
        try {
            log.info("Executing course search with parameters: {}", searchRequest);
            
            // Build the Elasticsearch search request
            SearchRequest request = buildSearchRequest(searchRequest);
            
            // Execute search against Elasticsearch - Fixed: Use CourseDocument.class instead of Course.class
            SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);
            System.out.println("HITS:");
            for (Hit<CourseDocument> hit : response.hits().hits()) {
                System.out.println(hit.source()); // ← This should never be null
            }
            // Extract course objects from search hits
            List<CourseDocument> courses = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
            
            // Get total number of matching documents
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;
            
            log.info("Search executed successfully - found {} total courses, returning {} for page {}", 
                    totalHits, courses.size(), searchRequest.getPage());
            
            return new CourseSearchResponse(courses, totalHits, searchRequest.getPage(), searchRequest.getSize());
            
        } catch (Exception e) {
            log.error("Error executing search query for request: {}", searchRequest, e);
            throw new RuntimeException("Failed to execute course search", e);
        }
    }
    
    /**
     * Builds the complete Elasticsearch SearchRequest object
     */
    private SearchRequest buildSearchRequest(CourseSearchRequest searchRequest) {
        return SearchRequest.of(s -> s
            .index("courses")                              // Target the courses index
            .query(buildQuery(searchRequest))              // Apply all filters and search terms
            .sort(buildSort(searchRequest))                // Apply sorting logic
            .from(searchRequest.getPage() * searchRequest.getSize())  // Pagination offset
            .size(searchRequest.getSize())                 // Number of results per page
        );
    }
    
    /**
     * Builds the boolean query with all filters and search conditions
     */
    private Query buildQuery(CourseSearchRequest searchRequest) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        
        // 1. FULL-TEXT SEARCH: Multi-match query on title and description
        if (StringUtils.hasText(searchRequest.getQuery())) {
            boolQuery.must(Query.of(q -> q
                .multiMatch(mm -> mm
                    .query(searchRequest.getQuery())
                    .fields("title", "description")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                )
            ));
            log.debug("Added full-text search for: '{}'", searchRequest.getQuery());
        }
        
        // 2. AGE RANGE FILTERS
        // If user has minAge, find courses where maxAge >= user's minAge
        if (searchRequest.getMinAge() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> r
                    .field("maxAge")
                    .gte(co.elastic.clients.json.JsonData.of(searchRequest.getMinAge()))
                )
            ));
            log.debug("Added minAge filter: {}", searchRequest.getMinAge());
        }
        
        // If user has maxAge, find courses where minAge <= user's maxAge
        if (searchRequest.getMaxAge() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> r
                    .field("minAge")
                    .lte(co.elastic.clients.json.JsonData.of(searchRequest.getMaxAge()))
                )
            ));
            log.debug("Added maxAge filter: {}", searchRequest.getMaxAge());
        }
        
        // 3. PRICE RANGE FILTERS
        if (searchRequest.getMinPrice() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> r
                    .field("price")
                    .gte(co.elastic.clients.json.JsonData.of(searchRequest.getMinPrice()))
                )
            ));
            log.debug("Added minPrice filter: {}", searchRequest.getMinPrice());
        }
        
        if (searchRequest.getMaxPrice() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> r
                    .field("price")
                    .lte(co.elastic.clients.json.JsonData.of(searchRequest.getMaxPrice()))
                )
            ));
            log.debug("Added maxPrice filter: {}", searchRequest.getMaxPrice());
        }
        
        // 4. EXACT FILTERS: Category and Type (keyword fields)
        if (StringUtils.hasText(searchRequest.getCategory())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t  // ✅ Changed back to .term() for keyword field
                    .field("category")
                    .value(searchRequest.getCategory())  // ✅ Use .value() not .query()
                )
            ));
            log.debug("Added category filter: '{}'", searchRequest.getCategory());
        }
        
        if (StringUtils.hasText(searchRequest.getType())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t  // ✅ Changed back to .term()
                    .field("type")
                    .value(searchRequest.getType())  // ✅ Use .value()
                )
            ));
            log.debug("Added type filter: '{}'", searchRequest.getType());
        }
//        if (searchRequest.getNextSessionDate() != null) {
//            boolQuery.filter(Query.of(q -> q
//                .range(r -> r
//                    .field("nextSessionDate")
//                    .gte(JsonData.of(searchRequest.getNextSessionDate()))  // Date in ISO-8601
//                )
//            ));
//            log.debug("Added nextSessionDate filter: {}", searchRequest.getNextSessionDate());
//        }

        
      
        
        return Query.of(q -> q.bool(boolQuery.build()));
    }
    
    /**
     * Builds sorting options based on the sort parameter
     */
 // 2. FIXED SORT METHOD - Remove title.keyword (not available in mapping)
    private List<SortOptions> buildSort(CourseSearchRequest searchRequest) {
        List<SortOptions> sortOptions = new ArrayList<>();
        
        String sortParam = searchRequest.getSort();
        log.debug("Applying sort: {}", sortParam);
        
        switch (sortParam) {
            case "priceAsc":
                sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f.field("price").order(SortOrder.Asc))
                ));
                break;
                
            case "priceDesc":
                sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f.field("price").order(SortOrder.Desc))
                ));
                break;
                
            case "categoryAsc":
                // Sort by category A-Z (category is keyword type)
                sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f.field("category").order(SortOrder.Asc))
                ));
                break;
                
            case "categoryDesc":
                // Sort by category Z-A
                sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f.field("category").order(SortOrder.Desc))
                ));
                break;
                
            case "titleAsc":
            case "titleDesc":
            case "dateAsc":
            case "upcoming":
            case "relevance":
            default:
                // Default: Sort by relevance/score
                sortOptions.add(SortOptions.of(s -> s
                    .score(sc -> sc.order(SortOrder.Desc))
                ));
                log.debug("Using relevance/score sorting as default");
                break;
        }
        
        return sortOptions;
    }

    
    public void debugIndexSimple() {
        try {
            log.info("🔍 DEBUGGING ELASTICSEARCH INDEX...");
            
            SearchRequest request = SearchRequest.of(s -> s
                .index("courses")
                .query(Query.of(q -> q.matchAll(ma -> ma)))
                .size(10)
            );
            
            SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);
            
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;
            log.info("🔍 TOTAL DOCUMENTS: {}", totalHits);
            
            if (totalHits == 0) {
                log.warn("❌ INDEX IS EMPTY! No documents found.");
                return;
            }
            
            log.info("🔍 SAMPLE DOCUMENTS:");
            for (Hit<CourseDocument> hit : response.hits().hits()) {
                CourseDocument doc = hit.source();
                if (doc != null) {
                    log.info("  📄 ID: {}", hit.id());
                    log.info("     Title: '{}'", doc.getTitle());
                    log.info("     Category: '{}'", doc.getCategory());
                    log.info("     Type: '{}'", doc.getType());
                    // ✅ REMOVED: Date field no longer exists
                    log.info("     Price: {}", doc.getPrice());
                    log.info("     MinAge: {}, MaxAge: {}", doc.getMinAge(), doc.getMaxAge());
                    log.info("  ---");
                } else {
                    log.warn("  ❌ Document source is null for ID: {}", hit.id());
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Debug failed: {}", e.getMessage(), e);
        }
    }

    
    
}