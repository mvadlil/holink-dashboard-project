<script setup>
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

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
  loadDashboard()
})

async function loadDashboard() {
  await loadDashboardProfile()

  if (profile.value?.id) {
    await loadAnalytics()
  } else {
    resetAnalyticsState(false)
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
  <section class="page-shell dashboard-shell">
    <div class="page-card dashboard-card">
      <div class="dashboard-copy">
        <p class="eyebrow">Dashboard</p>
        <h1>HoLink Dashboard</h1>
        <p class="page-copy">
          Manage your public profile, links, and click totals all in one place.
        </p>
      </div>

      <LoadingState v-if="isLoading" />

      <div v-else class="dashboard-stack">
        <ErrorMessage v-if="profileErrorMessage" :message="profileErrorMessage" />

        <div v-if="profileFeedbackMessage" class="feedback-card dashboard-success" role="status">
          <p>{{ profileFeedbackMessage }}</p>
        </div>

        <ProfileForm
          :initial-values="formValues"
          :mode="mode"
          :saving="isProfileSaving"
          @submit="handleProfileSubmit"
        />

        <div v-if="profile" class="dashboard-summary">
          <h2>Current Profile Snapshot</h2>
          <dl class="summary-grid">
            <div>
              <dt>Username</dt>
              <dd>{{ profile.username }}</dd>
            </div>
            <div>
              <dt>Display Name</dt>
              <dd>{{ profile.displayName }}</dd>
            </div>
            <div>
              <dt>Bio</dt>
              <dd>{{ profile.bio || 'No bio yet' }}</dd>
            </div>
            <div>
              <dt>Avatar URL</dt>
              <dd>{{ profile.avatarUrl || 'No avatar URL yet' }}</dd>
            </div>
          </dl>
          <p class="dashboard-note">
            Saved links found for this profile: {{ dashboardLinks.length }}.
          </p>
        </div>

        <EmptyState
          v-else
          title="Create your profile first"
          description="Fill out the profile form above first. Once it is saved, link management will appear here."
        />

        <section v-if="profile" class="dashboard-links">
          <div class="dashboard-links__copy">
            <p class="eyebrow">Links</p>
            <h2>Manage your public destinations</h2>
            <p>
              Add, reorder, edit, and disable links here. Every change refreshes from the backend so your dashboard stays in sync.
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
}

.dashboard-card {
  display: grid;
  gap: 26px;
}

.dashboard-copy {
  display: grid;
  gap: 12px;
}

.dashboard-stack {
  display: grid;
  gap: 18px;
}

.dashboard-success p {
  word-break: break-word;
}

.dashboard-success {
  border-color: rgba(74, 140, 88, 0.22);
  color: #2f6b3a;
  background: rgba(112, 176, 124, 0.12);
}

.dashboard-summary {
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 22px;
  background: var(--surface-strong);
}

.dashboard-summary h2 {
  margin: 0 0 16px;
  font-family: var(--font-heading);
  font-size: 1.35rem;
}

.summary-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.summary-grid div {
  display: grid;
  gap: 6px;
}

.summary-grid dt {
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--accent);
}

.summary-grid dd {
  margin: 0;
  color: var(--text);
  word-break: break-word;
}

.dashboard-note {
  margin: 18px 0 0;
  color: var(--muted);
}

.dashboard-links {
  display: grid;
  gap: 18px;
}

.dashboard-links__copy {
  display: grid;
  gap: 10px;
}

.dashboard-links__copy h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.5rem;
}

.dashboard-links__copy p:last-child {
  margin: 0;
  color: var(--muted);
}

@media (max-width: 720px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
