import java.io.*;
import java.net.*;

class EchoRunnable implements Runnable {
    private void handleRequest(String requestLine, String requestBody, DataOutputStream outToClient) throws IOException {
        HTTPRequest req = new HTTPRequest(requestLine, requestBody);
        System.out.println(requestLine);
        System.out.println(requestBody);
        if (req.getType().equals("GET")) {
            handleGETPOST(req, outToClient);
        } else if (req.getType().equals("POST")) {
            handleGETPOST(req, outToClient);
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
    private void handleGETPOST(HTTPRequest req, DataOutputStream outToClient) {
        String requestedPage = req.getRequestedPage();
        requestedPage = requestedPage.replaceAll("../", "");
        if (requestedPage.equals("/")) {
            requestedPage = defaultPage;
        }
        System.out.println("PARAMETERS:" + req.getParameters().values());
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
        StringBuilder requestHeaders = new StringBuilder();
        try (
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String clientSentence;
            // Keep reading lines until a blank line is reached, indicating the end of the request headers
            while ((clientSentence = inFromClient.readLine()) != null && !clientSentence.isEmpty()) {
                requestHeaders.append(clientSentence).append("\n");
            }
            
            int contentLength = 0;
            for (String header : requestHeaders.toString().split("\n")) {
                if (header.toLowerCase().startsWith("content-length:")) {
                    try {
                        contentLength = Integer.parseInt(header.substring("content-length:".length()).trim());
                    } catch (NumberFormatException e) {
                        // Handle malformed Content-Length header
                    }
                    break;
                }
            }
            
            String requestBody = "";
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                int bytesRead = inFromClient.read(body, 0, contentLength);
                if (bytesRead != contentLength) {
                    // Handle case where actual bytesRead doesn't match Content-Length header
                }
                requestBody = new String(body, 0, bytesRead);
            }
            if (requestHeaders.length() > 0) {
                handleRequest(requestHeaders.toString(), requestBody, outToClient);
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