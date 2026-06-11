import client from './client'

export async function getLinkAnalytics() {
  const response = await client.get('/api/analytics/links')
  return response.data
}
