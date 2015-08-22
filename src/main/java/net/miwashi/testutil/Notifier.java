package net.miwashi.testutil;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.google.gson.Gson;

public class Notifier implements TestListener {
	
	protected String statServer = "localhost";
    protected int statServerPort = 6500;

    private UUID uuid = UUID.randomUUID();

    public Notifier(){
    	super();
    }
    
    public Notifier(String host, int port){
    	super();
    	statServer = host;
    	statServerPort = port;
    }
    
    public Notifier(String host){
    	super();
    	statServer = host;
    }
    
    public Notifier(int port){
    	super();
    	statServerPort = port;
    }
    
    @Override
    public void beforeSuite(TestDescriptor testDescriptor) {
    }

    @Override
    public void afterSuite(TestDescriptor testDescriptor, TestResult testResult) {
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        String msg = toJsone(testDescriptor.getClassName() + "." + testDescriptor.getName(), "1",uuid.toString(), "STARTED");
        sendByUDP(msg);
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult testResult) {
        String msg = toJsone(testDescriptor.getClassName() + "." + testDescriptor.getName(), "2",uuid.toString(), testResult.getResultType().toString());
        sendByUDP(msg);
    }

    public String toJsone(String name, String type, String uuid, String result) {
        long timeStamp = DateTime.now().getMillis() ;
        String browser = System.getProperty("browser","any");
        String version = System.getProperty("version","any");
        String platform = System.getProperty("platform","any");
        String size = System.getProperty("size","any");
        String host = System.getProperty("host","default");
        String grid = System.getProperty("grid","default");
        String user = System.getProperty("user.name");

        Map<String, String> env = System.getenv();
        String buildNumber = env.get("BUILD_NUMBER");
        String buildId = env.get("BUILD_ID");
        String buildUrl = env.get("BUILD_URL");
        String jobName = env.get("JOB_NAME");
        String nodeName = env.get("NODE_NAME");
        String buildTag = env.get("BUILD_TAG");
        String jenkinsUrl = env.get("JENKINS_URL");

        String gitCommit = env.get("GIT_COMMIT");
        String gitURL = env.get("GIT_URL");
        String gitBranch = env.get("GIT_BRANCH");

        Map<String,String> status = new HashMap<String, String>();
        status.put("type", type);
        status.put("uuid", uuid);
        status.put("name",name);
        status.put("timeStamp","" + timeStamp);
        status.put("status",result);
        status.put("browser",browser);
        status.put("version",version);
        status.put("platform",platform);
        status.put("size",size);
        status.put("host",host);
        status.put("grid",grid);
        status.put("user",user);
        status.put("buildId",buildId);
        status.put("buildNumber",buildNumber);
        status.put("buildUrl",buildUrl);
        status.put("jenkinsUrl",jenkinsUrl);
        status.put("nodeName",nodeName);
        status.put("buildTag",buildTag);
        status.put("jobName",jobName);
        status.put("gitCommit",gitCommit);
        status.put("gitBranch",gitBranch);
        status.put("gitURL",gitURL);

        String json = new Gson().toJson(status);
        return json;
    }

    protected void sendByUDP(String msg) {
        try {
            DatagramSocket sock = new DatagramSocket();
            InetAddress addr = InetAddress.getByName(statServer);
            byte[] message = (msg + "\n").getBytes();
            System.out.println(new String(message));
            DatagramPacket packet = new DatagramPacket(message, message.length, addr, statServerPort);
            sock.send(packet);
            sock.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
