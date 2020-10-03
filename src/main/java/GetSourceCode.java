import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.TreeMap;
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

    private final MyNotifierClass myNotifierClass = new MyNotifierClass();
    private final ClassificationHelper classificationHelper = ClassificationHelper.classificationHelperInstance;

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
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null && editor.isInsertMode());
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        if (manager != null) {
            ToolWindow window = manager.getToolWindow("Cyclic Dependency Detection");
            if (window != null) {
                manager.unregisterToolWindow("Cyclic Dependency Detection");
            }
        }
        //Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (ErrorHelper.errorHelperInstance.getHasErrors()) {
            e.getPresentation().setEnabled(false);
            myNotifierClass.notify(e.getProject(), "Error in the Source-Code!");
        }
        FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
        //classificationHelper.clearEditors(editors);
        classificationHelper.clearEditors(editors);

        classificationHelper.getSourceCode().clear();
        //classificationHelper.clearAndInitializeGraph();
        classificationHelper.clearAndInitializeGraph();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, implement an action. For example, create and show a dialog.
        //iterateProjectContent(e.getProject());
        ResultHelper myHelper = classificationHelper.collectJavaFiles(e.getProject());
        if (classificationHelper.getSourceCode().isEmpty()) {
            myNotifierClass.notify(e.getProject(), "No Source-Code to check!");
        }
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(false);
            indicator.setText("Doing work...");
            indicator.setFraction(0.5);
            classificationHelper.buildGraphUsingMap(myHelper);
            indicator.setFraction(1.0);
        }, "Building Graph and Highlighting Dependencies", false, e.getProject());
        FileEditor[] editors = FileEditorManager.getInstance(e.getProject()).getAllEditors();
        try {
            //classificationHelper.checkForCycles(classificationHelper.getGraph(), editors, e.getProject());
            TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> treeMap = classificationHelper.checkForCyclicDependencies(classificationHelper.getGraph());
            classificationHelper.highlightDependenciesTextRanges(treeMap, e.getProject());
            classificationHelper.handleDependencySequenceNotifications(treeMap, e.getProject());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
