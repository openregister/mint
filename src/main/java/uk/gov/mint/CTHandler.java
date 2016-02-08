package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.MintConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class CTHandler implements Loader {
    public static final Logger LOGGER = LoggerFactory.getLogger(CTHandler.class);

    private final String ctserver;
    private final Client client;

    public CTHandler(MintConfiguration configuration, Environment environment, final String appName) {
        this.ctserver = configuration.getCTServer().get();

        this.client = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClientConfiguration())
                .build(appName);
    }

    @Override
    public void load(List<JsonNode> entries) {
        WebTarget wt = client.target(ctserver);

        entries.forEach(singleEntry -> {
            Response response = wt.request()
                    .post(Entity.entity(singleEntry, MediaType.APPLICATION_JSON), Response.class);
            try {
                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    String body = response.readEntity(String.class);
                    LOGGER.debug(String.format("CT server error: %d '%s'", response.getStatus(), body));
                    LOGGER.debug(String.format("Sent payload: '%s'", singleEntry));
                    throw new CTException(body);
                }
            } finally {
                response.close();
            }
        });
    }
}
