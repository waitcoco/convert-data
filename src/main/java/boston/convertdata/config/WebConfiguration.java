package boston.convertdata.config;

import boston.convertdata.utils.GsonInstances;
import lombok.val;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.ArrayList;

@Configuration
public class WebConfiguration {
    @Bean
    public HttpMessageConverters customConverters() {
        val messageConverters = new ArrayList<HttpMessageConverter<?>>();
        val gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(GsonInstances.API);
        messageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }
}
