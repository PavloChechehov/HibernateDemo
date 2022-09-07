package com.pch.transaction;

public class TransactionManager {

    private static final Transaction transaction = new Transaction();

    private TransactionManager() {
    }

    public static Transaction getTransaction() {
        return transaction;
    }

}
