package net.opentext.resource.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.opentext.resource.model.PhysicalResource;
import net.opentext.resource.repositories.PhysicalResourceRepository;

@Controller
@RequestMapping("/resources")
public class ResourceController {

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static Logger log = Logger.getLogger(ResourceController.class);
    PhysicalResourceRepository repository;

    static HttpClient client = new HttpClient();
    static String WCM_HOSTNAME = null;
    static String WCM_PORT = null;

    @Autowired
    public ResourceController(PhysicalResourceRepository repository) {
        this.repository = repository;
    }

    @RequestMapping("/getResourceThumbnail")
    public void getResourceThumbnail(@RequestParam("objectID") String idObject,
            HttpServletResponse response) {
        try {
            PhysicalResource resource;
            
            response.setContentType("image/jpg");
            
            resource = repository.findThumbnailById(idObject);
            repository.getImageFromResource(idObject, response.getOutputStream());
            
            doResponse(resource, response);
        } catch (Exception e) {
            try {
                log.error("Error during oracle operation: " + e, e);
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error during oracle operation:\n\n" + e.toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @RequestMapping("/getResource")
    public void getResource(@RequestParam("objectID") String idObject,
            HttpServletResponse response) {
        try {
            PhysicalResource resource;

            resource = repository.findContentById(idObject);
            doResponse(resource, response);
        } catch (Exception e) {
            try {
                log.error("Error during oracle operation: " + e, e);
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error during oracle operation:\n\n" + e.toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @RequestMapping("/getZip")
    public void getZip(
            @RequestParam(required = false, value = "idObject") String idObjects[],
            @RequestParam(required = false, value = "wcmID") String wcmIDs[],
            @RequestParam(required = false, value = "wcmResourceUrl") String wcmResourceUrls[],
            HttpServletRequest request, HttpServletResponse response) {

        if (WCM_HOSTNAME == null) {
//			WCM_HOSTNAME = request.getSession().getServletContext()
//					.getInitParameter("wcm_host");
//			WCM_PORT = request.getSession().getServletContext()
//					.getInitParameter("wcm_port");
            WCM_HOSTNAME = request.getServerName();
            WCM_PORT = String.valueOf(request.getServerPort());
        }
        File tempDir = null;

        try {
            OutputStream os = response.getOutputStream();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"DATA.ZIP\"");

            String path = request.getSession().getServletContext()
                    .getRealPath("data");

            System.out.println("Real Path:" + path);

            tempDir = createTempDir(path);

            sendZipFiles(tempDir, tempDir.list(), os);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error during oracle operation:\n\n" + e.toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }

    }

    private String clearRelativePath(String path) throws UnsupportedEncodingException {
        StringBuffer finalPath = new StringBuffer("/wps/wcm/connect/");
        //finalPath.append(path);
        String arrName[] = path.split("/");
        finalPath.append(arrName[0] + "/");
        finalPath.append(URLEncoder.encode(arrName[1], "UTF-8"));
        finalPath.append("?MOD=AJPERES");
        System.out.println("Final Relative Path:" + finalPath.toString());
        return finalPath.toString();
    }

    private void streamIt(InputStream in, OutputStream out) throws IOException {

        try {

            byte[] buf = new byte[2048];
            int count = -1;
            while ((count = in.read(buf)) != -1) {
                out.write(buf, 0, count);
            }

            out.flush();
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }

    private File createTempDir(String root) {
        File tempDir = new File(root + FILE_SEPARATOR + "tmp"
                + Thread.currentThread().getId() + "_"
                + System.currentTimeMillis());
        tempDir.mkdir();
        return tempDir;
    }

    private void doResponse(PhysicalResource resource,
            HttpServletResponse response) throws IOException {
        if (resource != null) {
            response.setContentType(resource.getMimeType());

            if (resource.getMimeType().indexOf("image") == -1) {
                if (resource.getFileName() != null && resource.getFileName().contains(".ai")) {
                    resource.setFileName(resource.getFileName().replace(".ai", ".pdf"));
                }
            }

            if (resource.getFileName() != null) {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + resource.getFileName()
                        + "\"; filename*=UTF-8''" + URLEncoder.encode(resource.getFileName(), "UTF-8").replaceAll("\\+", "_"));
            }

            response.setContentLength(resource.getFileLength());

            streamIt(resource.getInputStream(), response.getOutputStream());

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Oracle does not contains that resource.");
        }

    }

    private void sendZipFiles(File directory, String[] files, OutputStream os)
            throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        byte bytes[] = new byte[2048];

        try {
            for (String fileName : files) {
                FileInputStream fis = new FileInputStream(directory.getPath()
                        + FILE_SEPARATOR + fileName);
                BufferedInputStream bis = new BufferedInputStream(fis);

                zos.putNextEntry(new ZipEntry(fileName));

                int bytesRead;
                while ((bytesRead = bis.read(bytes)) != -1) {
                    zos.write(bytes, 0, bytesRead);
                }
                zos.closeEntry();
                bis.close();
                fis.close();
            }
            zos.flush();
            os.flush();
        } finally {
            zos.close();
            os.close();
        }
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}
