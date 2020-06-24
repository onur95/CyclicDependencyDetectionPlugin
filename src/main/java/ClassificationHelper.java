import at.aau.softwaredynamics.classifier.JChangeClassifier;
import at.aau.softwaredynamics.dependency.DependencyChanges;
import at.aau.softwaredynamics.dependency.DependencyExtractor;
import at.aau.softwaredynamics.dependency.NodeDependency;
import at.aau.softwaredynamics.gen.SpoonTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;

public class ClassificationHelper {

    private JChangeClassifier classifier;
    private CycleDetector<String, DefaultEdge> cycleDetector;
    private Graph<String, DefaultEdge> g;

    public ClassificationHelper() {
        //initialize classifier.
        try {
            classifier = new JChangeClassifier(false, JavaMatchers.IterativeJavaMatcher_Spoon.class, new SpoonTreeGenerator());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void extractDependencies(ArrayList<String> sourceCode) throws Exception {
        for (String sC : sourceCode) {
            classifier.classify(sC, sC, false);
            DependencyExtractor dependencyExtractor = new DependencyExtractor(classifier.getMappings(), classifier.getActions(), classifier.getSrcContext().getRoot(), classifier.getDstContext().getRoot(), sC, sC);
            dependencyExtractor.extractDependencies();
            DependencyChanges dependencyChanges = dependencyExtractor.getDependencyChanges();
            for (NodeDependency nodeDependency : dependencyChanges.getAllUnchangedNodeDependenciesSource()) {
                System.out.println(nodeDependency);
            }
        }
    }
}
