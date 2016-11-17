
package com.google.endpoints.examples.bookstore;

import javax.annotation.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ShelfEntity extends ShelfEntity {

  private final long shelfId;
  private final Shelf shelf;

  AutoValue_ShelfEntity(
      long shelfId,
      Shelf shelf) {
    this.shelfId = shelfId;
    if (shelf == null) {
      throw new NullPointerException("Null shelf");
    }
    this.shelf = shelf;
  }

  @Override
  public long getShelfId() {
    return shelfId;
  }

  @Override
  public Shelf getShelf() {
    return shelf;
  }

  @Override
  public String toString() {
    return "ShelfEntity{"
        + "shelfId=" + shelfId + ", "
        + "shelf=" + shelf
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ShelfEntity) {
      ShelfEntity that = (ShelfEntity) o;
      return (this.shelfId == that.getShelfId())
           && (this.shelf.equals(that.getShelf()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (shelfId >>> 32) ^ shelfId;
    h *= 1000003;
    h ^= shelf.hashCode();
    return h;
  }

}
