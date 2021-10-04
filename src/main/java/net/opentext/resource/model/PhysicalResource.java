package net.opentext.resource.model;

import java.io.InputStream;
import java.io.Serializable;

public class PhysicalResource {

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public class Id implements Serializable {

        private int versionNum;
        private int objectId;

        public int getVersionNum() {
            return versionNum;
        }

        public void setVersionNum(int versionNum) {
            this.versionNum = versionNum;
        }

        public int getObjectId() {
            return objectId;
        }

        public void setObjectId(int objectId) {
            this.objectId = objectId;
        }

        @Override
        public String toString() {
            return "Id{" + "versionNum=" + versionNum + ", objectId=" + objectId + '}';
        }
        
    }

    private Id id;
    private String contentType;
    private String providerInfo;
    private InputStream inputStream;
    private String mimeType;
    private String physicalPath;
    private String fileName;
    private int fileLength;
    private String storageProviderName;
    private String subProviderName;
    private String title;
    private int parentId;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getProviderInfo() {
        return providerInfo;
    }

    public void setProviderInfo(String providerInfo) {
        this.providerInfo = providerInfo;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPhysicalPath() {
        return physicalPath;
    }

    public void setPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public String getStorageProviderName() {
        return storageProviderName;
    }

    public void setStorageProviderName(String storageProviderName) {
        this.storageProviderName = storageProviderName;
    }

    public String getSubProviderName() {
        return subProviderName;
    }

    public void setSubProviderName(String subProviderName) {
        this.subProviderName = subProviderName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setId (Id id) {
        this.id = id;
    }
    
    
    
    public void setId (int objectId, int versionNum) {
        Id id = new Id();
        id.setObjectId(objectId);
        id.setVersionNum(versionNum);
        this.id = id;
    }
    
    public Id getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PhysicalResource{id=").append(id);
        sb.append(", contentType=").append(contentType);
        sb.append(", providerInfo=").append(providerInfo);
        sb.append(", inputStream=").append(inputStream);
        sb.append(", mimeType=").append(mimeType);
        sb.append(", physicalPath=").append(physicalPath);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileLength=").append(fileLength);
        sb.append(", storageProviderName=").append(storageProviderName);
        sb.append(", subProviderName=").append(subProviderName);
        sb.append(", title=").append(title);
        sb.append(", parentId=").append(parentId);
        sb.append('}');
        return sb.toString();
    }
    
    

}
