import http from './http'

/** 这里是你后端“图片读取”的前缀（如果你后端不是 /api/file/xxx，就改这里一处就行） */
export const FILE_GET_PREFIX = '/api/file/'

export function buildFileUrl(filename) {
    return FILE_GET_PREFIX + encodeURIComponent(filename)
}

export const uploadApi = {
    async uploadFile(file) {
        const fd = new FormData()
        fd.append('file', file)
        return http.post('/api/upload', fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
    },
}
