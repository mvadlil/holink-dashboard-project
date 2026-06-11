<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  initialValues: {
    type: Object,
    default: () => ({
      username: '',
      displayName: '',
      bio: '',
      avatarUrl: '',
    }),
  },
  mode: {
    type: String,
    default: 'create',
  },
  saving: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['submit'])

const form = reactive({
  username: '',
  displayName: '',
  bio: '',
  avatarUrl: '',
})

const previewName = computed(() => form.displayName.trim() || 'Your Display Name')
const previewUsername = computed(() => {
  const rawValue = form.username.trim() || 'yourusername'
  return rawValue.replace(/\s+/g, '').toLowerCase()
})
const previewBio = computed(() => form.bio.trim() || 'Add a short bio so visitors quickly understand what you create.')
const previewInitials = computed(() => {
  const source = previewName.value
  const parts = source
    .split(/\s+/)
    .filter(Boolean)

  if (parts.length === 0) {
    return 'H'
  }

  if (parts.length === 1) {
    return parts[0].charAt(0).toUpperCase()
  }

  return parts
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
})
const publicProfileUrl = computed(() => `holink.co/${previewUsername.value}`)

function applyValues(values) {
  form.username = values?.username ?? ''
  form.displayName = values?.displayName ?? ''
  form.bio = values?.bio ?? ''
  form.avatarUrl = values?.avatarUrl ?? ''
}

watch(
  () => props.initialValues,
  (values) => {
    applyValues(values)
  },
  { immediate: true, deep: true },
)

function handleSubmit() {
  emit('submit', {
    username: form.username,
    displayName: form.displayName,
    bio: form.bio,
    avatarUrl: form.avatarUrl,
  })
}
</script>

<template>
  <form class="profile-form" @submit.prevent="handleSubmit">
    <div class="profile-form__shell">
      <section class="profile-form__panel">
        <div class="profile-form__header">
          <div>
            <p class="eyebrow">{{ mode === 'create' ? 'Create Profile' : 'Profile Management' }}</p>
            <h2>Profile Management</h2>
            <p class="profile-form__subtitle">Update your public details and branding.</p>
          </div>
        </div>

        <div class="profile-form__grid">
          <label class="profile-form__field">
            <span>Username</span>
            <div class="profile-form__input-row">
              <span class="profile-form__prefix">holink.co/</span>
              <input
                v-model="form.username"
                type="text"
                name="username"
                placeholder="kevincreator"
                autocomplete="off"
              />
            </div>
          </label>

          <label class="profile-form__field">
            <span>Display Name</span>
            <input
              v-model="form.displayName"
              type="text"
              name="displayName"
              placeholder="Kevin Kennedy Kie"
              autocomplete="name"
            />
          </label>

          <label class="profile-form__field profile-form__field--full">
            <span>Bio</span>
            <textarea
              v-model="form.bio"
              name="bio"
              rows="4"
              placeholder="Tech, sports, and creator page"
            ></textarea>
          </label>

          <label class="profile-form__field profile-form__field--full">
            <span>Avatar URL</span>
            <input
              v-model="form.avatarUrl"
              type="url"
              name="avatarUrl"
              placeholder="https://example.com/avatar.jpg"
              inputmode="url"
            />
          </label>
        </div>

        <div class="profile-form__footer">
          <button class="profile-form__submit" type="submit" :disabled="saving">
            {{ saving ? 'Saving...' : mode === 'create' ? 'Create Profile' : 'Save Changes' }}
          </button>
        </div>
      </section>

      <aside class="profile-preview">
        <div class="profile-preview__phone">
          <div class="profile-preview__notch"></div>
          <div class="profile-preview__avatar">{{ previewInitials }}</div>
          <p class="profile-preview__name">{{ previewName }}</p>
          <p class="profile-preview__username">@{{ previewUsername }}</p>
          <p class="profile-preview__bio">{{ previewBio }}</p>
          <div class="profile-preview__links">
            <span></span>
            <span></span>
          </div>
        </div>

        <div class="profile-preview__url">
          <span>{{ publicProfileUrl }}</span>
        </div>
      </aside>
    </div>
  </form>
</template>

<style scoped>
.profile-form {
  display: grid;
}

.profile-form__shell {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(260px, 0.85fr);
  gap: 20px;
  align-items: stretch;
}

.profile-form__panel,
.profile-preview {
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-card);
  background: var(--surface-card);
  box-shadow: var(--shadow-medium);
}

.profile-form__panel {
  display: grid;
  gap: 24px;
  padding: 28px 24px;
}

.profile-form__header {
  display: grid;
  gap: 8px;
}

