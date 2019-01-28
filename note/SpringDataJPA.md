# Spring Data

## SpringData概述

- SpringData是Spring的一个子项目，用于简化数据库访问，支持NoSQL和关系型数据库。
- SpringData支持的NoSQL存储
  - MongoDB （文档数据库）
  - Neo4j（图形数据库）
  - Redis（键/值存储）
  - Hbase（列族数据库）
- SpringData所支持的关系型存储技术
  - JDBC
  - JPA

## Spring Data JPA 概述

- Spring Data JPA 致力于减少数据库访问层的开发量，开发者唯一需要做的是，就声明持久层的接口，其他交给Spring Data JPA完成。

## Spring Data JPA  HelloWorld

- 整合SpringDataJPA
- 编写HelloWorld程序

### 整合步骤

- 配置Spring整合JPA

  - `applicationContext.xml`

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xmlns:tx="http://www.springframework.org/schema/tx"
           xmlns:jpa="http://www.springframework.org/schema/data/jpa"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    		http://www.springframework.org/schema/data/jpa http://www.springframework.org/schema/data/jpa/spring-jpa-1.3.xsd
    		http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd
    		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd">
    
        <!-- 1. 配置数据源 -->
        <context:property-placeholder location="classpath:db.properties"/>
    
        <bean id="dataSource"
            class="com.mchange.v2.c3p0.ComboPooledDataSource">
            <property name="user" value="${jdbc.user}"/>
            <property name="password" value="${jdbc.password}"/>
            <property name="driverClass" value="${jdbc.driverClass}"/>
            <property name="jdbcUrl" value="${jdbc.jdbcUrl}"/>
    
            <!-- 配置连接池其他属性 -->
        </bean>
    
        <!-- 2. 配置JPA的EntityManagerFactory -->
        <bean id="entityManagerFactory" 
              class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
            <property name="dataSource" ref="dataSource"/>
            <property name="jpaVendorAdapter">
                <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter"></bean>
            </property>
            <property name="packagesToScan" value="com.devinkin.springdata"></property>
            <property name="jpaProperties">
                <props>
                    <!-- 二级缓存相关 -->
                    <!--
                    <prop key="hibernate.cache.region.factory_class">org.hibernate.cache.ehcache.EhCacheRegionFactory</prop>
                    <prop key="net.sf.ehcache.configurationResourceName">ehcache-hibernate.xml</prop>
                    -->
                    <!-- 生成的数据表的列的映射策略 -->
                    <prop key="hibernate.ejb.naming_strategy">org.hibernate.cfg.ImprovedNamingStrategy</prop>
                    <!-- hibernate 基本属性 -->
                    <prop key="hibernate.dialect">org.hibernate.dialect.MySQL5InnoDBDialect</prop>
                    <prop key="hibernate.show_sql">true</prop>
                    <prop key="hibernate.format_sql">true</prop>
                    <prop key="hibernate.hbm2ddl.auto">update</prop>
                </props>
            </property>
        </bean>
    
        <!-- 3. 配置事务管理器 -->
        <bean id="transactionManager"
              class="org.springframework.orm.jpa.JpaTransactionManager">
            <property name="entityManagerFactory" ref="entityManagerFactory"/>
        </bean>
    
        <!-- 4. 配置支持注解的事务 -->
        <tx:annotation-driven transaction-manager="transactionManager"/>
        <!-- 5. 配置SpringData -->
    </beans>
    ```

- 在Spring配置文件中配置Spring Data

  - 添加jpa的命名空间`xmlns:jpa="http://www.springframework.org/schema/data/jpa`

  - 在`applicationContext.xml`中添加SpringData的配置

    - `base-package`扫描Repository Bean所在的package

    ```xml
    <jpa:repositories base-package="com.devinkin.springdata"
    	entity-manager-factory-ref="entityManagerFactory">
    </jpa:repositories>
    ```

- 声明持久层接口，该接口继承`Repository`，在接口中声明需要的方法

  ```java
  package com.devinkin.springdata.test.repository;
  
  
  import com.devinkin.springdata.test.pojo.Person;
  import org.springframework.data.repository.Repository;
  
  public interface PersonRepository extends Repository<Person, Integer> {
      // 根据lastName来获取对应的Person
      Person getByLastName(String lastName);
  }
  ```

- 编写测试方法

  ```java
  @Test
  public void testHelloSpringData() {
      PersonRepository personRepository = ctx.getBean(PersonRepository.class);
      Person aa = personRepository.getByLastName("AA");
      System.out.println(aa);
  }
  ```

## Repository接口

- `Repository`是一个空接口，即是一个标记接口。
- 若我们定义的接口继承了`Repository`，则该接口会被IOC容器识别为一个`Repository Bean`纳入到IOC容器中，进而可以在该接口中定义满足一定规范的方法。
- 实际上可以通过`@RepositoryDefinition`注解来替代`Repository`接口。
  - `@RepositoryDefinition(domainClass = Person.class, idClass = Integer.class)`
