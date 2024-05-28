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
        if (e.getSource() == sendButton) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                sendMessage("MSG:" + username + ": " + message);
                messageField.setText("");
            }
        } else if (e.getSource() == logOffButton) {
            sendMessage("LEAVE:" + username);
            try {
                socket.leaveGroup(group);
                socket.close();
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReceiveMessages implements Runnable {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    SwingUtilities.invokeLater(() -> handleReceivedMessage(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleReceivedMessage(String message) {
            if (message.startsWith("JOIN:")) {
                String newUser = message.substring(5);
                if (onlineMembers.add(newUser)) {
                    listModel.addElement(newUser);
                    messageArea.append(newUser + " joined the chat.\n");
                    // Send current online members to the new user
                    sendMessage("MEMBERS:" + String.join(",", onlineMembers));
                }
            } else if (message.startsWith("LEAVE:")) {
                String leavingUser = message.substring(6);
                if (onlineMembers.remove(leavingUser)) {
                    listModel.removeElement(leavingUser);
                    messageArea.append(leavingUser + " left the chat.\n");
                }
            } else if (message.startsWith("MSG:")) {
                String chatMessage = message.substring(4);
                messageArea.append(chatMessage + "\n");
            } else if (message.startsWith("MEMBERS:")) {
                String[] members = message.substring(8).split(",");
                for (String member : members) {
                    if (onlineMembers.add(member)) {
                        listModel.addElement(member);
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


