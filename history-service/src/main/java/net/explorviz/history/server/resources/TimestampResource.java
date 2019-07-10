package net.explorviz.history.server.resources;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import net.explorviz.history.repository.persistence.mongo.TimestampRepository;
import net.explorviz.shared.landscape.model.store.Timestamp;
import net.explorviz.shared.querying.Query;
import net.explorviz.shared.querying.QueryException;
import net.explorviz.shared.querying.QueryResult;

/**
 * REST resource providing {@link net.explorviz.landscape.model.store.Timestamp} data for the
 * frontend.
 */
@Path("v1/timestamps")
@RolesAllowed({"admin"})
public class TimestampResource {

  private static final String MEDIA_TYPE = "application/vnd.api+json";

  private static final long QUERY_PARAM_DEFAULT_VALUE_LONG = 0L;

  private final TimestampRepository timestampRepo;

  @Inject
  public TimestampResource(final TimestampRepository timestampRepo) {
    this.timestampRepo = timestampRepo;
  }


  /**
   * Returns a list of either user-uploaded or service-generated
   * {@link net.explorviz.landscape.model.store.Timestamp}. The result depends on the passed query
   * parameters
   *
   * @return a filtered list of timestamps
   */
  @GET
  @Produces(MEDIA_TYPE)
  public QueryResult<Timestamp> getTimestamps(@Context final UriInfo uriInfo) {
    final Query<Timestamp> q = Query.fromParameterMap(uriInfo.getQueryParameters(true));
    try {
      return timestampRepo.query(q);
    } catch (QueryException e) {
      throw new BadRequestException(e);
    }
  }


}
