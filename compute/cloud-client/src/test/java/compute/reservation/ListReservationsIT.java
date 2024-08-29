package compute.reservation;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.google.cloud.compute.v1.Reservation;

@Timeout(value = 300, unit = TimeUnit.SECONDS)
public class ListReservationsIT {

  private static String PROJECT_ID;
  private static String ZONE;
  private static String RESERVATION_NAME_1;
  private static String RESERVATION_NAME_2;
  private static String RESERVATION_NAME_3;


  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    ZONE = "us-central1-a";
    RESERVATION_NAME_1 = "test-reservation-" + UUID.randomUUID();

    // Create the reservation 1.
    Reservation reservation = CreateReservation
        .createReservation(
            PROJECT_ID,
            ZONE,
            RESERVATION_NAME_1);
    // Create the reservation 1.
    Reservation reservation2 = CreateReservation
        .createReservation(
            PROJECT_ID,
            ZONE,
            RESERVATION_NAME_2);
    // Create the reservation 1.
    Reservation reservation3 = CreateReservation
        .createReservation(
            PROJECT_ID,
            ZONE,
            RESERVATION_NAME_3);

  }

  @AfterAll
  public static void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_1);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_2);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_3);
  }

  @Test
  public void testListReservations() throws IOException {
    List<Reservation> reservations =
        ListReservations.listReservations(PROJECT_ID, ZONE);
    assertThat(reservations).isNotNull();
  }

  @Test
  public void testListReservationsNotFound() throws IOException {
    List<Reservation> reservations =
        ListReservations.listReservations("invalid-project", ZONE);
    assertNull(reservations);
    Assert.assertEquals(RESERVATION_NAME_1, reservations.get(0).getName());
    Assert.assertEquals(RESERVATION_NAME_2, reservations.get(1).getName());
    Assert.assertEquals(RESERVATION_NAME_3, reservations.get(2).getName());

  }
}