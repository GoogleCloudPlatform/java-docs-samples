/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

let basket = {};

function fetchBasket() {
  fetch("http://localhost:8080/api/basket", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "localhost:3000",
      "Access-Control-Allow-Methods": "GET",
      "Access-Control-Allow-Credentials": "true",
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`Failed to load basket: ${response.statusText}`);
      }
      return response.json();
    })
    .then((data) => {
      if (!data) return;
      console.log("Loaded basket:", data);

      // returned data is a map
      basket = {};
      Object.keys(data).forEach((key) => {
        basket[key] = { id: parseInt(key), quantity: parseInt(data[key]) };
      });

      updateBasketDisplay();
    })
    .catch((error) => {
      console.error("Error loading basket:", error);
      alert("Failed to load basket. Please try again later.");
    });
}

function requestAddItem(itemId, quantity) {
  let msTaken = new Date().getTime();

  fetch(
    "http://localhost:8080/api/basket/add?itemId=" +
      itemId +
      "&quantity=" +
      quantity,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "localhost:3000",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Allow-Credentials": "true",
      },
    }
  )
    .then((response) => {
      msTaken = new Date().getTime() - msTaken;
      document.getElementById("response-time").textContent = msTaken;

      if (!response.ok) {
        throw new Error(`Failed to add item: ${response.statusText}`);
      }
      return response.text();
    })
    .then(() => {
      if (basket[itemId]) {
        basket[itemId].quantity += quantity;
      } else {
        basket[itemId] = { id: itemId, quantity };
      }

      updateBasketDisplay();
    })
    .catch((error) => {
      console.error("Error adding item:", error);
      alert("Failed to add item. Please try again later.");
    });
}

function requestRemoveItem(itemId, quantity) {
  let msTaken = new Date().getTime();

  fetch(
    "http://localhost:8080/api/basket/remove?itemId=" +
      itemId +
      "&quantity=" +
      quantity,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "localhost:3000",
        "Access-Control-Allow-Methods": "DELETE",
        "Access-Control-Allow-Credentials": "true",
      },
    }
  )
    .then((response) => {
      msTaken = new Date().getTime() - msTaken;
      document.getElementById("response-time").textContent = msTaken;

      if (!response.ok) {
        throw new Error(`Failed to remove item: ${response.statusText}`);
      }
      return response.text();
    })
    .then(() => {
      if (basket[itemId]) {
        basket[itemId].quantity -= quantity;
        if (basket[itemId].quantity <= 0) {
          delete basket[itemId];
        }
      }

      updateBasketDisplay();
    })
    .catch((error) => {
      console.error("Error removing item:", error);
      alert("Failed to remove item. Please try again later.");
    });
}

function requestClearBasket() {
  let msTaken = new Date().getTime();

  fetch("http://localhost:8080/api/basket/clear", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "localhost:3000",
      "Access-Control-Allow-Methods": "DELETE",
      "Access-Control-Allow-Credentials": "true",
    },
  })
    .then((response) => {
      msTaken = new Date().getTime() - msTaken;
      document.getElementById("response-time").textContent = msTaken;

      if (!response.ok) {
        throw new Error(`Failed to clear basket: ${response.statusText}`);
      }
      return response.text();
    })
    .then(() => {
      basket = {};
      updateBasketDisplay();
    })
    .catch((error) => {
      console.error("Error clearing basket:", error);
      alert("Failed to clear basket. Please try again later.");
    });
}

// Add item to basket
function addToBasket(itemId) {
  if (!window.items || window.items.length === 0) {
    alert("Items are not loaded yet. Please try again later.");
    return;
  }

  requestAddItem(itemId, 1);
}

// Remove item from basket
function removeFromBasket(itemId) {
  if (!window.items || window.items.length === 0) {
    alert("Items are not loaded yet. Please try again later.");
    return;
  }

  requestRemoveItem(itemId, 1);
}

// Clear basket
function clearBasket() {
  requestClearBasket();
}

// Purchase items
function checkoutItems() {
  alert("This operation is not supported in the demo.");
}

// Update basket display
function updateBasketDisplay() {
  const basketItems = document.getElementById("basket-items");
  const basketTotal = document.getElementById("basket-total");
  basketItems.innerHTML = "";
  let total = 0;

  if (Object.keys(basket).length === 0) {
    basketItems.innerHTML = `
      <p class="text-gray-500 text-center">Your basket is empty.</p>
    `;
    basketTotal.textContent = "$0.00";
    return;
  }

  Object.values(basket).forEach((item) => {
    const itemData = window.items.find((i) => i.id === item.id);
    total += itemData.price * item.quantity;

    const itemElement = document.createElement("div");
    itemElement.className = "flex justify-between items-center mb-2";
    itemElement.innerHTML = `
      <div class="flex-1">
        <h4 class="font-bold">${itemData.name}</h4>
        <p class="text-sm text-gray-600">$${itemData.price.toFixed(2)} x ${
      item.quantity
    }</p>
      </div>
      <button onclick="removeFromBasket(${
        item.id
      })" class="ml-4 text-red-500 hover:text-red-700">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
    `;

    basketItems.appendChild(itemElement);
  });

  basketTotal.textContent = `$${total.toFixed(2)}`;
}

// Expose functions globally for onclick handlers
window.addToBasket = addToBasket;
window.removeFromBasket = removeFromBasket;
window.clearBasket = clearBasket;
window.checkoutItems = checkoutItems;
