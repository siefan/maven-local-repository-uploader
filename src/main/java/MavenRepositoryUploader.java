import java.io.File;
import java.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class MavenRepositoryUploader 
{
    public String repository = "http://192.168.50.77:8081/nexus/service/rest/v1/components?repository=maven-releases";
    public String local_path = System.getProperty("user.home") + "/.m2/repository";
    public String username_password = "admin:admin123";

    public static void main(String[] args)
    {
        System.out.println("length of args:" + args.length);
        System.out.println("begin uploading");
        MavenRepositoryUploader uploader = new MavenRepositoryUploader();
        if (args.length == 3)
        {
            uploader.username_password = args[0];
            uploader.local_path = args[1];
            uploader.repository = args[2];
        }
        uploader.start();
        System.out.println("finish uploading");
    }   

    public void start()
    {
        File file = new File(local_path);
        find(file);
    }
    
    private void find(File file)
    {
        File[] fs = file.listFiles();
	    for (File f : fs)
	    {
	        if (f.isDirectory()) find(f);
	        if (f.isFile())
	        {
                String path = f.getAbsolutePath();    
		        
                if (path.endsWith(".pom"))
                {
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                    File dir = new File(f.getParent());
                    File[] _fs = dir.listFiles();
                    for (File _f : _fs)
                    {
                        String ap = _f.getAbsolutePath();
                        if (ap.endsWith(".pom"))
                        {
                            builder.addPart("maven2.asset1",new FileBody(new File(ap)));
                            builder.addTextBody("maven2.asset1.extension","pom");
                        }
                        else if (ap.endsWith(".jar"))
                        {
                            builder.addPart("maven2.asset2",new FileBody(new File(ap)));
                            builder.addTextBody("maven2.asset2.extension","jar");
                        }
                    }
                    HttpEntity entity = builder.build();
                    HttpPost request = new HttpPost(repository);
                    String encoding = new String(Base64.getEncoder().encode(username_password.getBytes()));
                    request.setHeader("Authorization", "Basic " + encoding);
                    request.setEntity(entity);
                    CloseableHttpClient client = HttpClientBuilder.create()
                    .build();
                    try 
                    {
                        System.out.println("dir:" + dir.getAbsolutePath());
                        client.execute(request);
                        System.out.println("-----------------");
                        client.close();
                    } 
                    catch (Exception e) 
                    {
                        System.out.println(e);
                        System.out.println("-----------------");
                    }
                    System.out.println("-----------------");

                }

	        }		    
	    }	
    }
}