package com.pch.actions;

import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.exceptions.ActionException;
import com.pch.orm.Orm;
import com.pch.transaction.TransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;

public class DeleteAction<T> implements Action {

    private static final Integer ORDER = 3;
    private final T entity;
    public static final String QUERY = "DELETE FROM %s WHERE id = ?";
    private final Field idField;
    private Orm orm;

    public DeleteAction(T entity, Orm orm) {
        this.entity = entity;
        this.idField = getIdField(entity.getClass().getDeclaredFields());
        this.orm = orm;
    }

    private Field getIdField(Field[] fields) {
        return Arrays
            .stream(fields)
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .orElseThrow(() -> new ActionException("Entity doesn't have id field"));
    }

    @Override
    public void perform() {

        String query = buildQuery();
        var transaction = TransactionManager.getTransaction();

        System.out.println(query);
        DataSource dataSource = orm.getDataSource();

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
    public Integer ordering() {
        return ORDER;
    }
}
