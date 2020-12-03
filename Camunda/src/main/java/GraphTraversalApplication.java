import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Stack;

public class GraphTraversalApplication {

    public static void main(String args[]) throws IllegalAccessException {

        if(args.length != 2) {
            System.err.println("Argument error: expected two arguments");
            System.exit(0);
        }

        String startId = args[0];
        String endId = args[1];

        try {
            BpmnModelInstance modelInstance = ModelBuilder.buildModelFromUrl();
            Stack path = GraphUtils.retrievePath(modelInstance, startId, endId);
            System.out.print(String.format("The path from %s to %s is: %s", startId, endId, path.toString()));
        } catch (IllegalArgumentException e) {
            // In real life code we should log the exception properly
            System.err.println("Error: either start or end node are not in the model");
            System.exit(0);
        }
    }
}
