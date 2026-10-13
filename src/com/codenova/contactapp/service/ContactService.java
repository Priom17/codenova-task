package com.codenova.contactapp.service;

import com.codenova.contactapp.exception.ContactNotFoundException;
import com.codenova.contactapp.exception.DuplicateContactException;
import com.codenova.contactapp.exception.ValidationException;
import com.codenova.contactapp.model.Contact;
import com.codenova.contactapp.repository.ContactRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ContactService {
    private final ContactRepository repository;
    private final ContactValidator validator;
    private final List<Contact> contacts = new ArrayList<>();
    private final Map<Long, Contact> contactsById = new HashMap<>();
    private final Map<String, Contact> contactsByMobile = new HashMap<>();
    private long nextId = 1;

    public ContactService(ContactRepository repository, ContactValidator validator) throws IOException {
        this.repository = repository;
        this.validator = validator;
        for (Contact contact : repository.loadAll()) {
            try {
                Contact checked = validator.validateAndCreate(contact.getId(), contact.getFullName(),
                        contact.getMobileNumber(), contact.getEmailAddress(), contact.getAddress());
                if (contactsById.containsKey(checked.getId())) {
                    throw new IOException("Duplicate contact ID " + checked.getId() + " in saved data.");
                }
                if (contactsByMobile.containsKey(checked.getMobileNumber())) {
                    throw new IOException("Duplicate mobile number in saved data: " + checked.getMobileNumber());
                }
                contacts.add(checked);
                contactsById.put(checked.getId(), checked);
                contactsByMobile.put(checked.getMobileNumber(), checked);
                nextId = Math.max(nextId, checked.getId() + 1);
            } catch (ValidationException exception) {
                throw new IOException("Invalid saved contact with ID " + contact.getId() + ": "
                        + exception.getMessage(), exception);
            }
        }
    }

    public List<Contact> getAllContacts() {
        return List.copyOf(contacts);
    }

    public List<Contact> search(String query) {
        String searchTerm = query == null ? "" : query.trim();
        if (searchTerm.matches("^[0-9]{10}$")) {
            Contact match = contactsByMobile.get(searchTerm);
            return match == null ? List.of() : List.of(match);
        }
        String lowerCaseTerm = searchTerm.toLowerCase(Locale.ROOT);
        return contacts.stream()
                .filter(contact -> contact.getFullName().toLowerCase(Locale.ROOT).contains(lowerCaseTerm))
                .toList();
    }

    public Contact getContact(long id) {
        Contact contact = contactsById.get(id);
        if (contact == null) {
            throw new ContactNotFoundException(id);
        }
        return contact;
    }

    public Contact addContact(String fullName, String mobileNumber, String emailAddress, String address) throws IOException {
        Contact contact = validator.validateAndCreate(nextId, fullName, mobileNumber, emailAddress, address);
        ensureMobileAvailable(contact.getMobileNumber(), null);
        List<Contact> updatedContacts = new ArrayList<>(contacts);
        updatedContacts.add(contact);
        repository.saveAll(updatedContacts);
        contacts.add(contact);
        contactsById.put(contact.getId(), contact);
        contactsByMobile.put(contact.getMobileNumber(), contact);
        nextId++;
        return contact;
    }

    public Contact updateContact(long id, String fullName, String mobileNumber, String emailAddress, String address)
            throws IOException {
        Contact existing = getContact(id);
        Contact updated = validator.validateAndCreate(id, fullName, mobileNumber, emailAddress, address);
        ensureMobileAvailable(updated.getMobileNumber(), id);

        List<Contact> updatedContacts = new ArrayList<>(contacts.size());
        for (Contact contact : contacts) {
            updatedContacts.add(contact.getId() == id ? updated : contact);
        }
        repository.saveAll(updatedContacts);
        int index = contacts.indexOf(existing);
        contacts.set(index, updated);
        contactsById.put(id, updated);
        contactsByMobile.remove(existing.getMobileNumber());
        contactsByMobile.put(updated.getMobileNumber(), updated);
        return updated;
    }

    public Contact deleteContact(long id) throws IOException {
        Contact existing = getContact(id);
        List<Contact> updatedContacts = new ArrayList<>(contacts);
        updatedContacts.remove(existing);
        repository.saveAll(updatedContacts);
        contacts.remove(existing);
        contactsById.remove(id);
        contactsByMobile.remove(existing.getMobileNumber());
        return existing;
    }

    private void ensureMobileAvailable(String mobileNumber, Long excludedContactId) {
        Contact existing = contactsByMobile.get(mobileNumber);
        if (existing != null && (excludedContactId == null || existing.getId() != excludedContactId)) {
            throw new DuplicateContactException(mobileNumber);
        }
    }
}
