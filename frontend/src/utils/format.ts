export function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
  });
}

export function formatTime(dateStr: string) {
  return new Date(dateStr).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formatDuration(startStr: string, endStr: string) {
  const diff = new Date(endStr).getTime() - new Date(startStr).getTime();
  const min = Math.round(diff / 60000);
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}h ${m.toString().padStart(2, "0")}m` : `${m}m`;
}

export function statusToColor(s?: string) {
  if (!s) return "primary";
  return s === "CONFIRMED"
    ? "success"
    : s === "CANCELLED"
    ? "error"
    : "primary";
}
