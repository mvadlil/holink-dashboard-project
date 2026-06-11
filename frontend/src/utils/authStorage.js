const TOKEN_KEY = 'holink_auth_token'
const USER_KEY = 'holink_auth_user'
const AUTH_EVENT = 'holink-auth-changed'

export function getToken() {
  return window.localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  window.localStorage.setItem(TOKEN_KEY, token)
  notifyAuthChanged()
}

export function removeToken() {
  window.localStorage.removeItem(TOKEN_KEY)
  notifyAuthChanged()
}

export function getStoredUser() {
  const rawValue = window.localStorage.getItem(USER_KEY)
  if (!rawValue) {
    return null
  }

  try {
    return JSON.parse(rawValue)
  } catch {
    window.localStorage.removeItem(USER_KEY)
    return null
  }
}

export function setStoredUser(user) {
  window.localStorage.setItem(USER_KEY, JSON.stringify(user))
  notifyAuthChanged()
}

export function removeStoredUser() {
  window.localStorage.removeItem(USER_KEY)
  notifyAuthChanged()
}

export function clearAuthStorage() {
  window.localStorage.removeItem(TOKEN_KEY)
  window.localStorage.removeItem(USER_KEY)
  notifyAuthChanged()
}

export function getAuthEventName() {
  return AUTH_EVENT
}

function notifyAuthChanged() {
  window.dispatchEvent(new Event(AUTH_EVENT))
}
