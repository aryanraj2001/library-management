package com.library;

import com.library.command.CommandProcessor;
import com.library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandProcessorTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new LibraryService());
    }

    @Test
    void addBook_outputsCorrectMessage() {
        String result = processor.process("add_book \"The Great Gatsby\" \"F. Scott Fitzgerald\" 2");
        assertEquals("Book \"The Great Gatsby\" by F. Scott Fitzgerald added with book ID 1 (2 copies)", result);
    }

    @Test
    void addBook_singleCopyUsesSingular() {
        String result = processor.process("add_book \"1984\" \"George Orwell\" 1");
        assertTrue(result.contains("1 copy)"), "Expected singular 'copy', got: " + result);
    }

    @Test
    void registerMember_outputsCorrectMessage() {
        String result = processor.process("register_member \"Alice Johnson\"");
        assertEquals("Member registered with member ID 1: Alice Johnson", result);
    }

    @Test
    void borrowBook_outputsCorrectMessage() {
        processor.process("add_book \"1984\" \"George Orwell\" 2");
        processor.process("register_member \"Alice\"");
        String result = processor.process("borrow_book 1 1");
        assertEquals("Book \"1984\" borrowed by member 1", result);
    }

    @Test
    void borrowBook_noCopiesAvailable_outputsErrorMessage() {
        processor.process("add_book \"1984\" \"George Orwell\" 1");
        processor.process("register_member \"Alice\"");
        processor.process("register_member \"Bob\"");
        processor.process("borrow_book 1 1");
        String result = processor.process("borrow_book 2 1");
        assertEquals("Cannot borrow book: No copies available", result);
    }

    @Test
    void returnBook_outputsCorrectMessage() {
        processor.process("add_book \"1984\" \"George Orwell\" 1");
        processor.process("register_member \"Alice\"");
        processor.process("borrow_book 1 1");
        String result = processor.process("return_book 1 1");
        assertEquals("Book \"1984\" returned by member 1", result);
    }

    @Test
    void checkAvailability_pluralCopies() {
        processor.process("add_book \"Book\" \"Author\" 3");
        String result = processor.process("check_availability 1");
        assertEquals("Book ID 1 availability: 3 copies available", result);
    }

    @Test
    void checkAvailability_singleCopy() {
        processor.process("add_book \"Book\" \"Author\" 2");
        processor.process("register_member \"Alice\"");
        processor.process("borrow_book 1 1");
        String result = processor.process("check_availability 1");
        assertEquals("Book ID 1 availability: 1 copy available", result);
    }

    @Test
    void displayMemberBooks_showsBorrowedBooks() {
        processor.process("add_book \"The Great Gatsby\" \"Author\" 1");
        processor.process("add_book \"1984\" \"Author\" 1");
        processor.process("register_member \"Alice\"");
        processor.process("borrow_book 1 1");
        processor.process("borrow_book 1 2");
        String result = processor.process("display_member_books 1");
        assertEquals("Member 1 books: \"The Great Gatsby\" (ID: 1), \"1984\" (ID: 2)", result);
    }

    @Test
    void displayLibraryStatus_correctOutput() {
        processor.process("add_book \"B1\" \"A\" 2");
        processor.process("add_book \"B2\" \"A\" 1");
        processor.process("register_member \"Alice\"");
        processor.process("borrow_book 1 2");
        String result = processor.process("display_library_status");
        assertEquals("Library Status - Total books: 3, Borrowed: 1, Available: 2", result);
    }

    @Test
    void memberNotFound_outputsErrorMessage() {
        processor.process("add_book \"Book\" \"Author\" 1");
        String result = processor.process("borrow_book 3 1");
        assertTrue(result.contains("Member ID 3 not found"));
    }

    @Test
    void emptyLine_returnsNull() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }

    @Test
    void tokenize_handlesQuotedStrings() {
        String[] tokens = com.library.command.CommandProcessor.tokenize("add_book \"The Great Gatsby\" \"F. Scott Fitzgerald\" 2");
        assertArrayEquals(new String[]{"add_book", "The Great Gatsby", "F. Scott Fitzgerald", "2"}, tokens);
    }

    @Test
    void fullScenario_matchesSampleOutput() {
        assertEquals("Book \"The Great Gatsby\" by F. Scott Fitzgerald added with book ID 1 (2 copies)",
                processor.process("add_book \"The Great Gatsby\" \"F. Scott Fitzgerald\" 2"));
        assertEquals("Book \"1984\" by George Orwell added with book ID 2 (1 copy)",
                processor.process("add_book \"1984\" \"George Orwell\" 1"));
        assertEquals("Member registered with member ID 1: Alice Johnson",
                processor.process("register_member \"Alice Johnson\""));
        assertEquals("Member registered with member ID 2: Bob Smith",
                processor.process("register_member \"Bob Smith\""));
        assertEquals("Book \"The Great Gatsby\" borrowed by member 1", processor.process("borrow_book 1 1"));
        assertEquals("Book \"The Great Gatsby\" borrowed by member 2", processor.process("borrow_book 2 1"));
        assertEquals("Book \"1984\" borrowed by member 1",              processor.process("borrow_book 1 2"));
        assertEquals("Book ID 1 availability: 0 copies available",      processor.process("check_availability 1"));
        assertEquals("Cannot borrow book: No copies available",         processor.process("borrow_book 1 1"));
        assertEquals("Member 1 books: \"The Great Gatsby\" (ID: 1), \"1984\" (ID: 2)",
                processor.process("display_member_books 1"));
        assertEquals("Book \"The Great Gatsby\" returned by member 1",  processor.process("return_book 1 1"));
        assertEquals("Book ID 1 availability: 1 copy available",        processor.process("check_availability 1"));
        assertEquals("Member ID 3 not found",                           processor.process("borrow_book 3 1"));
        assertEquals("Library Status - Total books: 3, Borrowed: 2, Available: 1",
                processor.process("display_library_status"));
    }
}
