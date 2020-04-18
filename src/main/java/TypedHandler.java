import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class TypedHandler implements TypedActionHandler {

    private TypedActionHandler myOriginalActionHandler;

    public TypedHandler(TypedActionHandler originalHandler) {
        myOriginalActionHandler = originalHandler;
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        myOriginalActionHandler.execute(editor, charTyped, dataContext);
        if (charTyped == ';' || charTyped == '}') {
            Project project = editor.getProject();
            iterateContent(project);
        }
    }

    private void iterateContent(Project project) {
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
