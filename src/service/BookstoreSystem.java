package service;

import model.Book;
import model.Order;
import model.OrderItem;
import structure.DoublyNode;
import structure.LinkedQueue;
import structure.LinkedStack;
import structure.SinglyNode;

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

        String availabilityMessage = validateOrderAgainstCatalogAndPending(order);
        if (availabilityMessage != null) {
            System.out.println("Order cannot be added.");
            System.out.println(availabilityMessage);
            return;
        }

        order.setStatus("Pending");
        pendingOrders.enqueue(order);
        fileManager.saveAllPendingOrders(pendingOrders);

        actionHistory.push("Added order: " + order.getOrderId());
        System.out.println("Order added successfully.");
        System.out.println("Order added to pending queue.");
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
        Order order = pendingOrders.peek();
        if (order == null) {
            System.out.println("No order to process.");
            return;
        }

        if (!catalog.deductStockForOrder(order)) {
            System.out.println("Cannot process order because stock is no longer sufficient.");
            System.out.println(catalog.getFirstUnavailableBookMessage(order));
            return;
        }

        order = pendingOrders.dequeue();
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
        System.out.println("=== Pending Orders ===");
        pendingOrders.display();
    }

    public void displayProcessedOrders() {
        System.out.println("=== Processed Orders ===");
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
        if (catalogBook == null) {
            return false;
        }

        int availableForThisOrder = getAvailableStockForOrder(book.getBookId(), orderId);
        int currentQuantityInOrder = getQuantityOfBookInOrder(order, book.getBookId());

        if (currentQuantityInOrder + quantity > availableForThisOrder) {
            return false;
        }

        OrderItem item = new OrderItem(
                new Book(book.getBookId(), book.getTitle(), book.getAuthor(), book.getPrice()),
                quantity
        );
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

    private String validateOrderAgainstCatalogAndPending(Order order) {
        SinglyNode<OrderItem> current = order.getItemsHead();

        while (current != null) {
            OrderItem item = current.data;
            Book catalogBook = catalog.searchBookById(item.getBook().getBookId());

            if (catalogBook == null) {
                return "Book ID " + item.getBook().getBookId() + " does not exist in the catalog.";
            }

            int available = getAvailableStockForNewOrder(item.getBook().getBookId());
            if (item.getQuantity() > available) {
                return "Not enough available stock for book " + catalogBook.getTitle()
                        + ". Available for new pending orders: " + available
                        + ", Requested: " + item.getQuantity();
            }

            current = current.next;
        }

        return null;
    }

    private int getAvailableStockForNewOrder(String bookId) {
        Book catalogBook = catalog.searchBookById(bookId);
        if (catalogBook == null) {
            return 0;
        }

        int stock = catalogBook.getStockQuantity();
        int reservedByPending = getReservedQuantityInPendingOrders(bookId, null, false);
        int available = stock - reservedByPending;

        return Math.max(available, 0);
    }

    private int getAvailableStockForOrder(String bookId, String currentOrderId) {
        Book catalogBook = catalog.searchBookById(bookId);
        if (catalogBook == null) {
            return 0;
        }

        int stock = catalogBook.getStockQuantity();
        int reservedByEarlierPending = getReservedQuantityInPendingOrders(bookId, currentOrderId, true);
        int available = stock - reservedByEarlierPending;

        return Math.max(available, 0);
    }

    private int getReservedQuantityInPendingOrders(String bookId, String stopOrderId, boolean stopBeforeMatchedOrder) {
        int reserved = 0;

        DoublyNode<Order> currentOrderNode = pendingOrders.getFrontNode();
        while (currentOrderNode != null) {
            Order currentOrder = currentOrderNode.data;

            if (stopOrderId != null && currentOrder.getOrderId().equalsIgnoreCase(stopOrderId.trim())) {
                if (stopBeforeMatchedOrder) {
                    break;
                }
            }

            reserved += getQuantityOfBookInOrder(currentOrder, bookId);

            if (stopOrderId != null && currentOrder.getOrderId().equalsIgnoreCase(stopOrderId.trim())) {
                break;
            }

            currentOrderNode = currentOrderNode.next;
        }

        return reserved;
    }

    private int getQuantityOfBookInOrder(Order order, String bookId) {
        if (order == null || bookId == null || bookId.trim().isEmpty()) {
            return 0;
        }

        int quantity = 0;
        SinglyNode<OrderItem> currentItem = order.getItemsHead();

        while (currentItem != null) {
            if (currentItem.data.getBook().getBookId().equalsIgnoreCase(bookId.trim())) {
                quantity += currentItem.data.getQuantity();
            }
            currentItem = currentItem.next;
        }

        return quantity;
    }
}