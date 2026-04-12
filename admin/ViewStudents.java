import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;

public class ViewStudents extends JFrame {

    JTable table;
    DefaultTableModel model;
    MongoCollection<Document> col;

    public ViewStudents() {

        setTitle("View Registered Students");
        setSize(800, 500);
        setLayout(null);

        String[] columns = {"ID", "Student Name", "Student ID", "Course", "Semester", "University Roll No", "Section"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        
        // Hide internal Mongo ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 20, 740, 300);
        add(sp);

        JButton deleteBtn = new JButton("Delete Student");
        deleteBtn.setBounds(300, 350, 200, 40);
        add(deleteBtn);

        MongoDatabase db = MongoDBConnection.getDatabase();
        col = db.getCollection("students");

        loadStudents();

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a student to delete.");
                return;
            }

            String idStr = (String) model.getValueAt(row, 0);
            String studentName = (String) model.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete student: " + studentName + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ObjectId studentIdStr = new ObjectId(idStr);
                    col.deleteOne(eq("_id", studentIdStr));
                    
                    JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                    loadStudents(); // Refresh table
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting student.");
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    private void loadStudents() {
        model.setRowCount(0); // Clear existing rows
        try (MongoCursor<Document> cursor = col.find().iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                model.addRow(new Object[]{
                        d.getObjectId("_id").toHexString(),
                        d.getString("name"),
                        d.getString("studentId"),
                        d.getString("course"),
                        d.getString("semester"),
                        d.getString("universityRollNo"),
                        d.getString("section")
                });
            }
        }
    }
}
