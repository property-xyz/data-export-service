package xyz.property.data.search.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;


@QuarkusTest
class SearchResourceTest {

    @Test
    void shouldFindAvailableProperties() {
        given()
                .get("/search/available")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Content-Type", Matchers.is("text/event-stream"))
                .header("X-SSE-Content-Type", Matchers.is(MediaType.APPLICATION_JSON));
    }


}