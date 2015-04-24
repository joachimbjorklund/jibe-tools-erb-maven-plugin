package jibe.tools.maven.plugin.erb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "run", requiresProject = false)
public class ErbMojo extends AbstractMojo {

    private static String rbInit = ""
            + "require 'erb'\n"
            + "require 'ostruct'\n"
            + "require 'java'\n"
            + "\n"
            + "def render(template, variables)\n"
            + "  context = OpenStruct.new(variables).instance_eval do\n"
            + "    variables.each do |k, v|\n"
            + "      instance_variable_set(k, v) if k[0] == '@'\n"
            + "    end\n"
            + "    binding\n"
            + "  end\n"
            + "  ERB.new(template.to_io.read).result(context);\n"
            + "end\n";

    @Parameter(name = "templateFile", required = true)
    private File templateFile;

    @Parameter(name = "propertiesFile", required = false)
    private File propertiesFile;

    @Parameter(name = "properties", required = false)
    private Map<String, String> properties;

    @Parameter(name = "outputFile", required = false)
    private File outputFile;

    private InputStream templateInputStream;
    private OutputStream resultOutputStream;
    private ScriptEngine jruby;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Map rbVariables = new HashMap<>();
        for (Map.Entry e : properties().entrySet()) {
            rbVariables.put("@" + e.getKey(), e.getValue());
        }

        try {
            String result = jruby().invokeFunction("render", templateInputStream(), rbVariables).toString();

            OutputStream outputStream = resultOutputStream();
            outputStream.write(result.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private Invocable jruby() {
        if (jruby == null) {
            jruby = new ScriptEngineManager().getEngineByName("jruby");
            try {
                jruby.eval(rbInit);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }
        return (Invocable) jruby;
    }

    private InputStream templateInputStream() {
        if (templateInputStream != null) {
            return templateInputStream;
        }

        try {
            return templateInputStream = new FileInputStream(templateFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties properties() {
        Properties answer = new Properties();
        if (propertiesFile != null) {
            try {
                answer.load(new FileInputStream(propertiesFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (properties != null) {
            for (Map.Entry e : properties.entrySet()) {
                answer.put(e.getKey(), e.getValue());
            }
        }

        return answer;
    }

    private OutputStream resultOutputStream() {
        if (resultOutputStream != null) {
            return resultOutputStream;
        }

        try {
            return resultOutputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    Map<String, String> getProperties() {
        return properties;
    }

    void setProperties(Map<String, String> props) {
        this.properties = props;
    }

    public InputStream getTemplateInputStream() {
        return templateInputStream;
    }

    void setTemplateInputStream(InputStream testTemplateInputStream) {
        this.templateInputStream = testTemplateInputStream;
    }

    public OutputStream getResultOutputStream() {
        return resultOutputStream;
    }

    void setResultOutputStream(OutputStream resultOutputStream) {
        this.resultOutputStream = resultOutputStream;
    }
}
