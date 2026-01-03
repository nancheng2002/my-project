// src/utils/cover.js
export function resolveCoverUrl(v) {
    if (!v) return "";
    const s = String(v);

    // 绝对 URL
    if (/^https?:\/\//i.test(s)) return s;

    // 以 / 开头（例如 /api/file/xxx.png）
    if (s.startsWith("/")) return s;

    // 只有文件名：xxx.png
    return `/api/file/${s}`;
}

export default resolveCoverUrl;
