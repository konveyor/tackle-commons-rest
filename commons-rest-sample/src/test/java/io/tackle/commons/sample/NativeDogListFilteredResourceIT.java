package io.tackle.commons.sample;

import io.quarkus.test.junit.NativeImageTest;
import org.junit.jupiter.api.Test;

@NativeImageTest
public class NativeDogListFilteredResourceIT extends DogListFilteredResourceTest {
    @Test
    public void testBusinessServiceCreateUpdateAndDeleteEndpointNative() {
        testDogCreateUpdateAndDeleteEndpoint(true);
    }
}