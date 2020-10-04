import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.sun.istack.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
public class PluginSettingsComponent {
    private final JPanel myMainPanel;
    private final JColorChooser myHighlighterColor = new JColorChooser();
    private final JSlider myCyclesLength = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);


    public PluginSettingsComponent() {
        myCyclesLength.setValue(5);
        myCyclesLength.setMajorTickSpacing(5);
        myCyclesLength.setMinorTickSpacing(1);
        myCyclesLength.setPaintTicks(true);
        myCyclesLength.setPaintLabels(true);
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Please choose max. cycle length: "), myCyclesLength, 1, false)
                .addComponent(myHighlighterColor, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return myCyclesLength;
    }

    @NotNull
    public int getCyclesLength() {
        return myCyclesLength.getValue();
    }

    public void setCyclesLength(@NotNull int newLength) {
        myCyclesLength.setValue(newLength);
    }

    public int getHighlighterColor() {
        return myHighlighterColor.getColor().getRGB();
    }

    public void setHighlighterColor(@NotNull Color newColor) {
        myHighlighterColor.setColor(newColor.getRGB());
    }

}
