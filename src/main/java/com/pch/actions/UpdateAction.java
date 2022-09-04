package com.pch.actions;

import com.pch.annotation.Column;
import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.exceptions.ActionException;
import com.pch.orm.Orm;
import com.pch.transaction.TransactionManager;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UpdateAction<T> implements Action {
    private final T entity;
    public static final String UPDATE_QUERY = "UPDATE %s set %s WHERE id = %s";
    private final List<Object> fieldValues = new ArrayList<>();

    public UpdateAction(T entity, Orm orm) {
        this.entity = entity;
    }

    @Override
    public void perform(DataSource dataSource) {
        String updateQuery = buildQuery();
        System.out.println(updateQuery);

        var transaction = TransactionManager.getTransaction();
        transaction.run(updateQuery, dataSource, preparedStatement -> {

            try {
                for (int i = 0; i < fieldValues.size(); i++) {
                    preparedStatement.setObject(i + 1, fieldValues.get(i));
                }
                preparedStatement.executeUpdate();
                return null;
            } catch (Exception e) {
                throw new ActionException("Update exception", e);
            }

        });
    }

    @Override
    @SneakyThrows
    public String buildQuery() {
        var fields = entity.getClass().getDeclaredFields();

        var tableName = entity.getClass().getAnnotation(Table.class).name();

        StringBuilder combine = new StringBuilder();

        Number id = null;

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (field.isAnnotationPresent(Id.class)) {
                id = (Number) field.get(entity);
                continue;
            }

            fieldValues.add(field.get(entity));
            var fieldName = getFieldName(field);

            combine.append(fieldName).append(" = ").append("?");

            if (i + 1 != fields.length) {
                combine.append(", ");
            }
        }

        return String.format(UPDATE_QUERY, tableName, combine, id);
    }

    @Override
        public T getEntity() {
        return entity;
    }


    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            var annotation = field.getAnnotation(Column.class);
            return annotation.name();
        } else {
            return field.getName();
        }
    }
}
