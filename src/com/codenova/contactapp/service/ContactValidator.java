package com.codenova.contactapp.service;

import com.codenova.contactapp.exception.ValidationException;
import com.codenova.contactapp.model.Contact;
import java.util.regex.Pattern;

public final class ContactValidator {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    public Contact validateAndCreate(long id, String fullName, String mobileNumber, String emailAddress, String address) {
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedMobile = mobileNumber == null ? "" : mobileNumber;
        String normalizedEmail = emailAddress == null ? "" : emailAddress.trim();
        String normalizedAddress = address == null ? "" : address.trim();

        if (normalizedName.isEmpty()) {
            throw new ValidationException("Full name cannot be blank.");
        }
        if (!MOBILE_PATTERN.matcher(normalizedMobile).matches()) {
            throw new ValidationException("Mobile number must contain exactly 10 digits with no spaces or symbols.");
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ValidationException("Enter an email address in a valid basic format, such as name@example.com.");
        }
        return new Contact(id, normalizedName, normalizedMobile, normalizedEmail, normalizedAddress);
    }
}
