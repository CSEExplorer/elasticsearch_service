package com.service.search.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.service.search.DTO.CourseSearchRequest;
import com.service.search.DTO.CourseSearchResponse;
import com.service.search.model.CourseDocument;
import com.service.search.repository.CourseRepository;
import com.service.search.service.CourseSearchService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {
    
    private final CourseSearchService courseSearchService;
    private final CourseRepository courseRepository;
    
    @GetMapping("/search")
    public ResponseEntity<CourseSearchResponse> searchCourses(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("Search request - q: '{}', category: '{}', type: '{}', sort: '{}', page: {}, size: {}", 
                query, category, type, sort, page, size);
        
        // Map sort parameter to internal format
        String internalSort = mapSortParameter(sort);
        
        // Build search request
        CourseSearchRequest searchRequest = CourseSearchRequest.builder()
                .query(query)
                .minAge(minAge)
                .maxAge(maxAge)
                .category(category)
                .type(type)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .nextSessionDate(startDate)
                .sort(internalSort)
                .page(page)
                .size(size)
                .build();
        
        // Execute search - this returns CourseSearchResponse directly
        CourseSearchResponse searchResult = courseSearchService.searchCourses(searchRequest);
        
        log.info("Search completed - total: {}, returned: {}", 
                searchResult.getTotalHits(), searchResult.getCourses().size());
        
        return ResponseEntity.ok(searchResult);
    }
    
    @GetMapping("/courses")
    public List<CourseDocument> getAllCourses() {
        try {
            Iterable<CourseDocument> iterable = courseRepository.findAll();
            return StreamSupport.stream(iterable.spliterator(), false)
                                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // return empty list on failure
        }
    }
    
    /**
     * Maps external sort parameters to internal sort format
     * External: upcoming, priceAsc, priceDesc
     * Internal: dateAsc, priceAsc, priceDesc
     */
    private String mapSortParameter(String sort) {
        return switch (sort.toLowerCase()) {
            case "upcoming" -> "dateAsc";
            case "priceasc" -> "priceAsc";
            case "pricedesc" -> "priceDesc";
            default -> "dateAsc"; // Default to upcoming
        };
    }
    
    @GetMapping("/debug/simple")
    public ResponseEntity<String> debugSimple() {
        courseSearchService.debugIndexSimple();
        return ResponseEntity.ok("Debug info logged - check console");
    }
}