package com.codenova.contactapp.exception;

public class DuplicateContactException extends RuntimeException {
    public DuplicateContactException(String mobileNumber) {
        super("A contact with mobile number " + mobileNumber + " already exists.");
    }
}
