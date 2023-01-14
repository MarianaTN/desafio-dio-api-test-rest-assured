import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.Entities.Booking;
import org.example.Entities.BookingDates;
import org.example.Entities.User;
import org.junit.Before;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

import static io.restassured.RestAssured.given;

public class BookingTest {
    public static Faker faker;
    private static RequestSpecification request;
    private static RequestSpecification resposta;
    private static Cookie cookie;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;

    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

        bookingDates = new BookingDates("2018-01-02", "2018-01-07");
        booking = new Booking(user.getFirstName(), user.getLastName(),
                (float)faker.number().randomDouble(2, 50, 100000),
                true,bookingDates,
                "");
        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic("admin", "password123");
    }

    @Test
    public void getAllBookingsById_returnOk(){
        Response response = request
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    public void getBookingsById_returnOk(){
        Response response = request
                .when()
                .get("/booking/1")
                .then()
                .extract()
                .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    public void getBookingsByCheckingCheckout_returnOk(){
        Response response = request
                .param("checkin", bookingDates.getCheckin())
                .param("checkout", bookingDates.getCheckout())
                .when()
                .get("/booking/")
                .then()
                .extract()
                .response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    public void  getAllBookingsByUserFirstName_BookingExists_returnOk(){
        request
                .when()
                .queryParam("firstName", "Mariana")
                .get("/booking")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .body("results", hasSize(greaterThan(0)));

    }

    @Test
    public void  CreateBooking_WithValidData_returnOk(){
        Booking test = booking;
        given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .when()
                .body(booking)
                .post("/booking")
                .then()
                .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(2000L));
    }

    @Test
    public void  UpdateBooking_WithValidData_returnOk(){
        RestAssured.given()
            .basePath("booking/1")
            .contentType(ContentType.JSON)
            .accept("application/json")
            .auth().preemptive().basic("admin", "password123")
            .body("{\n" +
                    "\"firstname\" : \"Mariana\",\n" +
                    "\"lastname\" : \"Brown\",\n" +
                    "\"totalprice\" : 131,\n" +
                    "\"depositpaid\" : true,\n" +
                    "\"bookingdates\" : {\n" +
                    "    \"checkin\" : \"2019-01-01\",\n" +
                    "    \"checkout\" : \"2020-01-01\"\n" +
                    "},\n" +
                    "\"additionalneeds\" : \"Britton - To My Younger Self\"\n" +
                    "}")
            .when().put()
            .then().statusCode(200);
    }
    @Test
    public void  UpdateBooking_WithValidData_returnOk_v2(){
        Booking test = booking;
        booking.setFirstname("Mariana");
        booking.setAdditionalneeds("Britton - To My Younger Self");
        booking.setTotalprice(136);
        RestAssured.given()
                .basePath("booking/1")
                .contentType(ContentType.JSON)
                .accept("application/json")
                .auth().preemptive().basic("admin", "password123")
                .body(booking)
                .when().put()
                .then().assertThat().statusCode(200);
    }
    @Test
    public void  patchBooking_WithValidData_returnOk(){
        RestAssured.given()
                .basePath("/booking/1")
                .contentType(ContentType.JSON)
                .accept("application/json")
                .auth().preemptive().basic("admin", "password123")
                .body("{\n" +
                        "\"firstname\" : \"Mariana\",\n" +
                        "\"lastname\" : \"Brown\",\n" +
                        "\"totalprice\" : 131,\n" +
                        "\"depositpaid\" : true,\n" +
                        "\"bookingdates\" : {\n" +
                        "    \"checkin\" : \"2012-01-01\",\n" +
                        "    \"checkout\" : \"2020-11-01\"\n" +
                        "},\n" +
                        "\"additionalneeds\" : \"Hymn for Her - Ames\"\n" +
                        "}")
                .when().patch()
                .then().statusCode(200);
    }

    @Test
    public void  patchBooking_WithValidData_returnOk_v2(){
        Booking test = booking;
        bookingDates = new BookingDates("2018-01-02", "2020-09-10");
        booking.setFirstname("Mariana");
        booking.setBookingdates(bookingDates);
        booking.setAdditionalneeds("Hymn for Her - Ames");
        booking.setTotalprice(134);
        RestAssured.given()
                .basePath("/booking/1")
                .contentType(ContentType.JSON)
                .accept("application/json")
                .auth().preemptive().basic("admin", "password123")
                .body(booking)
                .when().patch()
                .then().assertThat().statusCode(200);
    }
    @Test
    public void deleteBooking_returnOK(){
        RestAssured.given()
                .basePath("/booking/1")
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("admin", "password123")
                //.cookie("token", "a5729e81d56afe8")
                .when().delete()
                .then().assertThat().statusCode(201).log().all();
    }

    @Test
    public void getPing_returnOk(){
        Response response = request
                .when()
                .get("/ping")
                .then()
                .extract()
                .response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(201, response.statusCode());
    }

}
