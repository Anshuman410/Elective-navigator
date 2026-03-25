import javax.swing.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {

        setTitle("Admin Dashboard");
        setSize(400,420);
        setLayout(null);

        JButton add = new JButton("Add Elective");
        add.setBounds(100,30,200,40);
        add(add);

        JButton view = new JButton("View Electives");
        view.setBounds(100,90,200,40);
        add(view);

        JButton edit = new JButton("Edit Elective");
        edit.setBounds(100,150,200,40);
        add(edit);

        JButton delete = new JButton("Delete Elective");
        delete.setBounds(100,210,200,40);
        add(delete);

        JButton manageQueries = new JButton("Manage Queries");
        manageQueries.setBounds(100,270,200,40);
        add(manageQueries);

        add.addActionListener(e -> new AddElective());
        view.addActionListener(e -> new ViewElectives());
        edit.addActionListener(e -> new EditElective());
        delete.addActionListener(e -> new DeleteElective());
        manageQueries.addActionListener(e -> new ViewQueries());

        setVisible(true);
    }

}