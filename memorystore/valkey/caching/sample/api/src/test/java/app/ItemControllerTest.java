package app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

  @Autowired
  private MockMvc mockMvc; // MockMvc is used to perform HTTP requests in tests

  @MockBean
  private DataController dataController; // Mocked dependency of ItemController

  @Test
  @DisplayName("Test reading an item by ID")
  void testReadItem() throws Exception {
    // Arrange: DataController returns an Item for the provided ID
    long itemId = 1;
    Item item = new Item(1L, "ItemName", "ItemDescription", 100.0);

    given(dataController.get(itemId)).willReturn(item); // Simulate DataController behavior

    // Act: Perform GET /item/1
    mockMvc
      .perform(get("/item/{id}", itemId))
      .andExpect(status().isOk()) // Assert HTTP status is 200 OK
      .andExpect(content().string(Matchers.containsString("\"id\":1"))) // Assert JSON contains "id":1
      .andExpect(
        content().string(Matchers.containsString("\"name\":\"ItemName\""))
      ) // Assert JSON contains name
      .andExpect(
        content()
          .string(
            Matchers.containsString("\"description\":\"ItemDescription\"")
          )
      ) // Assert description
      .andExpect(content().string(Matchers.containsString("\"price\":100"))); // Assert price

    // Assert: Verify DataController's get method was called with the correct ID
    verify(dataController).get(itemId);
  }

  @Test
  @DisplayName("Test reading an item that does not exist")
  void testReadItem_NotFound() throws Exception {
    // Arrange: DataController returns null for the provided ID
    long itemId = 2;
    given(dataController.get(itemId)).willReturn(null); // Simulate item not found

    // Act: Perform GET /item/2
    mockMvc.perform(get("/item/{id}", itemId)).andExpect(status().isNotFound()); // Assert HTTP status is 404 Not Found

    // Assert: Verify DataController's get method was called
    verify(dataController).get(itemId);
  }

  @Test
  @DisplayName("Test creating a new item")
  void testCreateItem() throws Exception {
    // Arrange: DataController successfully creates the item
    Item item = new Item("NewItem", "NewDescription", 200.0);

    given(dataController.create(any(Item.class))).willReturn(0L); // Simulate creation with ID 0

    // Act: Perform POST /item/create
    JSONObject itemJson = item.toJSONObject();
    itemJson.remove("id"); // Remove ID from JSON for creation
    mockMvc
      .perform(
        post("/item/create")
          .contentType("application/json") // Specify JSON content type
          .content(itemJson.toString()) // Convert JSON to string
      )
      .andExpect(status().isOk()) // Assert HTTP status is 200 OK
      .andExpect(content().string("{\"id\":0}")); // Assert response message

    // Assert: Verify DataController's create method was called with an argument matching the expected properties
    verify(dataController).create(
      argThat(
        argument ->
          argument.getName().equals("NewItem") &&
          argument.getDescription().equals("NewDescription") &&
          argument.getPrice() == 200.0
      )
    );
  }

  @Test
  @DisplayName("Test creating an item with invalid request body")
  void testCreateItem_Validations() throws Exception {
    // Act: Perform POST /item/create with an invalid request body (missing required fields)
    mockMvc
      .perform(
        post("/item/create")
          .contentType("application/json") // Specify JSON content type
          .content("{\"name\":\"NewItem\"}") // Incomplete JSON (missing fields)
      )
      .andExpect(status().isBadRequest()) // Assert HTTP status is 400 Bad Request
      .andExpect(content().string("{\"error\":\"Invalid item!\"}")); // Assert response message

    // Assert: Verify DataController's create method was not called
    verify(dataController, never()).create(any());
  }

  @Test
  @DisplayName("Test deleting an item")
  void testDeleteItem() throws Exception {
    // Act: Perform DELETE /item/delete/5
    long itemId = 5;
    mockMvc
      .perform(delete("/item/delete/{id}", itemId))
      .andExpect(status().isOk()) // Assert HTTP status is 200 OK
      .andExpect(content().string("{\"id\":5}")); // Assert response message

    // Assert: Verify DataController's delete method was called with the correct ID
    verify(dataController).delete(itemId);
  }
}
