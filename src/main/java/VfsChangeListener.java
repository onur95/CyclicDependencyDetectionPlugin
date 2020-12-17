import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;


public class VfsChangeListener implements BulkAwareDocumentListener.Simple {

    @Override
    public void beforeDocumentChangeNonBulk(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        //System.out.println(document.getText());
        //PsiHelper.psiHelperInstance.setFuncCount(PsiHelper.psiHelperInstance.getFuncCount()+1);

    }

    @Override
    public void documentChangedNonBulk(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        //System.out.println(document.getText());
        //PsiHelper.psiHelperInstance.setSaved(false);
    }
}
