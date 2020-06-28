import at.aau.softwaredynamics.classifier.JChangeClassifier;
import at.aau.softwaredynamics.dependency.DependencyChanges;
import at.aau.softwaredynamics.dependency.DependencyExtractor;
import at.aau.softwaredynamics.dependency.NodeDependency;
import at.aau.softwaredynamics.gen.SpoonTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.*;

public class ClassificationHelper {

    private JChangeClassifier classifier;
    private final ArrayList<String> dependencies = new ArrayList<String>();
    private CycleDetector<String, NodeDependencyEdge> cycleDetector;
    private Graph<String, NodeDependencyEdge> g;

    public ClassificationHelper() {
        //initialize classifier.
        try {
            classifier = new JChangeClassifier(false, JavaMatchers.IterativeJavaMatcher_Spoon.class, new SpoonTreeGenerator());
            //g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
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

    public Vector<NodeDependency> checkForCycles(Graph<String, NodeDependencyEdge> g) {
        Vector<NodeDependency> nodeDependencies = new Vector<NodeDependency>();
        // Checking for cycles in the dependencies
        cycleDetector = new CycleDetector<String, NodeDependencyEdge>(g);
        // Cycle(s) detected.
        if (cycleDetector.detectCycles()) {
            Iterator<String> iterator;
            Set<String> cycleVertices;
            Set<String> subCycle;

            String cycle;

            System.out.println("Cycles detected.");

            // Get all vertices involved in cycles.
            cycleVertices = cycleDetector.findCycles();

            // Loop through vertices trying to find disjoint cycles.
            while (!cycleVertices.isEmpty()) {
                System.out.println("Cycle:");

                // Get a vertex involved in a cycle.
                iterator = cycleVertices.iterator();
                cycle = iterator.next();

                // Get all vertices involved with this vertex.
                subCycle = cycleDetector.findCyclesContainingVertex(cycle);
                for (String sub : subCycle) {
                    System.out.println("   " + sub);
                    Set<NodeDependencyEdge> edges = g.getAllEdges(cycle, sub);
                    for (NodeDependencyEdge ed : edges) {
                        nodeDependencies.add(ed.getNodeDependency());
                        System.out.println(ed.getNodeDependency().toString());
                    }
                    // Remove vertex so that this cycle is not encountered again
                    cycleVertices.remove(sub);
                }
            }
        }
        return nodeDependencies;
    }

}
