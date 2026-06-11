<script setup>
import axios from 'axios'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { getCurrentUser } from './api/authApi'
import { buildLoginRedirect, isProtectedPath } from './utils/authRoute'
import {
  clearAuthStorage,
  getAuthEventName,
  getStoredUser,
  setStoredUser,
  getToken,
} from './utils/authStorage'

const router = useRouter()
const storedUser = ref(null)
const hasToken = ref(false)
const isAuthChecking = ref(false)

const authLabel = computed(() => {
  if (!storedUser.value) {
    return ''
  }

  return storedUser.value.name || storedUser.value.email || ''
})

const authSubLabel = computed(() => storedUser.value?.email || '')

onMounted(() => {
  syncAuthState()
  window.addEventListener('storage', syncAuthState)
  window.addEventListener(getAuthEventName(), syncAuthState)
  validateStoredSession()
})

onUnmounted(() => {
  window.removeEventListener('storage', syncAuthState)
  window.removeEventListener(getAuthEventName(), syncAuthState)
})

function syncAuthState() {
  hasToken.value = Boolean(getToken())
  storedUser.value = getStoredUser()
}

async function validateStoredSession() {
  if (!getToken()) {
    return
  }

  isAuthChecking.value = true

  try {
    const currentUser = await getCurrentUser()
    setStoredUser(currentUser)
  } catch (error) {
    if (!axios.isAxiosError(error) || error.response?.status !== 401) {
      clearAuthStorage()

      if (isProtectedPath(router.currentRoute.value.path)) {
        await router.replace(buildLoginRedirect(router.currentRoute.value.fullPath))
      }
    }
  } finally {
    isAuthChecking.value = false
  }
}

async function handleLogout() {
  clearAuthStorage()
  await router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand-block">
        <p class="brand-kicker">HoLink Prototype</p>
        <p class="brand-title">Vue Frontend Base</p>
      </div>

      <div class="app-header__controls">
        <div v-if="storedUser" class="auth-badge">
          <span class="auth-badge__label">{{ authLabel }}</span>
          <span class="auth-badge__meta">{{ authSubLabel }}</span>
        </div>

        <nav class="app-nav" aria-label="Primary">
          <RouterLink to="/u/kevincreator">Public Profile</RouterLink>
          <RouterLink v-if="hasToken" to="/dashboard">Dashboard</RouterLink>
          <RouterLink v-if="!hasToken" to="/login">Login</RouterLink>
          <RouterLink v-if="!hasToken" to="/register">Create Account</RouterLink>
          <button v-if="hasToken" class="app-nav__button" type="button" @click="handleLogout">
            Logout
          </button>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <p v-if="isAuthChecking" class="auth-status">Checking your session...</p>
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.auth-status {
  margin: 0 0 14px;
  color: var(--muted);
  font-weight: 700;
}
</style>
