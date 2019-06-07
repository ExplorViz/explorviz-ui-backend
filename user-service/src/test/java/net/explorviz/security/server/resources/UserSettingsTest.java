package net.explorviz.security.server.resources;

import static org.junit.Assert.assertEquals;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.explorviz.security.server.main.DependencyInjectionBinder;
import net.explorviz.security.services.UserCrudException;
import net.explorviz.security.services.UserService;
import net.explorviz.security.testutils.TestDatasourceFactory;
import net.explorviz.shared.security.model.User;
import net.explorviz.shared.security.model.settings.UserSettings;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.morphia.Datastore;

/**
 * Test for settings resource.
 *
 */
public class UserSettingsTest {

  @Inject
  private UserService userCrudService;

  @Inject
  private Datastore datastore;



  @Before
  public void setUp() {
    final AbstractBinder b = new DependencyInjectionBinder();
    b.bindFactory(TestDatasourceFactory.class).to(Datastore.class).in(Singleton.class).ranked(2);
    final ServiceLocator locator = ServiceLocatorUtilities.bind(b);
    locator.inject(this);
  }

  @After
  public void tearDown() {
    this.datastore.getCollection(User.class).drop();
  }

  @Test
  public void testUpdateNewSettings() throws UserCrudException {

    final User u = new User("testuser");
    u.setPassword("testPassword");


    u.getSettings().put("appVizTransparencyIntensity", 0.5);
    this.userCrudService.saveNewEntity(u);

    final User u1 = this.userCrudService.getEntityById(u.getId()).orElse(null);
    assertEquals(0.5,
        u1.getSettings().getNumericAttributes().get("appVizTransparencyIntensity"),
        0.0001);
  }



  @Test(expected = IllegalStateException.class)
  public void testUnknownSetting() {

    final User u = new User("testuser");
    u.setPassword("testPassword");


    u.getSettings().put("unknownsetting", 0.5);
    u.getSettings().validate();
  }



  @Test(expected = IllegalStateException.class)
  public void testNotInRange() {
    final UserSettings u = new UserSettings();
    // Must be between 0.1 and 0.5
    u.getNumericAttributes().put("appVizTransparencyIntensity", 0.7);
    u.validate();
  }

}
