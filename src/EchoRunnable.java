import java.io.*;
import java.net.*;

import javax.xml.crypto.Data;
class EchoRunnable implements Runnable {
    private void handleRequest(String requestLine, DataOutputStream outToClient) throws IOException {
        HTTPRequest req = new HTTPRequest(requestLine);
        String response;
        if (req.getType().equals("GET")) {
            handleGET(req, outToClient);
        } else if (req.getType().equals("POST")) {
            outToClient.write("hey".getBytes());
        } else {
            outToClient.write("HTTP/1.1 501 Not Implemented\r\n\r\n".getBytes());
        }
    }
    private String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".ico")) {
            return "icon";
        } else {
            return "application/octet-stream";
        }
    }
    private void handleGET(HTTPRequest req, DataOutputStream outToClient) {
        String requestedPage = req.getRequestedPage();
        if (requestedPage.equals("/")) {
            requestedPage = defaultPage;
        }
        requestedPage = this.root + requestedPage.substring(1);
        File file = new File(requestedPage);
        if (file.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                String contentType = getContentType(requestedPage);
                long contentLength = file.length();
                byte[] fileContent = new byte[(int) contentLength];
                int bytesRead = fileInputStream.read(fileContent);
                if (bytesRead == contentLength) {
                    outToClient.write("HTTP/1.1 200 OK\r\n".getBytes());
                    outToClient.write(("Content-Type: " + contentType + "\r\n").getBytes());
                    outToClient.write(("Content-Length: " + contentLength + "\r\n").getBytes());
                    outToClient.write("\r\n".getBytes()); // Empty line to separate headers from content
                    outToClient.write(fileContent); // Append file content
                } else {
                    outToClient.write("HTTP/1.1 500 Internal Server Error\r\n\r\n".getBytes());
                }
            } catch (IOException e) {
            }
        } 
        else {
            try {
            outToClient.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            catch (IOException e) {
            }

        }
    }

    private String handlePOST(HTTPRequest req) {
        // You can implement handling logic for POST requests here
        // For example, processing form data, updating databases, etc.
        return "HTTP/1.1 501 Not Implemented\r\n\r\n";
    }
    private Socket clientSocket = null;
    private String root;
    private String defaultPage;

    EchoRunnable(Socket clientSocket, String root, String defaultPage) {
        this.clientSocket = clientSocket;
        this.root = root;
        this.defaultPage = defaultPage;
//        try {
//            this.clientSocket.setSoTimeout(500);
//        } catch (SocketException e) {
//        }

    }

    @Override
    public void run() {
        String clientSentence;
        try (
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            clientSentence = inFromClient.readLine();  //read
            // System.out.println("Received: " + clientSentence);
            if (clientSentence != null) {
                handleRequest(clientSentence, outToClient);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}