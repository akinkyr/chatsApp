import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage implements ActionListener {
    JFrame frame = new JFrame();
    JButton loginbtn = new JButton("Login");
    JButton registerbtn = new JButton("Register");
    JTextField userIDField = new JTextField();
    JPasswordField userPasswordField = new JPasswordField();
    JLabel userIDLabel = new JLabel("User ID");
    JLabel userPasswordLabel = new JLabel("Password");
    JLabel messageLabel = new JLabel();


    private IDandPasswords idPass;

    public LoginPage(IDandPasswords idPass){
        this.idPass = idPass;

        // Set component bounds
        userIDLabel.setBounds(50, 100, 75, 25);
        userPasswordLabel.setBounds(50, 150, 75, 25);
        messageLabel.setBounds(125, 250, 250, 35);
        messageLabel.setFont(new Font(null, Font.ITALIC, 25));
        userIDField.setBounds(125, 100, 200, 25);
        userPasswordField.setBounds(125, 150, 200, 25);

        loginbtn.setBounds(125, 200, 100, 25);
        loginbtn.setFocusable(false);
        loginbtn.addActionListener(this);

        registerbtn.setBounds(225, 200, 100, 25);
        registerbtn.setFocusable(false);
        registerbtn.addActionListener(this);


        frame.add(userIDLabel);
        frame.add(userPasswordLabel);
        frame.add(messageLabel);
        frame.add(userIDField);
        frame.add(userPasswordField);
        frame.add(loginbtn);
        frame.add(registerbtn);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 420);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == registerbtn){
            String userName = userIDField.getText();
            String password = String.valueOf(userPasswordField.getPassword());

            PasswordHasher.writeCredentials(userName, password);
            messageLabel.setText("Saved succesfully");

        }
        if(e.getSource() == loginbtn){
            String userID = userIDField.getText();
            String password = String.valueOf(userPasswordField.getPassword());


            if(idPass.authenticate(userID, password)){
                messageLabel.setText("Login successful");
                messageLabel.setForeground(Color.GREEN);
                frame.dispose();
                new WelcomePage(userID);
            } else {
                messageLabel.setText("Login failed");
                messageLabel.setForeground(Color.RED);
            }
        }
    }
}
