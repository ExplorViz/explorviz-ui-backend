package net.explorviz.security.server.resources.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.restassured.http.Header;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.explorviz.security.model.UserBatchRequest;
import net.explorviz.security.server.resources.test.helper.AuthorizationHelper;
import net.explorviz.security.server.resources.test.helper.JsonApiListMapper;
import net.explorviz.security.server.resources.test.helper.JsonApiMapper;
import net.explorviz.security.server.resources.test.helper.StatusCodes;
import net.explorviz.security.server.resources.test.helper.UsersHelper;
import net.explorviz.security.services.BatchService;
import net.explorviz.security.user.User;
import net.explorviz.settings.model.UserPreference;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// CHECKSTYLE.OFF: MagicNumberCheck
// CHECKSTYLE.OFF: MultipleStringLiteralsCheck


/**
 * Tests user batch requests.
 */
public class BatchRequest {

  private static final String BATCH_URL = "http://localhost:8090/v1/userbatch";
  private static final String USER_URL = "http://localhost:8090/v1/users/";
  private static final String PREF_URL =
      "http://localhost:8090/v1/preferences?filter[user]={uid}";
  private static final String MEDIA_TYPE = "application/vnd.api+json";
  private static final String PW_CHARSET = "abcdefghijklmnopqrstuvwxyz";
  private static final String ROLE_USER = "user";

  private static String adminToken;
  private Header authHeaderAdmin;




  /**
   * Retrieves token for both an admin and an unprivileged user ("normie"). The default admin is
   * used for the former, a normie is created.
   *
   * @throws IOException if serialization fails
   */
  @BeforeAll
  static void setUpAll() throws IOException {
    adminToken = AuthorizationHelper.getAdminToken();
  }

  @BeforeEach
  void setUp() {
    this.authHeaderAdmin = new Header("authorization", "Bearer " + adminToken);
  }

  @Test
  void createValid() {
    final int count = 10;
    final List<String> passwords = IntStream.range(0, count)
        .mapToObj(i -> RandomStringUtils.random(5, PW_CHARSET))
        .collect(Collectors.toList());
    final List<String> roles = new ArrayList<>(Arrays.asList(ROLE_USER));

    final UserBatchRequest ubr = new UserBatchRequest("test", count, passwords, roles, null);

    final UserBatchRequest retrieved = given().header(this.authHeaderAdmin)
        .body(ubr, new JsonApiMapper<>(UserBatchRequest.class))
        .contentType(MEDIA_TYPE)
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(StatusCodes.STATUS_OK)
        .extract()
        .body()
        .as(UserBatchRequest.class, new JsonApiMapper<>(UserBatchRequest.class));

    Assertions.assertEquals(count, retrieved.getUsers().size());


    // Delete the just created users
    retrieved.getUsers()
        .stream()
        .map(User::getId)
        .forEach(i -> UsersHelper.getInstance().deleteUserById(i));
  }

  @Test
  @SuppressWarnings("unchecked")
  void filterByBatchId() {
    final int count = 10;
    final List<String> passwords = IntStream.range(0, count)
        .mapToObj(i -> RandomStringUtils.random(5, PW_CHARSET))
        .collect(Collectors.toList());
    final List<String> roles = new ArrayList<>(Arrays.asList(ROLE_USER));

    final UserBatchRequest ubr = new UserBatchRequest("test", count, passwords, roles, null);

    // Create the batch
    final String bid = given().header(this.authHeaderAdmin)
        .body(ubr, new JsonApiMapper<>(UserBatchRequest.class))
        .contentType(MEDIA_TYPE)
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(StatusCodes.STATUS_OK)
        .extract()
        .body()
        .as(UserBatchRequest.class, new JsonApiMapper<>(UserBatchRequest.class))
        .getUsers()
        .get(0)
        .getBatchId();

    // Get all users with the batch id
    final List<User> retrieved = given().contentType(MEDIA_TYPE)
        .header(this.authHeaderAdmin)
        .params("filter[batchid]", bid)
        .when()
        .get(USER_URL)
        .then()
        .statusCode(StatusCodes.STATUS_OK)
        .body("data.size()", is(count))
        .extract()
        .body()
        .as(List.class, new JsonApiListMapper<>(User.class));

    // Delete the just created users
    retrieved.stream().map(User::getId).forEach(i -> UsersHelper.getInstance().deleteUserById(i));
  }

