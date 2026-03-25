import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminLogin extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton;

    public AdminLogin() {

        setTitle("Elective Navigator - Admin Login");
        setSize(400,260);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    
        getContentPane().setBackground(Color.lightGray);


        JLabel title = new JLabel("ADMIN LOGIN");
        title.setBounds(130,20,200,30);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

    
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(60,80,100,25);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(userLabel);

        
        usernameField = new JTextField();
        usernameField.setBounds(160,80,150,25);
        add(usernameField);

    
        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(60,120,100,25);
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(passLabel);


        passwordField = new JPasswordField();
        passwordField.setBounds(160,120,150,25);
        add(passwordField);

    
        loginButton = new JButton("Login");
        loginButton.setBounds(140,170,120,35);
        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(loginButton);

    
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String user = usernameField.getText();
                String pass = String.valueOf(passwordField.getPassword());

                if(user.equals("saka") && pass.equals("saka123")) {

                    JOptionPane.showMessageDialog(null,"Login Successful");

                    new AdminDashboard(); 
                    dispose();

                } else {

                    JOptionPane.showMessageDialog(null,"Invalid Username or Password");

                }

            }
        });


        setLocationRelativeTo(null);

        setVisible(true);
    }

    public static void main(String[] args) {

        new AdminLogin();

    }
}