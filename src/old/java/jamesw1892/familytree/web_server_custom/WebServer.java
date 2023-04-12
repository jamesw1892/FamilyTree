package web_server_custom;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DataFormatException;

/**
 * Runs the web server
 */
class WebServer implements Runnable {
    private static final int DEFAULT_PORT = 8080;
    private int port;
    private GUI gui;

    /**
     * Construct a new web server object that can be run with the given port
     * @param port
     * @throws IOException
     * @throws DataFormatException
     */
    public WebServer(int port) throws IOException, DataFormatException {
        this.gui = new GUI();
        this.port = port;
    }

    /**
     * Run the web server, creating a thread for each request made
     * and pass them to the gui to handle it
     */
    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            this.port = serverSocket.getLocalPort();
            System.out.println("Server started at http://localhost:" + this.port + " press enter to stop" + System.lineSeparator());
            ExecutorService pool = Executors.newCachedThreadPool();
            Thread thisThread = Thread.currentThread();

            // until interrupted, accept requests in a new thread
            while (!thisThread.isInterrupted()) {
                pool.execute(new Handler(serverSocket.accept(), gui, false));
            }

            pool.shutdown();
            serverSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Server stopped");
    }

    /**
     * Close the gui web server which closes the gui which
     * writes the data edited in PersonStore
     * @throws IOException
     */
    public void close() throws IOException {
        this.gui.close();
    }

    /**
     * Run the web server until the enter key is pressed.
     * Take an optional command line argument of the port
     * @param args
     * @throws IOException
     * @throws DataFormatException
     */
    public static void main(String[] args) throws IOException, DataFormatException {

        // use default port unless one has been provided
        // as a command line argument
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {}
        }

        Scanner sc = new Scanner(System.in);
        WebServer webServer = new WebServer(port);
        Thread thread = new Thread(webServer);

        // start handling requests
        thread.start();

        // this will block this thread until the user presses enter
        sc.nextLine();

        // when they do, inturrupt the thread so it doesn't
        // handle any more requests
        thread.interrupt();

        // update the port since if it was 0 then it will be randomly assigned
        port = webServer.port;

        // issue a final dummy request (as the thread doesn't realise
        // it's been interrupted until another connection comes in to
        // stop 'serverSocket.accept()' from blocking)
        Socket socket = new Socket("localhost", port);
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("GET /dummy HTTP/1.1");
        pw.flush();
        socket.close();

        // close everything
        sc.close();
        webServer.close();
    }
}