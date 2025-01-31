async function verifyToken() {
  // Get from cachingMode from localStorage
  const cachingModeLocalStorage = localStorage.getItem("cachingMode");
  let cachingMode =
    cachingModeLocalStorage === null
      ? true
      : cachingModeLocalStorage === "true";

  // Verify session
  const data = await fetch(
    "http://localhost:8080/auth/verify?useCaching=" + cachingMode,
    {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "localhost:3000",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Allow-Credentials": "true",
      },
    },
  )
    .then((response) => {
      if (!response.ok) {
        return null;
      }

      return response.json();
    })
    .then((data) => {
      if (data === null) {
        return null;
      }

      return data;
    });

  return data;
}
