package com.service.search.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDocument {

    private String id;

    private String title;

    private String description;

    private String category;  // E.g., "Math", "Science", etc.

    private String type;      // "ONE_TIME", "COURSE", "CLUB"

    private String gradeRange;  // e.g., "1st–3rd"

    private Integer minAge;

    private Integer maxAge;

    private Double price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant nextSessionDate;
}
