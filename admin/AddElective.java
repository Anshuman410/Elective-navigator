import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import com.mongodb.client.*;
import org.bson.Document;

public class AddElective extends JFrame {

    JTextField subject, semester, teacher, subjectIdField;
    JComboBox<String> categoryCombo, difficultyCombo, skillsCombo, scopeCombo;
    JTextArea descriptionArea;
    String syllabusBase64 = null;

    public AddElective() {

        setTitle("Add Elective");
        setSize(400, 700);
        setLayout(null);

        int y = 30;

        JLabel lId = new JLabel("Subject ID");
        lId.setBounds(40, y, 120, 30);
        add(lId);

        subjectIdField = new JTextField();
        subjectIdField.setBounds(160, y, 180, 30);
        add(subjectIdField);

        y += 50;
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
        JLabel lSyllabus = new JLabel("Syllabus (PDF)");
        lSyllabus.setBounds(40, y, 120, 30);
        add(lSyllabus);

        JButton uploadBtn = new JButton("Upload PDF");
        uploadBtn.setBounds(160, y, 180, 30);
        add(uploadBtn);

        uploadBtn.addActionListener(e -> uploadPDF());

        y += 60;
        JButton save = new JButton("Save");
        save.setBounds(130, y, 120, 35);
        add(save);

        save.addActionListener(e -> saveElective());

        setVisible(true);
    }

    void uploadPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                syllabusBase64 = Base64.getEncoder().encodeToString(fileContent);
                JOptionPane.showMessageDialog(this, "PDF uploaded successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading PDF file.");
                ex.printStackTrace();
            }
        }
    }

    void saveElective() {
        if (subjectIdField.getText().isEmpty() || subject.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject ID and Name are required.");
            return;
        }

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> col = db.getCollection("electives");

        // Check if ID already exists
        if (col.find(new Document("subjectId", subjectIdField.getText())).first() != null) {
            JOptionPane.showMessageDialog(this, "Subject ID already exists. Use a different ID.");
            return;
        }

        Document doc = new Document("subjectId", subjectIdField.getText())
                .append("subjectName", subject.getText())
                .append("semester", semester.getText())
                .append("teacher", teacher.getText())
                .append("category", categoryCombo.getSelectedItem().toString())
                .append("difficulty", difficultyCombo.getSelectedItem().toString())
                .append("skills", skillsCombo.getSelectedItem().toString())
                .append("scope", scopeCombo.getSelectedItem().toString())
                .append("description", descriptionArea.getText())
                .append("syllabus", syllabusBase64);

        col.insertOne(doc);

        JOptionPane.showMessageDialog(this, "Elective Added");
        dispose();
    }
}