import com.google.samples.BatchOperationSample;
import com.google.samples.CommuteSearchSample;
import com.google.samples.CompanyAndJobCrudSample;
import com.google.samples.CustomAttributeSample;
import com.google.samples.EmailAlertSearchSample;
import com.google.samples.HistogramSample;
import com.google.samples.LocationSearchSample;
import com.google.samples.SearchBasicSamples;
import com.google.samples.SearchFeaturedJobsSample;
import com.google.samples.SearchFiltersSample;
import com.google.samples.SearchQuickstart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SampleTests {

  @Test
  public void runAllSamples() throws Exception {
    BatchOperationSample.main();
    CommuteSearchSample.main();
    CompanyAndJobCrudSample.main();
    CustomAttributeSample.main();
    EmailAlertSearchSample.main();
    HistogramSample.main();
    LocationSearchSample.main();
    SearchBasicSamples.main();
    SearchFeaturedJobsSample.main();
    SearchFiltersSample.main();
    SearchQuickstart.main();
  }
}
