import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class App {
    static HashMap<Long, String> map = new HashMap<Long, String>();
    static String peers[] = {"http://localhost:8001/add","http://localhost:8080/add"};

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/ping", new PingHandler());
        server.createContext("/add", new AddHandler());
        server.createContext("/list", new ListHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Listening on port 8000");
    }

    public static void informPeers(String data) {
        System.out.println("Informing peers");
        try{
            for(String peerUrl: peers){
                URL url = new URL(peerUrl);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setDoOutput(true);
                try(OutputStream os = con.getOutputStream()) {
                    byte[] input = data.getBytes("utf-8");
                    os.write(input, 0, input.length);           
                }
                System.out.println("Informed "+ peerUrl + " - " + con.getResponseCode());
            }
        }catch(Exception e){
            System.out.println("[Error] Error while informing peers.");
            e.printStackTrace();
        }
    }

    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "PONG";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = new JSONObject(map).toString();
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Accepted";
            try{
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(t.getRequestBody(), "UTF-8"));
                long key = (Long)jsonObject.get("key");
                String value = (String)jsonObject.get("value");
                map.put(key, value);
                informPeers(jsonObject.toString());
                t.sendResponseHeaders(200, response.length());
            }catch(Exception e){
                response = "Invalid Request";
                t.sendResponseHeaders(401, response.length());
            }
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}