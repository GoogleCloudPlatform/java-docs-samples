/**
 * The Auth controller for the application.
 *
 * <p>The controller contains the following endpoints: - GET /api/basket - Get all items - POST
 * /api/basket/add - Add item with quantity - POST /api/basket/remove - Remove item quantity - POST
 * /api/basket/clear - Clear entire basket
 */
package app;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import redis.clients.jedis.Jedis;

import java.util.Map;

@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final Jedis jedis;

    public BasketController(Jedis jedis) {
        this.jedis = jedis;
    }

    // Get all items
    @GetMapping
    public ResponseEntity<Map<String, String>> getBasket(HttpServletRequest request) {
        String basketKey = getBasketKey(request);
        return ResponseEntity.ok(jedis.hgetAll(basketKey));
    }

    // Add item with quantity
    @PostMapping("/add")
    public ResponseEntity<String> addItem(
            @RequestParam String itemId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpServletRequest request) {
        String basketKey = getBasketKey(request);
        long newQty = jedis.hincrBy(basketKey, itemId, quantity);
        return ResponseEntity.ok("Quantity updated: " + newQty);
    }

    // Remove item quantity
    @PostMapping("/remove")
    public ResponseEntity<String> removeItem(
            @RequestParam String itemId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpServletRequest request) {
        String basketKey = getBasketKey(request);
        long newQty = jedis.hincrBy(basketKey, itemId, -quantity);
        if (newQty <= 0) {
            jedis.hdel(basketKey, itemId);
            return ResponseEntity.ok("Item removed");
        }
        return ResponseEntity.ok("Quantity updated: " + newQty);
    }

    // Clear entire basket
    @PostMapping("/clear")
    public ResponseEntity<String> clearBasket(HttpServletRequest request) {
        jedis.del(getBasketKey(request));
        return ResponseEntity.ok("Basket cleared");
    }

    private String getBasketKey(HttpServletRequest request) {
        String token = Utils.getTokenFromCookie(request.getCookies());
        return "basket:" + token;
    }
}
