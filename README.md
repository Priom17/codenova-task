# CodeNova Contact Management App

A Java 17 console application for managing contacts. It demonstrates object-oriented design, input validation, collections, exception handling, and persistent CSV file storage.

## Project objectives

- Add, list, search, edit, and delete contacts through a menu-driven console.
- Validate user input and prevent duplicate mobile numbers.
- Keep application logic, presentation, and file persistence in separate packages.
- Preserve contact data between application runs using `contacts.csv`.

## Features

- Automatically generated, persistent contact IDs.
- Search by case-insensitive partial name or exact 10-digit mobile number.
- Confirmation before deleting and preservation of IDs when editing.
- Comma-, quote-, Unicode-, and newline-safe CSV fields.
- Atomic replacement when saving so a failed write does not partially replace the previous file.
- A malformed or unreadable CSV is reported and is not overwritten. Correct or restore it before restarting.
- No third-party libraries or paid services are required.

## Technology

- Java Development Kit (JDK) 17 or newer, compiled for Java 17.
- Java OOP, `ArrayList`, `HashMap`, regular expressions, `Scanner`, and NIO file I/O.
- CSV persistence with UTF-8 encoding.
- Git and GitHub are optional tools for source-code submission.

Email validation checks basic syntax only. It cannot prove that the mailbox exists or can receive email.

## Project structure

```text
CodeNova-Contact-Management-App/
├── .gitignore
├── contacts.csv
├── README.md
└── src/
    └── com/codenova/contactapp/
        ├── app/Main.java
        ├── controller/ContactController.java
        ├── exception/
        │   ├── ContactNotFoundException.java
        │   ├── DuplicateContactException.java
        │   └── ValidationException.java
        ├── model/Contact.java
        ├── repository/
        │   ├── ContactRepository.java
        │   └── CsvContactRepository.java
        ├── service/
        │   ├── ContactService.java
        │   └── ContactValidator.java
        └── view/ContactView.java
```

## Architecture

- **`model`** — immutable `Contact` data object with private fields and getters.
- **`view`** — prints menus/results and reads console input. It does not implement business rules.
- **`controller`** — routes menu choices to the service and presents operation errors.
- **`service`** — validates contact details, checks uniqueness, searches, and performs CRUD operations.
- **`repository`** — defines persistence operations and implements UTF-8 CSV loading and saving.
- **`exception`** — domain-specific errors for validation, duplicate numbers, and missing contacts.
- **`app`** — creates the application components and starts the controller.

## Validation rules

- Name is required; leading/trailing whitespace is removed.
- Mobile number must match `^[0-9]{10}$`; spaces and symbols are rejected.
- Mobile numbers must be unique. An edit may retain that contact's existing number.
- Email must follow basic email syntax; this is not inbox verification.
- Address is optional and may be empty.
- IDs must be positive whole numbers. Invalid menu choices and IDs are reported without terminating the app.

## CSV storage

The program reads and writes `contacts.csv` in the **current working directory**. Start it from the project root so that the included sample data is loaded and future changes update that file. A missing file is treated as an empty contact list; the first successful change creates the file.

The file contains a header followed by `id,fullName,mobileNumber,emailAddress,address`. Fields containing commas, quotes, or line breaks are quoted and quotes are escaped according to CSV conventions. The application rejects structurally malformed records and invalid saved contacts at startup rather than silently saving over the file. It displays an error and asks you to correct or restore the file.

The included names, addresses, emails, and phone numbers are fictional demonstration data; the reserved `example.com` domain is not a real person's mailbox.

## Requirements and setup

1. Install a free JDK 17 or later and ensure `java` and `javac` are available in Command Prompt or PowerShell.
2. Open a terminal in the project root (the folder containing `contacts.csv` and `src`).
3. Compile and run using the commands below.

### Compile and run on Windows

From Command Prompt:

```bat
if not exist out mkdir out
javac --release 17 -encoding UTF-8 -d out src\com\codenova\contactapp\app\Main.java src\com\codenova\contactapp\controller\ContactController.java src\com\codenova\contactapp\exception\ContactNotFoundException.java src\com\codenova\contactapp\exception\DuplicateContactException.java src\com\codenova\contactapp\exception\ValidationException.java src\com\codenova\contactapp\model\Contact.java src\com\codenova\contactapp\repository\ContactRepository.java src\com\codenova\contactapp\repository\CsvContactRepository.java src\com\codenova\contactapp\service\ContactService.java src\com\codenova\contactapp\service\ContactValidator.java src\com\codenova\contactapp\view\ContactView.java
java -cp out com.codenova.contactapp.app.Main
```

PowerShell uses the same commands (including the backslash-separated source paths). The `out` directory contains compiled classes and is ignored by Git. Keep the terminal's current directory at the project root when running so `contacts.csv` stays in the expected location.

