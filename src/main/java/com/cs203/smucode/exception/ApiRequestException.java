package com.cs203.smucode.exception;

public class ApiRequestException extends RuntimeException{
    public ApiRequestException(String message){
        super(message);
    }
}
