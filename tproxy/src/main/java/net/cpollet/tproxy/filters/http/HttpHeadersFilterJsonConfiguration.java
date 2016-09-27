package net.cpollet.tproxy.filters.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christophe Pollet
 */
@SuppressWarnings("unused") // because of reflection when loading configuration
public class HttpHeadersFilterJsonConfiguration implements HttpHeadersFilterConfiguration {
    private final JsonObject jsonObject;

    private Map<String, String> replacements;

    public HttpHeadersFilterJsonConfiguration(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public Map<String, String> replacements() {
        return replacements;
    }

    @Override
    public void initialize() {
        replacements = new HashMap<>();
        for (Map.Entry<String, JsonElement> element : jsonObject.get("replace").getAsJsonObject().entrySet()) {
            replacements.put(element.getKey(), element.getValue().getAsString());
        }
    }
}
