package com.library.command;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;

import java.util.List;
import java.util.stream.Collectors;

public class CommandProcessor {

    private final LibraryService libraryService;

    public CommandProcessor(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public String process(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        try {
            // Split respecting quoted strings
            String[] tokens = tokenize(trimmed);
            String command = tokens[0].toLowerCase();

            return switch (command) {
                case "add_book"               -> handleAddBook(tokens);
                case "register_member"        -> handleRegisterMember(tokens);
                case "borrow_book"            -> handleBorrowBook(tokens);
                case "return_book"            -> handleReturnBook(tokens);
                case "check_availability"     -> handleCheckAvailability(tokens);
                case "display_member_books"   -> handleDisplayMemberBooks(tokens);
                case "display_library_status" -> handleDisplayLibraryStatus();
                default -> "Unknown command: " + command;
            };
        } catch (LibraryException e) {
            return e.getMessage();
        }
    }

    private String handleAddBook(String[] tokens) {
        requireArgs(tokens, 4, "add_book <title> <author> <copies>");
        String title = tokens[1];
        String author = tokens[2];
        int copies = Integer.parseInt(tokens[3]);
        Book book = libraryService.addBook(title, author, copies);
        String copyWord = copies == 1 ? "copy" : "copies";
        return String.format("Book \"%s\" by %s added with book ID %d (%d %s)",
                book.getTitle(), book.getAuthor(), book.getId(), copies, copyWord);
    }

    private String handleRegisterMember(String[] tokens) {
        requireArgs(tokens, 2, "register_member <name>");
        Member member = libraryService.registerMember(tokens[1]);
        return String.format("Member registered with member ID %d: %s",
                member.getId(), member.getName());
    }

    private String handleBorrowBook(String[] tokens) {
        requireArgs(tokens, 3, "borrow_book <member_id> <book_id>");
        int memberId = Integer.parseInt(tokens[1]);
        int bookId = Integer.parseInt(tokens[2]);
        // findBook/findMember validation happens in service; we need the book title for output
        // So we check existence first via service (it throws on missing IDs)
        libraryService.borrowBook(memberId, bookId);
        Book book = libraryService.checkAvailability(bookId);
        return String.format("Book \"%s\" borrowed by member %d", book.getTitle(), memberId);
    }

    private String handleReturnBook(String[] tokens) {
        requireArgs(tokens, 3, "return_book <member_id> <book_id>");
        int memberId = Integer.parseInt(tokens[1]);
        int bookId = Integer.parseInt(tokens[2]);
        Book book = libraryService.checkAvailability(bookId); // get title before returning
        libraryService.returnBook(memberId, bookId);
        return String.format("Book \"%s\" returned by member %d", book.getTitle(), memberId);
    }

    private String handleCheckAvailability(String[] tokens) {
        requireArgs(tokens, 2, "check_availability <book_id>");
        int bookId = Integer.parseInt(tokens[1]);
        Book book = libraryService.checkAvailability(bookId);
        int available = book.getAvailableCopies();
        String copyWord = available == 1 ? "copy" : "copies";
        return String.format("Book ID %d availability: %d %s available", bookId, available, copyWord);
    }

    private String handleDisplayMemberBooks(String[] tokens) {
        requireArgs(tokens, 2, "display_member_books <member_id>");
        int memberId = Integer.parseInt(tokens[1]);
        List<Book> borrowed = libraryService.getMemberBooks(memberId);
        if (borrowed.isEmpty()) {
            return "Member " + memberId + " books: none";
        }
        String bookList = borrowed.stream()
                .map(b -> String.format("\"%s\" (ID: %d)", b.getTitle(), b.getId()))
                .collect(Collectors.joining(", "));
        return "Member " + memberId + " books: " + bookList;
    }

    private String handleDisplayLibraryStatus() {
        LibraryService.LibraryStats stats = libraryService.getLibraryStatus();
        return String.format("Library Status - Total books: %d, Borrowed: %d, Available: %d",
                stats.total(), stats.borrowed(), stats.available());
    }

    private void requireArgs(String[] tokens, int expected, String usage) {
        if (tokens.length < expected) {
            throw new LibraryException("Invalid command. Usage: " + usage);
        }
    }

    /**
     * Tokenizes a command line, treating quoted strings as single tokens.
     * e.g. add_book "The Great Gatsby" "F. Scott Fitzgerald" 2
     *   -> ["add_book", "The Great Gatsby", "F. Scott Fitzgerald", "2"]
     */
     static public String[] tokenize(String line) {
        List<String> tokens = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens.toArray(new String[0]);
    }
}
