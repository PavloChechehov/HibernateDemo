package com.pch.actions;

import com.pch.annotation.Column;
import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.exceptions.ActionException;
import com.pch.model.EntityKey;
import com.pch.orm.Orm;
import com.pch.transaction.Transaction;
import com.pch.transaction.TransactionManager;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertAction<T> implements Action {

    private final T entity;
    public static final String QUERY = "INSERT INTO %s(%s) values (%s)";
    private final List<Field> columnFields;
    private final Orm orm;

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
        Transaction transaction = TransactionManager.getTransaction();

        System.out.println(insertQuery);
        transaction.run(insertQuery, dataSource, statement -> {

            try {
                setParameters(statement);
                statement.executeUpdate();
                Object id = getId(statement);
                setFieldId(id);
                saveEntity(id);

            } catch (Exception e) {
                throw new ActionException("Insert exception", e);
            }
            return null;
        });
    }

    private void saveEntity(Object id) {
        orm.addEntity(entity, id);
    }

    private void setFieldId(Object id) throws NoSuchFieldException, IllegalAccessException {
        var field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private Object getId(PreparedStatement statement) throws SQLException {
        var resultSet = statement.getGeneratedKeys();
        resultSet.next();
        var id = resultSet.getObject("id");
        return id;
    }

    private void setParameters(PreparedStatement statement) throws IllegalAccessException, SQLException {
        for (int i = 0; i < columnFields.size(); i++) {
            Field field = columnFields.get(i);
            field.setAccessible(true);
            var value = field.get(entity);
            statement.setObject(i + 1, value);
        }
    }

    @SneakyThrows
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

    @SneakyThrows
    public List<Object> getValues() {
        List<Object> columnValues = new ArrayList<>();
        for (Field field : columnFields) {

            field.setAccessible(true);
            var value = field.get(entity);
            columnValues.add(String.valueOf(value));
        }

        return columnValues;
    }

    @Override
    public T getEntity() {
        return entity;
    }
}
