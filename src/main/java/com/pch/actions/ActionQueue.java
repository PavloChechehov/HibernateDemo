package com.pch.actions;

import javax.sql.DataSource;
import java.util.LinkedList;

public class ActionQueue {

    private final LinkedList<Action> actions = new LinkedList<>();

    public ActionQueue() {
    }

    public void performActions() {
        for (Action action : actions) {
            action.perform();
        }

    }

    public void put(Action action) {
        actions.add(action);
    }
}
