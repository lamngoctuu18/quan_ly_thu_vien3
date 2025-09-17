package app;

import server.LibraryServer;
import client.ClientUI;
import client.AdminUI;

import javax.swing.*;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        System.out.println("=== Library Management System ===");
        System.out.println("1. Start Server");
        System.out.println("2. Start Client (User)");
        System.out.println("3. Start Admin");
        System.out.print("Choose option: ");

        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                // chạy server trong thread riêng
                new Thread(() -> {
                    try {
                        LibraryServer.main(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case 2:
                SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
                break;
            case 3:
                SwingUtilities.invokeLater(() -> new AdminUI().setVisible(true));
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ!");
        }
    }
}
