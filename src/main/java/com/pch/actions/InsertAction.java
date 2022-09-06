package com.pch.actions;

import com.pch.annotation.Column;
import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.exceptions.ActionException;
import com.pch.model.EntityKey;
import com.pch.orm.Orm;
import com.pch.transaction.TransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

public class InsertAction<T> implements Action {

    private final T entity;
    public static final String QUERY = "INSERT INTO %s(%s) values(%s)";
    private static final Integer ORDER = 1;
    private final List<Field> columnFields;
    private Orm orm;

    public InsertAction(T entity, Orm orm) {
        this.entity = entity;
        this.columnFields = getColumnFields(entity.getClass().getDeclaredFields());
        this.orm = orm;
    }

    private List<Field> getColumnFields(Field[] fields) {
        return Arrays
            .stream(fields)
            .filter(field -> field.isAnnotationPresent(Column.class)
                             && !field.isAnnotationPresent(Id.class))
            .toList();
    }

    public void perform() {

        var insertQuery = buildQuery();
        var transaction = TransactionManager.getTransaction();

        System.out.println(insertQuery);
        DataSource dataSource = orm.getDataSource();

        transaction.run(insertQuery, dataSource, (PreparedStatement statement) -> {

            try {
                for (int i = 0; i < columnFields.size(); i++) {
                    Field field = columnFields.get(i);
                    field.setAccessible(true);
                    var value = field.get(entity);
                    statement.setObject(i + 1, value);
                }

                statement.executeUpdate();
                var resultSet = statement.getGeneratedKeys();
                resultSet.next();
                var id = resultSet.getObject("id");

                var field = entity.getClass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);

                EntityKey<?> entityKey = new EntityKey<>(entity.getClass(), id);
                orm.entitiesMap.put(entityKey, entity);

            } catch (Exception e) {
                throw new ActionException("Insert exception", e);
            }
            return null;
        });


    }

    public String buildQuery() {
        var columnFieldNames = columnFields
            .stream()
            .map(field -> field.getAnnotation(Column.class))
            .map(Column::name)
            .toList();

        StringBuilder combine = new StringBuilder();
        var size = columnFields.size();

        for (int i = 0; i < size; i++) {
            combine.append("?");
            if (i + 1 != size) {
                combine.append(", ");
            }
        }

        return QUERY
            .formatted(
                entity.getClass().getAnnotation(Table.class).name(),
                String.join(",", columnFieldNames),
                combine.toString()
            );
    }

    @Override
    public Integer ordering() {
        return ORDER;
    }
}
