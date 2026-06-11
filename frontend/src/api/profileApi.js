import client from './client'

export async function getMyProfile() {
  const response = await client.get('/api/me/profile')
  return response.data
}

export async function createProfile(payload) {
  const response = await client.post('/api/profiles', payload)
  return response.data
}

export async function updateProfile(profileId, payload) {
  const response = await client.put(`/api/profiles/${profileId}`, payload)
  return response.data
}
