package net.cpollet.tproxy.filters.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.cpollet.tproxy.configuration.json.JsonConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christophe Pollet
 */
@SuppressWarnings("unused") // because of reflection when loading configuration
public class HeadersFilterJsonConfiguration implements JsonConfiguration, HeadersFilterConfiguration {
    private final JsonObject jsonObject;

    private Map<String, String> replacements;

    public HeadersFilterJsonConfiguration(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        this.replacements = new HashMap<>();
    }

    @Override
    public void load() throws Exception {
        //noinspection unchecked
        for (Map.Entry<String, JsonElement> entry : jsonObject.get("replacements").getAsJsonObject().entrySet()) {
            replacements.put(entry.getKey(), entry.getValue().getAsString());
        }
    }

    @Override
    public String toString() {
        return "HeadersFilterJsonConfiguration{" +
                "replacements=" + replacements +
                '}';
    }
}
