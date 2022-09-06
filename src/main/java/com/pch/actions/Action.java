package com.pch.actions;

import javax.sql.DataSource;

public interface Action {

    void perform();

    String buildQuery();

    Integer ordering();

}
