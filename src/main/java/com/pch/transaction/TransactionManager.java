package com.pch.transaction;

import javax.sql.DataSource;

public class TransactionManager {

    private static Transaction transaction;
    private DataSource dataSource;

    private TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static Transaction getTransaction() {
        return new Transaction()
    }

    public static TransactionManager of(DataSource dataSource) {
        return new TransactionManager(dataSource);
    }
}
