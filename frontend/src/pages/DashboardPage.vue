<script setup>
import axios from 'axios'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getLinkAnalytics } from '../api/analyticsApi'
import { createLink, deleteLink, updateLink } from '../api/linkApi'
import { createProfile, getMyProfile, updateProfile } from '../api/profileApi'
import AnalyticsSummary from '../components/AnalyticsSummary.vue'
import EmptyState from '../components/EmptyState.vue'
import ErrorMessage from '../components/ErrorMessage.vue'
import LinkEditor from '../components/LinkEditor.vue'
import LinkList from '../components/LinkList.vue'
import LoadingState from '../components/LoadingState.vue'
import ProfileForm from '../components/ProfileForm.vue'
import { clearAuthStorage, getStoredUser } from '../utils/authStorage'

const router = useRouter()

const isLoading = ref(true)
const isProfileSaving = ref(false)
const isCreatingLink = ref(false)
const profile = ref(null)
const dashboardLinks = ref([])
const analyticsItems = ref([])
const createLinkFormKey = ref(0)
const editingLinkId = ref('')
const activeLinkActionId = ref('')
const isAnalyticsLoading = ref(true)
const isAnalyticsRefreshing = ref(false)
const profileFeedbackMessage = ref('')
const profileErrorMessage = ref('')
const linkFeedbackMessage = ref('')
const linkErrorMessage = ref('')
const analyticsErrorMessage = ref('')
const storedUser = ref(getStoredUser())
const activeNavTab = ref('dashboard')

const dashboardSections = [
  { id: 'dashboard-hero', tab: 'dashboard' },
  { id: 'dashboard-profile', tab: 'settings' },
  { id: 'dashboard-links', tab: 'dashboard' },
  { id: 'dashboard-analytics', tab: 'analytics' },
]

const profileUrl = computed(() => {
  if (!profile.value?.username) {
    return ''
  }

  return `${window.location.origin}/u/${profile.value.username}`
})

const dashboardIdentity = computed(() => {
  const label = storedUser.value?.name || storedUser.value?.email || 'My Account'
  const email = storedUser.value?.email || 'Local prototype user'
  const initialsSource = label
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')

  return {
    label,
    email,
    initials: initialsSource || 'H',
  }
})

const formValues = computed(() => {
  if (!profile.value) {
    return {
      username: '',
      displayName: '',
      bio: '',
      avatarUrl: '',
    }
  }

  return {
    username: profile.value.username ?? '',
    displayName: profile.value.displayName ?? '',
    bio: profile.value.bio ?? '',
    avatarUrl: profile.value.avatarUrl ?? '',
  }
})

const mode = computed(() => (profile.value ? 'edit' : 'create'))

onMounted(() => {
  window.addEventListener('scroll', syncActiveTabFromScroll, { passive: true })
  loadDashboard()
  syncActiveTabFromScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', syncActiveTabFromScroll)
})

async function loadDashboard() {
  storedUser.value = getStoredUser()
  await loadDashboardProfile()

  if (profile.value?.id) {
    await loadAnalytics()
  } else {
    resetAnalyticsState(false)
  }
}

function scrollToSection(sectionId) {
  const sectionToTab = {
    'dashboard-hero': 'dashboard',
    'dashboard-profile': 'settings',
    'dashboard-links': 'dashboard',
    'dashboard-analytics': 'analytics',
  }

  activeNavTab.value = sectionToTab[sectionId] || 'dashboard'
  document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function syncActiveTabFromScroll() {
  let currentTab = 'dashboard'

  for (const section of dashboardSections) {
    const element = document.getElementById(section.id)
    if (!element) {
      continue
    }

    const rect = element.getBoundingClientRect()
    if (rect.top <= 140) {
      currentTab = section.tab
    }
  }

  activeNavTab.value = currentTab
}

async function handleLogout() {
  clearAuthStorage()
  await router.push('/login')
}

async function handleShareProfile() {
  if (!profileUrl.value) {
    profileErrorMessage.value = 'Profile not created yet.'
    profileFeedbackMessage.value = ''
    return
  }

  try {
    await navigator.clipboard.writeText(profileUrl.value)
    profileErrorMessage.value = ''
    profileFeedbackMessage.value = 'Public profile link copied to your clipboard.'
  } catch {
    profileErrorMessage.value = ''
    window.open(profileUrl.value, '_blank', 'noopener')
    profileFeedbackMessage.value = 'Public profile opened in a new tab.'
  }
}

async function loadDashboardProfile() {
  isLoading.value = true
  profileErrorMessage.value = ''

  try {
    const response = await getMyProfile()
    profile.value = response.profile
    dashboardLinks.value = response.links ?? []
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      return
    }

    if (axios.isAxiosError(error) && error.response?.status === 404) {
      profile.value = null
      dashboardLinks.value = []
      resetAnalyticsState(false)
      return
    }

    profileErrorMessage.value = toFriendlyMessage(error, 'We could not load your dashboard right now.')
  } finally {
    isLoading.value = false
  }
}

