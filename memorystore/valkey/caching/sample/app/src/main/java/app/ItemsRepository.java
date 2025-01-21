/**
 * Handles CRUD operations for the items table.
 */

package app;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ItemsRepository {

  private final JdbcTemplate jdbcTemplate;

  public ItemsRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<Item> get(long id) {
    try {
      return Optional.ofNullable(
        jdbcTemplate.queryForObject(
          "SELECT * FROM items WHERE id = ?",
          (rs, rowNum) ->
            new Item(
              rs.getLong("id"),
              rs.getString("name"),
              rs.getString("description"),
              rs.getDouble("price")
            ),
          id
        )
      );
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public List<Item> getMultiple(int amount) {
    return jdbcTemplate.query(
      "SELECT * FROM items ORDER BY random() LIMIT ?",
      (rs, rowNum) ->
        new Item(
          rs.getLong("id"),
          rs.getString("name"),
          rs.getString("description"),
          rs.getDouble("price")
        ),
      amount
    );
  }

  public long create(Item item) {
    String name = item.getName();
    String description = item.getDescription();
    double price = item.getPrice();

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
      connection -> {
        PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO items (name, description, price) VALUES (?, ?, ?)",
          new String[] { "id" } // Explicitly specify the generated key column
        );
        ps.setString(1, name);
        ps.setString(2, description);
        ps.setDouble(3, price);
        return ps;
      },
      keyHolder
    );

    // Ensure the keyHolder contains the generated ID only
    Map<String, Object> keys = keyHolder.getKeys();
    if (keys != null && keys.size() > 1) {
      throw new IllegalStateException(
        "Expected a single key, but multiple keys were returned: " + keys
      );
    }

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new IllegalStateException("No key generated during insert");
    }

    return key.longValue();
  }

  public void delete(long id) {
    jdbcTemplate.update("DELETE FROM items WHERE id = ?", id);
  }

  public boolean exists(long id) {
    /** Set the query to execute */
    String query = "SELECT EXISTS(SELECT 1 FROM items WHERE id = ?)";

    /** Return query result */
    return Boolean.TRUE.equals(
      jdbcTemplate.queryForObject(query, Boolean.class, id)
    );
  }
}
