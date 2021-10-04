package net.opentext.resource.repositories;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.sql.SQLException;


import net.opentext.resource.model.PhysicalResource;

public interface PhysicalResourceRepository {

    abstract PhysicalResource findThumbnailById(String idbject) throws SQLException, FileNotFoundException;

    abstract PhysicalResource findContentById(String idobject) throws SQLException, FileNotFoundException;

    public void getImageFromResource(String idObject, OutputStream outputStream);

}
