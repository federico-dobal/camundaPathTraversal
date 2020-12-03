import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtils {

    /**
     * Check whether there is path from startId to endId
     * @param node start flow node
     * @param endId end node id
     * @param visited list of visited nodes
     * @param path tentative path build so far
     * @return
     */
    public static boolean isTherePath(FlowNode node, String endId,
                                      Map<String,Boolean> visited, Stack<String> path)
    {
        String currentNodeId = node.getId();

        // visit current node
        visited.put(currentNodeId, true);

        // add current node to the path
        path.add(currentNodeId);

        // check whether current node is the target destination
        if (currentNodeId.equals(endId)) {
            return true;
        }

        // check path from current node to each adjacent ones
        for (FlowNode nextNode: node.getSucceedingNodes().list())
        {
            // succeeding nodes have not visited
            if (!visited.getOrDefault(nextNode.getId(), false))
            {
                // check whether there is a path from current node to target destination
                if (isTherePath(nextNode, endId, visited, path)) {
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

        // TODO: improve how to check node are present in model
        boolean isStartPresent = isNodePresent(startId, flowInstances);
        boolean isEndPresent = isNodePresent(endId, flowInstances);

        // Fail if either start or end node is not on the graph
        if (!isStartPresent || !isEndPresent) {
            throw new IllegalAccessException("Either start or end node is not in the model");
        }

        // Initialize helper variables such as visited and path
        Map<String,Boolean> visited = new TreeMap<String,Boolean>();
        Stack<String> path = new Stack<String>();

        FlowNode startNode = flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).findFirst().get();
        GraphUtils.isTherePath(startNode, endId, visited, path);

        return path;
    }

    /**
     * Given the list of model element instances checks whether there exists a node with the provided id
     * @param nodeIdentifier: node identifier
     * @param flowInstances list of model element instances
     * @return
     */
    private static boolean isNodePresent(String nodeIdentifier, Collection<ModelElementInstance> flowInstances) {
        return !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> nodeIdentifier.equals(x.getId())).collect(Collectors.toList()).isEmpty();
    }

}
