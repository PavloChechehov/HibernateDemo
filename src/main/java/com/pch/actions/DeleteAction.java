package com.pch.actions;

import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.exceptions.ActionException;
import com.pch.orm.Orm;
import com.pch.transaction.Transaction;
import com.pch.transaction.TransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;

public class DeleteAction<T> implements Action {

    private final T entity;
    public static final String QUERY = "DELETE FROM %s WHERE id = ?";
    private final Field idField;

    public DeleteAction(T entity, Orm orm) {
        this.entity = entity;
        this.idField = getIdField(entity.getClass().getDeclaredFields());
    }

    private Field getIdField(Field[] fields) {
        return Arrays
            .stream(fields)
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .orElseThrow(() -> new ActionException("Entity doesn't have id field"));
    }

    @Override
    public void perform(DataSource dataSource) {

        String query = buildQuery();

        System.out.println(query);
        transaction.run(query, dataSource, statement -> {

            try {
                idField.setAccessible(true);
                var idValue = idField.get(entity);
                statement.setObject(1, idValue);
                var deleteResult = statement.executeUpdate();

                if (deleteResult == 0) {
                    throw new ActionException("Entity wasn't deleted from database");
                }

            } catch (Exception e) {
                throw new ActionException("Delete exception", e);
            }
            return null;
        });
    }

    public String buildQuery() {
        var tableName = entity.getClass().getAnnotation(Table.class).name();
        return QUERY.formatted(tableName);
    }

    @Override
    public T getEntity() {
        return entity;
    }
}
