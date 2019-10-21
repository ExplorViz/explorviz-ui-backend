package net.explorviz.security.server.providers;

import com.github.jasminb.jsonapi.ResourceConverter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import net.explorviz.shared.security.model.User;
import xyz.morphia.Datastore;

public class UserJsonApiDeserializer implements MessageBodyReader<User> {

  private final ResourceConverter converter;
  private final Datastore datastore;

  @Inject
  public UserJsonApiDeserializer(final ResourceConverter converter, final Datastore datastore) {
    this.converter = converter;
    this.datastore = datastore;
  }

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType,
      final Annotation[] annotations, final MediaType mediaType) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public User readFrom(final Class<User> type, final Type genericType,
      final Annotation[] annotations, final MediaType mediaType,
      final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream)
      throws IOException, WebApplicationException {

    final User user = this.converter.readDocument(entityStream, type).get();
    return user;
  }

}
