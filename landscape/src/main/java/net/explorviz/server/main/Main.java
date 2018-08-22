package net.explorviz.server.main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.explorviz.shared.server.helper.PropertyService;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) {

		final Server server = new Server(getPort());

		final ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(createJaxRsApp()));
		final ServletContextHandler context = new ServletContextHandler(server, getContextPath());
		context.addServlet(jerseyServlet, "/*");

		try {
			server.start();
		} catch (final Exception e) {
			LOGGER.error("Server start failed", e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				server.stop();
			} catch (final Exception e) {
				LOGGER.error("Server stop failed", e);
			}
		}));
	}

	private static int getPort() {
		try {
			return PropertyService.getIntegerProperty("server.port");
		} catch (final NumberFormatException e) {
			LOGGER.info(
					"ATTENTION: Using default port 8081 for server. Maybe your stated server.port property cannot be casted to int",
					e);
		}
		return 8081;
	}

	private static String getContextPath() {
		final String statedContextPath = PropertyService.getStringProperty("server.contextPath");

		if (statedContextPath == null) {
			LOGGER.info(
					"ATTENTION: Using default contextPath '/' for server. Maybe your stated server.contextPath property is no valid string.");
			return "/";
		} else {
			return statedContextPath;
		}
	}

	private static ResourceConfig createJaxRsApp() {
		return new ResourceConfig(new Application());
	}

}