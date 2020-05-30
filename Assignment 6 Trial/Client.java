
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
       if(url.charAt(url.length()-1)!='/')url=url+'/';   
    	  HttpClient client = HttpClient.newHttpClient();
         int flag=1;
         do
         {
        	 System.out.println("Enter 1 to add data and enter 2 to list data and any other button to end");
        	 int choice=Integer.parseInt(sc.nextLine());
        	 switch(choice)
        	 {
        	     case 1:
        	    	 System.out.println("Enter file directory");
        	    	 String dir=sc.nextLine();
        	    	 HttpRequest request = HttpRequest.newBuilder()
                     .uri(URI.create(url+"add"))
                     .POST(HttpRequest.BodyPublishers.ofFile(Paths.get(dir)))
                     .build();
        	    	 HttpResponse<String> response = client.send(request,
        	                  HttpResponse.BodyHandlers.ofString());
        	          System.out.println(response.body());
        	          break;
        	     case 2:
        	    	 HttpRequest request2 = HttpRequest.newBuilder()
                     .uri(URI.create(url+"list"))
                     .build();
		             HttpResponse<String> response2 = client.send(request2,
		                     HttpResponse.BodyHandlers.ofString());
		             System.out.println(response2.body());
		             break;
                  default:
                	  flag=0;
                	  break;
        	 }
         }
         while(flag==1);
     }
}
