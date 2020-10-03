import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class JListAdapter extends MouseAdapter {
    private final Project project;
    private final JBList<Dependency> myList;

    public JListAdapter(Project project, JBList<Dependency> myList) {
        this.project = project;
        this.myList = myList;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) { //double click detected
            Dependency dependency = myList.getSelectedValue();
            if (dependency != null) {
                String filename = "";
                String fromNode = dependency.getFromeNode();
                if (fromNode.contains(".")) {
                    String[] splitted = fromNode.split("[.]");
                    filename = splitted[splitted.length - 1];
                } else {
                    filename = dependency.getFromeNode();
                }
                Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(project, filename + ".java", GlobalSearchScope.projectScope(project));
                if (!files.isEmpty()) {
                    for (VirtualFile file : files) {
                        if (file != null) {

                            FileEditorManager.getInstance(project).navigateToTextEditor(new OpenFileDescriptor(project, file, 0), true);
                            FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
                            TextEditor textEditor = (TextEditor) fileEditor;
                            if (textEditor != null) {
                                Document document = textEditor.getEditor().getDocument();
                                for (NodeDependency nodeDependency : dependency.getDependency()) {
                                    int startOffsetOfLine = document.getLineStartOffset(nodeDependency.getLineNumbers().getStartLine() - 1);
                                    RangeHighlighter highlighter = textEditor.getEditor().getMarkupModel().addRangeHighlighter(startOffsetOfLine + nodeDependency.getLineNumbers().getStartOffset(), startOffsetOfLine + nodeDependency.getLineNumbers().getEndOffset(), HighlighterLayer.WARNING, new TextAttributes(JBColor.black, JBColor.WHITE, JBColor.MAGENTA, EffectType.ROUNDED_BOX, 13), HighlighterTargetArea.EXACT_RANGE);
                                    highlighter.setErrorStripeMarkColor(JBColor.MAGENTA);
                                    highlighter.setErrorStripeTooltip(dependency.getDependency().toString());
                                }
                                FileEditorManager.getInstance(project).navigateToTextEditor(new OpenFileDescriptor(project, file, 0), true);
                            }

                        }
                    }
                }
            }
        }
    }
}
