import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class ListRenderer extends JBLabel implements ListCellRenderer<Dependency> {

    public ListRenderer() {
        setOpaque(true);
    }


    @Override
    public Component getListCellRendererComponent(JList<? extends Dependency> list, Dependency value, int index, boolean isSelected, boolean cellHasFocus) {
        Vector<NodeDependency> nodeDependencies = value.getDependency();
        for (NodeDependency nodeDependency : nodeDependencies) {
            setText(value.getFromeNode() + " dependent on " + nodeDependency.getDependency().getDependentOnClass());
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
