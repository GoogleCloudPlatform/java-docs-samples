package com.example.cloudrun;

import io.javalin.Javalin;
import java.io.BufferedReader;
// import java.io.File;
// import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Process;
import java.lang.StringBuilder;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

// import java.lang.ProcessBuilder;

public class App {
  public static void main(String[] args) {
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    Javalin app = Javalin.create().start(port);

    // [START run_system_package_handler]
    app.get("/diagram.png", ctx -> {
      try {
        String dot = ctx.queryParam("dot");
        InputStream image = createDiagram(dot);
        ctx.header("Content-Type", "image/png");
        // ctx.header("Content-Length", Integer.toString(image.length()));
        ctx.header("Cache-Control", "public, max-age=86400");
        ctx.result(image); // MIGHT NEED TO CHANGE THIS
      } catch (Exception e) {
        System.out.println(e);
      }
    });
    // [END run_system_package_handler]
  }

  public static InputStream createDiagram(String dot) {
    if (dot == null) {
      throw new NullPointerException("syntax: no graphviz definition provided");
    }
    List<String> args = new ArrayList<String>();
    args.add("/usr/bin/dot");
    args.add("-Glabel=\"Made on Cloud Run\"");
    args.add("-Gfontsize=10");
    args.add("-Glabeljust=right");
    args.add("-Glabelloc=bottom");
    args.add("-Gfontcolor=gray");
    args.add("-Tpng");

    StringBuilder output = new StringBuilder();
    InputStream stdout = null;
    try {
      ProcessBuilder pb = new ProcessBuilder(args);
      Process process = pb.start();

      OutputStream stdin = process.getOutputStream();
      stdout = process.getInputStream();

      Writer w = new OutputStreamWriter(stdin, "UTF-8");
      w.write(dot);
      w.close();
      // stdin.write(dot.getBytes());
      // stdin.flush();

      // stdin.close();
      process.waitFor();
    //   try(BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
    //     System.out.println("starting");
    //
    //     String line;
    //
    //     while ((line = reader.readLine()) != null) {
    //       output.append(line);
    //       System.out.println("line");
    //     }
    //   }
    //
    } catch(Exception e) {
      System.out.println(e);
    }
    // return output.toString();
    return stdout;
  }
}
