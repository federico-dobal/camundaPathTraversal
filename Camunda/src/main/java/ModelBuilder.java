import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ModelBuilder {

    // TODO: Get URL from config or properties file
    private static final String MODEL_URL = "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";

    /**
     * Retrieves model from the API endpoint and read it
     * @return the BPMN model
     */
    static BpmnModelInstance buildModelFromUrl() {

        // TODO: manage HTTP errors properly
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ModelBpmn> response
                = restTemplate.getForEntity(MODEL_URL, ModelBpmn.class);

        // TODO: separate duties: between accessing to the model by HTTP request and building the model itself
        InputStream stream = new ByteArrayInputStream(response.getBody().getBpmn20Xml().getBytes());
        return Bpmn.readModelFromStream(stream);
    }
}
