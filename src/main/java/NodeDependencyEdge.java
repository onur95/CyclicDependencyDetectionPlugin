import at.aau.softwaredynamics.dependency.NodeDependency;
import org.jgrapht.graph.DefaultEdge;

public class NodeDependencyEdge extends DefaultEdge {

    private final NodeDependency nodeDependency;

    public NodeDependencyEdge(NodeDependency nodeDependency) {
        this.nodeDependency = nodeDependency;
    }

    public NodeDependency getNodeDependency() {
        return this.nodeDependency;
    }

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget() + " : " + nodeDependency.toString() + ")";
    }
}
