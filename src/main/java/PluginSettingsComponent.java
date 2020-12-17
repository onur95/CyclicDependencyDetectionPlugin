import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.sun.istack.NotNull;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
public class PluginSettingsComponent {
    private final JPanel myMainPanel;
    private final JColorChooser myHighlighterColor = new JColorChooser();
    private final JSlider totalCyclesLength = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);
    private final JSlider myCyclesLength = new JSlider(JSlider.HORIZONTAL, 2, 6, 2);


    public PluginSettingsComponent() {
        totalCyclesLength.setValue(5);
        totalCyclesLength.setMajorTickSpacing(5);
        totalCyclesLength.setMinorTickSpacing(1);
        totalCyclesLength.setPaintTicks(true);
        totalCyclesLength.setPaintLabels(true);
        myCyclesLength.setValue(5);
        myCyclesLength.setMajorTickSpacing(2);
        myCyclesLength.setMinorTickSpacing(1);
        myCyclesLength.setPaintTicks(true);
        myCyclesLength.setPaintLabels(true);
        AbstractColorChooserPanel[] colorChooserPanels = myHighlighterColor.getChooserPanels();
        for (AbstractColorChooserPanel colorChooserPanel : colorChooserPanels) {
            if (!colorChooserPanel.getDisplayName().equals("Swatches")) {
                myHighlighterColor.removeChooserPanel(colorChooserPanel);
            }
        }
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Please choose max. cycle length: "), myCyclesLength, 1, false)
                .addLabeledComponent(new JBLabel("Please choose total cycle count: "), totalCyclesLength, 1, false)
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

    @NotNull
    public int getTotalCyclesCount() {
        return totalCyclesLength.getValue();
    }

    public void setTotalCyclesLength(@NotNull int newTotalCount) {
        totalCyclesLength.setValue(newTotalCount);
    }

    public int getHighlighterColor() {
        return myHighlighterColor.getColor().getRGB();
    }

    public void setHighlighterColor(@NotNull Color newColor) {
        myHighlighterColor.setColor(newColor.getRGB());
    }

}
