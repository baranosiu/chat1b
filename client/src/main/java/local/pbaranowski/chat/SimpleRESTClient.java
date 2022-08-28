package local.pbaranowski.chat;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class SimpleRESTClient {

    public static void get(String id, String fileName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(Request.get("http://localhost:8080/server-2.0/ftp/" + id)
                    .execute()
                    .returnContent()
                    .asBytes());
            System.out.println("File "+fileName+" downloaded");
        } catch (IOException e) {
            log.error("IOException {}", e.getMessage(), e);
        }
    }

    public static void put(String id, String fileName) {
        try {
            log.info(Request.post("http://localhost:8080/server-2.0/ftp/" + id)
                    .bodyFile(new File(fileName), ContentType.APPLICATION_OCTET_STREAM)
                    .execute()
                    .returnContent()
                    .asString());

        } catch (IOException e) {
            log.error("{}", e.getMessage(), e);

        }
    }
}

