package default_package;

import java.io.IOException;    
import java.io.OutputStream;   
import java.net.InetSocketAddress;   
import java.util.Queue;  
import java.util.concurrent.*;  

import com.sun.net.httpserver.HttpExchange;   
import com.sun.net.httpserver.HttpHandler;   
import com.sun.net.httpserver.HttpServer;   
public class HttpServerTest {   
    public static void main(String[] args) {  
        try {  
            //允许最大连接数  
            int backLog = 10;  
            InetSocketAddress inetSock = new InetSocketAddress(8080);  
            HttpServer httpServer = HttpServer.create(inetSock, backLog);
 
            httpServer.createContext("/answer/", new HandlerTest());
            httpServer.setExecutor(null);  
            httpServer.start();    
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}  
  
class HandlerTest implements HttpHandler{  
    private static int requestNum = 0;   
    ThreadPoolExecutor threadPoolExecutor;  
    HandlerTest(){  
         threadPoolExecutor = new  ThreadPoolExecutor(4,8, 30,   
                 TimeUnit.SECONDS,   
                 new ArrayBlockingQueue<Runnable>(4),    
                 new ThreadPoolExecutor.CallerRunsPolicy()  
                 );  
    }  
    public void handle(HttpExchange he) throws IOException {  
        // TODO Auto-generated method stub  
        if((getQueueSize(threadPoolExecutor.getQueue()))<4){  
            RequestTasks rqt = new RequestTasks(he);   
            threadPoolExecutor.execute(rqt);  
        }  
        else System.out.println("Please Wait!");  
    }  
    private synchronized int getQueueSize(Queue queue)    
    {    
        return queue.size();    
    }   
      
}  

//处理请求的任务  
class RequestTasks implements Runnable{  
  
    static int processedNum = 0;  
    HttpExchange httpExchange;  
    RequestTasks(HttpExchange he){  
        httpExchange = he;  
        processedNum++;  
    }  
    public void run() {        
        try{
        	String uri =  httpExchange.getRequestURI().toString();
        	int[] parameters = getMessage(uri);

            int[] products = getProduct(parameters);
            
            String response = reverse(products);
        	
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();     
            os.write(response.getBytes());     
            os.close();
        }catch (Exception e){  
            e.printStackTrace();  
        }  
    }  

    private int[] getMessage(String uri){
    	
        String[] str = uri.substring(11).split(",");
        int[] array = new int[str.length];
        for (int i = 0; i < str.length; i++){
            array[i] = Integer.parseInt(str[i]);
        }
        return array;
    }

    private int[] getProduct(int[] arr){
    	
    	int[] sum = new int[arr.length];
    	for(int i=0;i<arr.length;i++){
    		int product=1;
    		for(int j=0;j<arr.length;j++){
    			if(i==j){
    				continue;
    			}
    			product=product*arr[j];
    		}
    		sum[i]=product;
    	}
    	return sum;
    }  
    
    private String reverse(int[] arr){
    	
    	String answer="";
    	for(int i=0;i<arr.length;i++){
    		if(i==0){
    			answer=answer+arr[i];
    			continue;
    		}
    		answer=answer+","+arr[i];
    	}
    	return answer;
    }
}