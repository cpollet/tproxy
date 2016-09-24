package net.cpollet.tproxy.configuration.json;

import com.google.gson.JsonObject;
import net.cpollet.tproxy.configuration.ProxyConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Christophe Pollet
 */
public class JsonProxyConfiguration implements JsonConfiguration, ProxyConfiguration {
    private final JsonObject jsonObject;
    private final Validator validator;
    private final String proxyName;

    @NotNull
    @Pattern(regexp = "[a-z0-9.-]+:[0-9]+")
    private String from;

    @NotNull
    @Pattern(regexp = "[a-z0-9.-]+:[0-9]+")
    private String to;

    private List<JsonConfiguration> outputFilters;

    JsonProxyConfiguration(String proxyName, JsonObject jsonObject) {
        this.outputFilters = new ArrayList<>();
        this.proxyName = proxyName;
        this.jsonObject = jsonObject;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public void load() throws Exception {
        from = jsonObject.get("from").getAsString();
        to = jsonObject.get("to").getAsString();

        for (JsonConfiguration outputFilter : outputFilters) {
            outputFilter.load();
        }

        Set<ConstraintViolation<JsonProxyConfiguration>> validationErrors = validator.validate(this);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(validationErrors.toString());
        }
    }

    void addOutputFilters(Collection<JsonConfiguration> filterConfigurations) {
        outputFilters.addAll(filterConfigurations);
    }

    @Override
    public String fromHost() {
        return from.split(":")[0];
    }

    @Override
    public int fromPort() {
        return Integer.parseInt(from.split(":")[1]);
    }

    @Override
    public String toHost() {
        return to.split(":")[0];
    }

    @Override
    public int toPort() {
        return Integer.parseInt(to.split(":")[1]);
    }

    @Override
    public void outputFiltersConfiguration() {

    }

    @Override
    public String toString() {
        return "JsonProxyConfiguration[" + proxyName + "]: " + jsonObject + '}';
    }
}
