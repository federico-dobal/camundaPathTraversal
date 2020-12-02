import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.impl.instance.UserTaskImpl;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Graph {

    public static boolean isTherePath(FlowNode node, String src, String endId,
                                      LinkedList<String> visited, Stack<String> path)
    {
        // visit current node
        visited.add(src);

        // add current node to the path
        path.add(src);

        // check whether current node is the target destination
        if (src.equals(endId)) {
            return true;
        }

        // do for every edge (src -> i)
        for (FlowNode nextNode: node.getSucceedingNodes().list())
        {
            // succeeding nodes have not visited
            if (!visited.contains(nextNode.getId()))
            {
                // check whethere there is a path from current node to target destination
                if (isTherePath(nextNode, nextNode.getId(), endId, visited, path)) {
                    return true;
                }
            }
        }

        // backtrack: if not connected remove latest node added to the path
        path.pop();

        return false;
    }


    public List<String> buildPath(BpmnModelInstance model, String startId, String endId) throws IllegalAccessException {

        ModelElementType flowType = model.getModel().getType(FlowNode.class);
        Collection<ModelElementInstance> flowInstances = model.getModelElementsByType(flowType);
        boolean isStartPresent = !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).collect(Collectors.toList()).isEmpty();
        boolean isEndPresent = !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> endId.equals(x.getId())).collect(Collectors.toList()).isEmpty();

        if (!isStartPresent || !isEndPresent) {
            throw new IllegalAccessException("Either start or end node is not in the model");
        }

        LinkedList<String> visited = new LinkedList<String>();
        LinkedList<FlowNode> queue = new LinkedList<FlowNode>();

        Stack<String> stack = new Stack<String>();

        queue.add(flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).findFirst().get());
        FlowNode currentNode;

        FlowNode startNode = flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).findFirst().get();
        this.isTherePath(startNode, startId, endId, visited, stack);

        for (String nodeId: stack) {
            System.out.println("    " + nodeId);
        }

        return null;
    }

    public static void main(String args[]) throws IllegalAccessException {
        Graph g = new Graph();
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";
        ResponseEntity<ModelBpmn> response
                = restTemplate.getForEntity(fooResourceUrl, ModelBpmn.class);

        //System.out.println(response.getBody().getId());

        InputStream stream = new ByteArrayInputStream(response.getBody().getBpmn20Xml().getBytes());

        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(stream);
        String startId = "approveInvoice";
        String endId = "invoiceProcessed";
        String result = "succeess";
        try {
            List<String> path = g.buildPath(modelInstance, startId, endId);
            if (path != null) {
                for (String nodeId: path) {
                    System.out.println("    " + nodeId);
                }
            }
        } catch (IllegalArgumentException e) {
            // In real life code we should logg the exception properly
            result = "Error: either start or end node are not in the model";
        }

        System.out.println(result);




        //File file = new File("model.xml");

        //Bpmn.readModelFromFile(file);


    }
}
