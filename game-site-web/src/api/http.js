import axios from 'axios'

const http = axios.create({
    baseURL: '',
    timeout: 15000,
})

// 统一处理：后端返回 {code,data,msg}
http.interceptors.response.use(
    (res) => {
        const body = res?.data
        if (body && typeof body === 'object' && 'code' in body) {
            if (body.code === 0 || body.code === 200) return body.data
            const msg = body.msg || body.message || '请求失败'
            return Promise.reject(new Error(msg))
        }
        // 非统一结构就原样返回
        return body
    },
    (err) => Promise.reject(err)
)

export default http
