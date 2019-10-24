package com.mai;

import com.mai.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server.run(Main.class, 8000, 10);
    }
}
