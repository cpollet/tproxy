package net.cpollet.tproxy.configuration.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cpollet.tproxy.configuration.Configuration;
import net.cpollet.tproxy.configuration.ProxyConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * @author Christophe Pollet
 */
public class JsonConfiguration implements Configuration {
    private final String path;
    private final Gson gson;
    private final Validator validator;
    private RootJsonConfiguration rootConfiguration;

    public JsonConfiguration(String path) {
        this.path = path;
        this.gson = new GsonBuilder().create();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public void load() throws Exception {
        rootConfiguration = gson.fromJson(fileContent(), RootJsonConfiguration.class);

        Set<ConstraintViolation<RootJsonConfiguration>> validationErrors = validator.validate(rootConfiguration);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(validationErrors.toString());
        }

        rootConfiguration.initialize();
    }

    @Override
    public List<? extends ProxyConfiguration> proxies() {
        return rootConfiguration.proxies();
    }

    private String fileContent() throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
