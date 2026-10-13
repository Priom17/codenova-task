package com.codenova.contactapp.controller;

import com.codenova.contactapp.exception.ContactNotFoundException;
import com.codenova.contactapp.exception.DuplicateContactException;
import com.codenova.contactapp.exception.ValidationException;
import com.codenova.contactapp.model.Contact;
import com.codenova.contactapp.service.ContactService;
import com.codenova.contactapp.view.ContactView;
import java.io.IOException;

public final class ContactController {
    private final ContactService service;
    private final ContactView view;

    public ContactController(ContactService service, ContactView view) {
        this.service = service;
        this.view = view;
    }

    public void run() {
        view.showTitle();
        view.showMessage("[INFO] Contact data successfully synchronized with contacts.csv.");
        boolean running = true;
        while (running) {
            view.showMenu();
            String choice = view.readLine("Choose an option (1-6): ").trim();
            switch (choice) {
                case "1" -> addContact();
                case "2" -> view.showContacts(service.getAllContacts());
                case "3" -> searchContacts();
                case "4" -> editContact();
                case "5" -> deleteContact();
                case "6" -> {
                    view.showMessage("[INFO] Application closed. Your contact data is saved.");
                    running = false;
                }
                default -> view.showMessage("[ERROR] Please choose a valid option from 1 to 6.");
            }
        }
    }

    private void addContact() {
        view.showMessage("\n--- [ADD NEW CONTACT] ---");
        while (true) {
            try {
                Contact contact = service.addContact(
                        view.readLine("Enter Full Name        : "),
                        view.readLine("Enter 10-Digit Mobile  : "),
                        view.readLine("Enter Email Address    : "),
                        view.readLine("Enter Address (Optional): "));
                view.showMessage("[SUCCESS] Contact added successfully with ID: " + contact.getId());
                view.showContact(contact);
                return;
            } catch (ValidationException | DuplicateContactException exception) {
                view.showMessage("[ERROR] " + exception.getMessage() + " Please enter the contact details again.");
            } catch (IOException exception) {
                view.showMessage("[ERROR] Could not save the contact: " + exception.getMessage());
                return;
            }
        }
    }

    private void searchContacts() {
        String query = view.readLine("Enter a name or exact 10-digit mobile number: ");
        view.showContacts(service.search(query));
    }

    private void editContact() {
        view.showMessage("\n--- [EDIT CONTACT] ---");
        Contact selected = readExistingContact("Enter Contact ID to edit: ");
        if (selected == null) {
            return;
        }
        while (true) {
            try {
                view.showMessage("Enter the updated details:");
                Contact updated = service.updateContact(selected.getId(),
                        view.readLine("Enter Full Name        : "),
                        view.readLine("Enter 10-Digit Mobile  : "),
                        view.readLine("Enter Email Address    : "),
                        view.readLine("Enter Address (Optional): "));
                view.showMessage("[SUCCESS] Contact updated successfully.");
                view.showContact(updated);
                return;
            } catch (ValidationException | DuplicateContactException exception) {
                view.showMessage("[ERROR] " + exception.getMessage() + " The saved contact was not changed. Enter the details again.");
            } catch (IOException exception) {
                view.showMessage("[ERROR] Could not save the update: " + exception.getMessage());
                return;
            }
        }
    }

    private void deleteContact() {
        view.showMessage("\n--- [DELETE CONTACT] ---");
        Contact contact = readExistingContact("Enter Contact ID to delete: ");
        if (contact == null) {
            return;
        }
        try {
            view.showContact(contact);
            String confirmation;
            do {
                confirmation = view.readLine("Delete this contact? (y/n): ").trim();
                if (!confirmation.equalsIgnoreCase("y") && !confirmation.equalsIgnoreCase("n")) {
                    view.showMessage("[ERROR] Enter y to confirm or n to cancel.");
                }
            } while (!confirmation.equalsIgnoreCase("y") && !confirmation.equalsIgnoreCase("n"));

            if (confirmation.equalsIgnoreCase("y")) {
                service.deleteContact(contact.getId());
                view.showMessage("[SUCCESS] Contact deleted successfully.");
            } else {
                view.showMessage("[INFO] Deletion cancelled. The contact was not changed.");
            }
        } catch (IOException exception) {
            view.showMessage("[ERROR] Could not save the deletion: " + exception.getMessage());
        }
    }

    private long readId(String prompt) {
        while (true) {
            String value = view.readLine(prompt).trim();
            long id;
            try {
                id = Long.parseLong(value);
            } catch (NumberFormatException exception) {
                view.showMessage("[ERROR] Contact ID must be a positive whole number.");
                continue;
            }
            if (id > 0) {
                return id;
            }
            view.showMessage("[ERROR] Contact ID must be a positive whole number.");
        }
    }

    private Contact readExistingContact(String prompt) {
        while (true) {
            long id = readId(prompt);
            try {
                return service.getContact(id);
            } catch (ContactNotFoundException exception) {
                view.showMessage("[ERROR] " + exception.getMessage() + " Enter another ID.");
            }
        }
    }
}
