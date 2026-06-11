import axios from 'axios'

import { getToken } from '../utils/authStorage'

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

export default client
