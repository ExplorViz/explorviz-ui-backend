package net.explorviz.shared.server.helper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class to read configuration properties. Use the {@link Config} instead.
 */
public final class PropertyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertyService.class);
  private static final String PROPERTIES_FILENAME = "explorviz.properties";
  private static final String PROPERTIES_PATH;

  private static final Properties PROP = new Properties();

  private PropertyService() {
    // don't instantiate
  }

  static {
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    PROPERTIES_PATH = loader.getResource(PROPERTIES_FILENAME).getFile();

    try {
      PROP.load(loader.getResourceAsStream(PROPERTIES_FILENAME));
    } catch (final IOException e) {
      LOGGER.error(
          "Couldn't load properties file. Is WEB-INF/explorviz.properties a valid file?. Exception: {}", // NOCS
          e.getMessage());
    }
  }

  public static int getIntegerProperty(final String propName) {
    return Integer.valueOf(getStringProperty(propName));
  }

  public static String getStringProperty(final String propName) {
    return String.valueOf(PROP.get(propName));
  }

  public static boolean getBooleanProperty(final String propName) {
    return Boolean.valueOf(getStringProperty(propName));
  }

  /**
   * Set a boolean property inside the explorviz.properties file.
   * 
   * @param propName - Property key
   * @param value - Property value. String or string-castable object.
   * @throws FileNotFoundException - Thrown if explorviz.properties was not found.
   * @throws IOException - Thrown if cast to String did not work.
   */
  public static void setBooleanProperty(final String propName, final boolean value)
      throws FileNotFoundException, IOException {
    PROP.setProperty(propName, String.valueOf(value));

    final OutputStream out = new FileOutputStream(PROPERTIES_PATH);
    PROP.store(out, null);
  }

}
