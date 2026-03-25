import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ViewQueries extends JFrame {

    JTable table;
    DefaultTableModel model;
    MongoCollection<Document> col;

    public ViewQueries() {

        setTitle("Manage Queries");
        setSize(700, 500);
        setLayout(null);

        String[] columns = {"ID", "Student Name", "Semester", "Question"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        
        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 20, 640, 250);
        add(sp);

        JLabel l1 = new JLabel("Answer:");
        l1.setBounds(20, 290, 100, 30);
        add(l1);

        JTextArea answerArea = new JTextArea();
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        JScrollPane answerSp = new JScrollPane(answerArea);
        answerSp.setBounds(80, 290, 400, 100);
        add(answerSp);

        JButton submitBtn = new JButton("Submit Answer");
        submitBtn.setBounds(500, 320, 150, 40);
        add(submitBtn);

        MongoDatabase db = MongoDBConnection.getDatabase();
        col = db.getCollection("queries");

        loadQueries();

        submitBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a query to answer.");
                return;
            }

            String idStr = (String) model.getValueAt(row, 0);
            String answer = answerArea.getText().trim();

            if (answer.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Answer cannot be empty.");
                return;
            }

            try {
                ObjectId queryId = new ObjectId(idStr);
                col.updateOne(eq("_id", queryId),
                        combine(set("answer", answer), set("resolved", true)));
                
                JOptionPane.showMessageDialog(this, "Answer submitted successfully.");
                answerArea.setText("");
                loadQueries(); // Refresh table
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting answer.");
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    private void loadQueries() {
        model.setRowCount(0); // Clear existing rows
        for (Document d : col.find(eq("resolved", false))) {
            model.addRow(new Object[]{
                    d.getObjectId("_id").toHexString(),
                    d.getString("studentName"),
                    d.getString("semester"),
                    d.getString("question")
            });
        }
    }
}