## Example use

Select **1** and enter a name, a 10-digit mobile, an email, and an optional address. A successful save reports the assigned ID and displays the contact. Select **2** to list contacts, **3** to search, **4** to edit by ID, **5** to delete by ID after confirmation, or **6** to exit.

## Manual test plan

Run the application with a backup copy of `contacts.csv` if you want to preserve the sample data. The complete 20-case checklist and expected results are below.

| # | Input/action | Expected result | Pass/Fail |
|---|---|---|---|
| 1 | Add a name, unused 10-digit mobile, valid email, and address | Contact is saved, assigned an ID, displayed, and written to CSV | |
| 2 | Add a contact with a blank or whitespace-only name | Error is shown and the application asks for the details again | |
| 3 | Enter a mobile number with 9 digits, letters, spaces, symbols, or 11 digits | Number is rejected; no contact is saved | |
| 4 | Enter an email such as `missing-domain@` | Email is rejected; no contact is saved | |
| 5 | Add a second contact using an existing mobile number | Duplicate is rejected; existing contact remains unchanged | |
| 6 | Choose View All Contacts | All saved contacts display with ID, name, mobile, email, and address | |
| 7 | Search with a contact's complete name | Matching contact is displayed | |
| 8 | Search with a case-insensitive part of a name | Every contact whose name contains the text is displayed | |
| 9 | Search using an exact 10-digit mobile number | Matching contact is displayed | |
| 10 | Edit or delete using an ID that does not exist | A not-found message is shown and the app asks for another ID | |
| 11 | Edit a contact using valid values | Updated values are saved and the original ID is retained | |
| 12 | Try to edit using an invalid mobile or email, then re-enter valid values | Invalid values are not saved; valid retry is saved | |
| 13 | Edit a contact to use another contact's mobile number | Duplicate is rejected; the existing saved details remain unchanged | |
| 14 | Delete a contact and answer `y` | Contact is removed from memory and CSV | |
| 15 | Choose a contact to delete and answer `n` | Deletion is cancelled and the contact remains in CSV | |
| 16 | Add or edit a contact, exit, and restart from the project root | The changed contact is still present | |
| 17 | Temporarily use an empty/header-only CSV and choose View All Contacts | A no-contacts message appears without an error | |
| 18 | Enter a menu choice outside `1` through `6` | Error is shown and the menu is displayed again | |
| 19 | Temporarily rename `contacts.csv` and start the app | App starts with an empty list; first successful change creates the file | |
| 20 | Back up the CSV, make a malformed record, and start the app | Error is shown and malformed original file is not overwritten | |

Restore any backed-up sample data after tests that modify or rename `contacts.csv`. Leave the **Pass/Fail** column blank until you run each check.

## Taking screenshots

1. Compile and run the application from the project root.
2. Resize the terminal so the title, a representative menu/action, and its result are readable.
3. Capture one screenshot of the menu and one of successful contact details, plus any validation/search behavior you want to demonstrate.
4. Save screenshots in a folder such as `screenshots/` and add them to the repository if permitted by your internship instructions.
5. Reference them in this README with Markdown image links, for example `![Application menu](screenshots/menu.png)`.
6. Do not include real personal contact data in screenshots.

## GitHub submission

1. On GitHub, create a repository named `CodeNova-Contact-Management-App`.
2. Make it public if that is required for evaluation.
3. From the project root, initialize Git if needed, stage the source, `contacts.csv`, `.gitignore`, `README.md`, and approved screenshots, then commit and push using the repository URL GitHub provides.
4. Confirm that the repository shows the complete package tree, documentation, and sample CSV.
5. Before pushing, inspect staged files and search for passwords, API keys, private data, and real contact details. Do not upload private data or credentials.

No repository or live demo has been created by this project setup.

## Future improvements

A graphical interface, import/export options, and automated JUnit tests could be added later while keeping the service and repository responsibilities separated.

## Final acceptance checklist

- [ ] JDK 17 compilation completes without errors.
- [ ] All six menu choices work, and invalid choices do not terminate the application.
- [ ] Name, mobile, email, and duplicate-number validation behave as documented.
- [ ] Add, list, search, edit, confirmed delete, and cancelled delete work.
- [ ] Changes persist when the application is restarted.
- [ ] CSV commas, quotes, and multiline text round-trip correctly.
- [ ] Malformed saved data is reported without silently replacing the CSV.
- [ ] README, `.gitignore`, sample data, and approved screenshots are included.
- [ ] No credentials, secrets, or real private contact details are staged for upload.

## Author

**Name:** Priom Karmakar  
**GitHub:** https://github.com/Priom17
