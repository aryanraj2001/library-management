package com.library.service;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryService {

    private final Map<Integer, Book> books = new HashMap<>();
    private final Map<Integer, Member> members = new HashMap<>();

    private int nextBookId = 1;
    private int nextMemberId = 1;

    public Book addBook(String title, String author, int copies) {
        Book book = new Book(nextBookId++, title, author, copies);
        books.put(book.getId(), book);
        return book;
    }

    public Member registerMember(String name) {
        Member member = new Member(nextMemberId++, name);
        members.put(member.getId(), member);
        return member;
    }

    public void borrowBook(int memberId, int bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);

        if (!book.isAvailable()) {
            throw new LibraryException("Cannot borrow book: No copies available");
        }
        if (member.hasReachedBorrowLimit()) {
            throw new LibraryException("Cannot borrow book: Member has reached the borrowing limit");
        }

        book.borrowCopy();
        member.addBook(book);
    }

    public void returnBook(int memberId, int bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);

        if (!member.hasBorrowed(bookId)) {
            throw new LibraryException("Cannot return book: Member " + memberId + " did not borrow book " + bookId);
        }

        book.returnCopy();
        member.removeBook(bookId);
    }

    public Book checkAvailability(int bookId) {
        return findBook(bookId);
    }

    public List<Book> getMemberBooks(int memberId) {
        return findMember(memberId).getBorrowedBooks();
    }

    public LibraryStats getLibraryStatus() {
        int total = books.values().stream().mapToInt(Book::getTotalCopies).sum();
        int available = books.values().stream().mapToInt(Book::getAvailableCopies).sum();
        int borrowed = total - available;
        return new LibraryStats(total, borrowed, available);
    }

    private Book findBook(int bookId) {
        Book book = books.get(bookId);
        if (book == null) {
            throw new LibraryException("Book ID " + bookId + " not found");
        }
        return book;
    }

    private Member findMember(int memberId) {
        Member member = members.get(memberId);
        if (member == null) {
            throw new LibraryException("Member ID " + memberId + " not found");
        }
        return member;
    }

    public record LibraryStats(int total, int borrowed, int available) {}
}
