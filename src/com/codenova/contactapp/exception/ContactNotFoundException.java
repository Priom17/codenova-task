package com.codenova.contactapp.exception;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(long id) {
        super("No contact exists with ID " + id + ".");
    }
}
