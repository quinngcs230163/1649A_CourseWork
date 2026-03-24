import model.Book;
import model.Order;
import model.OrderItem;
import service.BookCatalog;
import service.BookstoreSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        BookCatalog catalog = new BookCatalog();
        catalog.loadSampleBooks();

        BookstoreSystem system = new BookstoreSystem(catalog);

        while (true) {
            showMenu();
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    Order order = createOrderFromCatalog(sc, system.getCatalog(), system);
                    if (order != null && order.hasItems()) {
                        system.addOrder(order);
                    } else {
                        System.out.println("Order was not added because it has no items.");
                    }
                    break;

                case "2":
                    system.getCatalog().displayCatalog();
                    break;

                case "3":
                    system.processNextOrder();
                    break;

                case "4":
                    system.viewNextOrder();
                    break;

                case "5":
                    system.displayPendingOrders();
                    break;

                case "6":
                    String searchId = readNonEmptyString(sc, "Enter Order ID to search: ");
                    Order foundOrder = system.searchOrderById(searchId);

                    if (foundOrder != null) {
                        System.out.println("Order found:");
                        System.out.println(foundOrder);
                        System.out.println("Order items:");
                        foundOrder.displayItems();
                    } else {
                        System.out.println("Order not found.");
                    }
                    break;

                case "7":
                    String sortId = readNonEmptyString(sc, "Enter pending Order ID to sort items by title: ");
                    system.sortOrderItemsByTitle(sortId);
                    break;

                case "8":
                    editExistingOrder(sc, system, system.getCatalog());
                    break;

                case "9":
                    system.showActionHistory();
                    break;

                case "10":
                    system.displayProcessedOrders();
                    break;

                case "11":
                    System.out.println("Exiting program...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid option. Please try again.");
            }

            System.out.println();
        }
    }

    public static void showMenu() {
        System.out.println("===== ONLINE BOOKSTORE SYSTEM =====");
        System.out.println("1. Add new order from catalog");
        System.out.println("2. Display book catalog");
        System.out.println("3. Process next order");
        System.out.println("4. View next order");
        System.out.println("5. Display pending orders");
        System.out.println("6. Search order by ID");
        System.out.println("7. Sort items in a pending order by title");
        System.out.println("8. Edit existing pending order");
        System.out.println("9. Show action history");
        System.out.println("10. Display processed orders");
        System.out.println("11. Exit");
    }

    public static Order createOrderFromCatalog(Scanner sc, BookCatalog catalog, BookstoreSystem system) {
        if (catalog.isEmpty()) {
            System.out.println("Catalog is empty. Cannot create order.");
            return null;
        }

        String orderId;
        while (true) {
            orderId = readNonEmptyString(sc, "Enter Order ID: ");
            if (system.isDuplicateOrderId(orderId)) {
                System.out.println("Order ID already exists. Please enter another ID.");
                continue;
            }
            break;
        }

        String customerName = readNonEmptyString(sc, "Enter Customer Name: ");
        String shippingAddress = readNonEmptyString(sc, "Enter Shipping Address: ");

        Order order = new Order(orderId, customerName, shippingAddress);

        int numberOfBooks = readPositiveInt(sc, "How many different book titles do you want to add to this order? ");

        for (int i = 1; i <= numberOfBooks; i++) {
            System.out.println("Choose book " + i + " from catalog:");
            catalog.displayCatalog();

            String bookId = readNonEmptyString(sc, "Enter Book ID: ");
            Book selectedBook = catalog.searchBookById(bookId);

            if (selectedBook == null) {
                System.out.println("Book ID not found. Skipping this item.");
                continue;
            }

            int quantity = readPositiveInt(sc, "Enter quantity: ");
            order.addOrUpdateItem(new OrderItem(
                    new Book(selectedBook.getBookId(), selectedBook.getTitle(), selectedBook.getAuthor(), selectedBook.getPrice()),
                    quantity
            ));
        }

        if (!order.hasItems()) {
            return order;
        }

        System.out.println("Availability check result:");
        System.out.println(catalog.getFirstUnavailableBookMessage(order));
        return order;
    }

    public static void editExistingOrder(Scanner sc, BookstoreSystem system, BookCatalog catalog) {
        String orderId = readNonEmptyString(sc, "Enter pending Order ID to edit: ");

        Order order = system.searchOrderById(orderId);
        if (order == null || !"Pending".equalsIgnoreCase(order.getStatus())) {
            System.out.println("Pending order not found.");
            return;
        }

        System.out.println("Current order information:");
        System.out.println(order);
        System.out.println("Current items:");
        order.displayItems();

        System.out.print("Enter new customer name (leave blank to keep current): ");
        String newCustomerName = sc.nextLine();
        if (!newCustomerName.trim().isEmpty()) {
            system.editOrderCustomerName(orderId, newCustomerName);
            System.out.println("Customer name updated.");
        }

        System.out.print("Enter new shipping address (leave blank to keep current): ");
        String newShippingAddress = sc.nextLine();
        if (!newShippingAddress.trim().isEmpty()) {
            system.editOrderShippingAddress(orderId, newShippingAddress);
            System.out.println("Shipping address updated.");
        }

        System.out.print("Do you want to add more books to this pending order? (yes/no): ");
        String answer = sc.nextLine().trim();

        if (answer.equalsIgnoreCase("yes")) {
            int count = readPositiveInt(sc, "How many book titles do you want to add? ");

            for (int i = 1; i <= count; i++) {
                catalog.displayCatalog();

                String bookId = readNonEmptyString(sc, "Enter Book ID: ");
                Book book = catalog.searchBookById(bookId);
                if (book == null) {
                    System.out.println("Book ID not found. Skipping.");
                    continue;
                }

                int quantity = readPositiveInt(sc, "Enter quantity to add: ");
                boolean updated = system.addBookToExistingOrder(orderId, book, quantity);
                if (updated) {
                    System.out.println("Book added successfully.");
                } else {
                    System.out.println("Book could not be added because the book does not exist or stock is insufficient.");
                }
            }
        }

        System.out.println("Order after editing:");
        Order updatedOrder = system.searchOrderById(orderId);
        if (updatedOrder != null) {
            System.out.println(updatedOrder);
            updatedOrder.displayItems();
        }
    }

    public static String readNonEmptyString(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field cannot be empty. Please try again.");
        }
    }

    public static int readPositiveInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Please enter an integer greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid integer.");
            }
        }
    }
}