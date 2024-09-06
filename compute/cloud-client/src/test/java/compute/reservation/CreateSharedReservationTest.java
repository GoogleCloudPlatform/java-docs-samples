package compute.reservation;

import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ShareSettings;
import com.google.cloud.compute.v1.ShareSettingsProjectConfig;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CreateSharedReservationTest {

  @Test
  public void testCreateSharedReservation() throws Exception {
    // Mock Objects
    ReservationsClient reservationsClient = Mockito.mock(ReservationsClient.class);
    ShareSettings shareSettings = Mockito.mock(ShareSettings.class);
    Reservation.Builder reservationBuilder = Mockito.mock(Reservation.Builder.class);
    Reservation reservation = Mockito.mock(Reservation.class);

    // Define Test Data
    String projectId = "your-project-id";
    String zone = "us-central1-a";
    String reservationName = "test-reservation";
    String instanceTemplateUri = "projects/your-project-id/global/instanceTemplates/your-instance-template";
    int vmCount = 3;

    // Mock Behavior
    when(reservationsClient.insertAsync(any(), any(), any()))
        .thenReturn(Mockito.mock(com.google.api.gax.longrunning.OperationFuture.class));
    when(reservationBuilder.setShareSettings(any(ShareSettings.class))).thenReturn(reservationBuilder);
    when(reservationBuilder.setName(any())).thenReturn(reservationBuilder);
    when(reservationBuilder.setZone(any())).thenReturn(reservationBuilder);
    when(reservationBuilder.setSpecificReservationRequired(any())).thenReturn(reservationBuilder);
    when(reservationBuilder.setShareSettings(shareSettings)).thenReturn(reservationBuilder); // Important!
    when(reservationBuilder.setSpecificReservation(any(AllocationSpecificSKUReservation.class))).thenReturn(reservationBuilder);
    when(reservationBuilder.build()).thenReturn(reservation);

    // Call the method to test
    CreateSharedReservation.createSharedReservation(
        projectId, zone, reservationName, instanceTemplateUri, vmCount);

    // Assertions
    Mockito.verify(reservationsClient, Mockito.times(1))
        .insertAsync(Mockito.eq(projectId), Mockito.eq(zone), Mockito.eq(reservation));
  }
}