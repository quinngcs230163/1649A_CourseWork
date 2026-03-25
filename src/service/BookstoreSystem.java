package service;

import model.Book;
import model.Order;
import model.OrderItem;
import structure.DoublyNode;
import structure.LinkedQueue;
import structure.LinkedStack;

public class BookstoreSystem {
    private LinkedQueue<Order> pendingOrders;
    private LinkedQueue<Order> processedOrders;
    private LinkedStack<String> actionHistory;
    private OrderFileManager fileManager;
    private BookCatalog catalog;

    public BookstoreSystem(BookCatalog catalog) {
        this.catalog = catalog;
        this.fileManager = new OrderFileManager();
        this.pendingOrders = fileManager.loadPendingOrders();
        this.processedOrders = fileManager.loadProcessedOrders();
        this.actionHistory = new LinkedStack<>();

        if (this.catalog != null) {
            this.catalog.reservePendingOrders(pendingOrders);
        }
    }

    public boolean isDuplicateOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }

        return findOrderInQueue(pendingOrders, orderId) != null || findOrderInQueue(processedOrders, orderId) != null;
    }

    public void addOrder(Order order) {
        if (!isValidBasicOrder(order)) {
            return;
        }

        if (isDuplicateOrderId(order.getOrderId())) {
            System.out.println("Order ID already exists. Cannot add duplicate order.");
            return;
        }

        if (!catalog.canFulfillOrder(order)) {
            System.out.println("Order cannot be added because availability check failed.");
            System.out.println(catalog.getFirstUnavailableBookMessage(order));
            return;
        }

        catalog.reserveOrder(order);
        order.setStatus("Pending");
        pendingOrders.enqueue(order);
        fileManager.saveAllPendingOrders(pendingOrders);

        actionHistory.push("Added order: " + order.getOrderId());
        System.out.println("Order added successfully.");
        System.out.println("Availability confirmed. Order added to pending queue.");
    }

    private boolean isValidBasicOrder(Order order) {
        if (order == null) {
            System.out.println("Invalid order.");
            return false;
        }
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            System.out.println("Order ID cannot be empty.");
            return false;
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            System.out.println("Customer name cannot be empty.");
            return false;
        }
        if (order.getShippingAddress() == null || order.getShippingAddress().trim().isEmpty()) {
            System.out.println("Shipping address cannot be empty.");
            return false;
        }
        if (!order.hasItems()) {
            System.out.println("Order must contain at least one item.");
            return false;
        }
        return true;
    }

    public void processNextOrder() {
        Order order = pendingOrders.dequeue();
        if (order == null) {
            System.out.println("No order to process.");
            return;
        }

        order.setStatus("Processed");
        processedOrders.enqueue(order);
        fileManager.saveAllPendingOrders(pendingOrders);
        fileManager.saveProcessedOrder(order);

        actionHistory.push("Processed order: " + order.getOrderId());
        System.out.println("Processed order successfully:");
        System.out.println(order);
        System.out.println("Order items:");
        order.displayItems();
    }

    public void viewNextOrder() {
        Order order = pendingOrders.peek();
        if (order == null) {
            System.out.println("No pending orders.");
            return;
        }

        System.out.println("Next order to process:");
        System.out.println(order);
        System.out.println("Order items:");
        order.displayItems();
    }

    public void displayPendingOrders() {
        System.out.println("=== PENDING ORDERS ===");
        pendingOrders.display();
    }

    public void displayProcessedOrders() {
        System.out.println("=== PROCESSED ORDERS ===");
        processedOrders.display();
    }

    public Order searchOrderById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return null;
        }

        Order pendingOrder = findOrderInQueue(pendingOrders, orderId);
        if (pendingOrder != null) {
            actionHistory.push("Searched order: " + orderId + " (Found in pending)");
            return pendingOrder;
        }

        Order processedOrder = findOrderInQueue(processedOrders, orderId);
        if (processedOrder != null) {
            actionHistory.push("Searched order: " + orderId + " (Found in processed)");
            return processedOrder;
        }

        actionHistory.push("Searched order: " + orderId + " (Not found)");
        return null;
    }

    public void sortOrderItemsByTitle(String orderId) {
        Order order = findOrderInQueue(pendingOrders, orderId);
        if (order == null) {
            System.out.println("Pending order not found. Only pending orders can be sorted/edited.");
            return;
        }

        order.sortItemsByTitle();
        fileManager.saveAllPendingOrders(pendingOrders);
        actionHistory.push("Sorted items in pending order: " + orderId + " by title");
        System.out.println("Items sorted successfully for order " + orderId);
    }

    public boolean editOrderCustomerName(String orderId, String newCustomerName) {
        Order order = findOrderInQueue(pendingOrders, orderId);
        if (order == null) {
            return false;
        }

        if (newCustomerName != null && !newCustomerName.trim().isEmpty()) {
            order.setCustomerName(newCustomerName.trim());
            fileManager.saveAllPendingOrders(pendingOrders);
            actionHistory.push("Updated customer name for order: " + orderId);
        }
        return true;
    }

    public boolean editOrderShippingAddress(String orderId, String newShippingAddress) {
        Order order = findOrderInQueue(pendingOrders, orderId);
        if (order == null) {
            return false;
        }

        if (newShippingAddress != null && !newShippingAddress.trim().isEmpty()) {
            order.setShippingAddress(newShippingAddress.trim());
            fileManager.saveAllPendingOrders(pendingOrders);
            actionHistory.push("Updated shipping address for order: " + orderId);
        }
        return true;
    }

    public boolean addBookToExistingOrder(String orderId, Book book, int quantity) {
        Order order = findOrderInQueue(pendingOrders, orderId);
        if (order == null || book == null || quantity <= 0) {
            return false;
        }

        Book catalogBook = catalog.searchBookById(book.getBookId());
        if (catalogBook == null || !catalogBook.hasEnoughStock(quantity)) {
            return false;
        }

        catalogBook.decreaseStock(quantity);
        OrderItem item = new OrderItem(new Book(book.getBookId(), book.getTitle(), book.getAuthor(), book.getPrice()), quantity);
        order.addOrUpdateItem(item);
        fileManager.saveAllPendingOrders(pendingOrders);
        actionHistory.push("Updated order: " + orderId + " with book " + book.getBookId());
        return true;
    }

    public void showActionHistory() {
        actionHistory.display();
    }

    public BookCatalog getCatalog() {
        return catalog;
    }

    private Order findOrderInQueue(LinkedQueue<Order> queue, String orderId) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        DoublyNode<Order> current = queue.getFrontNode();
        while (current != null) {
            if (current.data.getOrderId().equalsIgnoreCase(orderId.trim())) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }
}