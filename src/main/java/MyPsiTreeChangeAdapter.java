import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MyPsiTreeChangeAdapter extends PsiTreeChangeAdapter {

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        final PsiFile psiFile = event.getFile();
        int errorCount = 0;
        if (psiFile == null) return;
        if (PsiTreeUtil.hasErrorElements(psiFile)) {
            errorCount++;
        }
        if (errorCount == 0) {
            final VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile != null && !PsiHelper.psiHelperInstance.hasVFileSyntaxErrors(virtualFile) && Objects.equals(virtualFile.getExtension(), "java")) {
                final Document document = psiFile.getViewProvider().getDocument();
                if (document != null) {
                    document.addDocumentListener(new VfsChangeListener());
                    FileDocumentManager.getInstance().saveDocument(document);
                }
            }
        }
    }
}
