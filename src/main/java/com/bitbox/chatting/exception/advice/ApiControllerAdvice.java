package com.bitbox.chatting.exception.advice;

import com.bitbox.chatting.exception.BadRequestException;
import com.bitbox.chatting.exception.DuplicationRoomException;
import com.bitbox.chatting.exception.PaymentFailException;
import com.bitbox.chatting.exception.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {
    @ExceptionHandler(DuplicationRoomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDuplicationRoomException(DuplicationRoomException e){
        return getErrorResponse(e);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BadRequestException e){
        return getErrorResponse(e);
    }

    @ExceptionHandler(PaymentFailException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePaymentFailException(BadRequestException e){
        return getErrorResponse(e);
    }

    @ExceptionHandler(KafkaException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleKafkaException(KafkaException e){
        //TODO 카프카 예외 발생시 어떻게 처리할까?(카프카에 보내는것이 불가능한 케이스임)
        return ErrorResponse.builder()
                .message("카프카 서버에 접속할 수 없습니다.")
                .build();
    }

    private ErrorResponse getErrorResponse(RuntimeException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .build();
    }
}