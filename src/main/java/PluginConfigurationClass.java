import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Provides controller functionality for application settings.
 */
public class PluginConfigurationClass implements Configurable {

    private PluginSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "CDD Plugin Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new PluginSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        PluginSettingsState settings = PluginSettingsState.getInstance();
        boolean modified = mySettingsComponent.getCyclesLength() != settings.cyclesLength;
        modified |= mySettingsComponent.getHighlighterColor() != settings.highlighterColor;
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        PluginSettingsState settings = PluginSettingsState.getInstance();
        settings.cyclesLength = mySettingsComponent.getCyclesLength();
        settings.highlighterColor = mySettingsComponent.getHighlighterColor();
    }

    @Override
    public void reset() {
        PluginSettingsState settings = PluginSettingsState.getInstance();
        mySettingsComponent.setCyclesLength(settings.cyclesLength);
        mySettingsComponent.setHighlighterColor(new JBColor(new Color(settings.highlighterColor), new Color(settings.highlighterColor)));
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
