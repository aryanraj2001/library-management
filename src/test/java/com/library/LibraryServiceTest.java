package com.library;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
    }

    // ---- addBook ----

    @Test
    void addBook_assignsIncrementalIds() {
        Book b1 = service.addBook("Title A", "Author A", 1);
        Book b2 = service.addBook("Title B", "Author B", 2);
        assertEquals(1, b1.getId());
        assertEquals(2, b2.getId());
    }

    @Test
    void addBook_storesCorrectDetails() {
        Book book = service.addBook("1984", "George Orwell", 3);
        assertEquals("1984", book.getTitle());
        assertEquals("George Orwell", book.getAuthor());
        assertEquals(3, book.getTotalCopies());
        assertEquals(3, book.getAvailableCopies());
    }

    // ---- registerMember ----

    @Test
    void registerMember_assignsIncrementalIds() {
        Member m1 = service.registerMember("Alice");
        Member m2 = service.registerMember("Bob");
        assertEquals(1, m1.getId());
        assertEquals(2, m2.getId());
    }

    // ---- borrowBook ----

    @Test
    void borrowBook_reducesAvailability() {
        service.addBook("Book", "Author", 2);
        service.registerMember("Alice");
        service.borrowBook(1, 1);
        assertEquals(1, service.checkAvailability(1).getAvailableCopies());
    }

    @Test
    void borrowBook_throwsWhenNoCopiesAvailable() {
        service.addBook("Book", "Author", 1);
        service.registerMember("Alice");
        service.registerMember("Bob");
        service.borrowBook(1, 1);
        LibraryException ex = assertThrows(LibraryException.class, () -> service.borrowBook(2, 1));
        assertTrue(ex.getMessage().contains("No copies available"));
    }

    @Test
    void borrowBook_throwsWhenMemberLimitReached() {
        service.addBook("B1", "A", 2);
        service.addBook("B2", "A", 2);
        service.addBook("B3", "A", 2);
        service.addBook("B4", "A", 2);
        service.registerMember("Alice");
        service.borrowBook(1, 1);
        service.borrowBook(1, 2);
        service.borrowBook(1, 3);
        LibraryException ex = assertThrows(LibraryException.class, () -> service.borrowBook(1, 4));
        assertTrue(ex.getMessage().contains("borrowing limit"));
    }

    @Test
    void borrowBook_throwsWhenBookNotFound() {
        service.registerMember("Alice");
        assertThrows(LibraryException.class, () -> service.borrowBook(1, 99));
    }

    @Test
    void borrowBook_throwsWhenMemberNotFound() {
        service.addBook("Book", "Author", 1);
        assertThrows(LibraryException.class, () -> service.borrowBook(99, 1));
    }

    // ---- returnBook ----

    @Test
    void returnBook_increasesAvailability() {
        service.addBook("Book", "Author", 1);
        service.registerMember("Alice");
        service.borrowBook(1, 1);
        service.returnBook(1, 1);
        assertEquals(1, service.checkAvailability(1).getAvailableCopies());
    }

    @Test
    void returnBook_throwsWhenMemberDidNotBorrowBook() {
        service.addBook("Book", "Author", 2);
        service.registerMember("Alice");
        service.registerMember("Bob");
        service.borrowBook(1, 1);
        assertThrows(LibraryException.class, () -> service.returnBook(2, 1));
    }

    // ---- displayMemberBooks ----

    @Test
    void getMemberBooks_returnsCorrectBooks() {
        service.addBook("Book1", "Author", 1);
        service.addBook("Book2", "Author", 1);
        service.registerMember("Alice");
        service.borrowBook(1, 1);
        service.borrowBook(1, 2);
        List<Book> books = service.getMemberBooks(1);
        assertEquals(2, books.size());
    }

    @Test
    void getMemberBooks_emptyWhenNothingBorrowed() {
        service.registerMember("Alice");
        assertTrue(service.getMemberBooks(1).isEmpty());
    }

    // ---- libraryStatus ----

    @Test
    void getLibraryStatus_correctTotals() {
        service.addBook("B1", "A", 2);
        service.addBook("B2", "A", 1);
        service.registerMember("Alice");
        service.borrowBook(1, 1);

        LibraryService.LibraryStats stats = service.getLibraryStatus();
        assertEquals(3, stats.total());
        assertEquals(1, stats.borrowed());
        assertEquals(2, stats.available());
    }
}
