import client from './client'

export async function createLink(payload) {
  const response = await client.post('/api/links', payload)
  return response.data
}

export async function updateLink(linkId, payload) {
  const response = await client.put(`/api/links/${linkId}`, payload)
  return response.data
}

export async function deleteLink(linkId) {
  await client.delete(`/api/links/${linkId}`)
}
