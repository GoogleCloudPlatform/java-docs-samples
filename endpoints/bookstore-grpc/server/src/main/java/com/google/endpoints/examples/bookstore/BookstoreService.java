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

import io.grpc.stub.StreamObserver;

import java.util.concurrent.Executor;

/**
 * Implements the Bookstore GRPC service.
 */
public final class BookstoreService extends BookstoreGrpc.BookstoreImplBase {
  private final BookstoreData data;

  public BookstoreService(BookstoreData data) {
    this.data = data;
  }

  @Override
  public void listShelves(Empty request, StreamObserver<ListShelvesResponse> responseObserver) {
    ListShelvesResponse response;
    try {
      response = ListShelvesResponse.newBuilder()
          .addAllShelves(data.listShelves())
          .build();
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createShelf(CreateShelfRequest request, StreamObserver<Shelf> responseObserver) {
    Shelf response;
    try {
      response = data.createShelf(request.getShelf()).getShelf();
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getShelf(GetShelfRequest request, StreamObserver<Shelf> responseObserver) {
    Shelf response;
    try {
      response = data.getShelf(request.getShelf());
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteShelf(DeleteShelfRequest request, StreamObserver<Empty> responseObserver) {
    try {
      data.deleteShelf(request.getShelf());
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void listBooks(ListBooksRequest request, StreamObserver<ListBooksResponse> responseObserver) {
    ListBooksResponse response;
    try {
      response = ListBooksResponse.newBuilder()
          .addAllBooks(data.listBooks(request.getShelf()))
          .build();
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createBook(CreateBookRequest request, StreamObserver<Book> responseObserver) {
    Book response;
    try {
      response = data.createBook(request.getShelf(), request.getBook());
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getBook(GetBookRequest request, StreamObserver<Book> responseObserver) {
    Book response;
    try {
      response = data.getBook(request.getShelf(), request.getBook());
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteBook(DeleteBookRequest request, StreamObserver<Empty> responseObserver) {
    try {
      data.deleteBook(request.getShelf(), request.getBook());
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }
}
