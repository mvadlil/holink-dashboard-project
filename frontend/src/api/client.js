import axios from 'axios'

import { clearAuthStorage, getToken } from '../utils/authStorage'
import { getCurrentAppPath, isProtectedPath, redirectToLogin } from '../utils/authRoute'

const client = axios.create({
  baseURL: 'http://localhost:8080',
})

client.interceptors.request.use((config) => {
  const token = getToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  } else if (config.headers?.Authorization) {
    delete config.headers.Authorization
  }

  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearAuthStorage()

      if (isProtectedPath(window.location.pathname)) {
        redirectToLogin(getCurrentAppPath())
      }
    }

    return Promise.reject(error)
  },
)

export default client
