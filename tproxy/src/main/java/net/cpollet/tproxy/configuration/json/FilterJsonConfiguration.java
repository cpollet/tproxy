package net.cpollet.tproxy.configuration.json;

import com.google.gson.JsonObject;
import net.cpollet.tproxy.configuration.FilterConfiguration;
import net.cpollet.tproxy.filters.Filter;

import javax.validation.constraints.NotNull;

/**
 * @author Christophe Pollet
 */
public class FilterJsonConfiguration implements FilterConfiguration {
    @NotNull
    private String filter;
    private JsonObject configuration;

    private Filter filterInstance;

    @Override
    public Filter filter() throws Exception {
        return filterInstance;
    }

    @SuppressWarnings("unchecked")
    public void initialize() throws Exception {
        Class filterJsonConfigurationClass = Class.forName(filter + "JsonConfiguration");
        Class filterConfigurationClass = Class.forName(filter + "Configuration");

        Object filterConfigurationInstance = filterJsonConfigurationClass
                .getConstructor(JsonObject.class)
                .newInstance(configuration);

        filterInstance = (Filter) Class.forName(filter)
                .getConstructor(filterConfigurationClass)
                .newInstance(filterConfigurationInstance);

        filterInstance.initialize();
    }
}
