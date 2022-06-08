package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class MyRemoteServiceTest {

    @Inject
    MyRemoteService myRemoteService;

    @Test
    void textZen() {
        String zen = myRemoteService.getZen();
        System.out.println(zen);
        Assertions.assertNotNull(zen);
    }
}
