import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class DependenciesGUI {
    private final JBPanel panel = new JBPanel();
    private final JBList<RangeHighlighter> myList;

    public DependenciesGUI(Project project) {
        Vector<RangeHighlighter> myHighLighters = ClassificationHelper.classificationHelperInstance.getMyHighlighters();
        JBLabel label = new JBLabel("Dependencies: ");
        myList = new JBList<>(myHighLighters);
        myList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { //double click detected
                    RangeHighlighter highlighter = myList.getSelectedValue();
                    Document document = highlighter.getDocument();
                    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                    if (file != null) {
                        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
                        if (fileEditor == null) {
                            ToolWindowManager.getInstance(project).unregisterToolWindow("Cyclic Dependency Detection");
                            Messages.showInfoMessage(project, "File not open. Please rerun Plugin or try commiting again!", "Information");
                        } else {
                            FileEditorManager.getInstance(project).navigateToTextEditor(new OpenFileDescriptor(project, file, highlighter.getStartOffset()), true);
                        }
                    }
                }
            }
        });
        myList.setCellRenderer(new ListRenderer());
        panel.add(label);
        panel.add(myList);
    }

    public JPanel getPanel() {
        return panel;
    }
}
