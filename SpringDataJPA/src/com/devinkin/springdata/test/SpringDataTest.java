package com.devinkin.springdata.test;

import com.devinkin.springdata.test.pojo.Person;
import com.devinkin.springdata.test.repository.PersonRepository;
import com.devinkin.springdata.test.service.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.sql.DataSource;
import java.awt.print.Pageable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class SpringDataTest {

    private ApplicationContext ctx = null;
    private PersonRepository personRepository = null;
    private PersonService personService;

    @Before
    public void initial() {
        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        personRepository = ctx.getBean(PersonRepository.class);
        personService = ctx.getBean(PersonService.class);
    }

    @Test
    public void testJpa() {

    }

    @Test
    public void testHelloSpringData() {
        System.out.println(personRepository.getClass().getName());
        Person aa = personRepository.getByLastName("AA");
        System.out.println(aa);
    }

    @Test
    public void testKeyWords() {
        List<Person> personList = personRepository.getByLastNameStartingWithAndIdLessThan("A", 5);
        List<Person> personList2 = personRepository.getByLastNameEndingWithAndIdLessThan("A", 5);
        List<Person> personList3 = personRepository.getByEmailInAndBirthLessThan(Arrays.asList("aa@163.com", "ff@163.com", "gg@163.com"),
                new Date());
        System.out.println(personList);
        System.out.println(personList2);
        System.out.println(personList3.size());
    }


    @Test
    public void testKeyWords2() {
        List<Person> persons = personRepository.getByAddress_IdGreaterThan(1);
        System.out.println(persons);
    }


    @Test
    public void testQueryAnnotation() {
        Person person = personRepository.getMaxIdPerson();
        System.out.println(person);
    }


    @Test
    public void testQueryAnnotationParam1() {
        List<Person> persons = personRepository.testQueryAnnotationParams1("AA", "aa@163.com");
        System.out.println(persons);
    }

    @Test
    public void testQueryAnnotationParam2() {
        List<Person> persons = personRepository.testQueryAnnotationParams2("aa@163.com","AA");
        System.out.println(persons);
    }


    @Test
    public void testQueryAnnotationLikeParam() {
//        List<Person> personList = personRepository.testQueryAnnotationLikeParam("AA", "%bb%");
        List<Person> personList = personRepository.testQueryAnnotationLikeParam("AA", "bb");
        System.out.println(personList);
    }


    @Test
    public void getTotalCount() {
        Long totalCount = personRepository.getTotalCount();
        System.out.println(totalCount);
    }

    @Test
    public void testModifying() {
//        personRepository.updatePersonEmail(1,"1aa1@163.com");
        personService.updatePersonEmail("1aa1@163.com", 1);
    }

    @Test
    public void testCrudRepository() {
        List<Person> personList = new ArrayList<>();
        for (int i = 'a'; i <= 'z'; i++) {
            Person person = new Person();
            person.setAddressId(i + 1);
            person.setBirth(new Date());
            person.setEmail((char)i + "" + (char)i + "@163.com");
            person.setLastName((char)i + "" + (char)i);
            personList.add(person);
        }
        personService.savePersons(personList);
    }


    @Test
    public void testPagingAndSortingRepository() {
        // pageNo从0开始
        int pageNo = 3;
        int pageSize = 5;

        // 排序相关
        Sort.Order order1 = new Sort.Order(Sort.Direction.DESC, "id");
        Sort.Order order2 = new Sort.Order(Sort.Direction.ASC, "email");
        Sort sort = new Sort(order1, order2);

        // Pageable接口通常使用的是其PageRequest实现类，其中封装了需要分页的信息
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Page<Person> page = personRepository.findAll(pageRequest);

        System.out.println("总记录数: " + page.getTotalElements());
        System.out.println("当前第几页: " + (page.getNumber()));
        System.out.println("总页数: " + page.getTotalPages());
        System.out.println("当前页的List: " + page.getContent());
        System.out.println("当前页面的记录数: " + page.getNumberOfElements());
    }

    @Test
    public void testJpaRepository() {
        Person person = new Person();
        person.setBirth(new Date());
        person.setEmail("XY@163.com");
        person.setLastName("xyz");
        person.setId(28);

        Person person2 = personRepository.saveAndFlush(person);
        System.out.println(person == person2);
    }


    /**
     * 目标：实现带查询条件的分页，条件id > 5
     * 调用JpaSpecificationExecutor的findAll()方法
     * Specification: 丰庄路 JPA Criteria查询的查询条件
     * Page
     * @throws SQLException
     */
    @Test
    public void testJpaSpecificationExecutor() {
        int pageNo = 3 - 1;
        int pageSize = 5;
        PageRequest pageAble = new PageRequest(pageNo, pageSize);

        // 通常使用Specification的匿名内部类
        Specification<Person> specification = new Specification<Person>() {
            /**
             *
             * @param root 查询的实
             * @param criteriaQuery  可以从中得到Root对象，即告知JPA Criteria查询要查询哪一个实体类，它可以来添加查询条件，还可以结合EntityManager对象得到最终的TypedQuery对象
             * @param criteriaBuilder CriteriaBuilder对象，用于创建Criteria相关对象的工厂，当然可以从中获取Predicate对象
             * @return Predicate类型，代表一个查询条件
             */
            @Override
            public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Path path = root.get("id");
                Predicate predicate = criteriaBuilder.gt(path,5);
                return predicate;
            }
        };
        Page<Person> page = personRepository.findAll(specification, pageAble);
        System.out.println("总记录数: " + page.getTotalElements());
        System.out.println("当前第几页: " + (page.getNumber() + 1));
        System.out.println("总页数: " + page.getTotalPages());
        System.out.println("当前页的List: " + page.getContent());
        System.out.println("当前页面的记录数: " + page.getNumberOfElements());
    }

    @Test
    public void testCustomRepositoryMethod() {
        personRepository.test();
    }

    @Test
    public void testDataSource() throws SQLException {
        DataSource dataSource = ctx.getBean(DataSource.class);
        System.out.println(dataSource.getConnection());
    }
}
