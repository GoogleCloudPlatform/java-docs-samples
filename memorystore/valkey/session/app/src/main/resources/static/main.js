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

window.items = [];

let msTaken = new Date().getTime();

verifyToken().then((data) => {
  msTaken = new Date().getTime() - msTaken;

  if (!data) {
    window.location.href = "/login";
    return;
  }

  const { username, expirationSecs } = data;

  document.getElementById("response-time").textContent = msTaken;
  document.getElementById("session-token").textContent =
    document.cookie.split("=")[1];
  document.getElementById("session-expiry").textContent =
    formatTime(expirationSecs);
  document.querySelector(".username-value").textContent = username;

  document
    .querySelectorAll(".loading-required")
    .forEach((el) => el.classList.remove("hidden"));
  document.querySelector(".loading-required-inverse").classList.add("hidden");

  // Fetch items from the JSON file
  fetch("/items.json")
    .then((response) => {
      if (!response.ok) {
        throw new Error(`Failed to fetch items: ${response.statusText}`);
      }
      return response.json();
    })
    .then((items) => {
      window.items = items;

      // Populate product grid
      const productGrid = document.getElementById("product-grid");
      items.forEach((item) => {
        const card = document.createElement("div");
        card.className = "bg-white p-4 rounded-lg shadow-md";
        card.innerHTML = `
          <img src="${item.image_url}" alt="${
          item.name
        }" class="w-full h-48 object-cover mb-4 rounded">
          <h3 class="text-lg font-bold mb-2">${item.name}</h3>
          <p class="text-gray-600 text-sm mb-2">${item.description}</p>
          <p class="text-xl font-bold text-blue-600 mb-4">$${item.price.toFixed(
            2
          )}</p>
          <button onclick="addToBasket(${
            item.id
          })" class="w-full bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
            Add to Basket
          </button>
        `;
        productGrid.appendChild(card);
      });

      fetchBasket(); // Load the basket from the server
      updateBasketDisplay(); // Initialize the basket display

      document.getElementById("shopping-interface").classList.remove("hidden");
    })
    .catch((error) => {
      console.error("Error loading items:", error);
      alert("Failed to load items. Please try again later.");
    });
});

function logout() {
  fetch("/auth/logout", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      token: document.cookie.split("=")[1],
    }),
  }).then(() => {
    document.cookie =
      "session=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    window.location.href = "/login";
  });
}

window.logout = logout;
