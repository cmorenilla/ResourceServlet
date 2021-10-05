package net.opentext.resource.repositories.jdbc;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import oracle.sql.CLOB;

import org.apache.log4j.Logger;

import net.opentext.resource.model.PhysicalResource;
import net.opentext.resource.repositories.PhysicalResourceRepository;

public class OracleJdbcPhysicalRepository implements PhysicalResourceRepository {

    private static Logger logger = Logger.getLogger(OracleJdbcPhysicalRepository.class);

    public static final int UNKNOWN = -1;
    public static final int EXISTS_CONTEXT = 1;
    public static final int NOT_EXISTS = 2;

    public static final String RESORCE_BUNDLE_CONTEXT_DATA_FIELD_KEY = "CONTEXT_DATA_FIELD_KEY";

    public static final String LOAD_CONTEXT_ERROR_MSG = "No se pudo recuperar el contexto.";
    public static final String SAVE_CONTEXT_ERROR_MSG = "No se pudo guardar el contexto.";
    public static final String LOOKUP_CONTEXT_ERROR_MSG = "No se pudo acceder a la tabla de contextos.";
    public static final String DELETE_CONTEXT_ERROR_MSG = "Error al borrar el contexto del proceso.";

    private static final int IMAGE_MAX_WIDTH = 800;
    private static final int IMAGE_MAX_HEIGHT = 700;
    private static final String IMAGE_OUTPUT_FORMAT = "png";

    public OracleJdbcPhysicalRepository() {
    }

    private static final String querySelectBlock = " select d.dataid, d.versionnum, d.name, v.datasize, v.mimetype, v.filetype contentType, d.parentid, k.inivalue fs, p.providerdata fichero "
            + "from dtree d "
            + "inner join dversdata v on v.docid=d.dataid and v.version = d.versionnum "
            + "inner join providerdata p on(p.providerid=v.providerid) "
            + "inner join kini k on(k.inikeyword=p.providertype and inisection='Livelink.LogicalProviders') "
            + "where d.dataid=? "
            + " and v.vertype is null";

    private static final String querySelectBlockThumbnail = " select d.dataid, d.versionnum, d.name, v.datasize, v.mimetype, v.filetype contentType, d.parentid, k.inivalue fs, p.providerdata fichero "
            + "from dtree d "
            + "inner join dversdata v on v.docid=d.dataid and v.version = d.versionnum "
            + "inner join providerdata p on(p.providerid=v.providerid) "
            + "inner join kini k on(k.inikeyword=p.providertype and inisection='Livelink.LogicalProviders') "
            + " where d.dataid=? "
            + " and v.vertype = 'otthumb'";

