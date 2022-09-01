package com.pch.model;

import com.pch.annotation.Id;
import com.pch.annotation.Column;
import com.pch.annotation.Table;
import lombok.Data;
import lombok.ToString;

@Table(name = "persons")
@Data
@ToString
public class Person {

    @Id
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "age")
    private Integer age;

}