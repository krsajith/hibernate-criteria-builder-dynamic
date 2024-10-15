package com.example.accessingdatajpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;


@SuppressWarnings("unused")
@Slf4j
public class JpaSpecBuilder<T> {

    public Specification<T> buildSpec(Set<SearchCriteria> searchCriteriaList, String tenantId) {
        Specification<T> spec = equals("tenantId", tenantId);
        for (var searchCriteria : searchCriteriaList) {
            try {
                spec = spec.and(spec.and(buildCriteria(searchCriteria)));
            } catch (Exception e) {
                log.debug("Error building criteria {}", searchCriteria);
            }
        }
        return spec;
    }

    public Specification<T> buildOrSpec(Set<SearchCriteria> searchCriteriaList, String tenantId) {
        Specification<T> spec = equals("tenantId", tenantId);
        for (var searchCriteria : searchCriteriaList) {
            try {
                spec = spec.and(spec.or(buildCriteria(searchCriteria)));
            } catch (Exception e) {
                log.debug("Error building criteria {}", searchCriteria);
            }
        }
        return spec;
    }

    private Specification<T> buildCriteria(SearchCriteria criteria) {
        switch (criteria.getOperation()) {
            case "in" -> {
                if (criteria.getValue() instanceof List<?> list) {
                    return in(criteria.getKey(), list);
                }
            }
            case "notEquals" -> {
                return notEquals(criteria.getKey(),criteria.getValue());
            }
            case "betweenDate" -> {
                if (criteria.getValue() instanceof Map<?, ?> map &&
                        map.get("startDate") instanceof LocalDateTime startDate
                        && map.get("endDate") instanceof LocalDateTime endDate) {
                    return betweenDate(criteria.getKey(), startDate, endDate);
                }
            }
            case "greaterThanOrEqualToDate" -> {
                if (criteria.getValue() instanceof LocalDate date) {
                    return greaterThanOrEqualToDate(criteria.getKey(), date);
                }
            }
            case "lessThanOrEqualToDate" -> {
                if (criteria.getValue() instanceof LocalDate date) {
                    return lessThanOrEqualToDate(criteria.getKey(), date);
                }
            }
            case "equalsOrNull" -> {
                return equalsOrNull(criteria.getKey(),  criteria.getValue());
            }
            default -> {
                return equals(criteria.getKey(), criteria.getValue());
            }
        }
        return null;
    }

    public Specification<T> in(String field, List<?> values) {
        boolean isString = values.get(0) instanceof String;
        List<?> stringList = isString ? values.stream().map(v -> ((String) v).toLowerCase()).toList() : values;
        return (root, query, criteriaBuilder) ->
                isString ? criteriaBuilder.lower(root.get(field)).in(stringList) : root.get(field).in(values);

    }
    public Specification<T> notEquals(String field, Object value) {
        boolean isString = value instanceof String;
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(isString?criteriaBuilder.lower(root.get(field)):root.get(field), isString ? ((String) value).toLowerCase() : value);
    }

    public Specification<T> equals(String field, Object value) {
        return (root, query, criteriaBuilder) ->
                getEqualPredicate(field, value, root, criteriaBuilder);
    }

    private Predicate getEqualPredicate(String field, Object value, Root<T> root, CriteriaBuilder criteriaBuilder) {
        boolean isString = value instanceof String;
        if(isString){
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get(field)), ((String) value).toLowerCase());
        }
        return criteriaBuilder.equal(root.get(field),value);

    }

    public Specification<T> equalsOrNull(String field, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(criteriaBuilder.isNull(root.get(field)),
                        getEqualPredicate(field, value, root, criteriaBuilder));
    }

    private Specification<T> betweenDate(String field, LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(field), start, end);
    }

    private Specification<T> greaterThanOrEqualToDate(String field, LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(field), date);
    }
    private Specification<T> lessThanOrEqualToDate(String field, LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(field), date);
    }

    public void addCriteria(Set<SearchCriteria> criteriaList, String field, String value, String operator) {
        if(StringUtils.hasText(value) && !"ALL".equalsIgnoreCase(value)) {
            criteriaList.add(new SearchCriteria(field,operator,value));
        }
    }
}
