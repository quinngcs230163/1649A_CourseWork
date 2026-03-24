package service;

import model.Book;
import model.Order;
import model.OrderItem;
import structure.DoublyNode;
import structure.LinkedQueue;
import structure.SinglyNode;

public class BookCatalog {
    private SinglyNode<Book> head;

    public BookCatalog() {
        head = null;
    }

    public void loadSampleBooks() {
        addBook(new Book("B001", "Clean Code", "Robert C. Martin", 25.50, 10));
        addBook(new Book("B002", "Java Basics", "James Gosling", 30.00, 8));
        addBook(new Book("B003", "Data Structures", "Mark Allen Weiss", 28.75, 7));
        addBook(new Book("B004", "Algorithms Unlocked", "Thomas H. Cormen", 35.20, 6));
        addBook(new Book("B005", "Head First Java", "Kathy Sierra", 27.90, 9));
        addBook(new Book("B006", "Effective Java", "Joshua Bloch", 40.00, 5));
        addBook(new Book("B007", "Design Patterns", "Erich Gamma", 45.50, 4));
        addBook(new Book("B008", "Refactoring", "Martin Fowler", 42.30, 6));
        addBook(new Book("B009", "The Pragmatic Programmer", "Andrew Hunt", 38.40, 7));
        addBook(new Book("B010", "Introduction to Algorithms", "Thomas H. Cormen", 55.00, 5));
        addBook(new Book("B011", "Computer Networks", "Andrew S. Tanenbaum", 48.25, 6));
        addBook(new Book("B012", "Operating System Concepts", "Abraham Silberschatz", 52.80, 5));
        addBook(new Book("B013", "Database System Concepts", "Henry F. Korth", 47.60, 6));
        addBook(new Book("B014", "Artificial Intelligence: A Modern Approach", "Stuart Russell", 60.00, 4));
        addBook(new Book("B015", "Software Engineering", "Ian Sommerville", 44.90, 8));
    }

    public void addBook(Book book) {
        if (book == null || book.getBookId() == null || book.getBookId().trim().isEmpty()) {
            return;
        }

        if (containsBookId(book.getBookId())) {
            return;
        }

        SinglyNode<Book> newNode = new SinglyNode<>(book);

        if (head == null) {
            head = newNode;
            return;
        }

        SinglyNode<Book> current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
    }

    public boolean containsBookId(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return false;
        }

        SinglyNode<Book> current = head;
        while (current != null) {
            if (current.data.getBookId().equalsIgnoreCase(bookId.trim())) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public Book searchBookById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return null;
        }

        SinglyNode<Book> current = head;
        while (current != null) {
            if (current.data.getBookId().equalsIgnoreCase(bookId.trim())) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    public void displayCatalog() {
        if (head == null) {
            System.out.println("Book catalog is empty.");
            return;
        }

        System.out.println("=== BOOK CATALOG ===");
        SinglyNode<Book> current = head;
        while (current != null) {
            System.out.println(current.data);
            current = current.next;
        }
    }

    public boolean isEmpty() {
        return head == null;
    }

    public boolean canFulfillOrder(Order order) {
        if (order == null || !order.hasItems()) {
            return false;
        }

        SinglyNode<OrderItem> current = order.getItemsHead();
        while (current != null) {
            Book catalogBook = searchBookById(current.data.getBook().getBookId());
            if (catalogBook == null || !catalogBook.hasEnoughStock(current.data.getQuantity())) {
                return false;
            }
            current = current.next;
        }
        return true;
    }

    public String getFirstUnavailableBookMessage(Order order) {
        if (order == null || !order.hasItems()) {
            return "Order has no items.";
        }

        SinglyNode<OrderItem> current = order.getItemsHead();
        while (current != null) {
            Book catalogBook = searchBookById(current.data.getBook().getBookId());
            if (catalogBook == null) {
                return "Book ID " + current.data.getBook().getBookId() + " does not exist in the catalog.";
            }
            if (!catalogBook.hasEnoughStock(current.data.getQuantity())) {
                return "Not enough stock for book " + catalogBook.getTitle() + ". Available: " +
                        catalogBook.getStockQuantity() + ", Requested: " + current.data.getQuantity();
            }
            current = current.next;
        }
        return "All books are available.";
    }

    public boolean reserveOrder(Order order) {
        if (!canFulfillOrder(order)) {
            return false;
        }

        SinglyNode<OrderItem> current = order.getItemsHead();
        while (current != null) {
            Book catalogBook = searchBookById(current.data.getBook().getBookId());
            catalogBook.decreaseStock(current.data.getQuantity());
            current = current.next;
        }
        return true;
    }

    public void reservePendingOrders(LinkedQueue<Order> pendingOrders) {
        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return;
        }

        DoublyNode<Order> currentOrder = pendingOrders.getFrontNode();
        while (currentOrder != null) {
            reserveOrder(currentOrder.data);
            currentOrder = currentOrder.next;
        }
    }
}