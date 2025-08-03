package com.service.search.DTO;



import com.service.search.model.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchResponse {
    
    private List<CourseDocument> courses;
    private long totalHits;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    
    // Constructor that matches your service call
    public CourseSearchResponse(List<CourseDocument> courses, long totalHits, Integer page, Integer size) {
        this.courses = courses;
        this.totalHits = totalHits;
        this.currentPage = page != null ? page : 0;
        this.pageSize = size != null ? size : 10;
        this.totalPages = (int) Math.ceil((double) totalHits / this.pageSize);
    }
    
    // Additional convenience methods
    public boolean hasNextPage() {
        return currentPage < totalPages - 1;
    }
    
    public boolean hasPreviousPage() {
        return currentPage > 0;
    }
    
    public int getNumberOfElements() {
        return courses != null ? courses.size() : 0;
    }
    
    public boolean isEmpty() {
        return courses == null || courses.isEmpty();
    }
}