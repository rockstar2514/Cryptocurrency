
import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.file.Paths;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class Client {
     public static void main(String[] args) throws IOException, InterruptedException {
    	 Scanner sc=new Scanner(System.in);
    	 System.out.println("Enter Main URL");
    	 String url=sc.nextLine();
//    	 HttpClient client = HttpClient.newBuilder()
//    		        .version(Version.HTTP_1_1)
//    		        .followRedirects(Redirect.NORMAL)
//    		        .connectTimeout(Duration.ofSeconds(20))
//    		        .authenticator(Authenticator.getDefault())
//    		        .build();
//    	  HttpRequest request = HttpRequest.newBuilder()
//    		         .uri(URI.create("http://foo.com/"))
//    		         .build();
//    	 HttpClient client = HttpClient.newHttpClient();
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(url+"//add"))
//                 .build();
//
//         HttpResponse<String> response = client.send(request,
//                 HttpResponse.BodyHandlers.ofString());
//
//         System.out.println(response.body());
    	  HttpClient client = HttpClient.newHttpClient();
          HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(url+"add"))
                  .POST(HttpRequest.BodyPublishers.ofFile(Paths.get("json1.json")))
                  .build();

          HttpResponse<String> response = client.send(request,
                  HttpResponse.BodyHandlers.ofString());

          System.out.println(response.body());
          
        // HttpClient client2 = HttpClient.newHttpClient();
         HttpRequest request2 = HttpRequest.newBuilder()
                 .uri(URI.create(url+"list"))
                 .build();

         HttpResponse<String> response2 = client.send(request2,
                 HttpResponse.BodyHandlers.ofString());

         System.out.println(response2.body());
    	 
     }
}
