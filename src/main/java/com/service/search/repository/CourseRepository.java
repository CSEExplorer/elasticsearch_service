package com.service.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.service.search.model.CourseDocument;


import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {

    // Find by exact category
    List<CourseDocument> findByCategory(String category);

    // Find all courses with a specific type (ONE_TIME, COURSE, CLUB)
    List<CourseDocument> findByType(String type);

    // Search by partial match in title (you can improve this with custom queries or analyzers later)
    List<CourseDocument> findByTitleContainingIgnoreCase(String keyword);

    // Filter by age range
    List<CourseDocument> findByMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(int ageMin, int ageMax);

    // Filter by price range
    List<CourseDocument> findByPriceBetween(double min, double max);

    // Find courses whose nextSessionDate is after a given date
    List<CourseDocument> findByNextSessionDateAfter(ZonedDateTime date);

    // Combine filters (optional example)
    List<CourseDocument> findByCategoryAndType(String category, String type);
}
