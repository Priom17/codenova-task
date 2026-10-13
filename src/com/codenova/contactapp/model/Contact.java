package com.codenova.contactapp.model;

public final class Contact {
    private final long id;
    private final String fullName;
    private final String mobileNumber;
    private final String emailAddress;
    private final String address;

    public Contact(long id, String fullName, String mobileNumber, String emailAddress, String address) {
        this.id = id;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.emailAddress = emailAddress;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getAddress() {
        return address;
    }
}
