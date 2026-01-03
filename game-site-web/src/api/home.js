import http from './http'

export const homeApi = {
    categories() {
        return http.get('/api/home/categories')
    },
    recommend() {
        return http.get('/api/home/recommend')
    },
    gameDetail(id) {
        return http.get(`/api/home/games/${id}`)
    }
}
