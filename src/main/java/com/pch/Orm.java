package com.pch;

import com.pch.annotation.Column;
import com.pch.annotation.Id;
import com.pch.annotation.Table;
import com.pch.model.EntityKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Orm {

    /*
            create Person entity
            create required annotations to provide info about table/columns
            put annotations on class Person
            initialize DataSource
            create orm instanse using data source
            prepare SQL select that will fetch a record by id
            execute query
            create an instance of an entity
            parse result set and set entity fields
            return entity instance
        */
    private final DataSource dataSource;
    private static final String UPDATE_QUERY = "update persons set %s where id = %d";
    private static final String SELECT_FROM_BY_ID = "select * from %s where id = ?";
    private Map<EntityKey<?>, Object> entitiesMap = new HashMap<>();
    private Map<EntityKey<?>, Object[]> entitySnapshots = new HashMap<>();

    @SneakyThrows
    public <T> T find(Class<T> type, Integer id) {
        System.out.println("SQL: select * from " + type.getAnnotation(Table.class).name() + " where id = " + id);

        var key = new EntityKey<>(type, id);

        var entity = entitiesMap.computeIfAbsent(key, this::loadFromDb);

        return type.cast(entity);
    }

    @SneakyThrows
    public <T> void update(T t) {

        String updateQuery = createUpdateQuery(t);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate(updateQuery);
        }
    }

    @SneakyThrows
    //todo: it needs to rewrite this method to be more flexible
    private <T> String createUpdateQuery(T t) {

        var fields = t.getClass().getDeclaredFields();

        StringBuilder combine = new StringBuilder();

        Number id = null;

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (field.isAnnotationPresent(Id.class)) {
                id = (Number) field.get(t);
                continue;
            }

            var fieldValue = field.get(t);
            var fieldName = getFieldName(field);

            combine.append(fieldName).append(" = ").append("'").append(fieldValue).append("'");

            if (i + 1 != fields.length) {
                combine.append(", ");
            }
        }

        return String.format(UPDATE_QUERY, combine, id);
    }

    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            var annotation = field.getAnnotation(Column.class);
            return annotation.name();
        } else {
            return field.getName();
        }
    }

    @SneakyThrows
    private <T> T loadFromDb(EntityKey<T> entityKey) {
        var type = entityKey.type();
        var id = entityKey.id();

        if (type.isAnnotationPresent(Table.class)) {

            String prepareStatementQuery = prepareQuery(type);
            try (var connection = dataSource.getConnection();
                 var statement = connection.prepareStatement(prepareStatementQuery)) {

                statement.setObject(1, id);
                var rs = statement.executeQuery();

                return mapRSToObject(rs, entityKey);
            }
        }

        return null;
    }

    private String prepareQuery(Class<?> type) {
        var annotation = type.getAnnotation(Table.class);
        var tableName = annotation.name();
        return String.format(SELECT_FROM_BY_ID, tableName);
    }

    private <T> T mapRSToObject(ResultSet rs, EntityKey<T> entityKey) throws Exception {

        rs.next();

        var type = entityKey.type();

        var t = type.getDeclaredConstructor().newInstance();

        Field[] sortedFields = getSortedFields(type);

        Object[] snapshotCopy = new Object[sortedFields.length];

        for (int i = 0; i < sortedFields.length; i++) {
            Field field = sortedFields[i];
            setId(rs, t, field);
            setColumnToField(rs, t, field);
            snapshotCopy[i] = field.get(t);
        }

        entitySnapshots.put(entityKey, snapshotCopy);

        return t;
    }

    private <T> Field[] getSortedFields(Class<T> type) {
        return Arrays.stream(type.getDeclaredFields())
            .sorted(Comparator.comparing(Field::getName))
            .toArray(Field[]::new);
    }

    private <T> void setId(ResultSet rs, T t, Field field) throws SQLException, IllegalAccessException {
        if (field.isAnnotationPresent(Id.class)) {

            var columnName = field.getName();
            setField(rs, t, field, columnName);
        }
    }

    private <T> void setColumnToField(ResultSet rs, T t, Field field) throws SQLException, IllegalAccessException {
        if (field.isAnnotationPresent(Column.class)) {

            var annotation = field.getAnnotation(Column.class);
            var columnName = annotation.name();

            setField(rs, t, field, columnName);
        }
    }

    private <T> void setField(ResultSet rs, T t, Field field, String columnName) throws SQLException, IllegalAccessException {
        Object object = rs.getObject(columnName, field.getType());

        field.setAccessible(true);
        field.set(t, object);
    }


    public void close() {
        entitiesMap.entrySet()
            .stream()
            .filter(this::isChanged)
            .forEach(this::performUpdate);
    }

    private void performUpdate(Map.Entry<EntityKey<?>, Object> entityEntry) {
        update(entityEntry.getValue());
    }

    @SneakyThrows
    private boolean isChanged(Map.Entry<EntityKey<?>, Object> entityEntry) {

        //todo: find all fields of object in sorted way
        //todo: compare with field's value in the entitySnapshots
        //todo: if something change return true if not return false

        var object = entityEntry.getValue();
        EntityKey<?> entryKey = entityEntry.getKey();
        var fieldValues = entitySnapshots.get(entryKey);
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
}
