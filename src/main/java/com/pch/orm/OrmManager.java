package com.pch.orm;

public interface OrmManager {

    <T> T find(Class<T> type, Integer id);

    <T> void update(T t);

    <T> void persist(T entity);

    <T> void remove(T entity);

    void flush();

    void close();

}
