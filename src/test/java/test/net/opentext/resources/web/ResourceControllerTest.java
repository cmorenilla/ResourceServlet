package test.net.opentext.resources.web;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

public class ResourceControllerTest {

    @Test
    @Ignore
    public void testURLencode() {
        GetMethod httpget = null;
        try {
            // http://localhost:8088/ResourceServlet/resources/getResourceThumbnail?objectID=3417585
            HttpClient client = new HttpClient();
            String relativePath = "/ResourceServlet/resources/getResourceThumbnail?objectID=3417585";
            //String cleanRelativePath = clearRelativePath(relativePath);
            StringBuffer urlHostPort = new StringBuffer("http://");
            urlHostPort.append("localhost");
            urlHostPort.append(":");
            urlHostPort.append("8088");

            //httpget = new GetMethod(URLEncoder.encode(urlexample));
            httpget = new GetMethod(urlHostPort.toString()
                    + relativePath);
            client.executeMethod(httpget);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(httpget);
    }

    private String clearRelativePath(String path) throws UnsupportedEncodingException {
        StringBuffer finalPath = new StringBuffer();
        //finalPath.append(path);
        String arrName[] = path.split("/");
        finalPath.append(arrName[0] + "/");
        finalPath.append(URLEncoder.encode(arrName[1], "UTF-8"));
        
        System.out.println("Final Relative Path:" + finalPath.toString());
        return finalPath.toString();
    }

}
