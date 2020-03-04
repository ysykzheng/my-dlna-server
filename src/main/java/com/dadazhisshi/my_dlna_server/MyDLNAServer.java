package com.dadazhisshi.my_dlna_server;

import com.dadazhisshi.my_dlna_server.dlna.MediaServer;
import com.dadazhisshi.my_dlna_server.dlna.MediaWatcher;
import com.dadazhisshi.my_dlna_server.http.Server;
import com.dadazhisshi.my_dlna_server.model.ContainerNode;
import com.dadazhisshi.my_dlna_server.model.NodesMap;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.LogManager;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MyDLNAServer {

  private static final Logger LOG = LoggerFactory.getLogger(MyDLNAServer.class);

  public static void main(String[] args) throws Exception {
    bridgeJul();
    if (args.length != 1) {
      System.err.println(Config.APP_NAME);
      System.err.println(Config.METADATA_MODEL_DESCRIPTION);
      System.err.println("usage: java -jar my-dlna-server-*.jar config.json");
      System.exit(-1);
      return;
    }

    String configFile = args[0];
    File file = new File(configFile);
    LOG.info("use config file {}", file.getCanonicalPath());
    Config.get().load(configFile);
    MyDLNAServer server = new MyDLNAServer();
    server.start();
  }

  private static void bridgeJul() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private static List<InetAddress> getIpAddresses() throws SocketException {
    final List<InetAddress> addresses = new ArrayList<InetAddress>();
    for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        interfaces.hasMoreElements(); ) {
      final NetworkInterface iface = interfaces.nextElement();
      if (iface.isLoopback()) {
        continue;
      }
      for (final InterfaceAddress ifaceAddr : iface.getInterfaceAddresses()) {
        final InetAddress inetAddr = ifaceAddr.getAddress();
        if (!(inetAddr instanceof Inet4Address)) {
          continue;
        }
        addresses.add(inetAddr);
      }
    }
    return addresses;
  }

  public void start() throws Exception {
    /**
     * Set up IP address
     */
    if (Config.get().getIpAddress() == null) {
      List<InetAddress> ipAddresses = getIpAddresses();
      Config.get().setIpAddress(ipAddresses.get(0).getHostAddress());
    }

    /**
     * Will bind to a single IP address
     */
    System.setProperty("org.fourthline.cling.network.useAddresses", Config.get().getIpAddress());

    /**
     * Initialize root node
     */
    LOG.info("Initializing root node...");
    ContainerNode rootNode = Config.get().getContent();
    rootNode.setId("0");
    NodesMap.put(rootNode.getId(), rootNode);

    final MediaWatcher mediaWatcher = new MediaWatcher();
    mediaWatcher.start();

    /**
     * Start up UPNP service
     */
    final String hostName = InetAddress.getLocalHost().getHostName();
    LOG.info("hostName: {}", hostName);

    DefaultUpnpServiceConfiguration upnpServiceConfiguration = new DefaultUpnpServiceConfiguration();
    final UpnpService upnpService = new UpnpServiceImpl(upnpServiceConfiguration);

    upnpService.getRegistry().addDevice(new MediaServer(hostName).getDevice());

    /**
     * Start up content serving service
     */
    LOG.info("Starting HTTP server...");
    final Server server = new Server();
    server.start();
    LOG.info("HTTP server started");

    /**
     * Register shutdown hook
     */
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        LOG.info("Shutting down " + Config.APP_NAME);
        LOG.info("Shutting down MediaWatcher");
        mediaWatcher.stop();

        LOG.info("Shutting down Cling UPNP service");
        upnpService.shutdown();

        LOG.info("Shutting down HTTP server");
        try {
          server.stop();
        } catch (Exception ex) {
          LOG.error("Error occurred during HTTP server shutdown", ex);
        }
      }
    });
  }

}
