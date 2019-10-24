package com.mai.server;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {

    static Logger log = Logger.getLogger(Request.class);

    private Map<String, String> headers;
    private String path;
    private String body;
    private HttpMethod method;

    private Request(Map<String, String> headers, String path, String body, HttpMethod method) {
        this.headers = headers;
        this.path = path;
        this.body = body;
        this.method = method;
    }

    static Request parse(InputStream inputStream) throws IOException {

        log.debug("available = "+inputStream.available());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String firstLine = reader.readLine();
        log.debug("firstLine = " + firstLine);
        String[] parts = firstLine.split(" ");
        HttpMethod method = HttpMethod.valueOf(HttpMethod.class, parts[0]);
        String path = parts[1];

        String body = null;
        Map<String, String> headers = new HashMap<>();
        while(true){
            String line = reader.readLine();
            log.debug(line);
            if(line.isEmpty()){
                if(method == HttpMethod.GET || method == HttpMethod.DELETE) { //GET and DELETE have no body
                    break;
                } else { //read POST body
                    log.info("Reading body");
                    body = readPayload(reader);
                    log.debug("body = " + body);
                    break;
                }
            } else {
                String[] headerKeyValue = line.split(":");
                headers.put(headerKeyValue[0], headerKeyValue[1].trim());
            }
        }

        log.debug("Parsing completed");
        return new Request(headers, path, body, method);
    }

    private static String readPayload(BufferedReader reader) throws IOException {
        StringBuilder payload = new StringBuilder();
        while(reader.ready()){
            payload.append((char) reader.read());
        }
        return payload.toString();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public HttpMethod getMethod() {
        return method;
    }
}
