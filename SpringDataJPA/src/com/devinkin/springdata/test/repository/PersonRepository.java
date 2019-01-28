package com.devinkin.springdata.test.repository;


import com.devinkin.springdata.test.dao.PersonDao;
import com.devinkin.springdata.test.pojo.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

//public interface PersonRepository extends Repository<Person, Integer> {
@RepositoryDefinition(domainClass = Person.class, idClass = Integer.class)
//public interface PersonRepository extends JpaRepository<Person, Integer> {
public interface PersonRepository extends JpaRepository<Person, Integer>, JpaSpecificationExecutor<Person>, PersonDao {
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


    // 查询id为最大的那个Person
    @Query("SELECT p FROM Person p WHERE p.id = (SELECT max(p2.id) FROM Person p2)")
    Person getMaxIdPerson();


    // 使用占位符传参
    @Query("SELECT p FROM Person p WHERE p.lastName = ?1 AND p.email = ?2")
    List<Person> testQueryAnnotationParams1(String lastName, String email);


    // 使用命名参数方式
//    @Query("SELECT p FROM Person p WHERE p.lastName = :lastName AND p.email = :email")
//    @Query("SELECT p FROM Person p WHERE p.lastName = %:lastName% AND p.email = %:email%")
    @Query("SELECT p FROM Person p WHERE p.lastName LIKE %:lastName% AND p.email LIKE %:email%")
    List<Person> testQueryAnnotationParams2(@Param("email") String email, @Param("lastName") String lastName);

    //    @Query("SELECT p FROM Person p WHERE p.lastName LIKE ?1 OR p.email LIKE ?2")
    @Query("SELECT p FROM Person p WHERE p.lastName LIKE %?1% OR p.email LIKE %?2%")
    List<Person> testQueryAnnotationLikeParam(String lastName, String email);


    // 设置原生的SQL查询
    @Query(value = "SELECT COUNT(id) FROM springdata_jpa_persons", nativeQuery = true)
    Long getTotalCount();


    @Query("UPDATE Person p SET p.email = :email WHERE id = :id")
    @Modifying
    void updatePersonEmail(@Param("id") Integer id, @Param("email") String email);
}
