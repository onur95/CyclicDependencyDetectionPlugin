import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;

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
        e.getPresentation().setEnabledAndVisible(project != null && !ErrorHelper.errorHelperInstance.getHasErrors());
        if (count == 0) {
            iterateProjectContent(project, true);
        }
        count++;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, implement an action. For example, create and show a dialog.
        iterateProjectContent(e.getProject(), false);
        if (cachedValues.equals(newValues)) {
            myNotifierClass.notify(e.getProject(), "Nothing has changed in the Source-Code");
        } else {
            printOut(cachedValues);
            printOut(newValues);

        }
        cloneList(cachedValues, newValues);
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

}
