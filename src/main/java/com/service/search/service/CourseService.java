package com.service.search.service;

import  com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.service.search.model.CourseDocument;
import com.service.search.repository.CourseRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    @PostConstruct
    public void loadCoursesFromJson() {
    	try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // 👈 Add this line
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional: make date format readable
            
            InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream();
            List<CourseDocument> courses = mapper.readValue(inputStream, new TypeReference<>() {});
            courseRepository.saveAll(courses);
            System.out.println("✅ Courses indexed into Elasticsearch");
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // 🔍 Search by category
    public List<CourseDocument> searchByCategory(String category) {
        return courseRepository.findByCategory(category);
    }

    // 🔍 Search by type (ONE_TIME, COURSE, CLUB)
    public List<CourseDocument> searchByType(String type) {
        return courseRepository.findByType(type);
    }

    // 🔍 Search by keyword in title (case-insensitive)
    public List<CourseDocument> searchByTitle(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // 🔍 Search by age range (inclusive logic)
    public List<CourseDocument> searchByAgeRange(int age) {
        return courseRepository.findByMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(age, age);
    }

    // 🔍 Search courses within price range
    public List<CourseDocument> searchByPriceRange(double min, double max) {
        return courseRepository.findByPriceBetween(min, max);
    }

    // 🔍 Upcoming courses after a specific date
    public List<CourseDocument> searchByUpcomingSessions(ZonedDateTime date) {
        return courseRepository.findByNextSessionDateAfter(date);
    }

    // 🧩 Advanced search: category + type
    public List<CourseDocument> searchByCategoryAndType(String category, String type) {
        return courseRepository.findByCategoryAndType(category, type);
    }

    // 📦 Save all courses (useful for bootstrapping from JSON)
    public void saveAll(List<CourseDocument> courses) {
        courseRepository.saveAll(courses);
    }

    // 🧹 Delete all
    public void deleteAll() {
        courseRepository.deleteAll();
    }

    // You can add search methods here later
}