.profile-form__header h2 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1.8rem;
  letter-spacing: -0.03em;
  color: #132642;
}

.profile-form__subtitle {
  margin: 0;
  color: var(--muted-soft);
}

.profile-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px 18px;
}

.profile-form__field {
  display: grid;
  gap: 8px;
}

.profile-form__field span {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-strong);
}

.profile-form__input-row {
  display: flex;
  align-items: center;
  border: 1px solid var(--line-warm);
  border-radius: var(--radius-field);
  background: #fffdfb;
  overflow: hidden;
}

.profile-form__prefix {
  padding: 0 14px;
  color: #8f654d;
  background: rgba(251, 243, 236, 0.9);
  border-right: 1px solid var(--line-warm);
  height: 100%;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.profile-form__field input,
.profile-form__field textarea {
  width: 100%;
  border: 1px solid var(--line-warm);
  border-radius: var(--radius-field);
  background: #fffdfb;
  padding: 14px 15px;
  color: var(--text-strong);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.profile-form__input-row input {
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.profile-form__field input:focus,
.profile-form__field textarea:focus {
  outline: none;
  border-color: rgba(184, 90, 22, 0.45);
  box-shadow: 0 0 0 4px rgba(184, 90, 22, 0.1);
}

.profile-form__input-row:focus-within {
  border-color: rgba(184, 90, 22, 0.45);
  box-shadow: 0 0 0 4px rgba(184, 90, 22, 0.1);
}

.profile-form__field textarea {
  resize: vertical;
  min-height: 108px;
}

.profile-form__field input::placeholder,
.profile-form__field textarea::placeholder {
  color: #99a4b5;
}

.profile-form__field--full {
  grid-column: 1 / -1;
}

.profile-form__footer {
  display: flex;
  justify-content: flex-end;
}

.profile-form__submit {
  border: none;
  border-radius: var(--radius-pill);
  background: var(--accent-gradient);
  color: #fff8f1;
  min-width: 148px;
  padding: 13px 22px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(184, 90, 22, 0.18);
  transition: transform 0.18s ease, opacity 0.18s ease, box-shadow 0.18s ease;
}

.profile-form__submit:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(184, 90, 22, 0.22);
}

.profile-form__submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

.profile-preview {
  display: grid;
  align-content: center;
  justify-items: center;
  padding: 20px;
  background: linear-gradient(180deg, #edf3ff 0%, #f8fbff 100%);
}

.profile-preview__phone {
  width: min(100%, 250px);
  min-height: 430px;
  padding: 18px 20px 28px;
  border-radius: 26px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 2px solid #bfd7ff;
  box-shadow: 0 20px 40px rgba(99, 126, 174, 0.14);
  display: grid;
  justify-items: center;
  align-content: start;
  gap: 10px;
}

.profile-preview__notch {
  width: 60px;
  height: 8px;
  border-radius: 999px;
  background: #dce8fb;
}

.profile-preview__avatar {
  margin-top: 18px;
  width: 72px;
  height: 72px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #b85a16 0%, #cf6d22 100%);
  color: #fff7f0;
  font-size: 1.55rem;
  font-weight: 800;
}

.profile-preview__name,
.profile-preview__username,
.profile-preview__bio {
  margin: 0;
  text-align: center;
}

.profile-preview__name {
  margin-top: 8px;
  color: #142a47;
  font-size: 1.35rem;
  font-weight: 700;
}

.profile-preview__username {
  color: #7d88a0;
  font-weight: 700;
}

.profile-preview__bio {
  color: #617088;
  line-height: 1.6;
}

.profile-preview__links {
  width: 100%;
  margin-top: 18px;
  display: grid;
  gap: 10px;
}

.profile-preview__links span {
  display: block;
  height: 18px;
  border-radius: 8px;
  background: linear-gradient(90deg, #edf2fb 0%, #f5f8fd 100%);
}

.profile-preview__url {
  margin-top: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(222, 191, 173, 0.92);
  background: #fffdfb;
  color: #8f654d;
  font-size: 0.9rem;
  font-weight: 700;
}

@media (max-width: 1024px) {
  .profile-form__shell {
    grid-template-columns: 1fr;
  }

  .profile-preview__phone {
    min-height: 360px;
  }
}

@media (max-width: 720px) {
  .profile-form__panel {
    padding: 22px 18px;
  }

  .profile-form__footer,
  .profile-form__submit {
    width: 100%;
  }

  .profile-form__grid {
    grid-template-columns: 1fr;
  }

  .profile-form__field--full {
    grid-column: auto;
  }
}
</style>