  @SuppressWarnings("unchecked")
  @Test
  void validWithPrefs() {
    final int count = 10;
    final List<String> passwords = IntStream.range(0, count)
        .mapToObj(i -> RandomStringUtils.random(5, PW_CHARSET))
        .collect(Collectors.toList());
    final List<String> roles = new ArrayList<>(Arrays.asList(ROLE_USER));

    final float appVizTransparencyIntensity = 0.5f;
    final boolean showFpsCounter = true;
    final Map<String, Object> prefs = new HashMap<>();
    prefs.put("showFpsCounter", showFpsCounter);
    prefs.put("appVizTransparencyIntensity", appVizTransparencyIntensity);
    final UserBatchRequest ubr = new UserBatchRequest("test", count, passwords, roles, prefs);

    // Create batch and retrieve the ids of the created users
    final List<String> retrievedUids = given().header(this.authHeaderAdmin)
        .body(ubr, new JsonApiMapper<>(UserBatchRequest.class))
        .contentType(MEDIA_TYPE)
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(StatusCodes.STATUS_OK)
        .extract()
        .body()
        .as(UserBatchRequest.class, new JsonApiMapper<>(UserBatchRequest.class))
        .getUsers()
        .stream()
        .map(User::getId)
        .collect(Collectors.toList());

    // Get Preferences for each user
    for (final String uid : retrievedUids) {
      final String url = PREF_URL.replace("{uid}", uid);

      final List<UserPreference> retrievedPrefs = given().header(this.authHeaderAdmin)
          .when()
          .get(url)
          .then()
          .statusCode(StatusCodes.STATUS_OK)
          .extract()
          .body()
          .as(List.class, new JsonApiListMapper<>(UserPreference.class));

      // Test if both prefs are present and the values are correct
      final double appVizPref = (double) retrievedPrefs.stream()
          .filter(p -> p.getSettingId().contentEquals("appVizTransparencyIntensity"))
          .findAny()
          .get()
          .getValue();

      final boolean fpsPref = (boolean) retrievedPrefs.stream()
          .filter(p -> p.getSettingId().contentEquals("showFpsCounter"))
          .findAny()
          .get()
          .getValue();

      Assertions.assertEquals(showFpsCounter, fpsPref);
      Assertions.assertEquals(appVizTransparencyIntensity, appVizPref);
    }
    // Delete the just created users
    retrievedUids.forEach(i -> UsersHelper.getInstance().deleteUserById(i));
  }


  @Test
  void invalidPasswordsLength() {
    final int count = 10;
    final List<String> passwords = IntStream.range(0, count - 1)
        .mapToObj(i -> RandomStringUtils.random(5, PW_CHARSET))
        .collect(Collectors.toList());
    final List<String> roles = new ArrayList<>(Arrays.asList(ROLE_USER));

    final UserBatchRequest ubr = new UserBatchRequest("test", count, passwords, roles, null);

    given().header(this.authHeaderAdmin)
        .body(ubr, new JsonApiMapper<>(UserBatchRequest.class))
        .contentType(MEDIA_TYPE)
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(StatusCodes.STATUS_BAD_REQUEST);

    // TODO: How to test that no users were actually created?
  }


  @Test
  void countLimit() {
    final int count = BatchService.MAX_COUNT + 1;
    final List<String> passwords = IntStream.range(0, count)
        .mapToObj(i -> RandomStringUtils.random(5, PW_CHARSET))
        .collect(Collectors.toList());
    final List<String> roles = new ArrayList<>(Arrays.asList(ROLE_USER));

    final UserBatchRequest ubr = new UserBatchRequest("test", count, passwords, roles, null);

    given().header(this.authHeaderAdmin)
        .body(ubr, new JsonApiMapper<>(UserBatchRequest.class))
        .contentType(MEDIA_TYPE)
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(StatusCodes.STATUS_BAD_REQUEST);

    // TODO: How to test that no users were actually created?
  }


  @Test
  void createExistingUser() {
    final String prefix = "test";
    final int count = 5;
    final Optional<User> u = UsersHelper.getInstance().createUser(prefix + "-3", "pass", null);
    if (!u.isPresent()) {
      Assertions.fail();
    }
    final List<String> passwords = IntStream.range(0, count)
        .mapToObj(i -> RandomStringUtils.random(5, PW_CHARSET))
        .collect(Collectors.toList());
    final List<String> roles = new ArrayList<>(Arrays.asList(ROLE_USER));

    final UserBatchRequest ubr = new UserBatchRequest("test", count, passwords, roles, null);

    given().header(this.authHeaderAdmin)
        .body(ubr, new JsonApiMapper<>(UserBatchRequest.class))
        .contentType(MEDIA_TYPE)
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(StatusCodes.STATUS_BAD_REQUEST);

    // TODO: How to test that no users were actually created?

    UsersHelper.getInstance().deleteUserById(u.get().getId());

  }



}
