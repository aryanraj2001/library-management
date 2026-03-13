package com.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Member {

    public static final int MAX_BORROW_LIMIT = 3;

    private final int id;
    private final String name;
    private final List<Book> borrowedBooks = new ArrayList<>();

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public List<Book> getBorrowedBooks() {
        return Collections.unmodifiableList(borrowedBooks);
    }

    public boolean hasReachedBorrowLimit() {
        return borrowedBooks.size() >= MAX_BORROW_LIMIT;
    }

    public boolean hasBorrowed(int bookId) {
        return borrowedBooks.stream().anyMatch(b -> b.getId() == bookId);
    }

    public void addBook(Book book) {
        borrowedBooks.add(book);
    }

    public void removeBook(int bookId) {
        borrowedBooks.removeIf(b -> b.getId() == bookId);
    }
}
