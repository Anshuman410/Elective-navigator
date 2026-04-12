import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import com.mongodb.client.*;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

public class EditElective extends JFrame {

    JTextField subject, semester, teacher, subjectIdField;
    JComboBox<String> categoryCombo, difficultyCombo, skillsCombo, scopeCombo;
    JTextArea descriptionArea;
    String syllabusBase64 = null;
    MongoCollection<Document> col;

    public EditElective() {

        setTitle("Edit Elective");
        setSize(400, 750);
        setLayout(null);

        int y = 30;

        JLabel lId = new JLabel("Subject ID to Edit");
        lId.setBounds(40, y, 120, 30);
        add(lId);

        subjectIdField = new JTextField();
        subjectIdField.setBounds(160, y, 100, 30);
        add(subjectIdField);

        JButton searchBtn = new JButton("Search");
        searchBtn.setBounds(270, y, 80, 30);
        add(searchBtn);

        y += 50;
        JSeparator sep = new JSeparator();
        sep.setBounds(20, y, 350, 2);
        add(sep);

        y += 20;
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

        JButton uploadBtn = new JButton("Update PDF");
        uploadBtn.setBounds(160, y, 180, 30);
        add(uploadBtn);

        uploadBtn.addActionListener(e -> uploadPDF());

        y += 60;
        JButton update = new JButton("Update");
        update.setBounds(130, y, 120, 35);
        add(update);

        MongoDatabase db = MongoDBConnection.getDatabase();
        col = db.getCollection("electives");

        searchBtn.addActionListener(e -> searchElective());
        update.addActionListener(e -> updateElective());

        setVisible(true);
    }

    void searchElective() {
        String id = subjectIdField.getText();
        Document doc = col.find(eq("subjectId", id)).first();
        if (doc != null) {
            subject.setText(doc.getString("subjectName"));
            semester.setText(doc.getString("semester"));
            teacher.setText(doc.getString("teacher"));
            categoryCombo.setSelectedItem(doc.getString("category"));
            difficultyCombo.setSelectedItem(doc.getString("difficulty"));
            skillsCombo.setSelectedItem(doc.getString("skills"));
            scopeCombo.setSelectedItem(doc.getString("scope"));
            descriptionArea.setText(doc.getString("description"));
            syllabusBase64 = doc.getString("syllabus");
            JOptionPane.showMessageDialog(this, "Elective found and loaded.");
        } else {
            JOptionPane.showMessageDialog(this, "No elective found with Subject ID: " + id);
        }
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

    void updateElective() {
        String id = subjectIdField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for a Subject ID first.");
            return;
        }

        Document updateDoc = new Document("subjectName", subject.getText())
                .append("semester", semester.getText())
                .append("teacher", teacher.getText())
                .append("category", categoryCombo.getSelectedItem().toString())
                .append("difficulty", difficultyCombo.getSelectedItem().toString())
                .append("skills", skillsCombo.getSelectedItem().toString())
                .append("scope", scopeCombo.getSelectedItem().toString())
                .append("description", descriptionArea.getText())
                .append("syllabus", syllabusBase64);

        long result = col.updateOne(eq("subjectId", id), new Document("$set", updateDoc)).getMatchedCount();

        if (result > 0) {
            JOptionPane.showMessageDialog(this, "Updated successfully.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed. Subject ID might not exist.");
        }
    }
}