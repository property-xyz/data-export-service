package xyz.property.data.search.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;


@QuarkusTest
class SearchResourceTest {

    @Test
    void shouldFindAvailableProperties() {
        given()
                .get("/search/properties-for-sale")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }


}