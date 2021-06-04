package errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionResponseHandler implements ExceptionMapper<Exception> {
	@Override
	public Response toResponse(Exception ex) {
		return Response.status(500).entity(ex.getClass().getName()+": "+ex.getMessage()+" "+ex.getCause()).type("text/plain").build();
	}
}