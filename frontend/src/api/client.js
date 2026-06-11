import axios from 'axios'

const client = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'X-User-Id': 'user_001',
  },
})

export default client
