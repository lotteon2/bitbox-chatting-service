package com.bitbox.chatting.exception;

public class DuplicationRoomException extends RuntimeException{
    public DuplicationRoomException() {
    }

    public DuplicationRoomException(String message) {
        super(message);
    }

    public DuplicationRoomException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicationRoomException(Throwable cause) {
        super(cause);
    }

    public DuplicationRoomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
