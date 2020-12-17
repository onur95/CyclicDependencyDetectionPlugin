import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import org.jetbrains.annotations.NotNull;

public class MyDocumentManagerListener implements FileDocumentManagerListener {


    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        System.out.println("saving event");
        System.out.println(document.getText());
        /*if (PsiHelper.psiHelperInstance.getFuncCount() == 2) {
            PsiHelper.psiHelperInstance.setSaved(false);
        }
        if (PsiHelper.psiHelperInstance.getFuncCount() == 0){
            PsiHelper.psiHelperInstance.setSaved(true);
        }*/
    }
}
