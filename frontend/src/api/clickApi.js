import client from './client'

export async function trackLinkClick(linkId, payload) {
  const response = await client.post(`/api/links/${linkId}/click`, payload)
  return response.data
}
