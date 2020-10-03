import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;

public class DependenciesGUI {
    private final JPanel mainPanel = new JPanel();
    private final ArrayList<JBList<Dependency>> lists = new ArrayList<>();
    private JBList<Dependency> myList;

    public DependenciesGUI(Project project, TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> treeMap) {
        JBLabel labelTxt = new JBLabel("Dependencies: ");
        JBLabel totalCycles = new JBLabel("Total cycles " + treeMap.keySet().size());
        mainPanel.add(labelTxt);
        mainPanel.add(totalCycles);
        for (Integer integer : treeMap.keySet()) {
            TreeMap<String, Vector<NodeDependency>> myTreeMap = treeMap.get(integer);
            Vector<Dependency> cycleDependencies = new Vector<>();
            for (String vertex : myTreeMap.keySet()) {
                cycleDependencies.add(new Dependency(vertex, myTreeMap.get(vertex)));
            }
            myList = new JBList<>(cycleDependencies);
            myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            myList.setCellRenderer(new ListRenderer());
            myList.addMouseListener(new JListAdapter(project, myList));
            lists.add(myList);
        }
        int count = 1;
        for (JBList<Dependency> list : lists) {
            JPanel panel = new JPanel();
            JBScrollPane jbScrollPane = new JBScrollPane(list);
            JBLabel labelDep = new JBLabel("Cycle " + count);
            panel.add(labelDep);
            panel.add(jbScrollPane);
            panel.setOpaque(true);
            panel.setBackground(JBColor.white);
            count++;
            mainPanel.add(panel);
        }

    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
