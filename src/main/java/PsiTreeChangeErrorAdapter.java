import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class PsiTreeChangeErrorAdapter extends PsiTreeChangeAdapter {

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        final PsiFile psiFile = event.getFile();
        if (psiFile == null) return;
        ErrorHelper.errorHelperInstance.setHasErrors(PsiTreeUtil.hasErrorElements(psiFile));
    }
}
