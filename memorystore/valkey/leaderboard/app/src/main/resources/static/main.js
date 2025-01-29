const PAGE_SIZE = 25;

document.addEventListener("DOMContentLoaded", () => {
  let isLoading = true;
  let position = 0;
  let leaderboard = [];
  let page = 0;
  const fromCache = document.getElementById("from-cache");
  const timeToFetch = document.getElementById("time-to-fetch");

  function setTiming(inFromCache, inTimeToFetch) {
    // Update from-cache and time-to-fetch text
    fromCache.textContent = inFromCache ? "yes" : "no";
    switch (inFromCache) {
      case 0:
        fromCache.textContent = "no";
        break;
      case 1:
        fromCache.textContent = "partial";
        break;
      case 2:
        fromCache.textContent = "yes";
        break;
    }
    timeToFetch.textContent = isNaN(inTimeToFetch)
      ? "N/A"
      : inTimeToFetch + "ms";
  }

  function loadLeaderboard() {
    isLoading = true;
    render();
    setTiming(false, null);
    let msTaken = new Date().getTime();

    fetch(`/api/leaderboard?position=${page * PAGE_SIZE}`)
      .then((response) => {
        msTaken = new Date().getTime() - msTaken;

        return response.json();
      })
      .then((data) => {
        position = data.position;
        leaderboard = data.entries;

        setTiming(data.fromCache, msTaken);
      })
      .finally(() => {
        isLoading = false;
        render();
      });
  }

  function setPageNumber() {
    const pageNumber = document.getElementById("page-number");
    pageNumber.innerText = page + 1;
  }

  function upPage() {
    page += 1;
    setPageNumber();
    loadLeaderboard();
  }

  function downPage() {
    if (page > 0) {
      page -= 1;
      setPageNumber();
      loadLeaderboard();
    }
  }

  function render() {
    const app = document.getElementById("listings-container");
    app.innerHTML = `
      <ul class="relative w-full list-none p-0 m-0">
        ${
          isLoading
            ? `<p class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-2xl">Loading...</p>`
            : leaderboard.length > 0
              ? leaderboard
                  .map(
                    (entry, index) => `
              <li class="flex items-center justify-center w-full bg-gray-100 text-lg mb-2">
                <div class="w-[600px] max-w-[80%] flex justify-between items-center h-24">
                  <b>${position + index + 1}.</b>
                  <span>${entry.username}</span>
                  <span class="text-yellow-600">(${entry.score})</span>
                </div>
              </li>
            `,
                  )
                  .join("")
              : `<p class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-2xl">No entries found</p>`
        }
      </ul>
    `;
  }

  window.upPage = upPage;
  window.downPage = downPage;
  loadLeaderboard();
});
