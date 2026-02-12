package com.erenalyoruk.cashgrid.common.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {

    private final String errorCode;

    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
