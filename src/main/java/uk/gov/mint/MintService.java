package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;

@Path("/")
public class MintService {
    private final String register;
    private final ObjectReconstructor objectReconstructor;
    private final EntryValidator entryValidator;
    private final Loader loadHandler;

    public MintService(String register, ObjectReconstructor objectReconstructor, EntryValidator entryValidator, Loader loadHandler) {
        this.register = register;
        this.objectReconstructor = objectReconstructor;
        this.entryValidator = entryValidator;
        this.loadHandler = loadHandler;
    }

    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @PermitAll
    @Path("/load")
    public void load(String payload) {
        List<JsonNode> objects = objectReconstructor.reconstruct(payload.split("\n"));
        for(JsonNode singleObject : objects) {
            entryValidator.validateEntry(register, singleObject);
        }

        loadHandler.load(objects);
    }
}
