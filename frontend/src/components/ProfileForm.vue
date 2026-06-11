<script setup>
import { reactive, watch } from 'vue'

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
    <div class="profile-form__header">
      <div>
        <p class="eyebrow">{{ mode === 'create' ? 'Create Profile' : 'Edit Profile' }}</p>
        <h2>{{ mode === 'create' ? 'Set up your HoLink profile' : 'Update your HoLink profile' }}</h2>
      </div>
      <button class="profile-form__submit" type="submit" :disabled="saving">
        {{ saving ? 'Saving...' : mode === 'create' ? 'Create Profile' : 'Save Changes' }}
      </button>
    </div>

    <div class="profile-form__grid">
      <label class="profile-form__field">
        <span>Username</span>
        <input
          v-model="form.username"
          type="text"
          name="username"
          placeholder="kevincreator"
          autocomplete="off"
        />
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
  </form>
</template>

<style scoped>
.profile-form {
  display: grid;
  gap: 24px;
}

.profile-form__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.profile-form__header h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.6rem;
}

.profile-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.profile-form__field {
  display: grid;
  gap: 8px;
  color: var(--muted);
}

.profile-form__field span {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text);
}

.profile-form__field input,
.profile-form__field textarea {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--surface-strong);
  padding: 14px 15px;
  color: var(--text);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.profile-form__field input:focus,
.profile-form__field textarea:focus {
  outline: none;
  border-color: rgba(182, 84, 45, 0.45);
  box-shadow: 0 0 0 4px rgba(182, 84, 45, 0.12);
}

.profile-form__field textarea {
  resize: vertical;
}

.profile-form__field--full {
  grid-column: 1 / -1;
}

.profile-form__submit {
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff8f1;
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.profile-form__submit:hover:enabled {
  transform: translateY(-1px);
}

.profile-form__submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

@media (max-width: 720px) {
  .profile-form__header {
    flex-direction: column;
  }

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
