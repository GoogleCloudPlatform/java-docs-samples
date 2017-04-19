// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.endpoints.examples.bookstore;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Builds and starts a GRPC-based Bookstore server.
 */
public final class BookstoreServer {

  private static final int DEFAULT_PORT = 8000;

  public static void main(String[] args) throws Exception {
    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      printUsage(options);
      return;
    }

    int port = DEFAULT_PORT;

    if (line.hasOption("port")) {
      String portOption = line.getOptionValue("port");
      try {
        port =  Integer.parseInt(portOption);
      } catch (java.lang.NumberFormatException e) {
        System.err.println("Invalid port number: " + portOption);
        printUsage(options);
        return;
      }
    }

    final BookstoreData data = initializeBookstoreData();
    final BookstoreServer server = new BookstoreServer();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          System.out.println("Shutting down");
          server.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      });
    server.start(port, data);
    System.out.format("Bookstore service listening on %d\n", port);
    server.blockUntilShutdown();
  }

  private Server server;

  private void start(int port, BookstoreData data) throws IOException {
    server = ServerBuilder.forPort(port)
        .addService(new BookstoreService(data))
        .build().start();
  }

  private void stop() throws Exception {
    server.shutdownNow();
    if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
      System.err.println("Timed out waiting for server shutdown");
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  private static BookstoreData initializeBookstoreData() throws StatusException {
    BookstoreData data = new BookstoreData();
    ShelfEntity shelf = data.createShelf(Shelf.newBuilder().setTheme("Fiction").build());
    data.createBook(shelf.getShelfId(),
        Book.newBuilder().setAuthor("Neal Stephenson").setTitle("REAMDE").build());
    shelf = data.createShelf(Shelf.newBuilder().setTheme("Fantasy").build());
    data.createBook(shelf.getShelfId(),
        Book.newBuilder().setAuthor("George R. R. Martin").setTitle("A Game of Thrones").build());
    return data;
  }

  private static Options createOptions() {
    Options options = new Options();

    // port
    options.addOption(Option.builder()
        .longOpt("port")
        .desc("The port on which the server listens.")
        .hasArg()
        .argName("port")
        .type(Integer.class)
        .build());

    return options;
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("client",
        "A simple Bookstore gRPC server for use with Endpoints.", options, "", true);
  }
}
