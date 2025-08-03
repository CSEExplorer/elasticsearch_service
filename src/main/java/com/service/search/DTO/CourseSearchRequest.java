package com.service.search.DTO;

import lombok.Data;

@Data
public class CourseSearchRequest {
    private String query;               // for full-text search
    private String category;            // filter by category
    private String type;                // ONE_TIME, COURSE, CLUB
    private Double minPrice;
    private Double maxPrice;
    private Integer minAge;
    private Integer maxAge;
    private String sortBy;              // e.g. "price", "nextSessionDate"
    private String sortOrder;           // "asc" or "desc"
    private Integer page = 0;
    private Integer size = 10;
}
