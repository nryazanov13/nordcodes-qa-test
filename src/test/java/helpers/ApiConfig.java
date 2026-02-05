package helpers;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:config/api.properties"
})
public interface ApiConfig extends Config {
    @Key("api.key")
    @DefaultValue("qazWSXedc")
    String apiKey();

    @Key("base.url")
    @DefaultValue("http://localhost:8080")
    String baseUrl();

}
