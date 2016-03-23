import java.io.IOException;
import java.io.InputStream;

public class StorageUtils {
  
  /**
   * Reads the contents of an InputStream and does nothing with it.
   */
  public static void readStream(InputStream is) throws IOException {
    byte inputBuffer[] = new byte[256];
    while (is.read(inputBuffer) != -1) {}
    // The caller is responsible for closing this InputStream.
    is.close();
  }

  /**
   * A helper class to provide input streams of any size.
   * The input streams will be full of null bytes.
   */
  static class ArbitrarilyLargeInputStream extends InputStream {

    private long bytesRead;
    private final long streamSize;

    public ArbitrarilyLargeInputStream(long streamSizeInBytes) {
      bytesRead = 0;
      this.streamSize = streamSizeInBytes;
    }

    @Override
    public int read() throws IOException {
      if (bytesRead >= streamSize) {
        return -1;
      }
      bytesRead++;
      return 0;
    }
  }
}

