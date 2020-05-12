package net.explorviz.security.server.resources.test;

import static io.restassured.RestAssured.given;

import io.restassured.http.Header;
import java.io.IOException;
import java.util.Optional;
import net.explorviz.security.server.resources.test.helper.AuthorizationHelper;
import net.explorviz.security.server.resources.test.helper.StatusCodes;
import net.explorviz.security.server.resources.test.helper.UsersHelper;
import net.explorviz.security.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// CHECKSTYLE.OFF: MagicNumberCheck
// CHECKSTYLE.OFF: MultipleStringLiteralsCheck


/**
 * Tests deletion of users.
 */
public class UserDeletion {

  private static final String BASE_URI = "http://localhost:8090/v1/";


  private static String adminToken;
  private static String normieToken;

  private Header authHeaderAdmin;
  private Header authHeaderNormie;

  @BeforeAll
  public static void setUpAll() throws IOException {
    adminToken = AuthorizationHelper.getAdminToken();
    normieToken = AuthorizationHelper.getNormieToken();
  }

  @BeforeEach
  public void setUp() {
    this.authHeaderAdmin = new Header("authorization", "Bearer " + adminToken);
    this.authHeaderNormie = new Header("authorization", "Bearer " + normieToken);
  }


  @Test
  @DisplayName("Delete a User")
  public void deleteUser() {
    final Optional<User> deleteMe = UsersHelper.getInstance().createUser("deleteme", "pw", null);

    if (!deleteMe.isPresent()) {
      Assertions.fail();
    }

    given().header(this.authHeaderAdmin)
        .when()
        .delete(BASE_URI + "users/" + deleteMe.get().getId())
        .then()
        .statusCode(StatusCodes.STATUS_NO_CONTEND);

    given().header(this.authHeaderAdmin)
        .when()
        .get(BASE_URI + deleteMe.get().getId())
        .then()
        .statusCode(StatusCodes.STATUS_NOT_FOUND);
  }

  @Test
  @DisplayName("Delete a User without privileges")
  public void deleteUserAsNormie() {
    final Optional<User> deleteMe = UsersHelper.getInstance().createUser("deleteme", "pw", null);

    if (deleteMe.isEmpty()) {
      Assertions.fail();
    }

    given().header(this.authHeaderNormie)
        .when()
        .delete(BASE_URI + "users/" + deleteMe.get().getId())
        .then()
        .statusCode(StatusCodes.STATUS_UNAUTHORIZED);

    UsersHelper.getInstance().deleteUserById(deleteMe.get().getId());
  }

  @Test
  @DisplayName("Delete last admin")
  public void deleteLastAdmin() {

    // Check if there are other admins next to the default admin, and delete them
    UsersHelper.getInstance()
        .getAll()
        .stream()
        .filter(u -> u.getRoles().contains("admin"))
        .filter(u -> !u.getUsername().contentEquals("admin"))
        .map(User::getId)
        .forEach(i -> UsersHelper.getInstance().deleteUserById(i));

    final String id = AuthorizationHelper.getAdmin().getId();

    given().header(this.authHeaderAdmin)
        .when()
        .delete(BASE_URI + "users/" + id)
        .then()
        .statusCode(StatusCodes.STATUS_BAD_REQUEST);

  }

}