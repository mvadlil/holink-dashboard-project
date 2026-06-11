<script setup>
import axios from 'axios'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { trackLinkClick } from '../api/clickApi'
import { getPublicProfile } from '../api/publicApi'
import EmptyState from '../components/EmptyState.vue'
import ErrorMessage from '../components/ErrorMessage.vue'
import LoadingState from '../components/LoadingState.vue'

const route = useRoute()

const username = computed(() => route.params.username ?? '')
const isLoading = ref(true)
const isRedirectingLinkId = ref('')
const notFoundMessage = ref('')
const pageErrorMessage = ref('')
const profile = ref(null)
const links = ref([])
const hasAvatarLoadError = ref(false)

const avatarFallback = computed(() => {
  const source = profile.value?.displayName || profile.value?.username || username.value || 'HoLink'
  const normalizedParts = source
    .trim()
    .split(/\s+/)
    .filter(Boolean)

  if (normalizedParts.length === 0) {
    return 'H'
  }

  if (normalizedParts.length === 1) {
    return normalizedParts[0].charAt(0).toUpperCase()
  }

  return normalizedParts
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
})

const shouldShowAvatarImage = computed(() => {
  return Boolean(profile.value?.avatarUrl) && !hasAvatarLoadError.value
})

onMounted(() => {
  loadPublicProfile()
})

watch(
  () => route.params.username,
  () => {
    loadPublicProfile()
  },
)

async function loadPublicProfile() {
  isLoading.value = true
  pageErrorMessage.value = ''
  notFoundMessage.value = ''
  profile.value = null
  links.value = []
  hasAvatarLoadError.value = false

  try {
    const response = await getPublicProfile(username.value)
    profile.value = response.profile
    links.value = response.links ?? []
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      notFoundMessage.value = `We could not find a public profile for @${username.value}.`
      return
    }

    pageErrorMessage.value = toFriendlyMessage(
      error,
      'We could not load this public profile right now.',
    )
  } finally {
    isLoading.value = false
  }
}

function handleAvatarError() {
  hasAvatarLoadError.value = true
}

async function handleLinkClick(link) {
  isRedirectingLinkId.value = link.id
  pageErrorMessage.value = ''

  try {
    const response = await trackLinkClick(link.id, getUtmPayload())
    window.location.href = response.redirectUrl
  } catch (error) {
    if (link.url) {
      window.location.href = link.url
      return
    }

    pageErrorMessage.value = toFriendlyMessage(
      error,
      'We could not open this link right now. Please try again in a moment.',
    )
  } finally {
    isRedirectingLinkId.value = ''
  }
}

function getUtmPayload() {
  const params = new URLSearchParams(window.location.search)

  return {
    utmSource: params.get('utm_source') || params.get('utmSource') || null,
    utmMedium: params.get('utm_medium') || params.get('utmMedium') || null,
    utmCampaign: params.get('utm_campaign') || params.get('utmCampaign') || null,
  }
}

function toFriendlyMessage(error, fallbackMessage) {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Public profile service is unreachable right now. Please try again shortly.'
    }

    return error.response.data?.message || fallbackMessage
  }

  return fallbackMessage
}
</script>

<template>
  <section class="page-shell public-page">
    <div class="page-card public-card">
      <LoadingState v-if="isLoading" />

      <div v-else class="public-stack">
        <ErrorMessage v-if="pageErrorMessage" :message="pageErrorMessage" />

        <EmptyState
          v-else-if="notFoundMessage"
          title="Profile not found"
          :description="notFoundMessage"
        />

        <template v-else-if="profile">
          <header class="public-hero">
            <div class="public-avatar-wrap">
              <img
                v-if="shouldShowAvatarImage"
                class="public-avatar"
                :src="profile.avatarUrl"
                :alt="`${profile.displayName} avatar`"
                @error="handleAvatarError"
              />
              <div v-else class="public-avatar public-avatar--fallback" aria-hidden="true">
                {{ avatarFallback }}
              </div>
            </div>

            <div class="public-copy">
              <p class="eyebrow">Public Profile</p>
              <h1>{{ profile.displayName }}</h1>
              <p class="public-username">@{{ profile.username }}</p>
              <p v-if="profile.bio" class="page-copy public-bio">{{ profile.bio }}</p>
            </div>
          </header>

          <EmptyState
            v-if="links.length === 0"
            title="No active links yet"
            description="This profile is live, but there are no active destinations to visit right now."
          />

          <div v-else class="public-links">
            <a
              v-for="link in links"
              :key="link.id"
              class="public-link-card"
              :href="link.url"
              @click.prevent="handleLinkClick(link)"
            >
              <div class="public-link-copy">
                <h2>{{ link.title }}</h2>
                <p>{{ link.url }}</p>
              </div>
              <span class="public-link-action">
                {{ isRedirectingLinkId === link.id ? 'Opening...' : 'Visit' }}
              </span>
            </a>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.public-page {
  display: grid;
}

.public-card {
  display: grid;
  gap: 20px;
}

.public-stack {
  display: grid;
  gap: 24px;
}

.public-hero {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 22px;
  align-items: start;
}

.public-avatar-wrap {
  display: grid;
}

.public-avatar {
  width: 108px;
  height: 108px;
  border-radius: 999px;
  object-fit: cover;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  background: var(--surface-strong);
}

.public-avatar--fallback {
  display: grid;
  place-items: center;
  color: var(--accent);
  font-family: var(--font-heading);
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  background:
    radial-gradient(circle at top left, rgba(182, 84, 45, 0.18), transparent 54%),
    linear-gradient(180deg, #fffaf2 0%, #f7ecdc 100%);
}

.public-copy {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.public-copy h1 {
  margin: 0;
}

.public-username {
  margin: 0;
  color: var(--accent);
  font-weight: 700;
  word-break: break-word;
}

.public-bio {
  max-width: 44rem;
  word-break: break-word;
}

.public-links {
  display: grid;
  gap: 16px;
}

.public-link-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 20px 22px;
  border: 1px solid var(--line);
  border-radius: 22px;
  background: var(--surface-strong);
  text-decoration: none;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.public-link-card:hover {
  transform: translateY(-2px);
  border-color: rgba(182, 84, 45, 0.3);
  box-shadow: 0 14px 30px rgba(78, 55, 33, 0.1);
}

.public-link-copy {
  min-width: 0;
}

.public-link-copy h2 {
  margin: 0 0 6px;
  font-family: var(--font-heading);
  font-size: 1.25rem;
}

.public-link-copy p {
  margin: 0;
  color: var(--muted);
  word-break: break-word;
}

.public-link-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 16px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent);
  font-weight: 700;
  white-space: nowrap;
}

@media (max-width: 720px) {
  .public-hero {
    grid-template-columns: 1fr;
  }

  .public-avatar {
    width: 92px;
    height: 92px;
  }

  .public-link-card {
    grid-template-columns: 1fr;
    align-items: start;
  }
}
</style>
