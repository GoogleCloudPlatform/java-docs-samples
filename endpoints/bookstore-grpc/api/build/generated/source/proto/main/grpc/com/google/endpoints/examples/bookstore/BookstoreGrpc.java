package com.google.endpoints.examples.bookstore;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * A simple Bookstore API.
 * The API manages shelves and books resources. Shelves contain books.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.1)",
    comments = "Source: bookstore.proto")
public class BookstoreGrpc {

  private BookstoreGrpc() {}

  public static final String SERVICE_NAME = "endpoints.examples.bookstore.Bookstore";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.google.endpoints.examples.bookstore.ListShelvesResponse> METHOD_LIST_SHELVES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "ListShelves"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.ListShelvesResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.CreateShelfRequest,
      com.google.endpoints.examples.bookstore.Shelf> METHOD_CREATE_SHELF =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "CreateShelf"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.CreateShelfRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.Shelf.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.GetShelfRequest,
      com.google.endpoints.examples.bookstore.Shelf> METHOD_GET_SHELF =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "GetShelf"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.GetShelfRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.Shelf.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.DeleteShelfRequest,
      com.google.protobuf.Empty> METHOD_DELETE_SHELF =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "DeleteShelf"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.DeleteShelfRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.ListBooksRequest,
      com.google.endpoints.examples.bookstore.ListBooksResponse> METHOD_LIST_BOOKS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "ListBooks"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.ListBooksRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.ListBooksResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.CreateBookRequest,
      com.google.endpoints.examples.bookstore.Book> METHOD_CREATE_BOOK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "CreateBook"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.CreateBookRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.Book.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.GetBookRequest,
      com.google.endpoints.examples.bookstore.Book> METHOD_GET_BOOK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "GetBook"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.GetBookRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.Book.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.endpoints.examples.bookstore.DeleteBookRequest,
      com.google.protobuf.Empty> METHOD_DELETE_BOOK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "endpoints.examples.bookstore.Bookstore", "DeleteBook"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.endpoints.examples.bookstore.DeleteBookRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BookstoreStub newStub(io.grpc.Channel channel) {
    return new BookstoreStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BookstoreBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BookstoreBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static BookstoreFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BookstoreFutureStub(channel);
  }

