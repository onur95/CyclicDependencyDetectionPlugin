import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;

public class Helper {

    public Helper() {
    }

    public void handleChangeEvents(Document document) {
        if (document == null) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            FileDocumentManager instance = FileDocumentManager.getInstance();
            VirtualFile virtualFile = instance.getFile(document);
            if (virtualFile != null && !virtualFile.isDirectory()) {
                Editor[] editors = EditorFactory.getInstance().getEditors(document);
                if (editors.length > 0) {
                    Project project = editors[0].getProject();
                    if (project != null) {
                        checkPsiFiles(document, project);
                    }
                }
            }
        });
    }

    private void checkPsiFiles(Document document, Project project) {
        ApplicationManager.getApplication().runReadAction(() -> {
            //PsiFile cachedPsiFile = PsiDocumentManager.getInstance(project).getCachedPsiFile(document); //old
            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(JavaLanguage.INSTANCE, document.getText());
            if (psiFile instanceof PsiJavaFile) {
                PsiElementFilter filter = element -> !(element instanceof PsiWhiteSpace);
                PsiElement[] elements = PsiTreeUtil.collectElements(psiFile, filter);
                int errorCount = 0;
                for (PsiElement element : elements) {
                    if (PsiTreeUtil.hasErrorElements(element)) {
                        errorCount++;
                    }
                }
                if (errorCount == 0) {
                    Document[] docs = PsiDocumentManager.getInstance(project).getUncommittedDocuments();
                    for (Document dc : docs) {
                        System.out.println(dc.getText());
                    }
                    iterateContent(project);
                   /* Runnable runnable = () -> PsiDocumentManager.getInstance(project).commitDocument(document);
                    iterateContent(project);*/
                }
            }

        });
    }

    private void iterateContent(Project project) {
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
}
