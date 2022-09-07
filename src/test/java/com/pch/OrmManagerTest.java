package com.pch;

import com.pch.model.Person;
import com.pch.orm.Orm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;


public class OrmManagerTest {


    private static Orm orm;
    private static final String CREATE_TABLE_PERSON = "CREATE TABLE persons( " +
                                                      "id SERIAL PRIMARY KEY, " +
                                                      "first_name TEXT NOT NULL, " +
                                                      "last_name TEXT NOT NULL, " +
                                                      "age INTEGER NOT NULL " +
                                                      ");";

    @BeforeAll
    static void setUp() {
        var dataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {

            statement.execute(CREATE_TABLE_PERSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        orm = new Orm(dataSource);

    }

    @Test
    void findEntity() {
        var person = new Person();
        person.setFirstName("Pavlo");
        person.setLastName("Chechehov");
        person.setAge(10);
        orm.persist(person);
        orm.flush();

        var person1 = orm.find(Person.class, 1);

        System.out.println(person1 == person);
    }

}