  /**
   * <pre>
   * A simple Bookstore API.
   * The API manages shelves and books resources. Shelves contain books.
   * </pre>
   */
  public static abstract class BookstoreImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Returns a list of all shelves in the bookstore.
     * </pre>
     */
    public void listShelves(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.ListShelvesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_SHELVES, responseObserver);
    }

    /**
     * <pre>
     * Creates a new shelf in the bookstore.
     * </pre>
     */
    public void createShelf(com.google.endpoints.examples.bookstore.CreateShelfRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Shelf> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_SHELF, responseObserver);
    }

    /**
     * <pre>
     * Returns a specific bookstore shelf.
     * </pre>
     */
    public void getShelf(com.google.endpoints.examples.bookstore.GetShelfRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Shelf> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_SHELF, responseObserver);
    }

    /**
     * <pre>
     * Deletes a shelf, including all books that are stored on the shelf.
     * </pre>
     */
    public void deleteShelf(com.google.endpoints.examples.bookstore.DeleteShelfRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DELETE_SHELF, responseObserver);
    }

    /**
     * <pre>
     * Returns a list of books on a shelf.
     * </pre>
     */
    public void listBooks(com.google.endpoints.examples.bookstore.ListBooksRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.ListBooksResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_BOOKS, responseObserver);
    }

    /**
     * <pre>
     * Creates a new book.
     * </pre>
     */
    public void createBook(com.google.endpoints.examples.bookstore.CreateBookRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Book> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_BOOK, responseObserver);
    }

    /**
     * <pre>
     * Returns a specific book.
     * </pre>
     */
    public void getBook(com.google.endpoints.examples.bookstore.GetBookRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Book> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_BOOK, responseObserver);
    }

    /**
     * <pre>
     * Deletes a book from a shelf.
     * </pre>
     */
    public void deleteBook(com.google.endpoints.examples.bookstore.DeleteBookRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DELETE_BOOK, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_LIST_SHELVES,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                com.google.endpoints.examples.bookstore.ListShelvesResponse>(
                  this, METHODID_LIST_SHELVES)))
          .addMethod(
            METHOD_CREATE_SHELF,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.CreateShelfRequest,
                com.google.endpoints.examples.bookstore.Shelf>(
                  this, METHODID_CREATE_SHELF)))
          .addMethod(
            METHOD_GET_SHELF,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.GetShelfRequest,
                com.google.endpoints.examples.bookstore.Shelf>(
                  this, METHODID_GET_SHELF)))
          .addMethod(
            METHOD_DELETE_SHELF,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.DeleteShelfRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_DELETE_SHELF)))
          .addMethod(
            METHOD_LIST_BOOKS,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.ListBooksRequest,
                com.google.endpoints.examples.bookstore.ListBooksResponse>(
                  this, METHODID_LIST_BOOKS)))
          .addMethod(
            METHOD_CREATE_BOOK,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.CreateBookRequest,
                com.google.endpoints.examples.bookstore.Book>(
                  this, METHODID_CREATE_BOOK)))
          .addMethod(
            METHOD_GET_BOOK,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.GetBookRequest,
                com.google.endpoints.examples.bookstore.Book>(
                  this, METHODID_GET_BOOK)))
          .addMethod(
            METHOD_DELETE_BOOK,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.endpoints.examples.bookstore.DeleteBookRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_DELETE_BOOK)))
          .build();
    }
  }

  /**
   * <pre>
   * A simple Bookstore API.
   * The API manages shelves and books resources. Shelves contain books.
   * </pre>
   */
  public static final class BookstoreStub extends io.grpc.stub.AbstractStub<BookstoreStub> {
    private BookstoreStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BookstoreStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BookstoreStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BookstoreStub(channel, callOptions);
    }

    /**
     * <pre>
     * Returns a list of all shelves in the bookstore.
     * </pre>
     */
    public void listShelves(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.ListShelvesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_SHELVES, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a new shelf in the bookstore.
     * </pre>
     */
    public void createShelf(com.google.endpoints.examples.bookstore.CreateShelfRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Shelf> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_SHELF, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns a specific bookstore shelf.
     * </pre>
     */
    public void getShelf(com.google.endpoints.examples.bookstore.GetShelfRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Shelf> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_SHELF, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a shelf, including all books that are stored on the shelf.
     * </pre>
     */
    public void deleteShelf(com.google.endpoints.examples.bookstore.DeleteShelfRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE_SHELF, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns a list of books on a shelf.
     * </pre>
     */
    public void listBooks(com.google.endpoints.examples.bookstore.ListBooksRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.ListBooksResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_BOOKS, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a new book.
     * </pre>
     */
    public void createBook(com.google.endpoints.examples.bookstore.CreateBookRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Book> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_BOOK, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns a specific book.
     * </pre>
     */
    public void getBook(com.google.endpoints.examples.bookstore.GetBookRequest request,
        io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Book> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_BOOK, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a book from a shelf.
     * </pre>
     */
    public void deleteBook(com.google.endpoints.examples.bookstore.DeleteBookRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE_BOOK, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * A simple Bookstore API.
   * The API manages shelves and books resources. Shelves contain books.
   * </pre>
   */
  public static final class BookstoreBlockingStub extends io.grpc.stub.AbstractStub<BookstoreBlockingStub> {
    private BookstoreBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BookstoreBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BookstoreBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BookstoreBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Returns a list of all shelves in the bookstore.
     * </pre>
     */
    public com.google.endpoints.examples.bookstore.ListShelvesResponse listShelves(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_SHELVES, getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a new shelf in the bookstore.
     * </pre>
     */
    public com.google.endpoints.examples.bookstore.Shelf createShelf(com.google.endpoints.examples.bookstore.CreateShelfRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_SHELF, getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns a specific bookstore shelf.
     * </pre>
     */
    public com.google.endpoints.examples.bookstore.Shelf getShelf(com.google.endpoints.examples.bookstore.GetShelfRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_SHELF, getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a shelf, including all books that are stored on the shelf.
     * </pre>
     */
    public com.google.protobuf.Empty deleteShelf(com.google.endpoints.examples.bookstore.DeleteShelfRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DELETE_SHELF, getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns a list of books on a shelf.
     * </pre>
     */
    public com.google.endpoints.examples.bookstore.ListBooksResponse listBooks(com.google.endpoints.examples.bookstore.ListBooksRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_BOOKS, getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a new book.
     * </pre>
     */
    public com.google.endpoints.examples.bookstore.Book createBook(com.google.endpoints.examples.bookstore.CreateBookRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_BOOK, getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns a specific book.
     * </pre>
     */
    public com.google.endpoints.examples.bookstore.Book getBook(com.google.endpoints.examples.bookstore.GetBookRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_BOOK, getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a book from a shelf.
     * </pre>
     */
    public com.google.protobuf.Empty deleteBook(com.google.endpoints.examples.bookstore.DeleteBookRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DELETE_BOOK, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * A simple Bookstore API.
   * The API manages shelves and books resources. Shelves contain books.
   * </pre>
   */
  public static final class BookstoreFutureStub extends io.grpc.stub.AbstractStub<BookstoreFutureStub> {
    private BookstoreFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BookstoreFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BookstoreFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BookstoreFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Returns a list of all shelves in the bookstore.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.endpoints.examples.bookstore.ListShelvesResponse> listShelves(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_SHELVES, getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a new shelf in the bookstore.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.endpoints.examples.bookstore.Shelf> createShelf(
        com.google.endpoints.examples.bookstore.CreateShelfRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_SHELF, getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns a specific bookstore shelf.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.endpoints.examples.bookstore.Shelf> getShelf(
        com.google.endpoints.examples.bookstore.GetShelfRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_SHELF, getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a shelf, including all books that are stored on the shelf.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteShelf(
        com.google.endpoints.examples.bookstore.DeleteShelfRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE_SHELF, getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns a list of books on a shelf.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.endpoints.examples.bookstore.ListBooksResponse> listBooks(
        com.google.endpoints.examples.bookstore.ListBooksRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_BOOKS, getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a new book.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.endpoints.examples.bookstore.Book> createBook(
        com.google.endpoints.examples.bookstore.CreateBookRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_BOOK, getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns a specific book.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.endpoints.examples.bookstore.Book> getBook(
        com.google.endpoints.examples.bookstore.GetBookRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_BOOK, getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a book from a shelf.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteBook(
        com.google.endpoints.examples.bookstore.DeleteBookRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE_BOOK, getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_SHELVES = 0;
  private static final int METHODID_CREATE_SHELF = 1;
  private static final int METHODID_GET_SHELF = 2;
  private static final int METHODID_DELETE_SHELF = 3;
  private static final int METHODID_LIST_BOOKS = 4;
  private static final int METHODID_CREATE_BOOK = 5;
  private static final int METHODID_GET_BOOK = 6;
  private static final int METHODID_DELETE_BOOK = 7;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BookstoreImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(BookstoreImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_SHELVES:
          serviceImpl.listShelves((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.ListShelvesResponse>) responseObserver);
          break;
        case METHODID_CREATE_SHELF:
          serviceImpl.createShelf((com.google.endpoints.examples.bookstore.CreateShelfRequest) request,
              (io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Shelf>) responseObserver);
          break;
        case METHODID_GET_SHELF:
          serviceImpl.getShelf((com.google.endpoints.examples.bookstore.GetShelfRequest) request,
              (io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Shelf>) responseObserver);
          break;
        case METHODID_DELETE_SHELF:
          serviceImpl.deleteShelf((com.google.endpoints.examples.bookstore.DeleteShelfRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_LIST_BOOKS:
          serviceImpl.listBooks((com.google.endpoints.examples.bookstore.ListBooksRequest) request,
              (io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.ListBooksResponse>) responseObserver);
          break;
        case METHODID_CREATE_BOOK:
          serviceImpl.createBook((com.google.endpoints.examples.bookstore.CreateBookRequest) request,
              (io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Book>) responseObserver);
          break;
        case METHODID_GET_BOOK:
          serviceImpl.getBook((com.google.endpoints.examples.bookstore.GetBookRequest) request,
              (io.grpc.stub.StreamObserver<com.google.endpoints.examples.bookstore.Book>) responseObserver);
          break;
        case METHODID_DELETE_BOOK:
          serviceImpl.deleteBook((com.google.endpoints.examples.bookstore.DeleteBookRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_LIST_SHELVES,
        METHOD_CREATE_SHELF,
        METHOD_GET_SHELF,
        METHOD_DELETE_SHELF,
        METHOD_LIST_BOOKS,
        METHOD_CREATE_BOOK,
        METHOD_GET_BOOK,
        METHOD_DELETE_BOOK);
  }

}