async function loadAnalytics(options = {}) {
  const { silent = false } = options

  if (!profile.value?.id) {
    resetAnalyticsState(false)
    return
  }

  if (silent) {
    isAnalyticsRefreshing.value = true
  } else {
    isAnalyticsLoading.value = true
  }

  analyticsErrorMessage.value = ''

  try {
    const response = await getLinkAnalytics()
    analyticsItems.value = response.items ?? []
  } catch (error) {
    analyticsItems.value = []
    analyticsErrorMessage.value = toFriendlyMessage(
      error,
      'We could not load your analytics right now.',
    )
  } finally {
    isAnalyticsLoading.value = false
    isAnalyticsRefreshing.value = false
  }
}

function resetAnalyticsState(isLoadingState) {
  analyticsItems.value = []
  analyticsErrorMessage.value = ''
  isAnalyticsLoading.value = isLoadingState
  isAnalyticsRefreshing.value = false
}

async function handleProfileSubmit(payload) {
  isProfileSaving.value = true
  profileErrorMessage.value = ''
  profileFeedbackMessage.value = ''

  try {
    if (profile.value?.id) {
      await updateProfile(profile.value.id, payload)
      profileFeedbackMessage.value = 'Profile updated successfully.'
    } else {
      await createProfile(payload)
      profileFeedbackMessage.value = 'Profile created successfully.'
    }

    await loadDashboardProfile()
    await loadAnalytics()
  } catch (error) {
    profileErrorMessage.value = toFriendlyMessage(error, 'We could not save your profile right now.')
  } finally {
    isProfileSaving.value = false
  }
}

async function handleCreateLink(payload) {
  if (!profile.value?.id) {
    return
  }

  isCreatingLink.value = true
  linkErrorMessage.value = ''
  linkFeedbackMessage.value = ''

  try {
    await createLink({
      profileId: profile.value.id,
      title: payload.title,
      url: payload.url,
      isActive: payload.isActive,
      position: payload.position,
    })

    linkFeedbackMessage.value = 'Link added successfully.'
    createLinkFormKey.value += 1
    await loadDashboardProfile()
    await loadAnalytics({ silent: true })
  } catch (error) {
    linkErrorMessage.value = toFriendlyMessage(error, 'We could not add your link right now.')
  } finally {
    isCreatingLink.value = false
  }
}

function startEditingLink(link) {
  editingLinkId.value = link.id
  linkErrorMessage.value = ''
  linkFeedbackMessage.value = ''
}

function cancelEditingLink() {
  editingLinkId.value = ''
}

async function handleUpdateLink(linkId, payload) {
  activeLinkActionId.value = linkId
  linkErrorMessage.value = ''
  linkFeedbackMessage.value = ''

  try {
    await updateLink(linkId, payload)
    editingLinkId.value = ''
    linkFeedbackMessage.value = 'Link updated successfully.'
    await loadDashboardProfile()
    await loadAnalytics({ silent: true })
  } catch (error) {
    linkErrorMessage.value = toFriendlyMessage(error, 'We could not update your link right now.')
  } finally {
    activeLinkActionId.value = ''
  }
}

async function handleDeleteLink(link) {
  const shouldDelete = window.confirm(`Delete the link "${link.title}"?`)

  if (!shouldDelete) {
    return
  }

  activeLinkActionId.value = link.id
  linkErrorMessage.value = ''
  linkFeedbackMessage.value = ''

  try {
    await deleteLink(link.id)

    if (editingLinkId.value === link.id) {
      editingLinkId.value = ''
    }

    linkFeedbackMessage.value = 'Link deleted successfully.'
    await loadDashboardProfile()
    await loadAnalytics({ silent: true })
  } catch (error) {
    linkErrorMessage.value = toFriendlyMessage(error, 'We could not delete your link right now.')
  } finally {
    activeLinkActionId.value = ''
  }
}

