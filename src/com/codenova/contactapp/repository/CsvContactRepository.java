package com.codenova.contactapp.repository;

import com.codenova.contactapp.model.Contact;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CsvContactRepository implements ContactRepository {
    private static final String HEADER = "id,fullName,mobileNumber,emailAddress,address";
    private final Path csvPath;

    public CsvContactRepository(Path csvPath) {
        this.csvPath = csvPath.toAbsolutePath().normalize();
    }

    @Override
    public List<Contact> loadAll() throws IOException {
        if (Files.notExists(csvPath)) {
            return new ArrayList<>();
        }
        if (!Files.isRegularFile(csvPath) || !Files.isReadable(csvPath)) {
            throw new IOException("The contacts file is not a readable regular file: " + csvPath);
        }

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            List<List<String>> records = parseCsv(reader);
            if (records.isEmpty()) {
                return new ArrayList<>();
            }
            if (!records.get(0).equals(List.of("id", "fullName", "mobileNumber", "emailAddress", "address"))) {
                throw new IOException("Invalid CSV header in " + csvPath + ".");
            }

            List<Contact> contacts = new ArrayList<>();
            Set<Long> ids = new HashSet<>();
            Set<String> mobileNumbers = new HashSet<>();
            for (int index = 1; index < records.size(); index++) {
                List<String> row = records.get(index);
                int recordNumber = index + 1;
                if (row.size() != 5) {
                    throw new IOException("Malformed CSV record " + recordNumber + ": expected 5 fields.");
                }
                try {
                    long id = Long.parseLong(row.get(0));
                    if (id <= 0 || !ids.add(id)) {
                        throw new IllegalArgumentException("contact ID must be positive and unique");
                    }
                    if (!mobileNumbers.add(row.get(2))) {
                        throw new IllegalArgumentException("mobile number is duplicated");
                    }
                    contacts.add(new Contact(id, row.get(1), row.get(2), row.get(3), row.get(4)));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid CSV record " + recordNumber + ": " + exception.getMessage(), exception);
                }
            }
            return contacts;
        } catch (CsvFormatException exception) {
            throw new IOException("Malformed CSV data in " + csvPath + ": " + exception.getMessage(), exception);
        }
    }

    @Override
    public void saveAll(List<Contact> contacts) throws IOException {
        Path parent = csvPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, csvPath.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (Contact contact : contacts) {
                    writeField(writer, Long.toString(contact.getId()));
                    writer.write(',');
                    writeField(writer, contact.getFullName());
                    writer.write(',');
                    writeField(writer, contact.getMobileNumber());
                    writer.write(',');
                    writeField(writer, contact.getEmailAddress());
                    writer.write(',');
                    writeField(writer, contact.getAddress());
                    writer.newLine();
                }
            }
            try {
                Files.move(temporaryFile, csvPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, csvPath, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }

    private static List<List<String>> parseCsv(Reader source) throws IOException, CsvFormatException {
        List<List<String>> records = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean afterQuote = false;
        boolean recordStarted = false;
        int character;

        while ((character = source.read()) != -1) {
            char current = (char) character;
            if (inQuotes) {
                if (current == '"') {
                    source.mark(1);
                    int next = source.read();
                    if (next == '"') {
                        field.append('"');
                    } else {
                        inQuotes = false;
                        afterQuote = true;
                        if (next != -1) {
                            source.reset();
                        }
                    }
                } else {
                    field.append(current);
                }
            } else if (afterQuote) {
                if (current == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    afterQuote = false;
                    recordStarted = true;
                } else if (current == '\n' || current == '\r') {
                    row.add(field.toString());
                    field.setLength(0);
                    records.add(row);
                    row = new ArrayList<>();
                    afterQuote = false;
                    recordStarted = false;
                    if (current == '\r') {
                        consumeOptionalLineFeed(source);
                    }
                } else {
                    throw new CsvFormatException("unexpected character after a quoted field");
                }
            } else if (current == '"') {
                if (field.length() != 0) {
                    throw new CsvFormatException("quote found inside an unquoted field");
                }
                inQuotes = true;
                recordStarted = true;
            } else if (current == ',') {
                row.add(field.toString());
                field.setLength(0);
                recordStarted = true;
            } else if (current == '\n' || current == '\r') {
                row.add(field.toString());
                field.setLength(0);
                records.add(row);
                row = new ArrayList<>();
                recordStarted = false;
                if (current == '\r') {
                    consumeOptionalLineFeed(source);
                }
            } else {
                field.append(current);
                recordStarted = true;
            }
        }

        if (inQuotes) {
            throw new CsvFormatException("quoted field was not closed");
        }
        if (recordStarted || afterQuote || !row.isEmpty() || field.length() > 0) {
            row.add(field.toString());
            records.add(row);
        }
        return records;
    }

    private static void consumeOptionalLineFeed(Reader reader) throws IOException {
        reader.mark(1);
        int next = reader.read();
        if (next != '\n' && next != -1) {
            reader.reset();
        }
    }

    private static void writeField(BufferedWriter writer, String value) throws IOException {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            writer.write('"');
            writer.write(value.replace("\"", "\"\""));
            writer.write('"');
        } else {
            writer.write(value);
        }
    }

    private static final class CsvFormatException extends Exception {
        private CsvFormatException(String message) {
            super(message);
        }
    }
}
