import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



class TCPServerMultithreaded {
    public static void main(String[] argv) throws Exception {
        Properties prop = new Properties();
        FileInputStream input = null;

        try {
            input = new FileInputStream("/Users/ohadvaknin/config.ini");
            prop.load(input);

            // Reading values from the config.ini file
            int port = Integer.parseInt(prop.getProperty("port"));
            String root = prop.getProperty("root");
            String defaultPage = prop.getProperty("defaultPage");
            int maxThreads = Integer.parseInt(prop.getProperty("maxThreads"));

            ServerSocket welcomeSocket = new ServerSocket(port);  // bind + listen
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    
            while (true) {
                Socket clientSocket = welcomeSocket.accept();
                Runnable worker = new EchoRunnable(clientSocket, root, defaultPage);
    
                executor.execute(worker);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       
        // executor.shutdown();
    }
}