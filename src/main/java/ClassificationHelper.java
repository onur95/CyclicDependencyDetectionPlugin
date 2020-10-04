import at.aau.softwaredynamics.classifier.JChangeClassifier;
import at.aau.softwaredynamics.dependency.DependencyChanges;
import at.aau.softwaredynamics.dependency.DependencyExtractor;
import at.aau.softwaredynamics.dependency.NodeDependency;
import at.aau.softwaredynamics.gen.SpoonTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DirectedPseudograph;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ClassificationHelper {

    private JChangeClassifier classifier;
    private Graph<String, NodeDependencyEdge> g;
    private final MyNotifierClass myNotifierClass = new MyNotifierClass();
    private final Vector<RangeHighlighter> myHighlighters = new Vector<>();
    public static ClassificationHelper classificationHelperInstance = new ClassificationHelper();
    private final PluginSettingsState settingsState = PluginSettingsState.getInstance();

    private final ArrayList<String> sourceCode = new ArrayList<>();

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

    //returns number of dependencies of a class
    public int getDepCount(String sourceClass) throws Exception {
        classifier.classify(sourceClass, sourceClass, false);
        DependencyExtractor dependencyExtractor = new DependencyExtractor(classifier.getMappings(), classifier.getActions(), classifier.getSrcContext().getRoot(), classifier.getDstContext().getRoot(), sourceClass, sourceClass);
        dependencyExtractor.extractDependencies();
        DependencyChanges dependencyChanges = dependencyExtractor.getDependencyChanges();
        List<NodeDependency> nodeDependencies = dependencyChanges.getAllUnchangedNodeDependenciesSource();
        nodeDependencies.removeIf(nodeDependency -> nodeDependency.getDependency().getSelfDependency());
        return nodeDependencies.size();
    }


    public Graph<String, NodeDependencyEdge> getGraph() {
        return g;
    }

    public void clearAndInitializeGraph() {
        this.g = null;
        this.g = new DirectedPseudograph<String, NodeDependencyEdge>(NodeDependencyEdge.class);
    }

    /*public void checkForCycles(Graph<String, NodeDependencyEdge> g, FileEditor[] editors, Project project) throws Exception {
        ArrayList<Editor> myEditors = this.FileEditorToEditor(editors);
        ArrayList<String> vertices = new ArrayList<>();
        Vector<NodeDependency> nodeDependencies = new Vector<>();
        // Checking for cycles in the dependencies

        // computes all the strongly connected components of the directed graph
        StrongConnectivityAlgorithm<String, NodeDependencyEdge> scAlg = new KosarajuStrongConnectivityInspector<>(g);
        List<Graph<String, NodeDependencyEdge>> stronglyConnectedSubgraphs = scAlg.getStronglyConnectedComponents();

        // prints the strongly connected components
        System.out.println("Strongly connected components:");
        //ArrayList<String> dependeySequences = new ArrayList<>(Collections.emptyList());
        TreeMap<String, String> treeMap = new TreeMap<>();
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
                        //dependeySequences.add(vertex + " -> " + edge.getNodeDependency().getDependency().getDependentOnClass());
                        treeMap.put(vertex, edge.getNodeDependency().getDependency().getDependentOnClass());
                    }


                    highlightTextRange(edt, nodeDependencies);
                    edges.clear();
                    nodeDependencies.clear();
                }
            }
        }
        //myNotifierClass.notify(project, this.getRangeHighlighterCount() + " dependencies found!");
        this.handleDependencySequences(stronglyConnectedSubgraphs.size(), treeMap, project);
        //dependeySequences.clear();
        treeMap.clear();
    }*/

    public TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> checkForCyclicDependencies(Graph<String, NodeDependencyEdge> g) throws Exception {
        int cycleNumber = 1;
        ArrayList<String> vertices = new ArrayList<>();
        TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> dependency = new TreeMap<>();
        int totalCycles = settingsState.cyclesLength;
        ArrayList<Graph<String, NodeDependencyEdge>> totalLengthCycles = new ArrayList<>();
        // Checking for cycles in the dependencies

        // computes all the strongly connected components of the directed graph
        StrongConnectivityAlgorithm<String, NodeDependencyEdge> scAlg = new KosarajuStrongConnectivityInspector<>(g);
        List<Graph<String, NodeDependencyEdge>> stronglyConnectedSubgraphs = scAlg.getStronglyConnectedComponents();
        List<Graph<String, NodeDependencyEdge>> subgraphsWithMoreThanOneVertexSet = stronglyConnectedSubgraphs.stream().filter(subgraph -> subgraph.vertexSet().size() > 1).collect(Collectors.toList());
        if (totalCycles > subgraphsWithMoreThanOneVertexSet.size()) {
            totalLengthCycles.addAll(subgraphsWithMoreThanOneVertexSet);
        } else {
            for (int i = 0; i < totalCycles; i++) {
                totalLengthCycles.add(subgraphsWithMoreThanOneVertexSet.get(i));
            }
        }

        for (Graph<String, NodeDependencyEdge> stronglyConnectedSubgraph : totalLengthCycles) {
            vertices.clear();
            TreeMap<String, Vector<NodeDependency>> treeMap = new TreeMap<>();
            vertices.addAll(stronglyConnectedSubgraph.vertexSet());
            for (String vertex : vertices) {
                Vector<NodeDependency> nodeDependencies = new Vector<>();
                for (String vertex2 : vertices) {
                    Set<NodeDependencyEdge> npEdges = stronglyConnectedSubgraph.getAllEdges(vertex, vertex2);
                    if (npEdges != null && !npEdges.isEmpty()) {
                        for (NodeDependencyEdge edge : npEdges) {
                            nodeDependencies.add(edge.getNodeDependency());
                            }
                        }
                    }

                    treeMap.put(vertex, nodeDependencies);
                }
                dependency.put(cycleNumber, treeMap);
                cycleNumber++;


        }
        return dependency;
    }

    public void highlightDependenciesTextRanges(TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> dependencies, Project project) {
        FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
        ArrayList<Editor> myEditors = this.FileEditorToEditor(editors);
        if (!dependencies.isEmpty()) {
            for (Integer i : dependencies.keySet()) {
                TreeMap<String, Vector<NodeDependency>> treeMap = dependencies.get(i);
                if (!treeMap.isEmpty()) {
                    for (String vertex : treeMap.keySet()) {
                        Editor edt = this.getEditor(myEditors, vertex, project);
                        if (edt != null) {
                            highlightTextRange(edt, treeMap.get(vertex));
                        }
                    }
                }
            }
        }
    }

    //converts FileEditor instances to Editor instances
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
            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(startOffsetOfLine + nodeDependency.getLineNumbers().getStartOffset(), startOffsetOfLine + nodeDependency.getLineNumbers().getEndOffset(), HighlighterLayer.WARNING, new TextAttributes(JBColor.black, JBColor.WHITE, new JBColor(new Color(settingsState.highlighterColor), new Color(settingsState.highlighterColor)), EffectType.ROUNDED_BOX, 13), HighlighterTargetArea.EXACT_RANGE);
            highlighter.setErrorStripeMarkColor(new JBColor(new Color(settingsState.highlighterColor), new Color(settingsState.highlighterColor)));
            highlighter.setErrorStripeTooltip(beautifyOutput(nodeDependency));
            myHighlighters.add(highlighter);
        }
    }


    private String beautifyOutput(NodeDependency nodeDependency) {
        return "Type of Dependency: " + nodeDependency.getDependency().getType() + "\n" + "Dependent on Class: " + nodeDependency.getDependency().getDependentOnClass() + "\n" + "Qualified Name: " + nodeDependency.getDependency().getFullyQualifiedName();
    }

    //returns the Editor object for the current open file
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

    //remove RangeHighlighters for every open Editor object
    public void clearEditors(FileEditor[] editors) {
        for (FileEditor editor : editors) {
            TextEditor myTxtEditor = (TextEditor) editor;
            removeHighlighters(myTxtEditor.getEditor());
        }
    }

    //remove RangeHighlighters from the specified Editor object.
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

    private int getRangeHighlighterCount() {
        if (myHighlighters.isEmpty()) {
            return 0;
        }
        return myHighlighters.size();
    }

    //show the dependency sequence and dependency count in a notification
    /*private void handleDependencySequences(int totalCircles, TreeMap<String, String> treeMap, Project project) {
        if (!treeMap.isEmpty()) {
            StringBuilder sb = new StringBuilder("Total cycles found: " + totalCircles).append("\n").append("Current path with " + getRangeHighlighterCount() + " dependencies:" + "\n")
                    .append(treeMap.firstKey()).append(" -> ").append(treeMap.firstEntry().getValue());
            getCircle(treeMap, treeMap.firstEntry().getValue(), sb, treeMap.firstKey());

            myNotifierClass.notify(project, "<html>" + sb.toString().replaceAll("\n", "<br/>") + "</html>");
        } else {
            myNotifierClass.notify(project, getRangeHighlighterCount() + " cyclic dependencies found!");
        }

    }*/

    public void handleDependencySequenceNotifications(TreeMap<Integer, TreeMap<String, Vector<NodeDependency>>> dependencies, Project project) {
        if (!dependencies.isEmpty()) {
            int total = dependencies.keySet().size();
            StringBuilder sb = new StringBuilder("Total cycles found: " + total).append("\n");
            sb.append("---------------------------------------------------------").append("\n");
            for (Integer i : dependencies.keySet()) {
                TreeMap<String, Vector<NodeDependency>> treeMap = dependencies.get(i);
                if (!treeMap.isEmpty()) {
                    int currentCount = 0;
                    TreeMap<String, String> myTreeMap = new TreeMap<>();
                    for (String vertex : treeMap.keySet()) {
                        if (!treeMap.get(vertex).isEmpty()) {
                            for (NodeDependency nodeDependency : treeMap.get(vertex)) {
                                myTreeMap.put(vertex, nodeDependency.getDependency().getDependentOnClass());
                            }
                        }
                        currentCount = currentCount + treeMap.get(vertex).size();
                    }
                    sb.append("Current path with ").append(currentCount).append(" dependencies:").append("\n");
                    sb.append(myTreeMap.firstKey()).append(" -> ").append(myTreeMap.firstEntry().getValue());
                    getCircle(myTreeMap, myTreeMap.firstEntry().getValue(), sb, myTreeMap.firstKey());
                    sb.append("\n").append("---------------------------------------------------------").append("\n");
                }
            }
            myNotifierClass.notify(project, "<html>" + sb.toString().replaceAll("\n", "<br/>") + "</html>");
        } else {
            myNotifierClass.notify(project, "No dependencies found!");
        }
    }

    public Vector<RangeHighlighter> getMyHighlighters() {
        return myHighlighters;
    }

    private void getCircle(TreeMap<String, String> treeMap, String node, StringBuilder stringBuilder, String firstElement) {
        if (!node.equals(firstElement) && treeMap.containsKey(node)) {
            String next = treeMap.get(node);
            stringBuilder.append(" -> ").append(next);
            getCircle(treeMap, next, stringBuilder, firstElement);
        }
    }

    //collects all Java files of a projects except for test files
    public ResultHelper collectJavaFiles(Project project) {
        HashMap<String, String> psiJavaFiles = new HashMap<>();
        HashMap<String, String> psiInnerJavaFiles = new HashMap<>();
        ResultHelper myHelper = new ResultHelper(psiJavaFiles, psiInnerJavaFiles);
        Collection<VirtualFile> projectJavaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        if (!projectJavaFiles.isEmpty()) {
            for (VirtualFile virtualFile : projectJavaFiles) {
                if (virtualFile != null) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile);
                    if (psiJavaFile != null && !psiJavaFile.getContainingDirectory().toString().contains("src" + "\\" + "test")) {
                        PsiClass[] classes = psiJavaFile.getClasses();
                        String[] name = psiJavaFile.getName().split("[.]");
                        if (psiJavaFile.getPackageName().isEmpty()) {
                            psiJavaFiles.put(name[0], psiJavaFile.getText());
                        } else {
                            psiJavaFiles.put(psiJavaFile.getPackageName() + "." + name[0], psiJavaFile.getText());
                        }
                        sourceCode.add(psiJavaFile.getName());
                        /*if (classes.length > 0) {
                            for (PsiClass psiClass : classes) {
                                psiJavaFiles.put(psiClass.getName(), psiClass.getText());
                                sourceCode.add(psiClass.getName());
                                PsiClass[] psiInnerClasses = psiClass.getInnerClasses();
                                if (psiInnerClasses.length > 0) {
                                    for (PsiClass psiInnerClass : psiInnerClasses) {
                                        psiInnerJavaFiles.put(psiInnerClass.getName(), psiInnerClass.getText());
                                    }
                                }
                            }
                        }*/
                    }
                }
            }
        }
        return myHelper;
    }

    //builds the graph using the java files and dependencies
    public void buildGraphUsingMap(ResultHelper myHelper) {

        HashMap<String, String> psiJavaFiles = myHelper.getPsiClasses();
        HashMap<String, String> psiInnerJavaFiles = myHelper.getPsiInnerClasses();
        if (!psiJavaFiles.isEmpty()) {
            for (String psiClass : psiJavaFiles.keySet()) {
                try {

                    //Add graph vertices if there are dependencies
                    if (this.getDepCount(psiJavaFiles.get(psiClass)) != 0) {
                        this.getGraph().addVertex(psiClass);
                        for (NodeDependency nodeDependency : this.getNodeDependency(psiJavaFiles.get(psiClass))) {
                            if (!nodeDependency.getDependency().getDependentOnClass().contains("java.") && !nodeDependency.getDependency().getDependentOnClass().startsWith(".") && !nodeDependency.getDependency().getSelfDependency() && psiJavaFiles.containsKey(nodeDependency.getDependency().getDependentOnClass())) {
                                if (!this.getGraph().containsVertex(nodeDependency.getDependency().getDependentOnClass())) {
                                    this.getGraph().addVertex(nodeDependency.getDependency().getDependentOnClass());
                                }
                                this.getGraph().addEdge(psiClass, nodeDependency.getDependency().getDependentOnClass(), new NodeDependencyEdge(nodeDependency));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public ArrayList<String> getSourceCode() {
        return sourceCode;
    }


}
