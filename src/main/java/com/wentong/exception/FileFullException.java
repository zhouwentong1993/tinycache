package com.wentong.exception;

public class FileFullException extends RuntimeException {

    public FileFullException() {
    }

    public FileFullException(String s) {
        super(s);
    }
}
