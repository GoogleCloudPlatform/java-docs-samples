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

import com.google.protobuf.Empty;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * A client application which calls the Bookstore API over gRPC.
 */
public final class BookstoreClient {

  private static final String DEFAULT_ADDRESS = "localhost:8000";

  public static void main(String[] args) throws Exception {
    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine params;
    try {
      params = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      printUsage(options);
      return;
    }

    String address = params.getOptionValue("bookstore", DEFAULT_ADDRESS);
    String apiKey = params.getOptionValue("api_key");
    String authToken = params.getOptionValue("auth_token");
    String operation = params.getOptionValue("operation", "list");

    // Create gRPC stub.
    BookstoreGrpc.BookstoreBlockingStub bookstore = createBookstoreStub(
        address, apiKey, authToken);

    if ("list".equals(operation)) {
      listShelves(bookstore);
    } else if ("create".equals(operation)) {
      createShelf(bookstore);
    } else if ("enumerate".equals(operation)) {
      enumerate(bookstore);
    }
  }

  /**
   * Lists all shelves in the bookstore.
   *
   * @param bookstore a client stub to call Bookstore service.
   */
  static void listShelves(BookstoreGrpc.BookstoreBlockingStub bookstore) {
    ListShelvesResponse shelves = bookstore.listShelves(Empty.getDefaultInstance());
    System.out.println(shelves);
  }

  /**
   * Creates a new shelf in the bookstore.
   *
   * @param bookstore a client stub to call Bookstore service.
   */
  static void createShelf(BookstoreGrpc.BookstoreBlockingStub bookstore) {
    CreateShelfRequest.Builder builder = CreateShelfRequest.newBuilder();
    builder.getShelfBuilder().setTheme("Computers");
    Shelf shelf = bookstore.createShelf(builder.build());
    System.out.println(shelf);
  }

  /**
   * Enumerates all books in the bookstore.
   *
   * @param bookstore a client stub to call Bookstore service.
   */
  static void enumerate(BookstoreGrpc.BookstoreBlockingStub bookstore) {
    System.out.println("Calling listShelves");
    ListShelvesResponse shelves = bookstore.listShelves(Empty.getDefaultInstance());
    System.out.println(shelves);

    for (Shelf s : shelves.getShelvesList()) {
      System.out.format("Getting shelf %d\n", s.getId());
      GetShelfRequest getShelfRequest = GetShelfRequest.newBuilder()
          .setShelf(s.getId())
          .build();
      Shelf shelf = bookstore.getShelf(getShelfRequest);
      System.out.println(shelf);

      System.out.format("Getting books from shelf %d:\n", shelf.getId());
      ListBooksRequest listBooksRequest = ListBooksRequest.newBuilder()
          .setShelf(shelf.getId())
          .build();

      ListBooksResponse books = bookstore.listBooks(listBooksRequest);
      System.out.println(books);

      for (Book b : books.getBooksList()) {
        System.out.format("Getting book %d from shelf %d:\n", b.getId(), shelf.getId());

        GetBookRequest getBookRequest = GetBookRequest.newBuilder()
            .setShelf(shelf.getId())
            .setBook(b.getId())
            .build();

        Book book = bookstore.getBook(getBookRequest);
        System.out.println(book);
      }
    }
  }

  private static final class Interceptor implements ClientInterceptor {
    private final String apiKey;
    private final String authToken;

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    private static Metadata.Key<String> API_KEY_HEADER =
        Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> AUTHORIZATION_HEADER =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    public Interceptor(String apiKey, String authToken) {
      this.apiKey = apiKey;
      this.authToken = authToken;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT,RespT> method, CallOptions callOptions, Channel next) {
      LOGGER.info("Intercepted " + method.getFullMethodName());
      ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

      call = new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
          if (apiKey != null && !apiKey.isEmpty()) {
            LOGGER.info("Attaching API Key: " + apiKey);
            headers.put(API_KEY_HEADER, apiKey);
          }
          if (authToken != null && !authToken.isEmpty()) {
            System.out.println("Attaching auth token");
            headers.put(AUTHORIZATION_HEADER, "Bearer " + authToken);
          }
          super.start(responseListener, headers);
        }
      };
      return call;
    }
  }

  static BookstoreGrpc.BookstoreBlockingStub createBookstoreStub(
      String address, String apiKey, String authToken) {
    Channel channel = ManagedChannelBuilder.forTarget(address)
        .usePlaintext(true)
        .build();

    channel = ClientInterceptors.intercept(channel,  new Interceptor(apiKey, authToken));

    return BookstoreGrpc.newBlockingStub(channel);
  }

  private static Options createOptions() {
    Options options = new Options();

    // bookstore
    options.addOption(Option.builder()
        .longOpt("bookstore")
        .desc("The address of the bookstore server")
        .hasArg()
        .argName("address")
        .type(String.class)
        .build());

    // api_key
    options.addOption(Option.builder()
        .longOpt("api_key")
        .desc("The API key to use for RPC calls")
        .hasArg()
        .argName("key")
        .type(String.class)
        .build());

    // auth_token
    options.addOption(Option.builder()
        .longOpt("auth_token")
        .desc("The auth token to use for RPC calls")
        .hasArg()
        .argName("token")
        .type(String.class)
        .build());

    // operation
    options.addOption(Option.builder()
        .longOpt("operation")
        .desc("The bookstore operation to perform: list|create|enumerate")
        .hasArg()
        .argName("op")
        .type(String.class)
        .build());

    return options;
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("client",
        "A simple Bookstore gRPC client for use with Endpoints.", options, "", true);
  }
}
