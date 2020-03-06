package com.dadazhisshi.my_dlna_server.dlna;

import com.dadazhisshi.my_dlna_server.Config;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaServer {

  private static final String DEVICE_TYPE = "MediaServer";
  private static final int VERSION = 1;
  private static final Logger LOG = LoggerFactory.getLogger(MediaServer.class);

  private final LocalDevice localDevice;

  @SuppressWarnings("unchecked")
  public MediaServer(final String hostName) throws ValidationException {
    final DeviceType type = new UDADeviceType(DEVICE_TYPE, VERSION);
    final DeviceDetails details = new DeviceDetails(
        Config.METADATA_MODEL_NAME + " (" + hostName + ")",
        new ManufacturerDetails(Config.METADATA_MANUFACTURER),
        new ModelDetails(Config.METADATA_MODEL_NAME, Config.METADATA_MODEL_DESCRIPTION,
            Config.METADATA_MODEL_NUMBER));

    final LocalService<ContentDirectoryService> contDirSrv = new AnnotationLocalServiceBinder()
        .read(ContentDirectoryService.class);
    contDirSrv.setManager(new DefaultServiceManager<ContentDirectoryService>(contDirSrv,
        ContentDirectoryService.class) {
      @Override
      protected ContentDirectoryService createServiceInstance() {
        return new ContentDirectoryService();
      }
    });

    final LocalService<ConnectionManagerService> connManSrv = new AnnotationLocalServiceBinder()
        .read(ConnectionManagerService.class);
    connManSrv.setManager(new DefaultServiceManager<>(connManSrv,
        ConnectionManagerService.class));

    final UDN usi = UDN.uniqueSystemIdentifier(Config.UDN_ID);
    LOG.info("uniqueSystemIdentifier: {}", usi);
    this.localDevice = new LocalDevice(new DeviceIdentity(usi), type, details,
        new LocalService[]{contDirSrv, connManSrv});
  }

  public LocalDevice getDevice() {
    return this.localDevice;
  }
}