async function handleToggleLink(link) {
  activeLinkActionId.value = link.id
  linkErrorMessage.value = ''
  linkFeedbackMessage.value = ''

  try {
    await updateLink(link.id, {
      title: link.title,
      url: link.url,
      isActive: !link.isActive,
      position: link.position,
    })

    linkFeedbackMessage.value = link.isActive
      ? 'Link moved to inactive.'
      : 'Link is now active on your public profile.'
    await loadDashboardProfile()
    await loadAnalytics({ silent: true })
  } catch (error) {
    linkErrorMessage.value = toFriendlyMessage(error, 'We could not update this link right now.')
  } finally {
    activeLinkActionId.value = ''
  }
}

function toFriendlyMessage(error, fallbackMessage) {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Backend is unreachable right now. Please make sure the backend is running on http://localhost:8080.'
    }

    return error.response.data?.message || fallbackMessage
  }

  return fallbackMessage
}
</script>

<template>
  <section class="dashboard-shell">
    <header class="dashboard-topbar">
      <div class="dashboard-topbar__brand">
        <div class="dashboard-topbar__logo">Ho</div>
        <span>HoLink</span>
      </div>

      <nav class="dashboard-topbar__nav" aria-label="Dashboard sections">
        <button
          type="button"
          :class="['dashboard-topbar__tab', { 'dashboard-topbar__tab--active': activeNavTab === 'dashboard' }]"
          @click="scrollToSection('dashboard-hero')"
        >
          Dashboard
        </button>
        <button
          type="button"
          :class="['dashboard-topbar__tab', { 'dashboard-topbar__tab--active': activeNavTab === 'analytics' }]"
          @click="scrollToSection('dashboard-analytics')"
        >
          Analytics
        </button>
        <button
          type="button"
          :class="['dashboard-topbar__tab', { 'dashboard-topbar__tab--active': activeNavTab === 'settings' }]"
          @click="scrollToSection('dashboard-profile')"
        >
          Settings
        </button>
      </nav>

      <div class="dashboard-topbar__actions">
        <button class="dashboard-topbar__share" type="button" @click="handleShareProfile">
          Share Profile
        </button>

        <div class="dashboard-topbar__user">
          <div class="dashboard-topbar__avatar">{{ dashboardIdentity?.initials || 'H' }}</div>
          <div class="dashboard-topbar__user-copy">
            <strong>{{ dashboardIdentity?.label || 'My Account' }}</strong>
            <span>{{ dashboardIdentity?.email || 'Local prototype user' }}</span>
          </div>
        </div>

        <button class="dashboard-topbar__logout" type="button" @click="handleLogout">Logout</button>
      </div>
    </header>

    <div class="dashboard-container">
      <section id="dashboard-hero" class="dashboard-hero">
        <div>
          <p class="eyebrow">Creator Workspace</p>
          <h1>HoLink Dashboard</h1>
          <p class="page-copy">
            Manage your public identity, link stack, and performance from one airy workspace.
          </p>
        </div>
      </section>

      <LoadingState v-if="isLoading" />

      <div v-else class="dashboard-stack">
        <ErrorMessage v-if="profileErrorMessage" :message="profileErrorMessage" />

        <div v-if="profileFeedbackMessage" class="feedback-card dashboard-success" role="status">
          <p>{{ profileFeedbackMessage }}</p>
        </div>

        <section id="dashboard-profile" class="dashboard-section">
          <ProfileForm
            :initial-values="formValues"
            :mode="mode"
            :saving="isProfileSaving"
            @submit="handleProfileSubmit"
          />
        </section>

        <EmptyState
          v-if="!profile"
          title="Create your profile first"
          description="Fill out the profile form above first. Once it is saved, link management will appear here."
        />

        <section v-if="profile" id="dashboard-links" class="dashboard-section dashboard-links">
          <div class="dashboard-links__copy">
            <p class="eyebrow">Your Links</p>
            <h2>Your Links</h2>
            <p>
              Add, edit, and control which destinations appear on your public page.
            </p>
          </div>

          <ErrorMessage v-if="linkErrorMessage" :message="linkErrorMessage" />

          <div v-if="linkFeedbackMessage" class="feedback-card dashboard-success" role="status">
            <p>{{ linkFeedbackMessage }}</p>
          </div>

          <LinkEditor
            :key="createLinkFormKey"
            mode="create"
            :saving="isCreatingLink"
            @submit="handleCreateLink"
          />

          <LinkList
            :links="dashboardLinks"
            :editing-link-id="editingLinkId"
            :active-action-link-id="activeLinkActionId"
            @start-edit="startEditingLink"
            @cancel-edit="cancelEditingLink"
            @submit-edit="handleUpdateLink"
            @delete="handleDeleteLink"
            @toggle="handleToggleLink"
          />
        </section>

        <AnalyticsSummary
          v-if="profile"
          id="dashboard-analytics"
          :items="analyticsItems"
          :is-loading="isAnalyticsLoading"
          :is-refreshing="isAnalyticsRefreshing"
          :error-message="analyticsErrorMessage"
          @refresh="loadAnalytics({ silent: true })"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.dashboard-shell {
  display: grid;
  gap: 24px;
  padding: 20px 24px 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 232, 216, 0.72), transparent 24%),
    linear-gradient(180deg, #f8f5ef 0%, #f4efe6 100%);
}

