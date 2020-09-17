import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.CommonBundle.getCancelButtonText;
import static com.intellij.util.ui.UIUtil.getWarningIcon;

public class MyPluginCheckinHandlerFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        return new CheckinHandler() {
            private boolean checkForCycles = false;

            @Nullable
            @Override
            public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
                return new BooleanCommitOption(panel, "Check for Cyclic Dependencies", false, this::isCheckForCylces, this::setCheckForCylces);
            }

            public boolean isCheckForCylces() {
                return checkForCycles;
            }

            public void setCheckForCylces(boolean checkForCycles) {
                this.checkForCycles = checkForCycles;
            }

            @Override
            public ReturnResult beforeCheckin() {
                ReturnResult myResult = super.beforeCheckin();
                if (isCheckForCylces()) {
                    ActionManager manager = ActionManager.getInstance();
                    if (manager != null) {
                        AnAction cyclicDependencyDetectionAction = manager.getAction("GetSourceCode");
                        if (cyclicDependencyDetectionAction != null) {
                            AnAction myAction = new AnAction("Run Cyclic Dependency Action") {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    cyclicDependencyDetectionAction.startInTransaction();
                                    cyclicDependencyDetectionAction.update(e);
                                    cyclicDependencyDetectionAction.actionPerformed(e);
                                }
                            };
                            ActionUtil.invokeAction(myAction, panel.getComponent(), ActionPlaces.UNKNOWN, null, null);
                            if (!ClassificationHelper.classificationHelperInstance.getMyHighlighters().isEmpty()) {
                                myResult = askReviewOrCommit("Commit", "Are you sure you want to ignore the cylic dependencies found?", "Cyclic Dependencies Found!");
                            }
                        }
                    }
                }
                return myResult;
            }

            @NotNull
            private ReturnResult askReviewOrCommit(@NotNull String commitButton, @NotNull String text, @NotNull String title) {
                String yesButton = VcsBundle.message("todo.in.new.review.button");
                switch (Messages.showYesNoCancelDialog(panel.getProject(), text, title, yesButton, commitButton, getCancelButtonText(), getWarningIcon())) {
                    case Messages.YES:
                        return ReturnResult.CLOSE_WINDOW;
                    case Messages.NO:
                        return ReturnResult.COMMIT;
                }
                return ReturnResult.CANCEL;
            }

        };
    }

}
