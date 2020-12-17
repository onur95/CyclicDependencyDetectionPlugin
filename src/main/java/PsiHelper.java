import com.intellij.codeInsight.problems.WolfTheProblemSolverImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.awt.*;


public class PsiHelper {

    public static PsiHelper psiHelperInstance = new PsiHelper();
    private boolean isSaved = false;
    private int funcCount = 0;

    public Project getActiveProject() {
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

    public boolean isPsiFileInProject(Project project, PsiFile psiFile) {
        boolean inProject = ProjectRootManager.getInstance(project)
                .getFileIndex().isInContent(psiFile.getVirtualFile());
        if (!inProject) {
            System.out.println("File " + psiFile + " not in current project " + project);
        }
        return inProject;
    }

    public void iterateProjectContent(Project project) {
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(fileOrDir -> {
            PsiFile sourceFile = PsiManager.getInstance(project).findFile(fileOrDir);
            if (sourceFile instanceof PsiJavaFile) {
                PsiClass[] classes = ((PsiJavaFile) sourceFile).getClasses();
                for (PsiClass sc : classes) {
                    System.out.println(sc.getText());
                }
            }
            return true;
        });
    }

    public boolean hasVFileSyntaxErrors(VirtualFile virtualFile) {
        return WolfTheProblemSolverImpl.getInstance(this.getActiveProject()).hasSyntaxErrors(virtualFile);
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public int getFuncCount() {
        return funcCount;
    }

    public void setFuncCount(int funcCount) {
        this.funcCount = funcCount;
    }
}
