package zad1.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

record ClientSession(PrintWriter out) {
    ClientSession(Socket socket) throws IOException {
        this(new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true));
    }
}
