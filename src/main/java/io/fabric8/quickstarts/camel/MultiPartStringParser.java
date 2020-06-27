package io.fabric8.quickstarts.camel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

public class MultiPartStringParser implements org.apache.commons.fileupload.UploadContext {

    public static void main(String[] args) throws Exception {
        String s = new String(Files.readAllBytes(Paths.get(args[0])));
        MultiPartStringParser p = new MultiPartStringParser(s);
        for (String key : p.parameters.keySet()) {
            System.out.println(key + "=" + p.parameters.get(key));
        }
    }

    private String postBody;
    private String boundary;
    private Map<String, String> parameters = new HashMap<String, String>();

    public MultiPartStringParser(String postBody) throws Exception {
        this.postBody = postBody;
        // Sniff out the multpart boundary.
        this.boundary = postBody.substring(2, postBody.indexOf('\n')).trim();
        // Parse out the parameters.
        final FileItemFactory factory = new DiskFileItemFactory();
        FileUpload upload = new FileUpload(factory);
        List<FileItem> fileItems = upload.parseRequest(this);
        for (FileItem fileItem: fileItems) {
            if (fileItem.isFormField()){
                parameters.put(fileItem.getFieldName(), fileItem.getString());
            } // else it is an uploaded file
        }
    }

    public Map<String,String> getParameters() {
        return parameters;
    }

    // The methods below here are to implement the UploadContext interface.
    @Override
    public String getCharacterEncoding() {
        return "UTF-8"; // You should know the actual encoding.
    }

    // This is the deprecated method from RequestContext that unnecessarily
    // limits the length of the content to ~2GB by returning an int. 
    @Override
    public int getContentLength() {
        return -1; // Don't use this
    }

    @Override
    public String getContentType() {
        // Use the boundary that was sniffed out above.
        return "multipart/form-data, boundary=" + this.boundary;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(postBody.getBytes());
    }

    @Override
    public long contentLength() {
        return postBody.length();
    }
}