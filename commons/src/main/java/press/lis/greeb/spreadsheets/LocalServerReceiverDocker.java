package press.lis.greeb.spreadsheets;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

/**
 * Copy of LocalServerReceiver from Google Library adapted for Docker.
 * Pruned some logic, added the listener for 0.0.0.0 and disconnected it from the returning url.
 *
 * @author Alexander Eliseev
 */
public final class LocalServerReceiverDocker implements VerificationCodeReceiver {

    private static final String CALLBACK_PATH = "/Callback";
    /**
     * To block until receiving an authorization response or stop() is called.
     */
    final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);
    /**
     * Callback path of redirect_uri
     */
    private final String callbackPath;
    /**
     * Verification code or {@code null} for none.
     */
    String code;
    /**
     * Error code or {@code null} for none.
     */
    String error;
    /**
     * Server or {@code null} before {@link #getRedirectUri()}.
     */
    private Server server;
    /**
     * Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    private int port;

    /**
     * Constructor.
     *
     * @param port Port to use or {@code -1} to select an unused port
     */
    public LocalServerReceiverDocker(int port) {
        this.port = port;
        this.callbackPath = CALLBACK_PATH;
    }

    @Override
    public String getRedirectUri() throws IOException {
        server = new Server(port != -1 ? port : 0);
        Connector connector = server.getConnectors()[0];
        connector.setHost("0.0.0.0");
        server.setHandler(new LocalServerReceiverDocker.CallbackHandler());
        try {
            server.start();
            port = connector.getLocalPort();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e);
            throw new IOException(e);
        }
        return "http://localhost" + ":" + port + callbackPath;
    }

    /**
     * Blocks until the server receives a login result, or the server is stopped
     * by {@link #stop()}, to return an authorization code.
     *
     * @return authorization code if login succeeds; may return {@code null} if the server
     * is stopped by {@link #stop()}
     * @throws IOException if the server receives an error code (through an HTTP request
     *                     parameter {@code error})
     */
    @Override
    public String waitForCode() throws IOException {
        waitUnlessSignaled.acquireUninterruptibly();
        if (error != null) {
            throw new IOException("User authorization failed (" + error + ")");
        }
        return code;
    }

    @Override
    public void stop() throws IOException {
        waitUnlessSignaled.release();
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                Throwables.propagateIfPossible(e);
                throw new IOException(e);
            }
            server = null;
        }
    }

    /**
     * Returns the port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns callback path used in redirect_uri.
     */
    public String getCallbackPath() {
        return callbackPath;
    }

    /**
     * Jetty handler that takes the verifier token passed over from the OAuth provider and stashes it
     * where {@link #waitForCode} will find it.
     */
    class CallbackHandler extends AbstractHandler {

        @Override
        public void handle(
                String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response
        )
                throws IOException {
            if (!callbackPath.equals(target)) {
                return;
            }

            try {
                ((Request) request).setHandled(true);
                error = request.getParameter("error");
                code = request.getParameter("code");

                writeLandingHtml(response);
                response.flushBuffer();
            } finally {
                waitUnlessSignaled.release();
            }
        }

        private void writeLandingHtml(HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            PrintWriter doc = response.getWriter();
            doc.println("<html>");
            doc.println("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
            doc.println("<body>");
            doc.println("Received verification code. You may now close this window.");
            doc.println("</body>");
            doc.println("</html>");
            doc.flush();
        }
    }
}
