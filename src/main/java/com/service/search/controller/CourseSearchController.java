package com.service.search.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.service.search.DTO.CourseSearchRequest;
import com.service.search.DTO.CourseSearchResponse;
import com.service.search.service.CourseSearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class CourseSearchController {

    private final CourseSearchService courseSearchService;

    @PostMapping("/courses")
    public CourseSearchResponse searchCourses(@RequestBody CourseSearchRequest request) {
        return courseSearchService.searchCourses(request);
    }
}
