package com.timelog.timelog.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "There was no record for the provided ID.")
public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(String id) {

        super("There was no company with id: " + id);
    }
}
