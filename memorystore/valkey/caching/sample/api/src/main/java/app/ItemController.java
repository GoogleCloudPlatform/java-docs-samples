/**
 * Provides a RESTful API for interacting with the application's data.
 *
 * The controller contains three routes:
 * - GET /item/{id} - Get an item by ID
 * - POST /item/create - Create a new item
 * - DELETE /item/delete/{id} - Delete an item by ID
 */

package app;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item")
public class ItemController {

  private final DataController dataController;

  public ItemController(DataController dataController) {
    this.dataController = dataController;
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> read(@PathVariable String id) {
    long itemId;

    try {
      itemId = Long.parseLong(id);
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest()
        .body(
          JSONObject.valueToString(
            new JSONObject().put("error", "Invalid item ID!")
          )
        );
    }

    Item item = dataController.get(itemId);

    // If the data doesn't exist, return a not found response
    if (item == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(item.toJSONObject().toString());
  }

  @PostMapping("/create")
  public ResponseEntity<String> create(@RequestBody Item item) {
    if (
      item == null ||
      item.getName() == null ||
      item.getDescription() == null ||
      item.getPrice() == null ||
      Double.isNaN(item.getPrice()) ||
      item.getPrice() < 0
    ) {
      return ResponseEntity.badRequest()
        .body(
          JSONObject.valueToString(
            new JSONObject().put("error", "Invalid item!")
          )
        );
    }

    Item createdItem = new Item(
      item.getName(),
      item.getDescription(),
      item.getPrice()
    );
    long itemId = dataController.create(createdItem);
    return ResponseEntity.ok(
      JSONObject.valueToString(new JSONObject().put("id", itemId))
    );
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> delete(@PathVariable long id) {
    dataController.delete(id);
    return ResponseEntity.ok(
      JSONObject.valueToString(new JSONObject().put("id", id))
    );
  }
}
