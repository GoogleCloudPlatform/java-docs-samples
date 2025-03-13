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

/** Set API URL */
const API_URL = window.location.origin + "/api";

/** API Helper Function */
async function apiRequest(url, options = {}) {
  let msTaken = new Date().getTime();
  try {
    const res = await fetch(API_URL + url, options);

    if (!res.ok && res.status === 500) {
      const errorText = "An error occurred";
      throw new Error(errorText);
    }

    if (!res.ok && res.status === 404) {
      const errorText = "Item not found";
      throw new Error(errorText);
    }

    const data = await res.json();

    /** Calculate response time in ms */
    msTaken = new Date().getTime() - msTaken;

    /** Update the UI for responses */
    setTiming(data.fromCache, msTaken);
    setResponse({ type: "", data: null });

    return { data };
  } catch (error) {
    setResponse({ type: "error", data: error.message });
    setLoading(false);
    return null;
  }
}

/** Set Global State */
let loading = false;

/** Add Element References */
const queryRef = document.getElementById("search-input");
const nameRef = document.getElementById("name");
const descriptionRef = document.getElementById("description");
const priceRef = document.getElementById("price");

const searchBtn = document.getElementById("search-btn");
const randomBtn = document.getElementById("retrieve-random-btn");
const createBtn = document.getElementById("create-btn");

const errorMsg = document.getElementById("error-msg");
const successMsg = document.getElementById("success-msg");

const fromCache = document.getElementById("from-cache");
const timeToFetch = document.getElementById("time-to-fetch");
const itemsContainer = document.getElementById("items");

/** Loading Utility Function */
function setLoading(value) {
  loading = value;

  /** Disable inputs on loading */
  searchBtn.disabled = loading;
  createBtn.disabled = loading;

  nameRef.disabled = loading;
  descriptionRef.disabled = loading;
  priceRef.disabled = loading;
}

/** Update UI Timing */
function setTiming(inFromCache, inTimeToFetch) {
  fromCache.textContent = inFromCache ? "yes" : "no";
  timeToFetch.textContent = isNaN(inTimeToFetch)
    ? "N/A"
    : `${inTimeToFetch} ms`;
}

/** Clear Input Fields */
function clearInputFields() {
  nameRef.value = "";
  descriptionRef.value = "";
  priceRef.value = "";
}

/** Set Response */
function setResponse({ type, data }) {
  console.log("Response:", type, data);
  /** Set success message */
  if (type === "success") {
    successMsg.style.display = "block";
    errorMsg.style.display = "none";
    successMsg.textContent = data;
    return;
  }

  /** Set error message */
  if (type === "error") {
    successMsg.style.display = "none";
    errorMsg.style.display = "block";
    errorMsg.textContent = data;
    return;
  }

  /** Clear response message */
  successMsg.style.display = "none";
  successMsg.textContent = "";
  errorMsg.style.display = "none";
  errorMsg.textContent = "";
}

/** Search for an Item */
async function search() {
  setLoading(true);
  itemsContainer.innerHTML = "";

  const resp = await apiRequest(`/item/${queryRef.value.trim()}`);
  if (resp && resp.data) {
    const itemElement = document.createElement("div");
    itemElement.className =
      "item flex flex-row gap-2.5 p-4 bg-gray-200 rounded";
    itemElement.innerHTML = addItemStyling(resp.data);

    itemElement.dataset.id = resp.data.id;

    const deleteBtn = itemElement.querySelector(".delete-btn");
    deleteBtn.addEventListener("click", () => deleteItem(resp.data.id));

    itemsContainer.appendChild(itemElement);
  }

  setLoading(false);
}

/** Retrieve Random Items */
async function retrieveRandom() {
  setLoading(true);
  itemsContainer.innerHTML = "";

  const resp = await apiRequest("/item/random");
  if (resp && resp.data && Array.isArray(resp.data.items)) {
    resp.data.items.forEach((item) => {
      const itemElement = document.createElement("div");
      itemElement.className =
        "item flex flex-row gap-2.5 p-4 bg-gray-200 rounded";
      itemElement.innerHTML = addItemStyling(item);

      itemElement.dataset.id = item.id;

      const deleteBtn = itemElement.querySelector(".delete-btn");
      deleteBtn.addEventListener("click", () => deleteItem(item.id));

      itemsContainer.appendChild(itemElement);
    });
  } else {
    setResponse({ type: "error", data: "No items found." });
  }

  setLoading(false);
}

/** Create a New Item */
async function createItem() {
  const name = nameRef.value.trim();
  const description = descriptionRef.value.trim();
  const price = parseFloat(priceRef.value.trim());

  if (!name || !description || isNaN(price)) {
    setResponse({
      type: "error",
      data: "Please fill in all fields with valid data",
    });
    return;
  }

  setLoading(true);

  const resp = await apiRequest("/item/create", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, description, price }),
  });

  if (resp && resp.data) {
    setResponse({
      type: "success",
      data: `Item created successfully with ID: ${resp.data.id}`,
    });
    clearInputFields();
  }

  setLoading(false);
}

/** Delete an Item */
async function deleteItem(id) {
  setLoading(true);

  const resp = await apiRequest(`/item/delete/${id}`, { method: "DELETE" });

  if (!resp) {
    setResponse({ type: "error", data: "Failed to delete item" });
    setLoading(false);
    return;
  }
  setResponse({ type: "success", data: "Item deleted successfully" });

  /** Remove the item from the UI */
  const itemElement = document.querySelector(`.item[data-id="${id}"]`);
  if (itemElement) {
    itemElement.remove();
  }

  /** Reset loading status */
  setLoading(false);
}

/** Add Item Styling */
function addItemStyling({ id, name, price, description }) {
  return `
    <p class="m-auto pl-8 pr-16 text-lg font-bold">ID: <span class="id">${id}</span></p>
    <div class="flex-1">
        <div class="flex flex-row justify-between mb-3">
            <p class="text-lg">Name: <span class="name">${name}</span></p>
            <p class="text-lg">Price: <span class="price">$${price}</span></p>
        </div>
        <p class="text-md text-gray-600 mb-3"><span class="description">${description}</span></p>
        <div class="flex justify-end">
          <button class="delete-btn p-2 text-md text-white bg-red-500 rounded">Delete item</button>
        </div>
    </div>`;
}

/** Initialize */
function init() {
  errorMsg.style.display = "none";
  successMsg.style.display = "none";
}

window.addEventListener("load", init);
searchBtn.addEventListener("click", search);
randomBtn.addEventListener("click", retrieveRandom);
createBtn.addEventListener("click", createItem);
