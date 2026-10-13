package com.codenova.contactapp.repository;

import com.codenova.contactapp.model.Contact;
import java.io.IOException;
import java.util.List;

public interface ContactRepository {
    List<Contact> loadAll() throws IOException;

    void saveAll(List<Contact> contacts) throws IOException;
}
