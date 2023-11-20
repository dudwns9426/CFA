package com.project.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionUtil extends RuntimeException{
	
	public static ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // 에러 로그 출력

        HttpStatus status;
        String message;

        if (e instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "서버 오류가 발생했습니다.";
        }

        return ResponseEntity.status(status).body(message);
    }
	
	public ExceptionUtil() {
        super();
    }

    public ExceptionUtil(String message) {
        super(message);
    }

    public ExceptionUtil(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionUtil(Throwable cause) {
        super(cause);
    }
}
