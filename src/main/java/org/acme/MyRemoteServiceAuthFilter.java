package org.acme;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;

public class MyRemoteServiceAuthFilter implements ClientRequestFilter {

    private final String apiKey;
    private final String apiSecret;

    public MyRemoteServiceAuthFilter(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> requestHeaders = requestContext.getHeaders();

        if (requestContext.hasEntity() ) {
            @SuppressWarnings("unchecked")
            MultivaluedMap<String, String> request = (MultivaluedMap<String, String>) requestContext.getEntity();

            Providers providers = CDI.current().select(Providers.class).get();

            @SuppressWarnings("unchecked")
            MessageBodyWriter<Object> writer = (MessageBodyWriter<Object>) providers.getMessageBodyWriter(
                    requestContext.getEntityClass(), requestContext.getEntityType(), requestContext.getEntityAnnotations(),
                    requestContext.getMediaType());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.writeTo(request, requestContext.getEntityClass(), requestContext.getEntityType(),
                    requestContext.getEntityAnnotations(), requestContext.getMediaType(), requestHeaders, baos);
            byte[] body = baos.toByteArray();
            requestContext.setEntity(body, new Annotation[0], requestContext.getMediaType());

            URI uri = requestContext.getUri();
            requestHeaders.putSingle("Key", apiKey);
            requestHeaders.putSingle("Signature", createSignature(uri.getPath(), body));
        }
    }

    private String createSignature(String path, byte[] body) {
        return "my_service_signature_" + apiSecret;
    }
}
