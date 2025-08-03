package com.service.search.DTO;

import java.util.List;

import com.service.search.model.CourseDocument;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSearchResponse {
    private List<CourseDocument> results;
    private long totalHits;
    private int page;
    private int size;
}
