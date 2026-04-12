import javax.swing.*;
import com.mongodb.client.*;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

public class DeleteElective extends JFrame {

    JTextField subject;

    public DeleteElective() {

        setTitle("Delete Elective");
        setSize(300,200);
        setLayout(null);

        JLabel l1 = new JLabel("Subject ID");
        l1.setBounds(30,30,100,30);
        add(l1);

        subject = new JTextField();
        subject.setBounds(130,30,150,30);
        add(subject);

        JButton delete = new JButton("Delete");
        delete.setBounds(100,80,120,30);
        add(delete);

        delete.addActionListener(e -> deleteElective());

        setVisible(true);
    }

    void deleteElective() {

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> col = db.getCollection("electives");

        long count = col.deleteOne(eq("subjectId", subject.getText())).getDeletedCount();

        if (count > 0) {
            JOptionPane.showMessageDialog(this, "Deleted successfully.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "No elective found with Subject ID: " + subject.getText());
        }
    }
}