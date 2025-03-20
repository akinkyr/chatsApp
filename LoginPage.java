import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class LoginPage implements ActionListener {

    //Stuff we need to instantiate before program launches
    JFrame frame =new JFrame();
    JButton loginbtn = new JButton("Login");
    JButton resetbtn = new JButton("Reset");
    JTextField userIDField = new JTextField();
    JPasswordField userPasswordField = new JPasswordField();
    JLabel userIDLabel = new JLabel("User ID");
    JLabel userPasswordLabel = new JLabel("Password");
    JLabel messageLabel = new JLabel();
    HashMap <String,String> logininfo = new HashMap<String,String>();


    LoginPage(HashMap<String,String> loginInfoOriginal){
        logininfo = loginInfoOriginal;

        //x,y,width,height
        userIDLabel.setBounds(50,100,75,25);
        userPasswordLabel.setBounds(50,150,75,25);

        messageLabel.setBounds(125,250,250,35);
        messageLabel.setFont(new Font(null,Font.ITALIC,25));


        userIDField.setBounds(125,100,200,25);
        userPasswordField.setBounds(125,150,200,25);

        loginbtn.setBounds(125,200,100,25);
        loginbtn.setFocusable(false); //removes the weird box tingy when clciked on the button
        loginbtn.addActionListener(this);

        resetbtn.setBounds(225,200,100,25);
        resetbtn.setFocusable(false); //removes the weird box tingy when clciked on the button
        resetbtn.addActionListener(this);

        frame.add(userIDLabel);
        frame.add(userPasswordLabel);
        frame.add(messageLabel);
        frame.add(userIDField);
        frame.add(userPasswordField);
        frame.add(loginbtn);
        frame.add(resetbtn);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420,420);
        frame.setLayout(null);
        frame.setVisible(true);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        //if reset button pressed, clears the boxces this will be changed to reset my password later on
        if(e.getSource()==resetbtn){
            userIDField.setText("");
            userPasswordField.setText("");
        }
        if(e.getSource()==loginbtn){

            String userID = userIDField.getText();
            //Unlike jlabels we have to use valueof cuz it is not a jlabel! (yarim saat bunla ugrastim)
            String password = String.valueOf(userPasswordField.getPassword());

            if(logininfo.containsKey(userID)){
                if(logininfo.get(userID).equals(password)){
                    messageLabel.setText("Login successful");
                    messageLabel.setForeground(Color.GREEN);
                    frame.dispose();
                    WelcomePage welcomePage = new WelcomePage(userID);

                }

                else {
                    messageLabel.setText("Wrong password");
                    messageLabel.setForeground(Color.RED);
                }
            }
            else {
                messageLabel.setText("Username not found!");
                messageLabel.setForeground(Color.RED);
            }
        }

    }
}
