package com.service.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.service.search.DTO.CourseSearchRequest;
import com.service.search.DTO.CourseSearchResponse;
import com.service.search.model.CourseDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseSearchService {

    private final ElasticsearchClient elasticsearchClient;

    public CourseSearchResponse searchCourses(CourseSearchRequest request) {
        try {
            // 🌐 Build the bool query
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();

            // 🔍 Full-text search
            if (request.getQuery() != null && !request.getQuery().isBlank()) {
//                mustQueries.add(Query.of(q -> q
//                    .multiMatch(m -> m
//                        .fields("title", "description")
//                        .query(request.getQuery())
//                        .fuzziness("AUTO")        // 👈 Fuzziness added this is also correct 
            	
//                        .prefixLength(1)
//                        .maxExpansions(50)
//                    )
//                ));
            	
            	// Here i can make Make fuzziness configurable via the DTO 
            	
            	Query.of(q -> q
            		    .bool(b -> b
            		        .should(s1 -> s1.match(m -> m
            		            .field("title")
            		            .query(request.getQuery())
            		            .fuzziness("AUTO")
            		        ))
            		        .should(s2 -> s2.match(m -> m
            		            .field("description")
            		            .query(request.getQuery())
            		            .fuzziness("AUTO")
            		        ))
            		        .minimumShouldMatch("1")
            		    )
            		);

            }

            // 🔎 Filter: category
            if (request.getCategory() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("category")
                        .value(request.getCategory())
                    )
                ));
            }

            // 🔎 Filter: type
            if (request.getType() != null) {
                filterQueries.add(Query.of(q -> q
                    .term(t -> t
                        .field("type")
                        .value(request.getType())
                    )
                ));
            }

            // 🔎 Filter: price range
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                filterQueries.add(Query.of(q -> q
                    .range(r -> {
                        var rangeBuilder = new RangeQuery.Builder().field("price");
                        if (request.getMinPrice() != null) {
                            rangeBuilder.gte(JsonData.of(request.getMinPrice()));
                        }
                        if (request.getMaxPrice() != null) {
                            rangeBuilder.lte(JsonData.of(request.getMaxPrice()));
                        }
                        return rangeBuilder;
                    })
                ));
            }

            // 🔎 Filter: age range
            if (request.getMinAge() != null || request.getMaxAge() != null) {
                List<Query> ageQueries = new ArrayList<>();

                if (request.getMaxAge() != null) {
                    ageQueries.add(Query.of(q -> q.range(r ->
                        r.field("minAge").lte(JsonData.of(request.getMaxAge()))
                    )));
                }

                if (request.getMinAge() != null) {
                    ageQueries.add(Query.of(q -> q.range(r ->
                        r.field("maxAge").gte(JsonData.of(request.getMinAge()))
                    )));
                }

                // Only add if there are age conditions
                if (!ageQueries.isEmpty()) {
                    filterQueries.add(Query.of(q -> q
                        .bool(b -> b.must(ageQueries))
                    ));
                }
            }


            // 🧠 Combine must + filters
            Query finalQuery = Query.of(q -> q
                .bool(b -> b
                    .must(mustQueries)
                    .filter(filterQueries)
                )
            );

            // 📄 Sorting
            List<SortOptions> sort = new ArrayList<>();
            if (request.getSortBy() != null && request.getSortOrder() != null) {
                sort.add(SortOptions.of(s -> s
                    .field(f -> f
                        .field(request.getSortBy())
                        .order(request.getSortOrder().equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc)
                    )
                ));
            }

            // 🚀 Perform the search
            SearchResponse<CourseDocument> response = elasticsearchClient.search(s -> s
                    .index("courses")
                    .query(finalQuery)
                    .from(request.getPage() * request.getSize())
                    .size(request.getSize())
                    .sort(sort)
                    .trackTotalHits(t -> t.enabled(true)),

                CourseDocument.class
            );

            // ✅ Parse the response
            List<CourseDocument> results = response.hits().hits()
                .stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

            return new CourseSearchResponse(
                results,
                response.hits().total().value(),
                request.getPage(),
                request.getSize()
            );

        } catch (Exception e) {
            throw new RuntimeException("Error during search", e);
        }
    }

}
