package com.pch.actions;

import com.pch.exceptions.ActionException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ActionQueue {

    private final LinkedList<Action<?>> actions = new LinkedList<>();
    private final DataSource dataSource;

    public ActionQueue(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void performActions() {
        for (Action action : actions) {
            action.perform(dataSource);
        }

    }

//    public void runActions() {
//        try (var connection = dataSource.getConnection()) {
//            try (var statement = connection.createStatement()) {
//
//                for (var action : actions) {
//                    var query = action.buildQuery();
//                    var entity = action.getEntity();
//                    var rs = statement.executeQuery(query);
//                    updateEntity(entity, rs);
//                }
//
//            } catch (SQLException e) {
//                connection.rollback();
//                throw e;
//            }
//
//        } catch (Exception e) {
//            throw new ActionException("Run actions exception", e);
//        }
//    }

    private <T> void updateEntity(T entity, ResultSet rs) {

    }


    public void put(Action action) {
        actions.add(action);
    }
}
