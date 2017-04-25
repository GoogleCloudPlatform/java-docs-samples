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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import io.grpc.Status;
import io.grpc.StatusException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * The in-memory Bookstore database implementation.
 */
final class BookstoreData {
  private static final class ShelfInfo {
    private final Shelf shelf;
    private final Map<Long, Book> books;
    private long lastBookId;

    private ShelfInfo(Shelf shelf) {
      this.shelf = shelf;
      this.books = new HashMap<>();
      this.lastBookId = 0;
    }
  }

  private final Object lock;
  private final Map<Long, ShelfInfo> shelves;
  private long lastShelfId;
  private final Function<ShelfInfo, Shelf> shelfInfoToShelf =
      new Function<ShelfInfo, Shelf>() {
        @Nullable
        @Override
        public Shelf apply(@Nullable ShelfInfo shelfInfo) {
          if (shelfInfo == null) {
            return null;
          }
          return shelfInfo.shelf;
        }
      };

  BookstoreData() {
    lock = new Object();
    shelves = new HashMap<>();
    lastShelfId = 0;
  }

  public ShelfEntity createShelf(Shelf shelf) {
    synchronized (lock) {
      lastShelfId++;
      shelf = shelf.toBuilder()
          .setId(lastShelfId)
          .build();
      shelves.put(lastShelfId, new ShelfInfo(shelf));
      return ShelfEntity.create(lastShelfId, shelf);
    }
  }

  public Iterable<Shelf> listShelves() {
    synchronized (lock) {
      return Iterables.transform(ImmutableList.copyOf(shelves.values()),
              shelfInfoToShelf);
    }
  }

  public Shelf getShelf(long shelfId) throws StatusException {
    synchronized (lock) {
      @Nullable Shelf shelf = shelfInfoToShelf.apply(shelves.get(shelfId));
      if (shelf == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown shelf ID")
            .asException();
      }
      return shelf;
    }
  }

  public void deleteShelf(long shelfId) throws StatusException {
    synchronized (lock) {
      if (shelves.remove(shelfId) == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown shelf ID")
            .asException();
      }
    }
  }

  public Iterable<Book> listBooks(long shelfId) throws StatusException {
    synchronized (lock) {
      @Nullable ShelfInfo shelfInfo = shelves.get(shelfId);
      if (shelfInfo == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown shelf ID")
            .asException();
      }
      return ImmutableList.copyOf(shelfInfo.books.values());
    }
  }

  public Book createBook(long shelfId, Book book) throws StatusException {
    synchronized (lock) {
      @Nullable ShelfInfo shelfInfo = shelves.get(shelfId);
      if (shelfInfo == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown shelf ID")
            .asException();
      }
      shelfInfo.lastBookId++;
      book = book.toBuilder()
          .setId(shelfInfo.lastBookId)
          .build();
      shelfInfo.books.put(shelfInfo.lastBookId, book);
    }
    return book;
  }

  public Book getBook(long shelfId, long bookId) throws StatusException {
    synchronized (lock) {
      @Nullable ShelfInfo shelfInfo = shelves.get(shelfId);
      if (shelfInfo == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown shelf ID")
            .asException();
      }
      @Nullable Book book = shelfInfo.books.get(bookId);
      if (book == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown book ID")
            .asException();
      }
      return book;
    }
  }

  public void deleteBook(long shelfId, long bookId) throws StatusException {
    synchronized (lock) {
      @Nullable ShelfInfo shelfInfo = shelves.get(shelfId);
      if (shelfInfo == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown shelf ID")
            .asException();
      }
      if (shelfInfo.books.remove(bookId) == null) {
        throw Status.NOT_FOUND
            .withDescription("Unknown book ID")
            .asException();
      }
    }
  }
}

