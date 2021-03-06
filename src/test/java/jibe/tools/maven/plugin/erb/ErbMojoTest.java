package jibe.tools.maven.plugin.erb;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 */
public class ErbMojoTest {
    private ErbMojo erbMojo;

    @Before
    public void before() {
        erbMojo = new ErbMojo();
        erbMojo.setResultOutputStream(new ByteArrayOutputStream());
        erbMojo.setTemplateInputStream(getTestTemplate());

        Properties properties = new Properties();
        properties.put("foo", "bar");
        erbMojo.setErbVariables(properties);
    }

    @Test
    public void testMojoSimple() throws Exception {
        erbMojo.execute();
        String s = new String(((ByteArrayOutputStream) erbMojo.getResultOutputStream()).toByteArray());
        Assert.assertEquals("foo = bar", s);
    }

    @Test(expected = MojoExecutionException.class)
    public void testMojoMissingProp() throws Exception {
        erbMojo.getErbVariables().remove("foo");
        erbMojo.execute();
    }

    private InputStream getTestTemplate() {
        return new ByteArrayInputStream("foo = <%= defined?(@foo) ? @foo :  raise('\\'foo\\' is not defined') %>".getBytes());
    }
}
