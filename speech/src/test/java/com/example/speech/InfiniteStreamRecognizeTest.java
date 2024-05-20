package com.example.speech;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class InfiniteStreamRecognizeTest {
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException,
          TimeoutException {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
  }
  @Test
  public void infiniteStreamingRecognize() throws Exception {
    InfiniteStreamRecognize.putDataToSharedQueue(readAudioFile());
    InfiniteStreamRecognize.putDataToSharedQueue("exit".getBytes(StandardCharsets.UTF_8));
    InfiniteStreamRecognize.infiniteStreamingRecognize("en-US", () -> {}, InfiniteStreamRecognize.getResponseObserver());
    // wait responses from server
    Thread.sleep(5000);
    assertThat(bout.toString().toLowerCase()).contains("hi i want to");
  }

  private byte[] readAudioFile() throws UnsupportedAudioFileException, IOException {
    AudioInputStream stream = AudioSystem.getAudioInputStream(new File("C:\\projects\\java-docs-samples-dev\\speech\\resources\\commercial_mono.wav"));
    AudioFormat format = stream.getFormat();

    int length = (int)(stream.getFrameLength() * format.getFrameSize());
    byte[] samples = new byte[length];
    DataInputStream in = new DataInputStream(stream);
    try
    {
      in.readFully(samples);
    }
    catch (IOException ignored){}
    return samples;
  }
}