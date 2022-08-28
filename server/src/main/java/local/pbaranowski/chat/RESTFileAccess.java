package local.pbaranowski.chat;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Slf4j
@Path("/ftp")
public class RESTFileAccess {
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    @SneakyThrows
    public String uploadFile(@PathParam("id") String fileId, byte[] fileData) {
        log.info("REST: POST {} {}", fileId, fileData);
        try (FileOutputStream fileOutputStream = new FileOutputStream("/tmp/" + fileId)) {
            fileOutputStream.write(fileData);
        }
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
