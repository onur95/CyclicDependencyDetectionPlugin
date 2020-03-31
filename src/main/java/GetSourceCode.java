import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class GetSourceCode extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, implement an action. For example, create and show a dialog.
        Project project = e.getProject();
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(@NotNull VirtualFile fileOrDir) {
                PsiFile sourceFile = PsiManager.getInstance(project).findFile(fileOrDir);
                if (sourceFile instanceof PsiJavaFile) {
                    PsiClass[] classes = ((PsiJavaFile) sourceFile).getClasses();
                    if (classes.length < 0) {
                        System.out.println("No classes");
                    } else {
                        for (PsiClass sc : classes) {
                            System.out.println(sc.getText());
                        }
                    }
                }
                return true;
            }
        });

    }
}
