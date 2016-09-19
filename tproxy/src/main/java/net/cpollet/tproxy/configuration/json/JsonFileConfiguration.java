package net.cpollet.tproxy.configuration.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cpollet.tproxy.configuration.Configuration;
import net.cpollet.tproxy.configuration.ProxyConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Christophe Pollet
 */
public class JsonFileConfiguration implements Configuration {
    private final String filePath;
    private final JsonParser jsonParser;

    private JsonProxyConfiguration proxyConfiguration;

    public JsonFileConfiguration(String filePath) {
        this.filePath = filePath;
        this.jsonParser = new JsonParser();
    }

    public static void main(String[] args) throws Exception {
        JsonFileConfiguration jsonFileConfiguration = new JsonFileConfiguration("/Users/cpollet/Development/tproxy/config/sample.json");

        jsonFileConfiguration.load();

        System.out.println("from: " + jsonFileConfiguration.proxiesConfiguration().get(0).fromHost() + ":" +
                jsonFileConfiguration.proxiesConfiguration().get(0).fromPort());
    }

    @Override
    public void load() throws Exception {
        JsonElement root = jsonParser.parse(fileContent());

        for (Map.Entry<String, JsonElement> proxy : proxies(root).entrySet()) {
            proxyConfiguration = new JsonProxyConfiguration(proxy.getKey(), proxy.getValue().getAsJsonObject());

            JsonObject outputFilters = outputFilters(proxy.getValue().getAsJsonObject());

            List<JsonConfiguration> outputFiltersConfiguration = outputFilters.entrySet().stream()
                    .map(e -> filterConfiguration(e.getKey(), e.getValue().getAsJsonObject()))
                    .collect(Collectors.toList());

            proxyConfiguration.addOutputFilters(outputFiltersConfiguration);
            proxyConfiguration.load();
        }
    }

    private String fileContent() throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private JsonObject proxies(JsonElement element) {
        return element.getAsJsonObject().get("proxies").getAsJsonObject();
    }

    private JsonObject outputFilters(JsonObject proxyConfiguration) {
        final String OUTPUT_FILTER_KEY = "outputFilters";

        if (!proxyConfiguration.has(OUTPUT_FILTER_KEY)) {
            return new JsonObject();
        }

        return proxyConfiguration.get(OUTPUT_FILTER_KEY).getAsJsonObject();
    }

    private JsonConfiguration filterConfiguration(String filterClassName, JsonObject filterConfiguration) {
        try {
            return (JsonConfiguration) Class.forName(filterClassName + "JsonConfiguration")
                    .getConstructor(JsonObject.class)
                    .newInstance(filterConfiguration);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<ProxyConfiguration> proxiesConfiguration() {
        return Collections.singletonList(proxyConfiguration);
    }
}
