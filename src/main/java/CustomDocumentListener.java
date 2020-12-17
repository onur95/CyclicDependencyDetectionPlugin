import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.jetbrains.annotations.NotNull;

public class CustomDocumentListener implements DocumentListener {

    private final Helper helper = new Helper();

    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        //
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        helper.handleChangeEvents(document);
    }
}
