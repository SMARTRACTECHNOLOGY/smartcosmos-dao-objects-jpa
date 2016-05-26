package net.smartcosmos.dao.objects.impl;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.UUID;

/**
 * Initially created by SMART COSMOS Team on May 26, 2016.
 *
 * T is the Entity type (e.g., net.smartcosmos.dao.objects.domain.ObjectEntity)
 */
public class SearchSpecifications<T>
{
    //
    // The public methods, which return specifications
    //
    public Specification<T> matchUuid(UUID uuid, String fieldName) {
        return (root, criteriaQuery, criteriaBuilder) ->
            uuidMatches(root, criteriaQuery, criteriaBuilder, uuid, fieldName);
    }

    public Specification<T> stringStartsWith(String startsWith, String queryParameter) {
        return (root, criteriaQuery, criteriaBuilder) ->
            stringStartsWith(root, criteriaQuery, criteriaBuilder, startsWith, queryParameter);
    }

    public Specification<T> stringEndsWith(String endsWith, String queryParameter) {
        return (root, criteriaQuery, criteriaBuilder) ->
            stringEndsWith(root, criteriaQuery, criteriaBuilder, endsWith, queryParameter);
    }

    public Specification<T> stringMatchesExactly(String matchesExactly, String queryParameter) {
        return (root, criteriaQuery, criteriaBuilder) ->
            stringMatchesExactly(root, criteriaQuery, criteriaBuilder, matchesExactly, queryParameter);
    }

    public Specification<T> numberLessThan(Number lessThan, String queryParameter) {
        return (root, criteriaQuery, criteriaBuilder) ->
            numberLessThan(root, criteriaQuery, criteriaBuilder, lessThan, queryParameter);
    }

    public Specification<T> numberGreaterThan(Number greaterThan, String queryParameter) {
        return (root, criteriaQuery, criteriaBuilder) ->
            numberGreaterThan(root, criteriaQuery, criteriaBuilder, greaterThan, queryParameter);
    }

    //
    // The private methods, which return Predicates
    //
    private Predicate uuidMatches(Root root, CriteriaQuery<?> query,
                                  CriteriaBuilder builder, UUID matches, String queryParameter) {
        return builder.equal(root.get(queryParameter), matches);
    }

    private Predicate stringStartsWith(Root root, CriteriaQuery<?> query,
                                       CriteriaBuilder builder, String startsWith, String queryParameter) {
        return builder.like(root.get(queryParameter), startsWith + "%");
    }

    private Predicate stringEndsWith(Root root, CriteriaQuery<?> query,
                                     CriteriaBuilder builder, String endsWith, String queryParameter) {
        return builder.like(root.get(queryParameter), "%" + endsWith);
    }

    private Predicate stringMatchesExactly(Root root, CriteriaQuery<?> query,
                                           CriteriaBuilder builder, String matchesExactly, String queryParameter) {
        return builder.equal(root.get(queryParameter), matchesExactly);
    }

    private Predicate numberLessThan(Root root, CriteriaQuery<?> query,
                                     CriteriaBuilder builder, Number numberLessThan, String queryParameter) {
        return builder.lt(root.get(queryParameter), numberLessThan);
    }

    private Predicate numberGreaterThan(Root root, CriteriaQuery<?> query,
                                        CriteriaBuilder builder, Number numberGreaterThan, String queryParameter) {
        return builder.gt(root.get(queryParameter), numberGreaterThan);
    }
}
