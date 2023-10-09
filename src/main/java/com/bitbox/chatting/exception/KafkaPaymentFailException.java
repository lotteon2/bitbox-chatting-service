package com.bitbox.chatting.exception;

public class KafkaPaymentFailException extends RuntimeException{
    public KafkaPaymentFailException() {
        super();
    }

    public KafkaPaymentFailException(String message) {
        super(message);
    }

    public KafkaPaymentFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaPaymentFailException(Throwable cause) {
        super(cause);
    }

    protected KafkaPaymentFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
