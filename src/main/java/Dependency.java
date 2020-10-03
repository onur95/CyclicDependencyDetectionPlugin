import at.aau.softwaredynamics.dependency.NodeDependency;

import java.util.Vector;

public class Dependency {
    private String fromeNode;
    private Vector<NodeDependency> dependency;

    public Dependency(String fromeNode, Vector<NodeDependency> dependency) {
        this.fromeNode = fromeNode;
        this.dependency = dependency;
    }

    public String getFromeNode() {
        return fromeNode;
    }

    public void setFromeNode(String fromeNode) {
        this.fromeNode = fromeNode;
    }

    public Vector<NodeDependency> getDependency() {
        return dependency;
    }

    public void setDependency(Vector<NodeDependency> dependency) {
        this.dependency = dependency;
    }
}