    protected static DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource aDataSource) {
        dataSource = aDataSource;
    }

    protected void commit(Connection connection, PreparedStatement statement) {
        try {
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error at commit: " + e.getMessage(), e);
            rollbackAndClose(connection, statement);
        }
    }

    protected void close(Connection connection, PreparedStatement statement) {

        try {
            if (statement != null && !statement.getConnection().isClosed()) {
                statement.close();
                statement = null;
            }
        } catch (SQLException stmtSqle) {
            logger.error("Error closing cursor: " + stmtSqle.getMessage(), stmtSqle);
            try {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException connSqle) {
                logger.error("Error closing connection: " + connSqle.getMessage(), connSqle);
            }
        }
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException connSqle) {
            logger.error("Error closing connection: " + connSqle.getMessage(), connSqle);
        }

    }

    private void rollbackAndClose(Connection connection, PreparedStatement statement) {

        try {
            if (statement != null && !statement.getConnection().isClosed()) {
                statement.close();
                statement = null;
            }
        } catch (SQLException stmtSqle) {
            logger.error("Error closing cursor: " + stmtSqle.getMessage(), stmtSqle);
            try {
                if (connection != null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Closing connection ...");
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("... connection at rollback ... ");
                    }
                    connection.rollback();
                    if (logger.isInfoEnabled()) {
                        logger.info("... connection rolledback succesfully.");
                        logger.info("... closing connection ... ");
                    }
                    connection.close();
                    connection = null;
                    if (logger.isInfoEnabled()) {
                        logger.info("... connection closed succesfully.");
                    }
                }
            } catch (SQLException connSqle) {
                logger.error("Error closing connection: " + connSqle.getMessage(), connSqle);
            }
        }
        try {
            if (connection != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Closing connection ...");
                }
                if (logger.isInfoEnabled()) {
                    logger.info("... connection at rollback ... ");
                }
                connection.rollback();
                if (logger.isInfoEnabled()) {
                    logger.info("... connection rolledback succesfully.");
                    logger.info("... closing connection ... ");
                }
                connection.close();
                if (logger.isInfoEnabled()) {
                    logger.info("... connection closed succesfully.");
                }
            }
        } catch (SQLException connSqle) {
            logger.error("Error closing connection: " + connSqle.getMessage(), connSqle);
        }
    }

    @Override
    public PhysicalResource findThumbnailById(String idObject) throws SQLException, FileNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        PhysicalResource physicalResource = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(querySelectBlockThumbnail);
            statement.setString(1, idObject);
            ResultSet resultSet = statement.executeQuery();

            physicalResource = createPhysicalResource(physicalResource, resultSet);
        } finally {
            commit(connection, statement);
            close(connection, statement);
        }
        return physicalResource;
    }

    @Override
    public PhysicalResource findContentById(String idObject) throws SQLException, FileNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        PhysicalResource physicalResource = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(querySelectBlock);
            statement.setString(1, idObject);

            //System.out.println("statement:"+statement.toString());
            ResultSet resultSet = statement.executeQuery();
            //System.out.println("resultSet:"+resultSet.toString());
            physicalResource = createPhysicalResource(physicalResource, resultSet);
        } finally {
            commit(connection, statement);
            close(connection, statement);
        }
        return physicalResource;
    }

    

    private PhysicalResource createPhysicalResource(PhysicalResource physicalResource, ResultSet resultSet) throws SQLException, FileNotFoundException {
        if (resultSet.next()) {
            physicalResource = new PhysicalResource();
            int dataid = resultSet.getInt("dataid");
            int versionnum = resultSet.getInt("versionnum");
            physicalResource.setId(dataid, versionnum);
            physicalResource.setFileName(resultSet.getString("name"));
            physicalResource.setFileLength(resultSet.getInt("datasize"));
            physicalResource.setMimeType(resultSet.getString("mimetype"));
            physicalResource.setContentType(resultSet.getString("contentType"));
            physicalResource.setParentId(resultSet.getInt("parentid"));
            CLOB fs = (CLOB) resultSet.getClob("fs");
            CLOB pathFile = (CLOB) resultSet.getClob("fichero");
            File file = readPathFile (physicalResource, fs.stringValue(), pathFile.stringValue());
            
            physicalResource.setInputStream(new FileInputStream(file));
        } else {
            return null;
        }
        return physicalResource;
    }
    
/*
    @Override
    public void getImageFromResource(String resourceId, OutputStream output) {
        try {
            Image image = null; // = this.getImageFromResource(resourceId);
            BufferedImage bufferedImage = toBufferedImage(image);
            ImageIO.write(bufferedImage, IMAGE_OUTPUT_FORMAT, output);

        } catch (IOException e) {
            e.printStackTrace();
            //TODO Throw Exception
        }
    }
*/    
/*
    private static BufferedImage toBufferedImage(Image src) {
        if (src instanceof BufferedImage) {
            return (BufferedImage) src;
        }
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage dest = new BufferedImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return dest;
    }
*/
    private File readPathFile(PhysicalResource physicalResource, String fs, String pathFile) throws FileNotFoundException {        
        String aux = "";
        String[] fileSystem = fs.split(",");

        physicalResource.setPhysicalPath( fileSystem[1].substring(1, fileSystem[1].length()-2) );

        physicalResource.setStorageProviderName( fileSystem[0].substring(2, fileSystem[0].length()-1) );
        
        aux = pathFile.substring(pathFile.indexOf("'providerInfo'='")+16);
        physicalResource.setProviderInfo(aux.substring(0, aux.indexOf("'")) );
        aux = pathFile.substring(pathFile.indexOf("'subProviderName'='")+19);
        physicalResource.setSubProviderName(aux.substring(0, aux.lastIndexOf("'")) );
        String path = physicalResource.getPhysicalPath() + physicalResource.getProviderInfo();
        logger.info("readPathFile:" + path);
        File file = new File(path);
        
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException ("ERROR: File not exists");
        }
        
        return file;
    }

}
