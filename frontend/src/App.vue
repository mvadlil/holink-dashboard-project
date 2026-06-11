<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

import {
  clearAuthStorage,
  getAuthEventName,
  getStoredUser,
  getToken,
} from './utils/authStorage'

const router = useRouter()
const storedUser = ref(null)
const hasToken = ref(false)

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
})

onUnmounted(() => {
  window.removeEventListener('storage', syncAuthState)
  window.removeEventListener(getAuthEventName(), syncAuthState)
})

function syncAuthState() {
  hasToken.value = Boolean(getToken())
  storedUser.value = getStoredUser()
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
          <RouterLink to="/dashboard">Dashboard</RouterLink>
          <RouterLink to="/u/kevincreator">Public Profile</RouterLink>
          <RouterLink v-if="!hasToken" to="/login">Login</RouterLink>
          <RouterLink v-if="!hasToken" to="/register">Register</RouterLink>
          <button v-if="hasToken" class="app-nav__button" type="button" @click="handleLogout">
            Logout
          </button>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
