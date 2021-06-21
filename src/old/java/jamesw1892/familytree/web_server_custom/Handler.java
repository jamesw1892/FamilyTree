package web_server_custom;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Date;

/**
 * Handles a single request to the web server
 */
public class Handler implements Runnable {
    private Socket socket;
    private GUI gui;
    private PrintWriter outputString;
    private BufferedOutputStream outputFile;

    private boolean shouldSaveHTML;
    private Header currentHeader;

    private static final String CODE_OK = "200 OK";
    private static final String CODE_NOT_FOUND = "404 Not Found";
    private static final String CODE_NOT_IMPLEMENTED = "501 Not Implemented";

    /**
     * Construct a new handler to handle
     * a specific request from given socket
     * and use the given gui to get the response
     * @param socket
     * @param gui
     */
    public Handler(Socket socket, GUI gui, boolean shouldSaveHTML) throws IOException {
        this.socket = socket;
        OutputStream outputStream = this.socket.getOutputStream();
        this.outputString = new PrintWriter(outputStream);
        this.outputFile = new BufferedOutputStream(outputStream);
        this.gui = gui;
        this.shouldSaveHTML = shouldSaveHTML;
    }

    private void returnHeader(String responseCode) {

        // TODO: Add relevant fields
        this.outputString.println("HTTP/1.1 " + responseCode);
        this.outputString.println("Server: Java HTTP Server by 1892");
        this.outputString.println("Date: " + new Date());
        this.outputString.println("Content-type: " + "?");
        this.outputString.println("Content-length: " + "?");
        this.outputString.println();
        this.outputString.flush();
    }

    public void errorNotFound() throws IOException {
        this.returnHeader(CODE_NOT_FOUND);
        this.writeFile("web/NotFound.html");
    }

    public void errorNotImplemented() {
        this.returnHeader(CODE_NOT_IMPLEMENTED);
    }

    /**
     * Return the given string to the user
     * @param response    If newlines are included, they must be
     * system dependent using System.lineSeparator()
     * @throws IOException
     */
    public void returnString(String response) throws IOException {
        this.returnHeader(CODE_OK);
        this.outputString.println(response);
        this.outputString.flush();

        if (this.shouldSaveHTML) {
            PrintWriter pw = new PrintWriter("web/" + this.currentHeader.getPath() + ".html");
            pw.write(response);
            pw.close();
        }
    }

    /**
     * Return the file specified to the user
     * @param filePath
     * @throws IOException
     */
    public void returnFile(String filePath) throws IOException {
        this.returnHeader(CODE_OK);
        this.writeFile(filePath);
    }

    private void writeFile(String filePath) throws IOException {
        this.outputFile.write(Files.readAllBytes(FileSystems.getDefault().getPath(filePath)));
        this.outputFile.flush();
    }

    /**
     * Handle the request using the gui to get the response
     */
    public void run() {

        try {
            // read and parse header
            Header header = new Header(this.socket, false);

            this.currentHeader = header;

            // handle request - this method in the gui must call either
            // returnFile or returnString above exactly once
            this.gui.handleRequest(this, header);

            this.socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}