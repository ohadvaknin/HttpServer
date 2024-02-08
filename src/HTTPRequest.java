import java.util.HashMap;

public class HTTPRequest {
    private String type;
    private String requestedPage;
    private boolean isImage;
    private int contentLength;
    private String referer;
    private String userAgent;
    private HashMap<String, String> parameters;

    public HTTPRequest(String requestHeader) {
        // Parse the request header
        // Assuming requestHeader format: "GET /index.html HTTP/1.1\r\nContent-Length: 100\r\nReferer: example.com\r\nUser-Agent: Mozilla\r\n\r\n"

        String[] lines = requestHeader.split("\r\n");

        // Extract type and requested page
        String[] requestLine = lines[0].split(" ");
        this.type = requestLine[0];
        this.requestedPage = requestLine[1];

        // Check if requested page is an image
        if (this.requestedPage.endsWith(".jpg") || this.requestedPage.endsWith(".bmp") || this.requestedPage.endsWith(".gif")) {
            this.isImage = true;
        } else {
            this.isImage = false;
        }

        // Extract content length
        for (String line : lines) {
            if (line.startsWith("Content-Length:")) {
                this.contentLength = Integer.parseInt(line.split(": ")[1]);
                break;
            }
        }

        // Extract referer
        for (String line : lines) {
            if (line.startsWith("Referer:")) {
                this.referer = line.split(": ")[1];
                break;
            }
        }

        // Extract user agent
        for (String line : lines) {
            if (line.startsWith("User-Agent:")) {
                this.userAgent = line.split(": ")[1];
                break;
            }
        }

        // Extract parameters
        this.parameters = new HashMap<>();
        if (this.type.equals("POST")) {
            // Assuming parameters are in the body of the POST request
            String requestBody = lines[lines.length - 1];
            String[] params = requestBody.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                this.parameters.put(keyValue[0], keyValue[1]);
            }
        }
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getRequestedPage() {
        return requestedPage;
    }

    public boolean isImage() {
        return isImage;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getReferer() {
        return referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public static void main(String[] args) {
        String requestHeader = "GET /index.html HTTP/1.1\r\nContent-Length: 100\r\nReferer: example.com\r\nUser-Agent: Mozilla\r\n\r\n";
        HTTPRequest httpRequest = new HTTPRequest(requestHeader);
        System.out.println("Type: " + httpRequest.getType());
        System.out.println("Requested Page: " + httpRequest.getRequestedPage());
        System.out.println("Is Image: " + httpRequest.isImage());
        System.out.println("Content Length: " + httpRequest.getContentLength());
        System.out.println("Referer: " + httpRequest.getReferer());
        System.out.println("User Agent: " + httpRequest.getUserAgent());
    }
}
