import javax.swing.*;
import com.mongodb.client.*;
import org.bson.Document;

public class AddElective extends JFrame {

    JTextField subject, semester, teacher;
    JComboBox<String> categoryCombo, difficultyCombo, skillsCombo, scopeCombo;
    JTextArea descriptionArea;

    public AddElective() {

        setTitle("Add Elective");
        setSize(400, 600);
        setLayout(null);

        int y = 30;

        JLabel l1 = new JLabel("Subject Name");
        l1.setBounds(40, y, 120, 30);
        add(l1);

        subject = new JTextField();
        subject.setBounds(160, y, 180, 30);
        add(subject);

        y += 50;
        JLabel l2 = new JLabel("Semester");
        l2.setBounds(40, y, 120, 30);
        add(l2);

        semester = new JTextField();
        semester.setBounds(160, y, 180, 30);
        add(semester);

        y += 50;
        JLabel l3 = new JLabel("Teacher");
        l3.setBounds(40, y, 120, 30);
        add(l3);

        teacher = new JTextField();
        teacher.setBounds(160, y, 180, 30);
        add(teacher);

        y += 50;
        JLabel l4 = new JLabel("Category");
        l4.setBounds(40, y, 120, 30);
        add(l4);

        String[] categories = {"Programming", "Development", "Management", "Research"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setBounds(160, y, 180, 30);
        add(categoryCombo);

        y += 50;
        JLabel l5 = new JLabel("Difficulty");
        l5.setBounds(40, y, 120, 30);
        add(l5);

        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setBounds(160, y, 180, 30);
        add(difficultyCombo);

        y += 50;
        JLabel l6 = new JLabel("Required Skills");
        l6.setBounds(40, y, 120, 30);
        add(l6);

        String[] skills = {"Coding", "Math", "Communication", "Analysis"};
        skillsCombo = new JComboBox<>(skills);
        skillsCombo.setBounds(160, y, 180, 30);
        add(skillsCombo);

        y += 50;
        JLabel l7 = new JLabel("Career Scope");
        l7.setBounds(40, y, 120, 30);
        add(l7);

        String[] scopes = {"High Demand", "Moderate", "Research Field"};
        scopeCombo = new JComboBox<>(scopes);
        scopeCombo.setBounds(160, y, 180, 30);
        add(scopeCombo);

        y += 50;
        JLabel l8 = new JLabel("Description");
        l8.setBounds(40, y, 120, 30);
        add(l8);

        descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBounds(160, y, 180, 60);
        add(scrollPane);

        y += 80;
        JButton save = new JButton("Save");
        save.setBounds(130, y, 120, 35);
        add(save);

        save.addActionListener(e -> saveElective());

        setVisible(true);
    }

    void saveElective() {

        MongoDatabase db = MongoDBConnection.getDatabase();

        MongoCollection<Document> col = db.getCollection("electives");

        Document doc = new Document("subjectName", subject.getText())
                .append("semester", semester.getText())
                .append("teacher", teacher.getText())
                .append("category", categoryCombo.getSelectedItem().toString())
                .append("difficulty", difficultyCombo.getSelectedItem().toString())
                .append("skills", skillsCombo.getSelectedItem().toString())
                .append("scope", scopeCombo.getSelectedItem().toString())
                .append("description", descriptionArea.getText());

        col.insertOne(doc);

        JOptionPane.showMessageDialog(this, "Elective Added");
    }
}