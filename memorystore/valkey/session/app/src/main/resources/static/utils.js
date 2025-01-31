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
      `${remainingSeconds} ${remainingSeconds === 1 ? "second" : "seconds"}`,
    );
  }

  return timeParts.join(", ");
}
