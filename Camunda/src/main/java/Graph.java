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

    public List<String> buildPath(BpmnModelInstance model, String startId, String endId) throws IllegalAccessException {
        //ModelElementInstance currentTask =  model.getModelElementById("approveInvoice");

        //StartEvent start = (StartEvent) model.getModelElementById("StartEvent_1");

        //ModelElementType taskType = model.getModel().getType(Task.class);
        //ModelElementType gatewayType = model.getModel().getType(Gateway.class);
        ModelElementType flowType = model.getModel().getType(FlowNode.class);
        //Collection<ModelElementInstance> taskInstances = model.getModelElementsByType(taskType);
        //Collection<ModelElementInstance> gatewayInstances = model.getModelElementsByType(gatewayType);
        Collection<ModelElementInstance> flowInstances = model.getModelElementsByType(flowType);
        //FlowNode fn = (FlowNode) flowInstances.stream().collect(Collectors.toList()).get(0);

        //.getId()).filter(x -> startId.equals(x)).collect(Collectors.toList());
        boolean isStartPresent = !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).collect(Collectors.toList()).isEmpty();
        boolean isEndPresent = !flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> endId.equals(x.getId())).collect(Collectors.toList()).isEmpty();

        if (!isStartPresent || !isEndPresent) {
            throw new IllegalAccessException("Either start or end node is not in the model");
        }

        LinkedList<String> visited = new LinkedList<String>();
        LinkedList<FlowNode> queue = new LinkedList<FlowNode>();
        queue.add(flowInstances.stream().map(x -> ((FlowNode) x)).filter(x -> startId.equals(x.getId())).findFirst().get());
        FlowNode currentNode;

        Stack<String> path = new Stack<String>();
        path.add(startId);
        while (!queue.isEmpty()) {
            currentNode = queue.poll();

            if (endId.equals(currentNode.getId())) {
                System.out.println("Found");
                return path;
            }

            for(FlowNode adjNode:  currentNode.getSucceedingNodes().list()) {
                //System.out.println("        " + adjNode.getId());



                if (!visited.contains(adjNode.getId())) {
                    path.add(adjNode.getId());
                    visited.add(adjNode.getId());
                    //System.out.println(adjNode.getId());
                    queue.add(adjNode);
                }

            }


        }
        System.out.println("Not found");
        return null;




        /*
        Map<String, Query<FlowNode>> vertexToSucceedingNodes = new TreeMap<String, Query<FlowNode>>();
        for (FlowNode fn : flowInstances.stream().map(x -> (FlowNode)x).collect(Collectors.toList())){
            System.out.println("" + fn.getId() + " " + fn.getSucceedingNodes().count());
            vertexToSucceedingNodes.put(fn.getId(), fn.getSucceedingNodes());
            for (FlowNode sn : fn.getSucceedingNodes().list()) {
                System.out.println("    " + sn.getId() + " " + sn.getSucceedingNodes().count());
            }

        }






        LinkedList<FlowNode> visited = new LinkedList<FlowNode>();
        LinkedList<ModelElementInstance> queue = new LinkedList<ModelElementInstance>();
        queue.add(currentTask);

        while (!queue.isEmpty()) {
            currentTask = queue.poll();
            for(FlowNode adjNode:  currentTask.getS().list()){
                if ("invoiceProcessed".equals(adjNode.getId())) {
                    System.out.println("Found");
                    break;
                }

                if (adjNode instanceof Gateway)
                if (!visited.contains(adjNode)) {
                    visited.add(adjNode);
                    System.out.println(adjNode.getId());
                    queue.add((Task) modelInstance.getModelElementById(adjNode.getId()));
                }
            }
        }


        */



    }

    public static void main(String args[]) throws IllegalAccessException {
        System.out.println("Hi!");

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
