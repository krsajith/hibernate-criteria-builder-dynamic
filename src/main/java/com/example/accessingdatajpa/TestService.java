package com.example.accessingdatajpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    private final EntityManager entityManager;


    public TestService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void test(){

    }
}
