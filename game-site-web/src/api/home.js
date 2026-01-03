import http from './http'

export const homeApi = {
  categories() {
    return http.get('/api/home/categories')
  },
  recommend() {
    return http.get('/api/home/recommend')
  },
  dashboard() {
    return http.get('/api/dashboard/overview')
  }
}
