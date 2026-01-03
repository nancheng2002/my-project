import http from './http'

export const gamesApi = {
    page(params) {
        return http.get('/api/games', { params })
    },
    detail(id) {
        return http.get(`/api/games/${id}`)
    },
    add(data) {
        return http.post('/api/games', data)
    },
    update(id, data) {
        return http.put(`/api/games/${id}`, data)
    },
    remove(id) {
        return http.delete(`/api/games/${id}`)
    },
    setStatus(id, status) {
        return http.put(`/api/games/${id}/status`, null, { params: { status } })
    }
}
