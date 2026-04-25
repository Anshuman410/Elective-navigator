import javax.swing.*;
import java.awt.event.*;
import com.mongodb.client.*;
import org.bson.Document;

public class ManageAnnouncements extends JFrame {

    JTextField titleField;
    JTextArea contentArea;

    public ManageAnnouncements() {

        setTitle("Manage Announcements");
        setSize(450, 450);
        setLayout(null);

        int y = 30;

        JLabel l1 = new JLabel("Announcement Title");
        l1.setBounds(40, y, 150, 30);
        add(l1);

        titleField = new JTextField();
        titleField.setBounds(180, y, 200, 30);
        add(titleField);

        y += 50;
        JLabel l2 = new JLabel("Notice Content");
        l2.setBounds(40, y, 150, 30);
        add(l2);

        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBounds(180, y, 200, 150);
        add(scrollPane);

        y += 180;
        JButton save = new JButton("Post Announcement");
        save.setBounds(140, y, 180, 40);
        add(save);

        save.addActionListener(e -> postAnnouncement());

        setVisible(true);
    }

    void postAnnouncement() {
        if (titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Content are required.");
            return;
        }

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> col = db.getCollection("announcements");

        Document doc = new Document("title", titleField.getText())
                .append("content", contentArea.getText())
                .append("createdAt", System.currentTimeMillis());

        col.insertOne(doc);

        JOptionPane.showMessageDialog(this, "Announcement Posted Successfully!");
        
        // Clear fields 
        titleField.setText("");
        contentArea.setText("");
        dispose();
    }
}
