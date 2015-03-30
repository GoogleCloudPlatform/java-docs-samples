// [START image_resize]
import com.google.appengine.api.capabilities.*;

CapabilitiesService service =
CapabilitiesServiceFactory.getCapabilitiesService();
CapabilityStatus status = service.getStatus(Capability.IMAGES).getStatus();

if (status == CapabilityStatus.DISABLED) {
	// Images API is not available.
}
// [END image_resize]

// [START intro]
CapabilityStatus status =
service.getStatus(Capability.DATASTORE_WRITE).getStatus();

if (status == CapabilityStatus.DISABLED) {
	// Datastore is in read-only mode.
}
// [END intro]