- 基础的`Repository`提供了最基本的数据访问功能，其几个子接口则扩展了一些功能，它们的继承关系如下
  - `Repository`仅仅是一个标识，表明任何继承它的均为仓库接口类。
  - `CrudRepository`继承`Repository`，实现一组CRUD相关的方法。
  - `PagingAndSortingRepository`继承`CrudRepository`实现了一组分页排序相关的方法。
  - `JpaRepository`继承`PagingAndSortingRepository`，实现一组JPA规范相关的方法。
  - `自定义的XxxRepository`需要继承`JpaRepository`，这样的`XxxxRepository`接口就具备了通用的数据访问控制层的能力。
  - `JpaSpecificationExecutor`不属于Repository体系，实现一组`JPACriteria`查询相关的方法。

## Repository查询方法定义规范

### 在Repository子接口中声明方法的规范

- 查询方法以`find`，`read`，`get`开头。

- 涉及查询条件时，条件的属性作用条件关键字连接。

- 要注意的是，条件属性以首字母大写。

- 支持属性的级联查询。

- 若当前类有符合条件的属性，则优先使用。而不使用级联属性。

- 若需要使用级联属性，则属性之间使用`_`进行连接。示例`getByAddress_IdGreaterThan`

- 使用示例

  ```java
  package com.devinkin.springdata.test.repository;
  
  
  import com.devinkin.springdata.test.pojo.Person;
  import org.springframework.data.repository.Repository;
  import org.springframework.data.repository.RepositoryDefinition;
  
  import java.util.Date;
  import java.util.List;
  
  //public interface PersonRepository extends Repository<Person, Integer> {
  @RepositoryDefinition(domainClass = Person.class, idClass = Integer.class)
  public interface PersonRepository {
      // 根据lastName来获取对应的Person
      Person getByLastName(String lastName);
  
      // WHERE lastName LIKE ?% AND id < ?
      List<Person> getByLastNameStartingWithAndIdLessThan(String lastName, Integer id);
  
      // WHERE lastName LIKE %? AND id < ?
      List<Person> getByLastNameEndingWithAndIdLessThan(String lastName, Integer id);
  
      // WHERE email IN (?, ?, ?) OR birth < ?
      List<Person> getByEmailInAndBirthLessThan(List<String> emails, Date birth);
  
      // WHERE a.id > ?
      List<Person> getByAddress_IdGreaterThan(Integer id);
  }
  ```

  ```java
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
  ```


## Query接口

- 使用`@Query`注解，可以自定义JPQL语句以实现更灵活的查询。
- 为`@Query`注解传递参数的方式
  - 使用占位符`?#`，`#`为第几个参数，1,2,3等。占位符旁边可以写`%`用于模糊匹配。
  - 使用命名参数的方式`:paramName`为传入的参数，传入参数中还需要添加`@Param("paramName")`注解。命名参数符旁边可以写`%`用于模糊匹配。
- `@Query`注解的`nativeQuery`属性可以使用原生的SQL进行查询。

## Modifying注解

- 可以通过自定义的JPQL完成`UPDATE`和`delete`操作，注意，JPQL不支持使用INSERT。
- 在`@Query`注解中编写JPQL语句，但必须使用`@Modifying`进行修饰，以通知`SpringData`这是一个`UPDATE`或`DELETE`从操作。
- `UPDATE`和`DELETE`操作需要使用事务，此时需要定义Service层，在Service层的方法上加入`@Transactional`。
- 默认情况下，SpringData的每个方法上有事务，但都是一个只读事务，他们不能完成修改操作。

## JpaSpecificationExecutor接口

- 不属于`Repository`体系，实现一组JPA Criteria查询相关的方法。

- `Specification`封装`JPA Criteria`查询条件，通常使用匿名内部类的方式来创建改接口的对象。

- 通常使用`Specification`匿名内部类。

- 使用示例

  ```java
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
      System.out.println("当前第几页: " + (page.getNumber()));
      System.out.println("总页数: " + page.getTotalPages());
      System.out.println("当前页的List: " + page.getContent());
      System.out.println("当前页面的记录数: " + page.getNumberOfElements());
  }
  ```

## 为某个Repository上添加自定义方法

### 步骤

- 提供一个接口：声明要添加，并实现的方法。
- 该接口的实现类：类名需要在声明的`Repository`后添加`Impl`，并实现方法。
- 声明`Repository`接口，并继承步骤1声明的接口。
- 使用。
- 注意，默认情况下，Spring Data会在`base-package`中查找接口名`Impl`作为实现类，也可以通过`repository-impl-postfix`后缀声明