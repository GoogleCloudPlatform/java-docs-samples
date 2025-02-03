/**
 * Provides a RESTful API for interacting with the application's data.
 *
 * The controller contains three routes:
 * - GET /item/{id} - Get an item by ID
 * - POST /item/create - Create a new item
 * - DELETE /item/delete/{id} - Delete an item by ID
 */

package app;

import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/item")
public class ItemController {

  public static final int TOTAL_RANDOM_ITEMS = 10;

  private final DataController dataController;

  public ItemController(DataController dataController) {
    this.dataController = dataController;
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> read(@PathVariable Long id) {
    Item item = dataController.get(id);

    if (item == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(item.toJSONObject().toString());
  }

  @GetMapping("/random")
  public ResponseEntity<String> read() {
    return ResponseEntity.ok(
      new JSONObject()
        .put("items", dataController.getMultiple(TOTAL_RANDOM_ITEMS))
        .toString()
    );
  }

  @PostMapping("/create")
  public ResponseEntity<String> create(@Valid @RequestBody Item item) {
    /** Create a new item */
    Item createdItem = new Item(
      item.getName(),
      item.getDescription(),
      item.getPrice()
    );

    /** Save the item */
    long itemId = dataController.create(createdItem);

    /** Return a successful response */
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
