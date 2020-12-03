import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;
import java.util.stream.Collectors;

public class GraphUtils {

    /**
     * Check whether there is path from startId to endId
     * @param node start flow node(used to access to the adjacent nodes)
     * @param startId start node id
     * @param endId end node id
     * @param visited list of visited nodes
     * @param path tentative path build so far
     * @return
     */
    public static boolean isTherePath(FlowNode node, String startId, String endId,
                                      LinkedList<String> visited, Stack<String> path)
    {
        // visit current node
        visited.add(startId);

        // add current node to the path
        path.add(startId);

        // check whether current node is the target destination
        if (startId.equals(endId)) {
            return true;
        }

        // check path from current node to each adjacent ones
        for (FlowNode nextNode: node.getSucceedingNodes().list())
        {
            // succeeding nodes have not visited
            if (!visited.contains(nextNode.getId()))
            {
                // check whether there is a path from current node to target destination
                if (isTherePath(nextNode, nextNode.getId(), endId, visited, path)) {
                    return true;
                }
            }
        }

        // backtrack: if not connected remove latest node added to the path
        path.pop();

        return false;
    }

    /**
     * Retrives the path from start to end node
     * @param model model graph
     * @param startId start node id
     * @param endId start node id
     * @return list of nodes to go from start node to end node or empty if no path exists
     * @throws IllegalAccessException if either startId or endId is not a node in the graph
     */
    public static Stack<String> retrievePath(BpmnModelInstance model, String startId, String endId) throws IllegalAccessException {

        ModelElementType flowType = model.getModel().getType(FlowNode.class);
        Collection<ModelElementInstance> flowInstances = model.getModelElementsByType(flowType);
        boolean isStartPresent = !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).collect(Collectors.toList()).isEmpty();
        boolean isEndPresent = !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> endId.equals(x.getId())).collect(Collectors.toList()).isEmpty();

        // Fail if either start or end node is not on the graph
        if (!isStartPresent || !isEndPresent) {
            throw new IllegalAccessException("Either start or end node is not in the model");
        }

        // Initialise helper variables such as visited and path
        LinkedList<String> visited = new LinkedList<String>();
        Stack<String> path = new Stack<String>();

        FlowNode startNode = flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).findFirst().get();
        GraphUtils.isTherePath(startNode, startId, endId, visited, path);

        return path;
    }

}
