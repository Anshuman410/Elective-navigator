import javax.swing.*;
import com.mongodb.client.*;
import org.bson.Document;
import javax.swing.table.DefaultTableModel;

public class ViewElectives extends JFrame {

    JTable table;

    public ViewElectives() {

        setTitle("View Electives");
        setSize(500,400);

        String[] columns = {"Subject","Semester","Teacher"};
        DefaultTableModel model = new DefaultTableModel(columns,0);

        table = new JTable(model);
        add(new JScrollPane(table));

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> col = db.getCollection("electives");

        for(Document d : col.find()) {

            model.addRow(new Object[]{
                    d.getString("subjectName"),
                    d.getString("semester"),
                    d.getString("teacher")
            });

        }

        setVisible(true);
    }
}