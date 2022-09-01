package com.pch;

import com.pch.model.Person;
import com.pch.orm.Orm;
import org.postgresql.ds.PGSimpleDataSource;


public class Main {

    public static void main(String[] args) {

        var orm = buildOrm();
        var person = orm.find(Person.class, 9);
        person.setFirstName("newnewnew pavlo3");
        person.setLastName("chec3");
        orm.close();
    }

    private static Orm buildOrm() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUser("postgres");
        dataSource.setURL("jdbc:postgresql://localhost:5432/person-db");
        dataSource.setPassword("earTy8vd");
        return new Orm(dataSource);
    }

}
