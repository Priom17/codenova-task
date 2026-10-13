package com.codenova.contactapp.view;

import com.codenova.contactapp.model.Contact;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public final class ContactView {
    private final Scanner scanner;
    private final PrintStream output;

    public ContactView(Scanner scanner, PrintStream output) {
        this.scanner = scanner;
        this.output = output;
    }

    public void showTitle() {
        output.println("==============================================================");
        output.println("           CODENOVA CONTACT MANAGEMENT APPLICATION");
        output.println("                   Batch 01 - Java Internship");
        output.println("==============================================================");
    }

    public void showMenu() {
        output.println();
        output.println("------------------------ MAIN MENU ---------------------------");
        output.println(" 1. Add New Contact");
        output.println(" 2. View All Contacts");
        output.println(" 3. Search Contact");
        output.println(" 4. Edit Contact");
        output.println(" 5. Delete Contact");
        output.println(" 6. Exit Application");
        output.println("--------------------------------------------------------------");
    }

    public String readLine(String prompt) {
        output.print(prompt);
        return scanner.nextLine();
    }

    public void showContacts(List<Contact> contacts) {
        if (contacts.isEmpty()) {
            output.println("[INFO] No contacts found.");
            return;
        }
        for (Contact contact : contacts) {
            showContact(contact);
        }
    }

    public void showContact(Contact contact) {
        output.println();
        output.println("======================= CONTACT DETAILS ======================");
        output.printf(" Contact ID     : %d%n", contact.getId());
        output.printf(" Full Name      : %s%n", contact.getFullName());
        output.printf(" Mobile Number  : %s%n", contact.getMobileNumber());
        output.printf(" Email Address  : %s%n", contact.getEmailAddress());
        output.printf(" Street Address : %s%n", contact.getAddress().isBlank() ? "(not provided)" : contact.getAddress());
        output.println("==============================================================");
    }

    public void showMessage(String message) {
        output.println(message);
    }
}
