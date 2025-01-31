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

function formatTime(seconds) {
  if (seconds < 0) {
    throw new Error("Seconds cannot be negative");
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;

  const timeParts = [];

  if (hours > 0) timeParts.push(`${hours} ${hours === 1 ? "hour" : "hours"}`);
  if (minutes > 0)
    timeParts.push(`${minutes} ${minutes === 1 ? "minute" : "minutes"}`);
  if (remainingSeconds > 0 || timeParts.length === 0) {
    timeParts.push(
      `${remainingSeconds} ${remainingSeconds === 1 ? "second" : "seconds"}`
    );
  }

  return timeParts.join(", ");
}
