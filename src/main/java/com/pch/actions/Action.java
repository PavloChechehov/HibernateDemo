package com.pch.actions;

import javax.sql.DataSource;

public interface Action {

    void perform(DataSource dataSource);

    String buildQuery();

    Integer ordering();
}
