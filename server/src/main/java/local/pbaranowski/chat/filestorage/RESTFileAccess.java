package local.pbaranowski.chat.filestorage;

import local.pbaranowski.chat.persistence.FileBinaryData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Slf4j
@Path("/ftp")
public class RESTFileAccess {

    @Inject
    private JPAFileStorage jpaFileStorage;

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    @SneakyThrows
    public String uploadFile(@PathParam("id") String fileId, byte[] fileData) {
        log.info("REST: POST {} {}", fileId, fileData);
        FileBinaryData fileBinaryData = new FileBinaryData(fileId, fileData);
        jpaFileStorage.saveBinaryData(fileBinaryData);
        return "OK";
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @SneakyThrows
    public Response downloadFile(@PathParam("id") String fileId) {
        FileBinaryData fileBinaryData = jpaFileStorage.loadBinaryData(UUID.fromString(fileId));
        if (fileBinaryData == null)
            return Response.noContent().build();
        return Response.ok(fileBinaryData.getBinaryData(),MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }
}
