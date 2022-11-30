package org.matthenry87.httpinterfaceformfieldscodecbug;

import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.matthenry87.httpinterfaceformfieldscodecbug.HttpInterfaceFormFieldsCodecBugApplication.ServiceClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor
class HttpInterfaceFormFieldsCodecBugApplicationTest {

    private final WebClient webClient;
    private final ServiceClient serviceClient;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {

        mockWebServer = new MockWebServer();
        mockWebServer.start(7584);
    }

    @AfterAll
    static void after() throws IOException {

        mockWebServer.shutdown();
    }

    @Nested
    class WebClientTest {

        @Test
        void addsAdditionalFormField_whenSendingRequest() throws InterruptedException {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("response")
                    .setHeader("Content-Type", "application/text"));

            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            requestParams.add("moo", "fee");

            // Act
            webClient
                    .post()
                    .uri("/foo")
                    .body(BodyInserters.fromFormData(requestParams))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Assert
            var request = mockWebServer.takeRequest();

            assertThat(request.getBody().readUtf8()).isEqualTo("moo=fee&foo=bar");
        }

    }

    @Nested
    class InterfaceClientTest {

        @Test
        void addsAdditionalFormField_whenSendingRequest() throws InterruptedException {
            // Arrange
            mockWebServer.enqueue(new MockResponse().setBody("response")
                    .setHeader("Content-Type", "application/text"));

            MultiValueMap<String, String> formFields = new LinkedMultiValueMap<>();
            formFields.add("moo", "fee");

            // Act
            serviceClient.makeCall(formFields);

            // Assert
            var request = mockWebServer.takeRequest();

            assertThat(request.getBody().readUtf8()).isEqualTo("moo=fee&foo=bar");
        }

    }

}
