package local.pbaranowski.chat;

import local.pbaranowski.chat.persistence.JPABinaryData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;

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
        jpaFileStorage.echo(fileId);
        JPABinaryData jpaBinaryData = new JPABinaryData(fileId,"Tu leca binarne dane".getBytes());
        jpaFileStorage.storeBinaryData(jpaBinaryData);
        return "OK";
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @SneakyThrows
    public Response downloadFile(@PathParam("id") String fileId) {
        try (FileInputStream fileInputStream = new FileInputStream("/tmp/" + fileId)) {
            return Response.ok(fileInputStream.readAllBytes(), MediaType.APPLICATION_OCTET_STREAM)
                    .build();
        }
    }
}
