import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
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

    private final ArrayList<String> cachedValues = new ArrayList<>();
    private final ArrayList<String> newValues = new ArrayList<>();
    private final HashSet<RangeHighlighter> myHighlighters = new HashSet<>();
    MyNotifierClass myNotifierClass = new MyNotifierClass();
    private int count = 0;

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
        if (ErrorHelper.errorHelperInstance.getHasErrors()) {
            e.getPresentation().setEnabled(false);
            myNotifierClass.notify(e.getProject(), "Error in the Source-Code!");
        }
        if (count == 0) {
            iterateProjectContent(project, true);
        }
        count++;
        removeHighlighters(e.getData(CommonDataKeys.EDITOR));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, implement an action. For example, create and show a dialog.
        iterateProjectContent(e.getProject(), false);
        /*if (cachedValues.equals(newValues)) {
            myNotifierClass.notify(e.getProject(), "Nothing has changed in the Source-Code");
        } else {
            printOut(cachedValues);
            printOut(newValues);

        }*/
        printOut(cachedValues);
        printOut(newValues);
        cloneList(cachedValues, newValues);
        //highlightTextRange(e.getData(CommonDataKeys.EDITOR));
        highlightLine(e.getData(CommonDataKeys.EDITOR));
    }

    private void iterateProjectContent(Project project, boolean isOld) {
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(fileOrDir -> {
            PsiFile sourceFile = PsiManager.getInstance(project).findFile(fileOrDir);
            if (sourceFile instanceof PsiJavaFile) {
                PsiClass[] classes = ((PsiJavaFile) sourceFile).getClasses();
                for (PsiClass sc : classes) {
                    if (isOld) {
                        cachedValues.add(sc.getText());
                    } else {
                        newValues.add(sc.getText());
                    }
                }

            }
            return true;
        });
    }

    private void printOut(ArrayList<String> myList) {
        myList.forEach(System.out::println);
    }

    private void cloneList(ArrayList<String> oldList, ArrayList<String> newList) {
        oldList.clear();
        oldList.addAll(newList);
        newList.clear();
    }

    private void highlightTextRange(Editor editor) {
        Document doc = editor.getDocument();
        int startOffset = doc.getLineStartOffset(4);
        int endOffset = doc.getLineEndOffset(7); // assuming open line range [4-8)
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
