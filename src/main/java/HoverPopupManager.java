import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

//Manages Popup generation over RangeHighlighters
public class HoverPopupManager implements EditorMouseMotionListener, EditorMouseListener {

    private boolean skipMovement;

    @Override
    public void mouseMoved(@NotNull EditorMouseEvent e) {
        clearPopups(e);
        if (ignoreEvent(e)) return;
        handleMouseMoved(e);

    }

    @Override
    public void mouseEntered(@NotNull EditorMouseEvent event) {
        // we receive MOUSE_MOVED event after MOUSE_ENTERED even if mouse wasn't physically moved,
        // e.g. if a popup overlapping editor has been closed
        skipMovement();
    }

    @Override
    public void mouseExited(@NotNull EditorMouseEvent event) {
        clearPopups(event);
    }


    @Override
    public void mousePressed(@NotNull EditorMouseEvent event) {
        clearPopups(event);
    }

    private void clearPopups(@NotNull EditorMouseEvent event) {
        List<JBPopup> popups = JBPopupFactory.getInstance().getChildPopups(event.getEditor().getComponent());
        for (JBPopup popup : popups) {
            popup.cancel();
        }
    }

    private void skipMovement() {
        skipMovement = true;
    }

    private boolean ignoreEvent(EditorMouseEvent e) {
        boolean ignore = true;
        if (skipMovement) {
            skipMovement = false;
            return true;
        }
        ignore = checkPositionInRange(e);
        return ignore;
    }

    private boolean checkPositionInRange(EditorMouseEvent e) {
        boolean ignore = true;
        Vector<RangeHighlighter> myHighLighters = ClassificationHelper.classificationHelperInstance.getMyHighlighters();
        if (myHighLighters != null && !myHighLighters.isEmpty()) {
            LogicalPosition pos = e.getEditor().xyToLogicalPosition(e.getMouseEvent().getPoint());
            int documentOffset = e.getEditor().logicalPositionToOffset(pos);
            RangeHighlighter[] highlightersOfEditor = e.getEditor().getMarkupModel().getAllHighlighters();
            for (RangeHighlighter highlighter : myHighLighters) {
                boolean result = Arrays.asList(highlightersOfEditor).contains(highlighter);
                if (result) {
                    int startOffsetOfHighlighter = highlighter.getStartOffset();
                    int endOffsetOfHighlighter = highlighter.getEndOffset();
                    if (startOffsetOfHighlighter <= documentOffset && documentOffset < endOffsetOfHighlighter) {
                        ignore = false;
                    }
                }
            }
        }
        return ignore;
    }

    private void handleMouseMoved(@NotNull EditorMouseEvent e) {
        Vector<RangeHighlighter> myHighLighters = ClassificationHelper.classificationHelperInstance.getMyHighlighters();
        RangeHighlighter[] highlightersOfEditor = e.getEditor().getMarkupModel().getAllHighlighters();
        Vector<RangeHighlighter> editorHighlighters = new Vector<>();
        if (myHighLighters != null && !myHighLighters.isEmpty()) {
            for (RangeHighlighter highlighter : myHighLighters) {
                boolean result = Arrays.asList(highlightersOfEditor).contains(highlighter);
                if (result) {
                    editorHighlighters.add(highlighter);
                }
            }
        }
        if (!editorHighlighters.isEmpty()) {
            for (RangeHighlighter highlighter : editorHighlighters) {
                LogicalPosition pos = e.getEditor().xyToLogicalPosition(e.getMouseEvent().getPoint());
                int documentOffset = e.getEditor().logicalPositionToOffset(pos);
                int startOffsetOfHighlighter = highlighter.getStartOffset();
                int endOffsetOfHighlighter = highlighter.getEndOffset();
                if (startOffsetOfHighlighter <= documentOffset && documentOffset < endOffsetOfHighlighter) {
                    String info = Objects.requireNonNull(highlighter.getErrorStripeTooltip()).toString();
                    JLabel label = new JLabel("<html>" + info.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
                    JBPopupFactory.getInstance().createComponentPopupBuilder(label, null).createPopup().show(new RelativePoint(e.getEditor().getComponent(), e.getEditor().logicalPositionToXY(pos)));
                }

            }
        }
    }
}
