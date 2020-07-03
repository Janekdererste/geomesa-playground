package org.matsim.contribs.analysis;

import lombok.RequiredArgsConstructor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;

@Path("info")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class InfoResource {

    private final java.nio.file.Path infoPath;

    @GET
    public String getInfo() throws IOException {
        return Files.readString(infoPath);
    }
}
