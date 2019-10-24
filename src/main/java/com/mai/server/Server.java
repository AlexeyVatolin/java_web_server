package com.mai.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Server {
    private static Logger log = Logger.getLogger(Server.class);

    public static <T> void run(Class<T> primarySource, int port, int nThreads) throws IOException {
        log.info("Starting server on " + port + " port");
        ServerSocket server = new ServerSocket(port);

        ExecutorService service = Executors.newFixedThreadPool(nThreads);

        log.info("Waiting for client connection");
        while(true) {
            Socket s = server.accept();
            log.info("Client connected to server");

            service.submit(new RequestHandler(s, primarySource.getPackageName()));
        }
    }
}
