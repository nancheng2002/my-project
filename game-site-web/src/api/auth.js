import http from './http'

export const authApi = {
  login(data) {
    return http.post('/api/auth/login', data)
  }
}
