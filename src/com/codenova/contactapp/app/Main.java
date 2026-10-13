package com.codenova.contactapp.app;

import com.codenova.contactapp.controller.ContactController;
import com.codenova.contactapp.repository.CsvContactRepository;
import com.codenova.contactapp.service.ContactService;
import com.codenova.contactapp.service.ContactValidator;
import com.codenova.contactapp.view.ContactView;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        CsvContactRepository repository = new CsvContactRepository(Path.of("contacts.csv"));
        try {
            ContactService service = new ContactService(repository, new ContactValidator());
            ContactView view = new ContactView(new Scanner(System.in), System.out);
            new ContactController(service, view).run();
        } catch (IOException exception) {
            System.err.println("[ERROR] Could not safely load contacts.csv: " + exception.getMessage());
            System.err.println("[INFO] Existing data was not overwritten. Correct or restore the CSV file, then restart.");
        }
    }
}
