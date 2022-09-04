package com.pch.transaction;

import com.pch.exceptions.ActionException;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.function.Function;

public class Transaction {

    private DataSource dataSource;

    public Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    public  <T> T run(String query, DataSource dataSource, Function<PreparedStatement, T> function) {
        T t;
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                connection.beginRequest();
                t = function.apply(statement);
                connection.endRequest();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new ActionException("Transaction exception", e);
            }
        }
        return t;
    }

}
