package org.matthenry87.httpinterfaceformfieldscodecbug;

import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.FormHttpMessageWriter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@SpringBootApplication
public class HttpInterfaceFormFieldsCodecBugApplication {

    public static void main(String[] args) {

        SpringApplication.run(HttpInterfaceFormFieldsCodecBugApplication.class, args);
    }

    @Bean
    ServiceClient serviceClient(WebClient webClient) {

        var httpServiceProxyFactory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();

        return httpServiceProxyFactory.createClient(ServiceClient.class);
    }

    @Bean
    WebClient webClient(WebClient.Builder webClient) {

        return webClient
                .baseUrl("http://localhost:7584")
                .codecs(configurer -> configurer.customCodecs().register(new FormFieldsCustomizer()))
                .build();
    }

    interface ServiceClient {

        @PostExchange(url = "/foo", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        void makeCall(@RequestParam MultiValueMap<String, String> formFields);

    }

    static class FormFieldsCustomizer extends FormHttpMessageWriter {

        @Override
        public Mono<Void> write(Publisher<? extends MultiValueMap<String, String>> inputStream,
                                ResolvableType elementType, MediaType mediaType, ReactiveHttpOutputMessage message,
                                Map<String, Object> hints) {

            Mono<? extends MultiValueMap<String, String>> updatedFormFields = Mono.from(inputStream).flatMap(form -> {

                form.put("foo", Collections.singletonList("bar"));

                return Mono.just(form);
            });

            return super.write(updatedFormFields, elementType, mediaType, message, hints);
        }

    }

}
