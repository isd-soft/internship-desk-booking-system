export function formatDateTimeLocal(dateStr: string): string {
    const date = new Date(dateStr);
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
        date.getDate()
    )}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatDate(dateStr: string | null): string {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
    });
}

export function formatTime(dateStr: string | null): string {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false, // This forces 24-hour format
    });
}

export function calculateDuration(startStr: string, endStr: string): string {
    if (!startStr || !endStr) return "—";

    const diffMs = +new Date(endStr) - +new Date(startStr);
    const totalMinutes = Math.max(0, Math.round(diffMs / 60000));
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    return hours > 0
        ? `${hours}h ${minutes.toString().padStart(2, "0")}m`
        : `${minutes}m`;
}

export function formatDateTime(dateStr: string): string {
    if (!dateStr) return "Not set";
    const date = new Date(dateStr);
    return date.toLocaleString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}