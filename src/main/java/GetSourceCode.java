import at.aau.softwaredynamics.classifier.util.LineNumberRange;
import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;


public class GetSourceCode extends AnAction {

    /*
     * the GetSourceCode class is convenient because as an action it gets instantiated at application startup.
     * On instantiation, the static block of code in GetSourceCode gets evaluated.
     * */
    static {
        Project project = getActiveProject();
        if (project != null) {
            PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeErrorAdapter());
        }
    }

    private final ArrayList<String> sourceCode = new ArrayList<>();
    private final HashSet<RangeHighlighter> myHighlighters = new HashSet<>();
    private final MyNotifierClass myNotifierClass = new MyNotifierClass();
    private final ClassificationHelper classificationHelper = new ClassificationHelper();

    public static Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (ErrorHelper.errorHelperInstance.getHasErrors()) {
            e.getPresentation().setEnabled(false);
            myNotifierClass.notify(e.getProject(), "Error in the Source-Code!");
        }
        if (editor != null) {
            removeHighlighters(editor);
        }
        sourceCode.clear();
        classificationHelper.clearAndInitializeGraph();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, implement an action. For example, create and show a dialog.
        iterateProjectContent(e.getProject());
        //System.out.println(classificationHelper.getGraph().toString());
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Vector<LineNumberRange> ranges = classificationHelper.checkForCycles(classificationHelper.getGraph());
        if (sourceCode.isEmpty()) {
            myNotifierClass.notify(e.getProject(), "No Source-Code to check!");
        }
        if (editor != null) {
            for (LineNumberRange range : ranges) {
                System.out.println(range.getStartOffset() + " " + range.getEndOffset());
                highlightTextRange(editor, range.getStartOffset(), range.getEndOffset());
            }
        }
        //highlightLine(e.getData(CommonDataKeys.EDITOR));
    }

    private void iterateProjectContent(Project project) {
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(fileOrDir -> {
            PsiFile sourceFile = PsiManager.getInstance(project).findFile(fileOrDir);
            if (sourceFile instanceof PsiJavaFile) {
                PsiClass[] classes = ((PsiJavaFile) sourceFile).getClasses();
                for (PsiClass sc : classes) {
                    sourceCode.add(sc.getText());
                    //Add graph vertices if there are dependencies
                    try {
                        if (classificationHelper.getDepCount(sc.getText()) != 0) {
                            classificationHelper.getGraph().addVertex(sc.getQualifiedName());
                            for (NodeDependency nodeDependency : classificationHelper.getNodeDependency(sc.getText())) {
                                if (!nodeDependency.getDependency().getDependentOnClass().contains("java") && !nodeDependency.getDependency().getSelfDependency()) {
                                    if (!classificationHelper.getGraph().containsVertex(nodeDependency.getDependency().getDependentOnClass())) {
                                        classificationHelper.getGraph().addVertex(nodeDependency.getDependency().getDependentOnClass());
                                    }
                                    classificationHelper.getGraph().addEdge(sc.getQualifiedName(), nodeDependency.getDependency().getDependentOnClass(), new NodeDependencyEdge(nodeDependency));
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            return true;
        });
    }

    private void printOut(ArrayList<String> myList) {
        myList.forEach(System.out::println);
    }


    private void highlightTextRange(Editor editor, int startOffset, int endOffset) {
        /*Document doc = editor.getDocument();
        int startOffset = doc.getLineStartOffset(7);
        int endOffset = doc.getLineEndOffset(7); // assuming open line range [4-8)*/
        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, 0, new TextAttributes(JBColor.black, JBColor.WHITE, JBColor.RED, EffectType.WAVE_UNDERSCORE, 13), HighlighterTargetArea.EXACT_RANGE);
        highlighter.setErrorStripeMarkColor(JBColor.RED);
        highlighter.setErrorStripeTooltip("Dependency Detection Tool: Dependency detected");
        myHighlighters.add(highlighter);
    }

    private void highlightLine(Editor editor) {
        RangeHighlighter highlighter = editor.getMarkupModel().addLineHighlighter(4, HighlighterLayer.ERROR, new TextAttributes(JBColor.darkGray, JBColor.WHITE, JBColor.RED, EffectType.WAVE_UNDERSCORE, 13));
        highlighter.setErrorStripeMarkColor(JBColor.RED);
        highlighter.setErrorStripeTooltip("Dependency Detection Tool: Dependency detected");
        myHighlighters.add(highlighter);
    }

    private void removeHighlighters(Editor editor) {
        if (!myHighlighters.isEmpty()) {
            for (RangeHighlighter highlighter : myHighlighters) {
                editor.getMarkupModel().removeHighlighter(highlighter);
                myHighlighters.remove(highlighter);
            }
        }
    }

}
