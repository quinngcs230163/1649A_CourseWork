package model;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private double price;
    private int stockQuantity;

    public Book(String bookId, String title, String author, double price) {
        this(bookId, title, author, price, 0);
    }

    public Book(String bookId, String title, String author, double price, int stockQuantity) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean hasEnoughStock(int quantity) {
        return quantity > 0 && stockQuantity >= quantity;
    }

    public boolean decreaseStock(int quantity) {
        if (!hasEnoughStock(quantity)) {
            return false;
        }
        stockQuantity -= quantity;
        return true;
    }

    public void increaseStock(int quantity) {
        if (quantity > 0) {
            stockQuantity += quantity;
        }
    }

    @Override
    public String toString() {
        return "Book ID: " + bookId +
                ", Title: " + title +
                ", Author: " + author +
                ", Price: $" + String.format("%.2f", price) +
                ", Stock: " + stockQuantity;
    }
}