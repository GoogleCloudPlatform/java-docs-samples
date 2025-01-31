package app;

public class BasketItem {

  private int id;
  private int quantity;

  public BasketItem(int id, int quantity) {
    this.id = id;
    this.quantity = quantity;
  }

  public int getId() {
    return id;
  }

  public int getQuantity() {
    return quantity;
  }
}
