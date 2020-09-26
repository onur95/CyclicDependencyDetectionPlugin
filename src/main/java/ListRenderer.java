import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;

public class ListRenderer extends JBLabel implements ListCellRenderer<RangeHighlighter> {

    public ListRenderer() {
        setOpaque(true);
    }

    @Override
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
    }
}
