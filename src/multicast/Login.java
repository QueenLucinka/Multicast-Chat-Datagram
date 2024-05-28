package multicast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class Login extends JFrame implements ActionListener{
    JPanel jpLogin = new JPanel();

    //defining panels for login
    JPanel centerPanel = new JPanel();
    JPanel southPanel = new JPanel();
    JPanel northPanel = new JPanel();

    //define login labels
    JLabel wlm = new JLabel("Welcome");
    JLabel wyn = new JLabel("What's your name?");
    JLabel nameBox = new JLabel();
    JTextField name = new JTextField(20);

    // Define the log in button
    JButton loginButton = new JButton("Log In");

    public Login(){
        this.add(jpLogin);

        jpLogin.setLayout(new BorderLayout());

        jpLogin.add(centerPanel, BorderLayout.CENTER);
        jpLogin.add(southPanel, BorderLayout.SOUTH);
        jpLogin.add(northPanel, BorderLayout.NORTH);

        //dimensions of labels' place in layout
        centerPanel.setMinimumSize(new Dimension(500, 150));
        southPanel.setMinimumSize(new Dimension(500,125));
        northPanel.setMinimumSize(new Dimension(500,125));

        //what labels each dimension contains
        northPanel.add(wlm);
        centerPanel.add(wyn);
        southPanel.add(nameBox);
        southPanel.add(name);
        southPanel.add(loginButton);

        //labels font and such
        wlm.setFont(new Font("Serif", Font.ITALIC, 20));
        wyn.setFont(new Font("Serif", Font.ITALIC, 15));

        // Add action listener to the login button
        loginButton.addActionListener(this);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //needs for users input
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String userName = name.getText().trim();
            if (!userName.isEmpty()) {
                try {
                    new MulticastChatting("239.254.254.253", 4446, userName); // Open the chat window with the entered username
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                this.dispose(); // Close the login window
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public static void main(String[] args){
        new multicast.Login();
    }
}
