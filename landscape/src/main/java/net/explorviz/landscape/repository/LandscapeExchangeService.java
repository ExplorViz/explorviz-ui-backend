package net.explorviz.landscape.repository;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.explorviz.landscape.model.landscape.Landscape;
import net.explorviz.landscape.model.store.Timestamp;
import net.explorviz.landscape.server.helper.FileSystemHelper;
import net.explorviz.shared.annotations.Config;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exchange Service for timestamps and landscapes - used by resources (REST).
 */
@Service
@Singleton
public class LandscapeExchangeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LandscapeExchangeService.class);

  private static final String EXPLORVIZ_FILE_ENDING = ".expl";

  private static Map<String, Timestamp> timestampCache = new HashMap<>();

  @SuppressWarnings("unused")
  private static Long timestamp;
  @SuppressWarnings("unused")
  private static Long activity;

  private static final String REPLAY_FOLDER =
      FileSystemHelper.getExplorVizDirectory() + File.separator + "replay";
  private static final String REPOSITORY_FOLDER =
      FileSystemHelper.getExplorVizDirectory() + File.separator;

  private final LandscapeRepositoryModel model;

  @Config("repository.useDummyMode")
  private boolean useDummyMode;

  @Inject
  public LandscapeExchangeService(final LandscapeRepositoryModel model) {
    this.model = model;
  }

  public LandscapeRepositoryModel getModel() {
    return this.model;
  }

  public Landscape getCurrentLandscape() {
    return this.model.getLastPeriodLandscape();
  }



  public List<Timestamp> getTimestampObjectsInRepo(final String folderName) {
    final File directory = new File(REPOSITORY_FOLDER + folderName);
    final File[] fList = directory.listFiles();
    final List<Timestamp> timestamps = new LinkedList<>();

    if (fList != null) {
      for (final File f : fList) {
        final String filename = f.getName();

        if (filename.endsWith(EXPLORVIZ_FILE_ENDING)) {
          // first validation check -> filename

          final String timestampAsString = filename.split("-")[0];
          final String callsAsString = filename.split("-")[1].split(EXPLORVIZ_FILE_ENDING)[0];

          Timestamp possibleTimestamp = timestampCache.get(timestampAsString + callsAsString);

          if (possibleTimestamp == null) {

            // new timestamp -> add to cache
            // and initialize ID of entity
            long timestamp;
            int calls;

            try {
              timestamp = Long.parseLong(timestampAsString);
              calls = Integer.parseInt(callsAsString);
            } catch (final NumberFormatException e) {
              continue;
            }

            possibleTimestamp = new Timestamp(timestamp, calls); // NOPMD
            possibleTimestamp.initializeId();
            timestampCache.put(timestampAsString + callsAsString, possibleTimestamp);
          }

          timestamps.add(possibleTimestamp);
        }
      }
    }
    return timestamps;
  }

  public Landscape getLandscape(final long timestamp) {
    return this.model.getLandscape(timestamp);
  }

  public Landscape getReplay(final long timestamp) {
    return this.model.getReplay(timestamp);

  }

  public void startRepository() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        new RepositoryStarter().start(LandscapeExchangeService.this.model);
      }
    }).start();
  }
}
