package com.example.seatmanager.service;

/**
 * DataAccessException：用于在 Service 层将底层 DAO 抛出的 SQLException
 * 包装为一个运行时异常，向上层（Controller/UI）传递。
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
