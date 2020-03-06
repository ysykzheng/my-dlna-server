package com.dadazhisshi.my_dlna_server;

import com.dadazhisshi.my_dlna_server.dlna.MediaServer;
import com.dadazhisshi.my_dlna_server.http.Server;
import com.dadazhisshi.my_dlna_server.model.FolderNode;
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
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MyDLNAServer {

  private final String basePath;
  private final String host;
  private final Integer port;
  private static final Logger LOG = LoggerFactory.getLogger(MyDLNAServer.class);

  public MyDLNAServer(String host, Integer port, String basePath) {
    this.basePath = basePath;
    this.host = host;
    this.port = port;
  }

  public static void main(String[] args) throws Exception {
    bridgeJul();

    ArgumentParser parser = ArgumentParsers
        .newFor("java -jar my-dlna-server-*-jar-with-dependencies.jar").build()
        .defaultHelp(true)
        .description("simple DLNA server for serving media files");
    parser.addArgument("-p", "--port")
        .setDefault(8899)
        .required(false)
        .help("http port");
    List<InetAddress> ipAddresses = getIpAddresses();
    parser.addArgument("-H", "--host")
        .required(false)
        .setDefault(ipAddresses.get(0)
            .getHostAddress());
    parser.addArgument("-d", "--dir")
        .required(true)
        .help("media file dir");
    Namespace ns;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
      return;
    }
    Integer port = ns.getInt("port");
    String host = ns.getString("host");
    String dir = ns.getString("dir");

    MyDLNAServer server = new MyDLNAServer(host, port, dir);
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
    Config.get().setPort(port);
    Config.get().setHost(host);
    Config.get().setBasePath(basePath);
    /**
     * Will bind to a single IP address
     */
    System.setProperty("org.fourthline.cling.network.useAddresses", Config.get().getHost());

    /**
     * Initialize root node
     */
    LOG.info("Initializing root node...");
    FolderNode node = new FolderNode(new File(basePath));
    node.setId("0");
    NodesMap.put(node.getId(), node);

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
    Server server = new Server(basePath);
    server.start();
    LOG.info("HTTP server started");

    /**
     * Register shutdown hook
     */
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        LOG.info("Shutting down " + Config.APP_NAME);

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
