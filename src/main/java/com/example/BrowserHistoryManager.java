package com.example;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.*;

public class BrowserHistoryManager extends JFrame {
    JTextField urlInput;
    JTextArea historyDisplay, backStackDisplay, forwardStackDisplay, linkedListDisplay;
    JButton visitBtn, backBtn, forwardBtn, showHistoryBtn, clearHistoryBtn, deleteUrlBtn;

    Node head = null, tail = null, current = null;
    Stack<String> backStack = new Stack<>();
    Stack<String> forwardStack = new Stack<>();

    class Node {
        String url;
        Node prev, next;

        Node(String url) {
            this.url = url;
        }
    }

    public BrowserHistoryManager() {
        setTitle("Browser History Manager");
        setSize(900, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Top Panel (URL input + Visit)
        JPanel topPanel = new JPanel(new BorderLayout());
        urlInput = new JTextField();
        visitBtn = new JButton("Visit");
        topPanel.add(urlInput, BorderLayout.CENTER);
        topPanel.add(visitBtn, BorderLayout.EAST);

        // Control Buttons Panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        backBtn = new JButton("Back");
        forwardBtn = new JButton("Forward");
        showHistoryBtn = new JButton("Show History");
        clearHistoryBtn = new JButton("Clear History");
        deleteUrlBtn = new JButton("Delete URL");
        controlPanel.add(backBtn);
        controlPanel.add(forwardBtn);
        controlPanel.add(showHistoryBtn);
        controlPanel.add(clearHistoryBtn);
        controlPanel.add(deleteUrlBtn);

        // History Display
        historyDisplay = new JTextArea(15, 40);
        historyDisplay.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(historyDisplay);

        // Stack Displays
        backStackDisplay = new JTextArea(15, 20);
        forwardStackDisplay = new JTextArea(15, 20);
        backStackDisplay.setEditable(false);
        forwardStackDisplay.setEditable(false);

        JPanel stackPanel = new JPanel(new GridLayout(1, 2));
        stackPanel.add(new JScrollPane(backStackDisplay));
        stackPanel.add(new JScrollPane(forwardStackDisplay));

        // Stack Labels
        JPanel labelPanel = new JPanel(new GridLayout(1, 2));
        labelPanel.add(new JLabel("Back Stack", SwingConstants.CENTER));
        labelPanel.add(new JLabel("Forward Stack", SwingConstants.CENTER));

        // Linked List Display
        linkedListDisplay = new JTextArea(3, 80);
        linkedListDisplay.setEditable(false);

        // Bottom Panel (Labels + Linked List)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(labelPanel, BorderLayout.NORTH);
        bottomPanel.add(linkedListDisplay, BorderLayout.SOUTH);

        // Add all to main layout
        add(topPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(historyScroll, BorderLayout.WEST);
        add(stackPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        visitBtn.addActionListener(e -> visitPage());
        backBtn.addActionListener(e -> goBack());
        forwardBtn.addActionListener(e -> goForward());
        showHistoryBtn.addActionListener(e -> showHistory());
        clearHistoryBtn.addActionListener(e -> clearHistory());
        deleteUrlBtn.addActionListener(e -> deleteUrl());


        updateUI();
    }

    void visitPage() {
        String url = urlInput.getText().trim();
        if (url.isEmpty()) return;

        if (current != null) {
            backStack.push(current.url);
        }

        forwardStack.clear();
        addPage(url);
        urlInput.setText("");
        updateUI();
        openWebpage(url);
    }

    void addPage(String url) {
        Node newNode = new Node(url);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        current = newNode;
    }

    void goBack() {
        if (current != null && current.prev != null) {
            forwardStack.push(current.url);
            current = current.prev;
        } else if (!backStack.isEmpty()) {
            forwardStack.push(current.url);
            String backUrl = backStack.pop();
            current = findNode(backUrl);
        } else {
            JOptionPane.showMessageDialog(this, "No more back history");
            return;
        }
        updateUI();
        openWebpage(current.url);
    }

    void goForward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(current.url);
            String forwardUrl = forwardStack.pop();
            current = findNode(forwardUrl);
            updateUI();
            openWebpage(current.url);
        } else {
            JOptionPane.showMessageDialog(this, "No more forward history");
        }
    }

    void showHistory() {
        java.util.List<String> urls = getAllHistory();
        historyDisplay.setText("History:\n");
        for (String url : urls) {
            historyDisplay.append(url + (current != null && url.equals(current.url) ? " (current)\n" : "\n"));
        }
    }

    java.util.List<String> getAllHistory() {
        java.util.List<String> list = new ArrayList<>();
        Node temp = head;
        while (temp != null) {
            list.add(temp.url);
            temp = temp.next;
        }
        return list;
    }

    Node findNode(String url) {
        Node temp = head;
        while (temp != null) {
            if (temp.url.equals(url)) return temp;
            temp = temp.next;
        }
        return null;
    }

    void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all history?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            head = null;
            tail = null;
            current = null;
            backStack.clear();
            forwardStack.clear();
            historyDisplay.setText("");
            JOptionPane.showMessageDialog(this, "History cleared.");
            updateUI();
        }
    }

    void deleteUrl() {
        String url = urlInput.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a URL to delete.");
            return;
        }

        Node nodeToDelete = findNode(url);
        if (nodeToDelete != null) {
            if (nodeToDelete.prev != null) {
                nodeToDelete.prev.next = nodeToDelete.next;
            } else {
                head = nodeToDelete.next;
            }
            if (nodeToDelete.next != null) {
                nodeToDelete.next.prev = nodeToDelete.prev;
            } else {
                tail = nodeToDelete.prev;
            }

            if (current == nodeToDelete) {
                current = nodeToDelete.prev != null ? nodeToDelete.prev : nodeToDelete.next;
            }

            backStack.remove(url);
            forwardStack.remove(url);
            JOptionPane.showMessageDialog(this, "Deleted: " + url);
        } else {
            JOptionPane.showMessageDialog(this, "URL not found in history: " + url);
        }
        updateUI();
    }

    void openWebpage(String urlString) {
        try {
            URI uri = new URI(urlString);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to open URL: " + urlString);
        }
    }

    void updateUI() {
        // Back stack
        backStackDisplay.setText("Back Stack:\n");
        for (int i = backStack.size() - 1; i >= 0; i--) {
            backStackDisplay.append(backStack.get(i) + "\n");
        }

        // Forward stack
        forwardStackDisplay.setText("Forward Stack:\n");
        for (int i = forwardStack.size() - 1; i >= 0; i--) {
            forwardStackDisplay.append(forwardStack.get(i) + "\n");
        }

        // Linked list
        StringBuilder listBuilder = new StringBuilder("Linked List:\n");
        Node temp = head;
        while (temp != null) {
            listBuilder.append(temp == current ? "[" + temp.url + "]" : temp.url);
            if (temp.next != null) listBuilder.append(" <-> ");
            temp = temp.next;
        }
        linkedListDisplay.setText(listBuilder.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BrowserHistoryManager().setVisible(true);
        });
    }
}
