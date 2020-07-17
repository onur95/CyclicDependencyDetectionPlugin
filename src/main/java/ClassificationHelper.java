import at.aau.softwaredynamics.classifier.JChangeClassifier;
import at.aau.softwaredynamics.dependency.DependencyChanges;
import at.aau.softwaredynamics.dependency.DependencyExtractor;
import at.aau.softwaredynamics.dependency.NodeDependency;
import at.aau.softwaredynamics.gen.SpoonTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.*;

public class ClassificationHelper {

    private JChangeClassifier classifier;
    private Graph<String, NodeDependencyEdge> g;
    private final Vector<RangeHighlighter> myHighlighters = new Vector<>();

    public ClassificationHelper() {
        //initialize classifier.
        try {
            classifier = new JChangeClassifier(false, JavaMatchers.IterativeJavaMatcher_Spoon.class, new SpoonTreeGenerator());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public List<NodeDependency> getNodeDependency(String sourceCode) throws Exception {
        classifier.classify(sourceCode, sourceCode, false);
        DependencyExtractor dependencyExtractor = new DependencyExtractor(classifier.getMappings(), classifier.getActions(), classifier.getSrcContext().getRoot(), classifier.getDstContext().getRoot(), sourceCode, sourceCode);
        dependencyExtractor.extractDependencies();
        DependencyChanges dependencyChanges = dependencyExtractor.getDependencyChanges();
        return dependencyChanges.getAllUnchangedNodeDependenciesSource();
    }

    public int getDepCount(String sourceClass) throws Exception {
        classifier.classify(sourceClass, sourceClass, false);
        DependencyExtractor dependencyExtractor = new DependencyExtractor(classifier.getMappings(), classifier.getActions(), classifier.getSrcContext().getRoot(), classifier.getDstContext().getRoot(), sourceClass, sourceClass);
        dependencyExtractor.extractDependencies();
        DependencyChanges dependencyChanges = dependencyExtractor.getDependencyChanges();
        return dependencyChanges.getAllUnchangedNodeDependenciesSource().size();
    }


    public Graph<String, NodeDependencyEdge> getGraph() {
        return g;
    }

    public void clearAndInitializeGraph() {
        this.g = null;
        this.g = new DirectedPseudograph<String, NodeDependencyEdge>(NodeDependencyEdge.class);
    }

    public void checkForCycles(Graph<String, NodeDependencyEdge> g, FileEditor[] editors, Project project) throws Exception {
        ArrayList<Editor> myEditors = this.FileEditorToEditor(editors);
        ArrayList<String> vertices = new ArrayList<>();
        Vector<NodeDependency> nodeDependencies = new Vector<>();
        // Checking for cycles in the dependencies

        // computes all the strongly connected components of the directed graph
        StrongConnectivityAlgorithm<String, NodeDependencyEdge> scAlg = new KosarajuStrongConnectivityInspector<>(g);
        List<Graph<String, NodeDependencyEdge>> stronglyConnectedSubgraphs = scAlg.getStronglyConnectedComponents();

        // prints the strongly connected components
        System.out.println("Strongly connected components:");
        for (Graph<String, NodeDependencyEdge> stronglyConnectedSubgraph : stronglyConnectedSubgraphs) {
            vertices.addAll(stronglyConnectedSubgraph.vertexSet());
            HashSet<NodeDependencyEdge> edges = new HashSet<>();
            for (String vertex : vertices) {
                Editor edt = this.getEditor(myEditors, vertex, project);
                if (edt != null) {
                    for (String vertex2 : vertices) {
                        Set<NodeDependencyEdge> npEdges = stronglyConnectedSubgraph.getAllEdges(vertex, vertex2);
                        if (npEdges != null) {
                            edges.addAll(npEdges);
                        }

                    }
                    for (NodeDependencyEdge edge : edges) {
                        nodeDependencies.add(edge.getNodeDependency());
                        System.out.println("Class " + vertex + " dependent on " + edge.getNodeDependency().getDependency().getDependentOnClass() + " fully name: " + edge.getNodeDependency().getDependency().getFullyQualifiedName() + " type: " + edge.getNodeDependency().getDependency().getType());
                    }
                    highlightTextRange(edt, nodeDependencies);
                    edges.clear();
                    nodeDependencies.clear();
                }
            }
        }
    }

    private ArrayList<Editor> FileEditorToEditor(FileEditor[] editors) {
        ArrayList<Editor> myEditors = new ArrayList<>();
        for (FileEditor editor : editors) {
            TextEditor txtEditor = (TextEditor) editor;
            myEditors.add(txtEditor.getEditor());
        }
        return myEditors;
    }

    private void highlightTextRange(Editor editor, Vector<NodeDependency> nodeDependencies) {
        Document document = editor.getDocument();
        for (NodeDependency nodeDependency : nodeDependencies) {
            int startOffsetOfLine = document.getLineStartOffset(nodeDependency.getLineNumbers().getStartLine() - 1);
            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(startOffsetOfLine + nodeDependency.getLineNumbers().getStartOffset(), startOffsetOfLine + nodeDependency.getLineNumbers().getEndOffset(), 0, new TextAttributes(JBColor.black, JBColor.WHITE, JBColor.RED, EffectType.WAVE_UNDERSCORE, 13), HighlighterTargetArea.EXACT_RANGE);
            highlighter.setErrorStripeMarkColor(JBColor.RED);
            highlighter.setErrorStripeTooltip(beautifyOutput(nodeDependency));
            myHighlighters.add(highlighter);
        }
    }

    private String beautifyOutput(NodeDependency nodeDependency) {
        return "Type of Dependency: " + nodeDependency.getDependency().getType() + "\n" + "Dependent on Class: " + nodeDependency.getDependency().getDependentOnClass() + "\n" + "Qualified Name: " + nodeDependency.getDependency().getFullyQualifiedName();
    }

    private Editor getEditor(ArrayList<Editor> editors, String name, Project project) {
        Editor myEditor = null;
        String filename = "";
        for (Editor editor : editors) {
            Document doc = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
            if (virtualFile != null) {
                PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    if (psiFile.getPackageName().isEmpty()) {
                        filename = virtualFile.getName().split("[.]")[0];
                    } else {
                        filename = psiFile.getPackageName() + "." + virtualFile.getNameWithoutExtension();
                    }

                    if (filename.equals(name)) {
                        myEditor = editor;
                    }
                }
            }
        }
        return myEditor;
    }

    public void clearEditors(FileEditor[] editors) {
        for (FileEditor editor : editors) {
            TextEditor myTxtEditor = (TextEditor) editor;
            removeHighlighters(myTxtEditor.getEditor());
        }
    }

    private void removeHighlighters(Editor editor) {
        ArrayList<RangeHighlighter> highlighters = new ArrayList<>();
        if (!myHighlighters.isEmpty()) {
            for (RangeHighlighter highlighter : myHighlighters) {
                Document docedit = editor.getDocument();
                Document docrange = highlighter.getDocument();
                VirtualFile vfedit = FileDocumentManager.getInstance().getFile(docedit);
                VirtualFile vfrange = FileDocumentManager.getInstance().getFile(docrange);
                if (vfedit != null && vfrange != null) {
                    if (vfedit.getName().equals(vfrange.getName())) {
                        editor.getMarkupModel().removeHighlighter(highlighter);
                        highlighters.add(highlighter);
                    }
                }
            }
        }
        for (RangeHighlighter highlighter : highlighters) {
            myHighlighters.remove(highlighter);
        }
    }

}
