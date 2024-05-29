package multicast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

public class MulticastChatting extends JFrame implements ActionListener {
    //components in chatting
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton logOffButton;
    private JList<String> onlineMembersList;
    private DefaultListModel<String> listModel;
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private Set<String> onlineMembers;
    private String username;

    public MulticastChatting(String groupAddress, int port, String username) throws IOException {
        this.port = port;
        this.username = username;
        this.onlineMembers = new HashSet<>();
        this.socket = new MulticastSocket(port);
        this.group = InetAddress.getByName(groupAddress);
        this.socket.joinGroup(group);

        setTitle("Multicast Chatting - " + username);

        // Set up the GUI
        messageArea = new JTextArea(20, 40);
        messageArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);

        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Serif", Font.ITALIC, 15));
        logOffButton = new JButton("Log Off");
        logOffButton.setFont(new Font("Serif", Font.ITALIC, 15));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        listModel = new DefaultListModel<>();
        onlineMembersList = new JList<>(listModel);
        JScrollPane membersScrollPane = new JScrollPane(onlineMembersList);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messageScrollPane, membersScrollPane);
        splitPane.setResizeWeight(0.7);

        JPanel jpChatting = new JPanel(new BorderLayout());
        jpChatting.add(splitPane, BorderLayout.CENTER);
        jpChatting.add(panel, BorderLayout.SOUTH);
        jpChatting.add(logOffButton, BorderLayout.NORTH);

        add(jpChatting);

        sendButton.addActionListener(this);
        logOffButton.addActionListener(this);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Send join message
        sendMessage("JOIN:" + username);

        // Receive messages
        new Thread(new ReceiveMessages()).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) { // When the send button is clicked
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                sendMessage("MSG:" + username + ": " + message);// Send the message
                messageField.setText("");// Clear the message field
            }
        } else if (e.getSource() == logOffButton) { // When the log off button is clicked
            sendMessage("LEAVE:" + username);// Send leave message
            try {
                socket.leaveGroup(group); // Leave the multicast group
                socket.close();// Close the socket
                dispose();// Close the GUI
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) {
        try {
            byte[] buffer = message.getBytes();// Convert message to bytes
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);// Send the packet
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReceiveMessages implements Runnable {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];// Buffer for incoming messages
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);// Packet to receive data
                    socket.receive(packet); // Receive the packet
                    String message = new String(packet.getData(), 0, packet.getLength()); // Convert packet to string
                    SwingUtilities.invokeLater(() -> handleReceivedMessage(message));// Handle the message in the GUI thread
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            //this part handles online members list, who is online, who loggs off
        private void handleReceivedMessage(String message) {
            if (message.startsWith("JOIN:")) {
                String newUser = message.substring(5);// Extract the new user's name
                if (onlineMembers.add(newUser)) {// Add to the online members set
                    listModel.addElement(newUser);// Add to the GUI list
                    messageArea.append(newUser + " joined the chat.\n");// Display message
                    // Send current online members to the new user
                    sendMessage("MEMBERS:" + String.join(",", onlineMembers));
                }
            } else if (message.startsWith("LEAVE:")) {
                String leavingUser = message.substring(6);// Extract the leaving user's name
                if (onlineMembers.remove(leavingUser)) {// Remove from the online members set
                    listModel.removeElement(leavingUser);// Remove from the GUI list
                    messageArea.append(leavingUser + " left the chat.\n"); // Display message
                }
            } else if (message.startsWith("MSG:")) {
                String chatMessage = message.substring(4);// Extract the chat message
                messageArea.append(chatMessage + "\n");// Display the chat message
            } else if (message.startsWith("MEMBERS:")) {// Extract the list of members
                String[] members = message.substring(8).split(",");// Extract the list of members
                for (String member : members) {
                    if (onlineMembers.add(member)) { // Add to the online members set
                        listModel.addElement(member);// Add to the GUI list
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            new MulticastChatting("239.254.254.253", 4446, JOptionPane.showInputDialog("Enter your username:"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


