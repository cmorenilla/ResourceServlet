package test.net.opentext.resources.repositories;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.sql.DataSource;
import net.opentext.resource.repositories.PhysicalResourceRepository;
import net.opentext.resource.repositories.jdbc.OracleJdbcPhysicalRepository;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.Assert.assertNotNull;

public class PhysicalRepositoryTest {

    @Test
    @Ignore
    public void getTransformationTest() throws SQLException, FileNotFoundException {
        DataSource dataSource = null;
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        bds.setUrl("(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=tlcmorac4d01.cnx.ad.internal)(PORT = 1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=AD17)))");
        bds.setUsername("OTS_DE");
        bds.setPassword("OTCS_DE");
        dataSource = bds;
        OracleJdbcPhysicalRepository ojdbcr = new OracleJdbcPhysicalRepository();
        ojdbcr.setDataSource(dataSource);
        PhysicalResourceRepository repository = ojdbcr;

        String objectId = "3417359";

        repository.findThumbnailById(objectId);

        try {
            repository.findContentById(objectId);
        } catch (SQLException e) {
            System.out.println("error ->" + e);
            e.printStackTrace();
        }

        System.out.println("hila->" + repository);
        //assertNotNull(repository);
    }
    
    

}
