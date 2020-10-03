import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class ListRenderer extends JBLabel implements ListCellRenderer<Dependency> {

    public ListRenderer() {
        setOpaque(true);
    }

    /*@Override
    public Component getListCellRendererComponent(JList<? extends RangeHighlighter> list, RangeHighlighter value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getErrorStripeTooltip().toString());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }*/

    /*@Override
    public Component getListCellRendererComponent(JList<? extends TreeMap<String, Vector<NodeDependency>>> list, TreeMap<String, Vector<NodeDependency>> value, int index, boolean isSelected, boolean cellHasFocus) {
        ArrayList<String> arrayList = new ArrayList<>();
        for(String vertex : value.keySet()){
            for(NodeDependency nodeDependency : value.get(vertex)){
                arrayList.add("Class " + vertex + " dependent on " + nodeDependency.getDependency().getDependentOnClass() + " fully name: " + nodeDependency.getDependency().getFullyQualifiedName() + " type: " + nodeDependency.getDependency().getType());
            }
        }
        setText(arrayList.toString());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }*/

    /*@Override
    public Component getListCellRendererComponent(JList<? extends NodeDependency> list, NodeDependency value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getDependency().getDependentOnClass());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }*/

    @Override
    public Component getListCellRendererComponent(JList<? extends Dependency> list, Dependency value, int index, boolean isSelected, boolean cellHasFocus) {
        Vector<NodeDependency> nodeDependencies = value.getDependency();
        for (NodeDependency nodeDependency : nodeDependencies) {
            setText(value.getFromeNode() + " dependent on " + nodeDependency.getDependency().getDependentOnClass());
        }
        //setText(value.getFromeNode()+" dependent on "+value.getDependency().iterator().next().getDependency().getDependentOnClass());

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
