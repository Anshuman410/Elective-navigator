import javax.swing.*;
import com.mongodb.client.*;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

public class EditElective extends JFrame {

    JTextField subject, teacher;

    public EditElective() {

        setTitle("Edit Elective");
        setSize(350,200);
        setLayout(null);

        JLabel l1 = new JLabel("Subject");
        l1.setBounds(30,30,100,30);
        add(l1);

        subject = new JTextField();
        subject.setBounds(130,30,150,30);
        add(subject);

        JLabel l2 = new JLabel("New Teacher");
        l2.setBounds(30,70,100,30);
        add(l2);

        teacher = new JTextField();
        teacher.setBounds(130,70,150,30);
        add(teacher);

        JButton update = new JButton("Update");
        update.setBounds(100,120,120,30);
        add(update);

        update.addActionListener(e -> updateElective());

        setVisible(true);
    }

    void updateElective() {

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> col = db.getCollection("electives");

        col.updateOne(eq("subjectName",subject.getText()),
                new Document("$set", new Document("teacher",teacher.getText())));

        JOptionPane.showMessageDialog(this,"Updated");
    }
}