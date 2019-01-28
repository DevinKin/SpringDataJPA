package com.devinkin.springdata.test.repository;

import com.devinkin.springdata.test.dao.PersonDao;
import com.devinkin.springdata.test.pojo.Person;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class PersonRepositoryImpl implements PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void test() {
        Person person = entityManager.find(Person.class, 11);
        System.out.println("-->" + person);
    }
}
