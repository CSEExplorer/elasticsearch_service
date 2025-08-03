package com.service.search.DTO;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchRequest {
    
    private String query; // For full-text search on title and description
    
    @Min(0)
    private Integer minAge;
    
    @Max(150)
    private Integer maxAge;
    
    @Min(0)
    private Double minPrice;
    
    private Double maxPrice;
    
    private String category; // Exact filter
    
    private String type; // Exact filter
    
    private LocalDate nextSessionDate; // Date filter (on or after this date)
    
    @Builder.Default
    private String sort = "dateAsc"; // Default sort by nextSessionDate ascending
    // Possible values: "dateAsc", "priceAsc", "priceDesc"
    
    @Min(0)
    @Builder.Default
    private Integer page = 0; // Default page
    
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer size = 10; // Default page size
}