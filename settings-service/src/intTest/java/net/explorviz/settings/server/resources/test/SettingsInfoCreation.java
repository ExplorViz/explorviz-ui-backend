package net.explorviz.settings.server.resources.test;

import io.restassured.http.Header;
import java.io.IOException;
import net.explorviz.settings.model.FlagSetting;
import net.explorviz.settings.model.RangeSetting;
import net.explorviz.settings.model.Setting;
import net.explorviz.settings.server.resources.test.helper.AuthorizationHelper;
import net.explorviz.settings.server.resources.test.helper.DefaultSettings;
import net.explorviz.settings.server.resources.test.helper.JsonAPIMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

class SettingsInfoCreation {

  private static final String SETTINGS_URL = "http://localhost:8087/v1/settings/info";

  private static String adminToken;
  private static String normieToken;

  private Header authHeaderAdmin;
  private Header authHeaderNormie;

  private static final String MEDIA_TYPE = "application/vnd.api+json";


  /**
   * Retrieves token for both an admin and an unprivileged user ("normie").
   * The default admin is used for the former, a normie is created.
   *
   * @throws IOException if serialization fails
   */
  @BeforeAll static void setUpAll() throws IOException {
    adminToken = AuthorizationHelper.getAdminToken();
    normieToken = AuthorizationHelper.getNormieToken();
  }

  @BeforeEach void setUp() {
    this.authHeaderAdmin = new Header("authorization", "Bearer " + adminToken);
    this.authHeaderNormie = new Header("authorization", "Bearer " + normieToken);
  }


  @Test
  void createAsAdmin(){
    Setting toCreate
        = new FlagSetting("testname", "a test setting",
          DefaultSettings.origin, false);

    Setting created = given()
      .header(authHeaderAdmin)
      .contentType(MEDIA_TYPE)
      .body(toCreate, new JsonAPIMapper<Setting>(Setting.class))
      .when()
      .post(SETTINGS_URL)
      .then()
      .statusCode(200)
      .extract().body().as(Setting.class, new JsonAPIMapper<Setting>(Setting.class));

    // Delete to not affect other tests
    deleteSetting(created.getId());
    // Workaround to check equality,otherwise 'created' has no id
    toCreate.setId(created.getId());
    Assert.assertEquals(toCreate, created);

  }

  @Test
  void createAsANormie(){
    Setting toCreate
            = new FlagSetting("testname", "a test setting",
            DefaultSettings.origin, false);
    given()
      .header(authHeaderNormie)
      .contentType(MEDIA_TYPE)
      .body(toCreate, new JsonAPIMapper<Setting>(Setting.class))
      .when()
      .post(SETTINGS_URL)
      .then()
      .statusCode(403);
  }

  private void deleteSetting(String id) {
    given()
      .header(authHeaderAdmin)
      .contentType(MEDIA_TYPE)
      .when()
      .delete(SETTINGS_URL+"/"+id);
  }


}
