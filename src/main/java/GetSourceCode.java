import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

    private final ArrayList<String> sourceCode = new ArrayList<>();
    private final HashSet<RangeHighlighter> myHighlighters = new HashSet<>();
    private final MyNotifierClass myNotifierClass = new MyNotifierClass();
    //private final ClassificationHelper classificationHelper = new ClassificationHelper();

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

        //Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (ErrorHelper.errorHelperInstance.getHasErrors()) {
            e.getPresentation().setEnabled(false);
            myNotifierClass.notify(e.getProject(), "Error in the Source-Code!");
        }
        FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
        //classificationHelper.clearEditors(editors);
        ClassificationHelper.classificationHelperInstance.clearEditors(editors);

        sourceCode.clear();
        //classificationHelper.clearAndInitializeGraph();
        ClassificationHelper.classificationHelperInstance.clearAndInitializeGraph();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, implement an action. For example, create and show a dialog.
        //iterateProjectContent(e.getProject());
        ResultHelper myHelper = collectJavaFiles(e.getProject());
        if (sourceCode.isEmpty()) {
            myNotifierClass.notify(e.getProject(), "No Source-Code to check!");
        }
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(false);
            indicator.setText("Doing work...");
            indicator.setFraction(0.5);
            buildGraphUsingMap(myHelper);
            indicator.setFraction(1.0);
        }, "Building Graph and Highlighting Dependencies", false, e.getProject());
        FileEditor[] editors = FileEditorManager.getInstance(e.getProject()).getAllEditors();
        try {
            ClassificationHelper.classificationHelperInstance.checkForCycles(ClassificationHelper.classificationHelperInstance.getGraph(), editors, e.getProject());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    //collects all Java files of a projects except for test files
    private ResultHelper collectJavaFiles(Project project) {
        HashMap<String, String> psiJavaFiles = new HashMap<>();
        HashMap<String, String> psiInnerJavaFiles = new HashMap<>();
        ResultHelper myHelper = new ResultHelper(psiJavaFiles, psiInnerJavaFiles);
        Collection<VirtualFile> projectJavaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        if (!projectJavaFiles.isEmpty()) {
            for (VirtualFile virtualFile : projectJavaFiles) {
                if (virtualFile != null) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile);
                    if (psiJavaFile != null && !psiJavaFile.getContainingDirectory().toString().contains("src" + "\\" + "test")) {
                        PsiClass[] classes = psiJavaFile.getClasses();
                        String[] name = psiJavaFile.getName().split("[.]");
                        if (psiJavaFile.getPackageName().isEmpty()) {
                            psiJavaFiles.put(name[0], psiJavaFile.getText());
                        } else {
                            psiJavaFiles.put(psiJavaFile.getPackageName() + "." + name[0], psiJavaFile.getText());
                        }
                        sourceCode.add(psiJavaFile.getName());
                        /*if (classes.length > 0) {
                            for (PsiClass psiClass : classes) {
                                psiJavaFiles.put(psiClass.getName(), psiClass.getText());
                                sourceCode.add(psiClass.getName());
                                PsiClass[] psiInnerClasses = psiClass.getInnerClasses();
                                if (psiInnerClasses.length > 0) {
                                    for (PsiClass psiInnerClass : psiInnerClasses) {
                                        psiInnerJavaFiles.put(psiInnerClass.getName(), psiInnerClass.getText());
                                    }
                                }
                            }
                        }*/
                    }
                }
            }
        }
        return myHelper;
    }

    //builds the graph using the java files and dependencies
    private void buildGraphUsingMap(ResultHelper myHelper) {

        HashMap<String, String> psiJavaFiles = myHelper.getPsiClasses();
        HashMap<String, String> psiInnerJavaFiles = myHelper.getPsiInnerClasses();
        if (!psiJavaFiles.isEmpty()) {
            for (String psiClass : psiJavaFiles.keySet()) {
                try {

                    //Add graph vertices if there are dependencies
                    if (ClassificationHelper.classificationHelperInstance.getDepCount(psiJavaFiles.get(psiClass)) != 0) {
                        ClassificationHelper.classificationHelperInstance.getGraph().addVertex(psiClass);
                        for (NodeDependency nodeDependency : ClassificationHelper.classificationHelperInstance.getNodeDependency(psiJavaFiles.get(psiClass))) {
                            if (!nodeDependency.getDependency().getDependentOnClass().contains("java.") && !nodeDependency.getDependency().getDependentOnClass().startsWith(".") && !nodeDependency.getDependency().getSelfDependency() && psiJavaFiles.containsKey(nodeDependency.getDependency().getDependentOnClass())) {
                                if (!ClassificationHelper.classificationHelperInstance.getGraph().containsVertex(nodeDependency.getDependency().getDependentOnClass())) {
                                    ClassificationHelper.classificationHelperInstance.getGraph().addVertex(nodeDependency.getDependency().getDependentOnClass());
                                }
                                ClassificationHelper.classificationHelperInstance.getGraph().addEdge(psiClass, nodeDependency.getDependency().getDependentOnClass(), new NodeDependencyEdge(nodeDependency));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
