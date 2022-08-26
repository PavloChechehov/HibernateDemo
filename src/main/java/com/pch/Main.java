package com.pch;

import com.pch.model.Person;
import org.postgresql.ds.PGSimpleDataSource;


public class Main {

    public static void main(String[] args) {

        var orm = buildOrm();

        Person person = orm.find(Person.class, 1);
        Person theSame = orm.find(Person.class, 1);

        System.out.println(person);
        System.out.println(person == theSame);


        person.setFirstName("Pavlo");

        orm.close();
    }

    private static Orm buildOrm() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUser("postgres");
        dataSource.setURL("jdbc:postgresql://localhost:5432/person-db");
        dataSource.setPassword("");
        return new Orm(dataSource);
    }

}
