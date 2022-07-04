package org.acme;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MyRemoteService {

    private final ConcurrentHashMap<String, MyRemoteServiceDefinition> gateways = new ConcurrentHashMap<>();

    public String getZen() {
        MyRemoteServiceDefinition gateway = getRemoteService("github");
        MultivaluedMap<String, String> request = new MultivaluedHashMap<>();
        request.add("hello", "service");

        return gateway.getZen(request);
    }

    private MyRemoteServiceDefinition getRemoteService(String serviceParam) {
        return gateways.computeIfAbsent(serviceParam, param -> {
            try {
                String url = ConfigProvider.getConfig().getOptionalValue("service." + serviceParam + ".url", String.class).orElse("https://reqbin.com");
                String key = ConfigProvider.getConfig().getOptionalValue("service." + serviceParam + ".key", String.class).orElse("");
                String secret = ConfigProvider.getConfig().getOptionalValue("service." + serviceParam + ".service", String.class).orElse(null);

                return RestClientBuilder.newBuilder()
                        .baseUrl(new URL(url))
                        .register(new MyRemoteServiceAuthFilter(key, secret))
                        .build(MyRemoteServiceDefinition.class);
            } catch (MalformedURLException e) {
                throw new InternalServerErrorException("Failed to parse url", e);
            }
        });
    }


}
