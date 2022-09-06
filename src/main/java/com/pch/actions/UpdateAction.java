package com.pch.actions;

import com.pch.annotation.Column;
import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.exceptions.ActionException;
import com.pch.model.EntityKey;
import com.pch.orm.Orm;
import com.pch.transaction.TransactionManager;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;

public class UpdateAction<T> implements Action {
    private static final Integer ORDER = 2;
    private final T entity;
    public static final String UPDATE_QUERY = "UPDATE %s set %s WHERE id = %s";
    private final List<Object> fieldValues = new ArrayList<>();
    private final Orm orm;

    public UpdateAction(T entity, Orm orm) {
        this.entity = entity;
        this.orm = orm;
    }

    @Override
    public void perform() {
        orm.entitiesMap.entrySet()
                .stream()
                .filter(this::isChanged)
                .forEach(entryKey -> performUpdate());
        // todo: refactoring UpdateAction
        //todo .forEach(entryKey -> performUpdate(entryKey.getKey(), entryKey.getValue()));
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
    public Integer ordering() {
        return ORDER;
    }


    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            var annotation = field.getAnnotation(Column.class);
            return annotation.name();
        } else {
            return field.getName();
        }
    }

    public void performUpdate() {

        String updateQuery = buildQuery();
        System.out.println(updateQuery);

        var transaction = TransactionManager.getTransaction();
        DataSource dataSource = orm.getDataSource();
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

    @SneakyThrows
    private boolean isChanged(Map.Entry<EntityKey<?>, Object> entityEntry) {

        //todo: find all fields of object in sorted way
        //todo: compare with field's value in the entitySnapshots
        //todo: if something change return true if not return false

        var object = entityEntry.getValue();
        EntityKey<?> entryKey = entityEntry.getKey();
        var fieldValues = orm.entitySnapshots.get(entryKey);
        Class<?> type = entryKey.type();

        var sortedFields = getSortedFields(type);

        for (int i = 0; i < sortedFields.length; i++) {
            var sortedField = sortedFields[i];
            sortedField.setAccessible(true);
            if (!sortedField.get(object).equals(fieldValues[i])) {
                return true;
            }
        }

        return false;
    }

    private <T> Field[] getSortedFields(Class<T> type) {
        return Arrays.stream(type.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }
}
