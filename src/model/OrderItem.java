package model;

public class OrderItem {
    private Book book;
    private int quantity;

    public OrderItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return book.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return "Book ID: " + book.getBookId() +
                ", Title: " + book.getTitle() +
                ", Author: " + book.getAuthor() +
                ", Price: $" + String.format("%.2f", book.getPrice()) +
                ", Quantity: " + quantity +
                ", Subtotal: $" + String.format("%.2f", getSubtotal());
    }
}