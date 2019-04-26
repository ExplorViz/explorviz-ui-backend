package net.explorviz.history.repository.persistence.mongo;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import javax.inject.Inject;
import net.explorviz.history.server.main.DependencyInjectionBinder;
import net.explorviz.history.server.main.HistoryApplication;
import net.explorviz.shared.common.idgen.IdGenerator;
import net.explorviz.shared.landscape.model.landscape.Landscape;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests the {@link MongoReplayRepositoryTest}. Expects a running mongodb server.
 *
 */
public class MongoReplayRepositoryTest {

  @Inject
  private MongoReplayRepository repo;

  @Inject
  private IdGenerator idGenerator;

  @BeforeClass
  public static void setUpAll() {
    HistoryApplication.registerLandscapeModels();
  }



  /**
   * Injects depedencies.
   */
  @Before
  public void setUp() {
    if (this.repo == null) {
      final DependencyInjectionBinder binder = new DependencyInjectionBinder();
      final ServiceLocator locator = ServiceLocatorUtilities.bind(binder);
      locator.inject(this);
    }
    this.repo.clear();
  }


  @After
  public void tearDown() {
    this.repo.clear();
  }


  @Test
  public void findReplayByTimestamp() {
    final long ts = System.currentTimeMillis();
    final Landscape landscape = LandscapeDummyCreator.createDummyLandscape(idGenerator);
    this.repo.save(ts, landscape, 0);

    final Landscape landscapeRetrieved = this.repo.getByTimestamp(ts);

    assertEquals("Ids don't match", landscape.getId(), landscapeRetrieved.getId()); // NOCS
  }

  @Test
  public void findReplayById() {
    final long ts = System.currentTimeMillis();
    final Landscape landscape = LandscapeDummyCreator.createDummyLandscape(idGenerator);
    final Landscape landscape2 = LandscapeDummyCreator.createDummyLandscape(idGenerator);
    this.repo.save(ts, landscape, 0);
    this.repo.save(ts, landscape2, 0);

    final String id = landscape.getId();
    final Landscape landscapeRetrieved = this.repo.getById(id);

    assertEquals("Ids don't match", id, landscapeRetrieved.getId());

  }

  @Test
  public void testTotalRequestsReplay() {
    final Random rand = new Random();
    final long ts = System.currentTimeMillis();
    final int requests = rand.nextInt(Integer.MAX_VALUE) + 1;
    final Landscape landscape = LandscapeDummyCreator.createDummyLandscape(idGenerator);
    this.repo.save(ts, landscape, requests);

    final int retrievedRequests = this.repo.getTotalRequests(ts);
    assertEquals("Requests not matching", requests, retrievedRequests);
  }

  @Test
  public void testAllTimestamps() {
    final Landscape landscape = LandscapeDummyCreator.createDummyLandscape(idGenerator);
    final long ts = System.currentTimeMillis();
    final Landscape landscape2 = LandscapeDummyCreator.createDummyLandscape(idGenerator);
    final long ts2 = ts + 1;
    this.repo.save(ts, landscape, 0);
    this.repo.save(ts2, landscape2, 0);

    final int timestamps = this.repo.getAllTimestamps().size();
    assertEquals("Amount of objects don't match", 2, timestamps);
  }

}
