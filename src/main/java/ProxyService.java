import java.io.IOException;
import java.util.concurrent.Future;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.nio.client.methods.HttpAsyncMethods;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

@SuppressWarnings("serial")
public class ProxyService extends HttpServlet {

	private CloseableHttpAsyncClient httpClient;

	@Override
	public void init() throws ServletException {
		httpClient = HttpAsyncClients.custom().
				setMaxConnTotal(Integer.parseInt(getInitParameter("maxtotalconnections"))).				
				setMaxConnPerRoute(Integer.parseInt(getInitParameter("maxconnectionsperroute"))).
				build();
		httpClient.start();
	}

	@Override
	public void destroy() {
		try {
			httpClient.close();
		} catch (IOException e) { }
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		AsyncContext asyncCtx = request.startAsync(request, response);
		asyncCtx.setTimeout(ExternalServiceMock.TIMEOUT_SECONDS * ExternalServiceMock.K);		
		ResponseListener listener = new ResponseListener();
		asyncCtx.addListener(listener);
		Future<String> result = httpClient.execute(HttpAsyncMethods.createGet(getInitParameter("serviceurl")), new ResponseConsumer(asyncCtx), null);
	}

}
