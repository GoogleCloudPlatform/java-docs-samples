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
    }
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
