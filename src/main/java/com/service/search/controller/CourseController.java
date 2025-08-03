package com.service.search.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.search.service.CourseAutocompleteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseAutocompleteService autocompleteService;

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String field,
            @RequestParam String prefix
    ) {
        List<String> suggestions = autocompleteService.getAutocompleteSuggestions(field, prefix);
        return ResponseEntity.ok(suggestions);
    }
}

