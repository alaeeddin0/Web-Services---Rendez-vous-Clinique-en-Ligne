package com.example.service_creneaux.exception;

public class ServiceIndisponibleException extends RuntimeException {

    public ServiceIndisponibleException(String message) {
        super(message);
    }
}