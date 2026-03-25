import javax.swing.*;
import com.mongodb.client.*;
import static com.mongodb.client.model.Filters.eq;

public class DeleteElective extends JFrame {

    JTextField subject;

    public DeleteElective() {

        setTitle("Delete Elective");
        setSize(300,200);
        setLayout(null);

        JLabel l = new JLabel("Subject Name");
        l.setBounds(30,40,100,30);
        add(l);

        subject = new JTextField();
        subject.setBounds(140,40,120,30);
        add(subject);

        JButton delete = new JButton("Delete");
        delete.setBounds(90,100,120,30);
        add(delete);

        delete.addActionListener(e -> remove());

        setVisible(true);
    }

    void remove() {

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection col = db.getCollection("electives");

        col.deleteOne(eq("subjectName",subject.getText()));

        JOptionPane.showMessageDialog(this,"Deleted");
    }
}