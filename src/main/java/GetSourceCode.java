import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class GetSourceCode extends AnAction {

    /*
     * the GetSourceCode class is convenient because as an action it gets instantiated at application startup.
     * On instantiation, the static block of code in GetSourceCode gets evaluated.
     * */
    static {
        /*final TypedAction typedAction = TypedAction.getInstance();
        typedAction.setupRawHandler(new TypedHandler(typedAction.getRawHandler()));*/
        PsiHelper psiHelper = PsiHelper.psiHelperInstance;
        if (psiHelper.getActiveProject() != null) {
            PsiManager.getInstance(psiHelper.getActiveProject()).addPsiTreeChangeListener(new MyPsiTreeChangeAdapter());
        }
        //ApplicationManager.getApplication().getMessageBus().connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, new MyDocumentManagerListener());
        //EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new VfsChangeListener(),psiHelper.getActiveProject());
    }

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
    }

}
