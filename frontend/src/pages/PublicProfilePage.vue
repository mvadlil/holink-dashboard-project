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

function getLinkHint(link) {
  const normalizedTitle = (link.title || '').toLowerCase()

  if (normalizedTitle.includes('instagram')) {
    return 'Visual diary and daily updates'
  }

  if (normalizedTitle.includes('profile') || normalizedTitle.includes('portfolio')) {
    return 'Professional portfolio and links'
  }

  return link.url
}

function getLinkIcon(link) {
  const normalizedTitle = (link.title || '').toLowerCase()

  if (normalizedTitle.includes('instagram')) {
    return 'IG'
  }

  if (normalizedTitle.includes('youtube')) {
    return 'YT'
  }

  if (normalizedTitle.includes('profile') || normalizedTitle.includes('portfolio')) {
    return 'P'
  }

  return (link.title || 'L').charAt(0).toUpperCase()
}

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
  <section class="public-page">
    <div class="public-card">
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
              <h1>{{ profile.displayName }}</h1>
              <p class="public-username">@{{ profile.username }}</p>
              <p v-if="profile.bio" class="public-bio">{{ profile.bio }}</p>
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
              <div class="public-link-copy-wrap">
                <div class="public-link-icon" aria-hidden="true">
                  {{ getLinkIcon(link) }}
                </div>

                <div class="public-link-copy">
                  <h2>{{ link.title }}</h2>
                  <p>{{ getLinkHint(link) }}</p>
                </div>
              </div>

              <span class="public-link-action">
                {{ isRedirectingLinkId === link.id ? 'Opening...' : 'Visit' }}
              </span>
            </a>
          </div>

          <footer class="public-footer">
            <span>Powered by</span>
            <strong>HoLink</strong>
          </footer>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.public-page {
  display: grid;
  min-height: 100vh;
  place-items: start center;
  padding: 44px 20px 32px;
  background:
    radial-gradient(circle at top, rgba(255, 236, 222, 0.75), transparent 26%),
    linear-gradient(180deg, #faf7f1 0%, #f5f0e8 100%);
}

.public-card {
  width: min(100%, 640px);
  display: grid;
  gap: 20px;
  padding: 0;
  border: none;
  background: transparent;
  box-shadow: none;
}

.public-stack {
  display: grid;
  gap: 24px;
}

.public-hero {
  display: grid;
  justify-items: center;
  text-align: center;
  gap: 16px;
}

.public-avatar-wrap {
  display: grid;
}

.public-avatar {
  width: 116px;
  height: 116px;
  border-radius: 999px;
  object-fit: cover;
  border: 2px solid rgba(255, 250, 244, 0.95);
  box-shadow: 0 20px 40px rgba(121, 109, 95, 0.12);
  background: #fffdf9;
}

.public-avatar--fallback {
  display: grid;
  place-items: center;
  color: #fff8f1;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  background:
    radial-gradient(circle at top left, rgba(255, 206, 176, 0.22), transparent 54%),
    linear-gradient(135deg, #b85a16 0%, #cf6d22 100%);
}

.public-copy {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.public-copy h1 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: clamp(2rem, 4vw, 2.7rem);
  line-height: 1.05;
  letter-spacing: -0.04em;
  color: #132642;
}

.public-username {
  margin: 0;
  color: #6f7b90;
  font-weight: 700;
  word-break: break-word;
}

.public-bio {
  margin: 4px 0 0;
  max-width: 28rem;
  color: #58667b;
  line-height: 1.7;
  word-break: break-word;
}

.public-links {
  display: grid;
  gap: 14px;
}

.public-link-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 18px 18px 18px 16px;
  border: 1px solid rgba(206, 215, 231, 0.95);
  border-radius: var(--radius-card-sm);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 12px 26px rgba(121, 109, 95, 0.05);
  text-decoration: none;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.public-link-card:hover {
  transform: translateY(-2px);
  border-color: rgba(184, 90, 22, 0.26);
  box-shadow: 0 18px 34px rgba(121, 109, 95, 0.09);
}

.public-link-copy-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.public-link-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  background: #eef4ff;
  color: #b85a16;
  font-weight: 800;
}

.public-link-copy {
  min-width: 0;
}

.public-link-copy h2 {
  margin: 0 0 6px;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1.02rem;
  color: #132642;
}

.public-link-copy p {
  margin: 0;
  color: #6f7b90;
  font-size: 0.9rem;
  word-break: break-word;
}

.public-link-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 82px;
  padding: 10px 16px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--line-soft);
  background: #fff;
  color: var(--text-strong);
  font-weight: 700;
  white-space: nowrap;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.public-link-card:hover .public-link-action {
  transform: translateY(-1px);
  border-color: rgba(184, 90, 22, 0.24);
}

.public-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding-top: 8px;
  color: #9a8a77;
  font-size: 0.82rem;
}

.public-footer strong {
  color: #b85a16;
  font-size: 0.88rem;
}

@media (max-width: 720px) {
  .public-page {
    padding: 28px 16px 28px;
  }

  .public-avatar {
    width: 92px;
    height: 92px;
  }

  .public-link-card {
    grid-template-columns: 1fr;
    align-items: start;
    padding: 16px;
  }

  .public-link-action {
    width: 100%;
    min-height: 44px;
  }

  .public-link-copy-wrap {
    align-items: flex-start;
  }
}
</style>
