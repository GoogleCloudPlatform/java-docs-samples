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

const PAGE_SIZE = 25;
let currentPage = 0;
let isLoading = false;
let leaderboardData = [];

// Central DOM reference object for easier maintenance
const elements = {
  entriesContainer: document.getElementById("entries-container"),
  filter: document.getElementById("filter"),
  search: document.getElementById("search"),
  prevPage: document.getElementById("prev-page"),
  nextPage: document.getElementById("next-page"),
  pageNumber: document.getElementById("page-number"),
  usernameInput: document.getElementById("username-input"),
  scoreInput: document.getElementById("score-input"),
  addEntry: document.getElementById("add-entry"),
  fromCache: document.getElementById("from-cache"),
  timeToFetch: document.getElementById("time-to-fetch"),
};

// Event Listeners
elements.filter.addEventListener("change", () => refreshLeaderboard());
elements.search.addEventListener("input", () => refreshLeaderboard());
elements.prevPage.addEventListener("click", previousPage);
elements.nextPage.addEventListener("click", nextPage);
elements.addEntry.addEventListener("click", addEntry);

async function fetchLeaderboard() {
  isLoading = true;
  const startTime = Date.now();

  try {
    // URLSearchParams automatically encodes URL parameters
    const params = new URLSearchParams({
      position: currentPage * PAGE_SIZE,
      size: PAGE_SIZE,
      orderBy: elements.filter.value,
    });

    if (elements.search.value) {
      params.append("username", elements.search.value);
    }

    const response = await fetch(`/api/leaderboard?${params}`);
    const data = await response.json();

    updateAnalytics(data.fromCache, Date.now() - startTime);
    return data;
  } catch (error) {
    console.error("Fetch error:", error);
    return { entries: [] };
  } finally {
    isLoading = false;
  }
}

function updateAnalytics(cacheStatus, duration) {
  // Ternary chain for cache status text
  elements.fromCache.textContent = cacheStatus ? "Yes" : "No";
  elements.timeToFetch.textContent = duration ? `${duration}ms` : "-";
}

async function refreshLeaderboard() {
  currentPage = 0;
  updatePagination();
  const data = await fetchLeaderboard();
  leaderboardData = data.entries;
  renderEntries();
}

function renderEntries() {
  elements.entriesContainer.innerHTML = isLoading
    ? loadingTemplate()
    : leaderboardData.length
    ? entriesTemplate()
    : emptyTemplate();
}

function loadingTemplate() {
  return `<div class="text-center py-8 text-gray-500">Loading entries...</div>`;
}

function emptyTemplate() {
  return `<div class="text-center py-8 text-gray-500">No entries found</div>`;
}

// Template functions separate markup generation
function entriesTemplate() {
  return `
    <ul class="divide-y">
      ${leaderboardData
        .map(
          (entry) => `
        <li class="py-3 hover:bg-gray-50">
          <div class="flex justify-between items-center">
            <span class="w-16">${entry.position}.</span>
            <span class="flex-1">${entry.username}</span>
            <span class="w-24 text-right font-mono text-blue-600">${entry.score}</span>
          </div>
        </li>
      `
        )
        .join("")}
    </ul>
  `;
}

async function addEntry() {
  const username = elements.usernameInput.value.trim();
  const score = parseInt(elements.scoreInput.value);

  if (!username || isNaN(score)) {
    alert("Please enter valid name and score");
    return;
  }

  try {
    const response = await fetch("/api/leaderboard", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, score }),
    });

    if (response.ok) {
      elements.usernameInput.value = "";
      elements.scoreInput.value = "";
      refreshLeaderboard();
    }
  } catch (error) {
    console.error("Submission error:", error);
  }
}

function updatePagination() {
  elements.pageNumber.textContent = currentPage + 1;
  elements.prevPage.disabled = currentPage === 0;
}

async function nextPage() {
  currentPage++;
  await loadPage();
}

async function previousPage() {
  currentPage = Math.max(0, currentPage - 1);
  await loadPage();
}

async function loadPage() {
  const data = await fetchLeaderboard();
  leaderboardData = data.entries;
  updatePagination();
  renderEntries();
}

// Initial load
refreshLeaderboard();
