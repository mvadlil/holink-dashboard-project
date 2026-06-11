import client from './client'

export async function getPublicProfile(username) {
  const response = await client.get(`/api/public/${encodeURIComponent(username)}`)
  return response.data
}
