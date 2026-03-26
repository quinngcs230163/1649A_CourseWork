package service;

import model.Book;
import model.Order;
import model.OrderItem;
import structure.SinglyNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BookCatalog {
    private static final String BOOK_FILE = "data/books.csv";
    private static final String HEADER = "bookId,title,author,price,stockQuantity";

    private SinglyNode<Book> head;

    public BookCatalog() {
        head = null;
        initializeBookFile();
    }

    private void initializeBookFile() {
        try {
            File file = new File(BOOK_FILE);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(HEADER);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error creating books file.");
        }
    }

    public void loadBooks() {
        clear();

        boolean loaded = loadBooksFromCsv();
        if (!loaded || head == null) {
            loadSampleBooks();
            saveBooksToFile();
        }
    }

    public void loadSampleBooks() {
        addBookInternal(new Book("B001", "Clean Code", "Robert C. Martin", 25.50, 10));
        addBookInternal(new Book("B002", "Java Basics", "James Gosling", 30.00, 8));
        addBookInternal(new Book("B003", "Data Structures", "Mark Allen Weiss", 28.75, 7));
        addBookInternal(new Book("B004", "Algorithms Unlocked", "Thomas H. Cormen", 35.20, 6));
        addBookInternal(new Book("B005", "Head First Java", "Kathy Sierra", 27.90, 9));
        addBookInternal(new Book("B006", "Effective Java", "Joshua Bloch", 40.00, 5));
        addBookInternal(new Book("B007", "Design Patterns", "Erich Gamma", 45.50, 4));
        addBookInternal(new Book("B008", "Refactoring", "Martin Fowler", 42.30, 6));
        addBookInternal(new Book("B009", "The Pragmatic Programmer", "Andrew Hunt", 38.40, 7));
        addBookInternal(new Book("B010", "Introduction to Algorithms", "Thomas H. Cormen", 55.00, 5));
        addBookInternal(new Book("B011", "Computer Networks", "Andrew S. Tanenbaum", 48.25, 6));
        addBookInternal(new Book("B012", "Operating System Concepts", "Abraham Silberschatz", 52.80, 5));
        addBookInternal(new Book("B013", "Database System Concepts", "Henry F. Korth", 47.60, 6));
        addBookInternal(new Book("B014", "Artificial Intelligence: A Modern Approach", "Stuart Russell", 60.00, 4));
        addBookInternal(new Book("B015", "Software Engineering", "Ian Sommerville", 44.90, 8));
    }

    public boolean addBook(Book book) {
        if (book == null) {
            return false;
        }

        if (!isValidBook(book)) {
            return false;
        }

        if (containsBookId(book.getBookId()) || containsBookTitle(book.getTitle())) {
            return false;
        }

        addBookInternal(book);
        saveBooksToFile();
        return true;
    }

    private void addBookInternal(Book book) {
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

    public boolean containsBookTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        SinglyNode<Book> current = head;
        while (current != null) {
            if (current.data.getTitle() != null && current.data.getTitle().trim().equalsIgnoreCase(title.trim())) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public boolean containsOtherBookTitle(String title, String excludedBookId) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        SinglyNode<Book> current = head;
        while (current != null) {
            Book book = current.data;
            if (book.getTitle() != null
                    && book.getTitle().trim().equalsIgnoreCase(title.trim())
                    && !book.getBookId().equalsIgnoreCase(excludedBookId == null ? "" : excludedBookId.trim())) {
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

    public boolean updateBook(String bookId, String newTitle, String newAuthor, double newPrice, int newStockQuantity) {
        Book book = searchBookById(bookId);
        if (book == null) {
            return false;
        }

        if (newTitle == null || newTitle.trim().isEmpty()) {
            return false;
        }
        if (newAuthor == null || newAuthor.trim().isEmpty()) {
            return false;
        }
        if (newPrice <= 0 || newStockQuantity < 0) {
            return false;
        }
        if (containsOtherBookTitle(newTitle, bookId)) {
            return false;
        }

        book.setTitle(newTitle.trim());
        book.setAuthor(newAuthor.trim());
        book.setPrice(newPrice);
        book.setStockQuantity(newStockQuantity);
        saveBooksToFile();
        return true;
    }

    public boolean deleteBook(String bookId) {
        if (bookId == null || bookId.trim().isEmpty() || head == null) {
            return false;
        }

        String normalizedId = bookId.trim();

        if (head.data.getBookId().equalsIgnoreCase(normalizedId)) {
            head = head.next;
            saveBooksToFile();
            return true;
        }

        SinglyNode<Book> previous = head;
        SinglyNode<Book> current = head.next;

        while (current != null) {
            if (current.data.getBookId().equalsIgnoreCase(normalizedId)) {
                previous.next = current.next;
                saveBooksToFile();
                return true;
            }
            previous = current;
            current = current.next;
        }

        return false;
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

    public boolean deductStockForOrder(Order order) {
        if (!canFulfillOrder(order)) {
            return false;
        }

        SinglyNode<OrderItem> current = order.getItemsHead();
        while (current != null) {
            Book catalogBook = searchBookById(current.data.getBook().getBookId());
            catalogBook.decreaseStock(current.data.getQuantity());
            current = current.next;
        }

        saveBooksToFile();
        return true;
    }

    public void saveBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOK_FILE))) {
            writer.write(HEADER);
            writer.newLine();

            SinglyNode<Book> current = head;
            while (current != null) {
                Book book = current.data;
                writer.write(escape(book.getBookId()) + "," +
                        escape(book.getTitle()) + "," +
                        escape(book.getAuthor()) + "," +
                        book.getPrice() + "," +
                        book.getStockQuantity());
                writer.newLine();
                current = current.next;
            }
        } catch (IOException e) {
            System.out.println("Error saving books to file.");
        }
    }

    private boolean loadBooksFromCsv() {
        File file = new File(BOOK_FILE);
        if (!file.exists()) {
            return false;
        }

        boolean hasData = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 5) {
                    continue;
                }

                try {
                    String bookId = removeQuotes(parts[0]);
                    String title = removeQuotes(parts[1]);
                    String author = removeQuotes(parts[2]);
                    double price = Double.parseDouble(parts[3]);
                    int stockQuantity = Integer.parseInt(parts[4]);

                    if (bookId.isEmpty() || title.isEmpty() || author.isEmpty() || price <= 0 || stockQuantity < 0) {
                        continue;
                    }

                    addBookInternal(new Book(bookId, title, author, price, stockQuantity));
                    hasData = true;
                } catch (NumberFormatException e) {
                    // skip bad row
                }
            }
        } catch (IOException e) {
            return false;
        }

        return hasData;
    }

    private String[] parseCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String removeQuotes(String value) {
        return value.replace("\"\"", "\"").replace("\"", "").trim();
    }

    private String escape(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private boolean isValidBook(Book book) {
        return book.getBookId() != null && !book.getBookId().trim().isEmpty()
                && book.getTitle() != null && !book.getTitle().trim().isEmpty()
                && book.getAuthor() != null && !book.getAuthor().trim().isEmpty()
                && book.getPrice() > 0
                && book.getStockQuantity() >= 0;
    }

    private void clear() {
        head = null;
    }
}