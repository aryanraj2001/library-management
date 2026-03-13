package com.library.model;

public class Book {

    private final int id;
    private final String title;
    private final String author;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, int copies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void borrowCopy() {
        if (!isAvailable()) {
            throw new IllegalStateException("No copies available");
        }
        availableCopies--;
    }

    public void returnCopy() {
        availableCopies++;
    }
}
