const PROTECTED_PATHS = ['/dashboard']
const GUEST_ONLY_PATHS = ['/login', '/register']
const DEFAULT_PROTECTED_REDIRECT = '/dashboard'

export function isProtectedPath(path) {
  return PROTECTED_PATHS.some((protectedPath) => path.startsWith(protectedPath))
}

export function isGuestOnlyPath(path) {
  return GUEST_ONLY_PATHS.some((guestOnlyPath) => path.startsWith(guestOnlyPath))
}

export function normalizeRedirectTarget(target) {
  if (typeof target !== 'string' || !target.startsWith('/')) {
    return DEFAULT_PROTECTED_REDIRECT
  }

  if (target.startsWith('/login') || target.startsWith('/register')) {
    return DEFAULT_PROTECTED_REDIRECT
  }

  return target
}

export function buildLoginRedirect(target) {
  const redirectTarget = normalizeRedirectTarget(target)
  return {
    path: '/login',
    query: {
      redirect: redirectTarget,
    },
  }
}

export function getCurrentAppPath() {
  return `${window.location.pathname}${window.location.search}${window.location.hash}`
}

export function redirectToLogin(target = getCurrentAppPath()) {
  const redirectTarget = normalizeRedirectTarget(target)
  const loginUrl = new URL('/login', window.location.origin)
  loginUrl.searchParams.set('redirect', redirectTarget)
  window.location.assign(loginUrl.toString())
}
