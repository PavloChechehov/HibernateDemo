package com.pch.model;

public record EntityKey<T>(Class<T> type, Object id) {
}
