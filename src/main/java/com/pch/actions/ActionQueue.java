package com.pch.actions;

import javax.sql.DataSource;
import java.util.LinkedList;

public class ActionQueue {

    private final LinkedList<Action> actions = new LinkedList<>();
    private final DataSource dataSource;

    public ActionQueue(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void performActions() {
        for (Action action : actions) {
            action.perform(dataSource);
        }

    }

    public void put(Action action) {
        actions.add(action);
    }
}
