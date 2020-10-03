import at.aau.softwaredynamics.dependency.NodeDependency;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;
import java.util.Vector;

import static com.intellij.CommonBundle.getCancelButtonText;
import static com.intellij.util.ui.UIUtil.getWarningIcon;

public class MyPluginCheckinHandlerFactory extends CheckinHandlerFactory {
    private final ClassificationHelper classificationHelper = ClassificationHelper.classificationHelperInstance;

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
                ToolWindowManager manager = ToolWindowManager.getInstance(panel.getProject());
                if (manager != null) {
                    ToolWindow window = manager.getToolWindow("Cyclic Dependency Detection");
                    if (window != null) {
                        manager.unregisterToolWindow("Cyclic Dependency Detection");
                    }
                }
                if (isCheckForCylces()) {
                    classificationHelper.clearAndInitializeGraph();
                    ResultHelper helper = classificationHelper.collectJavaFiles(panel.getProject());
                    if (helper != null) {
                        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                            indicator.setIndeterminate(false);
                            indicator.setText("Doing work...");
                            indicator.setFraction(0.5);
                            classificationHelper.buildGraphUsingMap(helper);
                            indicator.setFraction(1.0);
                        }, "Building Graph", false, panel.getProject());
                        try {
                            TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> treeMap = classificationHelper.checkForCyclicDependencies(classificationHelper.getGraph());
                            //classificationHelper.highlightDependenciesTextRanges(treeMap,panel.getProject());
                            if (!treeMap.isEmpty()) {
                                myResult = askReviewOrCommit("Commit", "Are you sure you want to ignore the cyclic dependencies found?", "Cyclic Dependencies Found!", treeMap);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    /*ActionManager manager = ActionManager.getInstance();
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
                                myResult = askReviewOrCommit("Commit", "Are you sure you want to ignore the cyclic dependencies found?", "Cyclic Dependencies Found!");
                            }
                        }
                    }*/
                }
                return myResult;
            }

            @NotNull
            private ReturnResult askReviewOrCommit(@NotNull String commitButton, @NotNull String text, @NotNull String title, TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> treeMap) {
                String yesButton = VcsBundle.message("todo.in.new.review.button");
                switch (Messages.showYesNoCancelDialog(panel.getProject(), text, title, yesButton, commitButton, getCancelButtonText(), getWarningIcon())) {
                    case Messages.YES:
                        showDependencies(treeMap);
                        return ReturnResult.CLOSE_WINDOW;
                    case Messages.NO:
                        return ReturnResult.COMMIT;
                }
                return ReturnResult.CANCEL;
            }

            private void showDependencies(TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> treeMap) {
                ToolWindowManager manager = ToolWindowManager.getInstance(panel.getProject());
                if (manager != null) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        ToolWindow window = manager.registerToolWindow("Cyclic Dependency Detection", false, ToolWindowAnchor.BOTTOM);
                        window.getComponent().add(new DependenciesGUI(panel.getProject(), treeMap).getPanel());
                    }, ModalityState.NON_MODAL, panel.getProject().getDisposed());
                }
            }

        };

    }

}
