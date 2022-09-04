package com.pch.actions;

import javax.sql.DataSource;

public interface Action<T> {

    void perform(DataSource dataSource);

    String buildQuery();

     T getEntity();
}