.dashboard-topbar {
  width: min(100%, 1320px);
  margin: 0 auto;
  position: sticky;
  top: 12px;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 14px 18px;
  border: 1px solid var(--line-soft);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 16px 36px rgba(121, 109, 95, 0.07);
  backdrop-filter: blur(14px);
}

.dashboard-topbar__brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #b85a16;
  font-weight: 800;
  font-size: 1.45rem;
  letter-spacing: -0.03em;
}

.dashboard-topbar__logo {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: rgba(184, 90, 22, 0.12);
  font-size: 0.95rem;
}

.dashboard-topbar__nav {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.dashboard-topbar__tab {
  border: none;
  background: transparent;
  padding: 9px 12px;
  border-radius: var(--radius-pill);
  color: #6c7688;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.dashboard-topbar__tab--active {
  color: #b85a16;
  background: rgba(184, 90, 22, 0.1);
}

.dashboard-topbar__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.dashboard-topbar__share,
.dashboard-topbar__logout {
  border-radius: var(--radius-pill);
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.dashboard-topbar__share {
  border: 1px solid var(--line-soft);
  background: #ffffff;
  color: #455267;
  padding: 11px 16px;
}

.dashboard-topbar__logout {
  border: none;
  background: var(--accent-gradient);
  color: #fff8f1;
  padding: 11px 16px;
  box-shadow: 0 10px 22px rgba(184, 90, 22, 0.16);
}

.dashboard-topbar__share:hover,
.dashboard-topbar__logout:hover {
  transform: translateY(-1px);
}

.dashboard-topbar__user {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 18px;
  background: #f8fbff;
  border: 1px solid var(--line-soft);
  min-width: 0;
}

.dashboard-topbar__avatar {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #b85a16 0%, #cf6d22 100%);
  color: #fff8f1;
  font-weight: 800;
}

.dashboard-topbar__user-copy {
  display: grid;
  gap: 2px;
}

.dashboard-topbar__user-copy strong {
  color: #182c47;
  font-size: 0.95rem;
  word-break: break-word;
}

.dashboard-topbar__user-copy span {
  color: #6c7688;
  font-size: 0.82rem;
  word-break: break-word;
}

.dashboard-container {
  width: min(100%, 1320px);
  margin: 0 auto;
  display: grid;
  gap: 24px;
}

.dashboard-hero {
  display: grid;
  gap: 10px;
  padding: 6px 4px 0;
}

.dashboard-hero h1 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: clamp(2.6rem, 4vw, 4rem);
  line-height: 1;
  letter-spacing: -0.05em;
  color: #142946;
}

.dashboard-stack {
  display: grid;
  gap: 24px;
}

.dashboard-section {
  display: grid;
}

.dashboard-success p {
  word-break: break-word;
}

.dashboard-success {
  border-color: rgba(74, 140, 88, 0.22);
  color: #2f6b3a;
  background: rgba(112, 176, 124, 0.1);
}

.dashboard-links {
  display: grid;
  gap: 18px;
}

.dashboard-links__copy {
  display: grid;
  gap: 10px;
  padding: 0 4px;
}

.dashboard-links__copy h2 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1.5rem;
  color: #132642;
}

.dashboard-links__copy p:last-child {
  margin: 0;
  color: var(--muted-soft);
}

@media (max-width: 1080px) {
  .dashboard-topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .dashboard-topbar__actions {
    width: 100%;
    justify-content: flex-start;
  }

  .dashboard-topbar__user {
    flex: 1 1 260px;
  }
}

@media (max-width: 720px) {
  .dashboard-shell {
    gap: 18px;
    padding: 12px 16px 24px;
  }

  .dashboard-topbar__nav,
  .dashboard-topbar__actions {
    width: 100%;
  }

  .dashboard-topbar__tab,
  .dashboard-topbar__share,
  .dashboard-topbar__logout {
    flex: 1 1 auto;
    text-align: center;
  }

  .dashboard-topbar__user {
    width: 100%;
  }

  .dashboard-topbar__logout {
    width: 100%;
  }

  .dashboard-hero {
    padding-top: 4px;
  }

  .dashboard-hero h1 {
    font-size: 2.25rem;
  }
}
</style>
