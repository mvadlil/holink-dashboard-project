<script setup>
import axios from 'axios'
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { register } from '../api/authApi'
import ErrorMessage from '../components/ErrorMessage.vue'
import { setStoredUser, setToken } from '../utils/authStorage'

const router = useRouter()

const isSubmitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const passwordMismatch = computed(() => {
  return Boolean(form.confirmPassword) && form.password !== form.confirmPassword
})

async function handleSubmit() {
  errorMessage.value = ''

  if (!form.name.trim()) {
    errorMessage.value = 'Name is required.'
    return
  }

  if (!form.email.trim()) {
    errorMessage.value = 'Email is required.'
    return
  }

  if (form.password.length < 8) {
    errorMessage.value = 'Password must be at least 8 characters.'
    return
  }

  if (form.password !== form.confirmPassword) {
    errorMessage.value = 'Confirm password must match password.'
    return
  }

  isSubmitting.value = true

  try {
    const response = await register(form.name, form.email, form.password)
    setToken(response.token)
    setStoredUser(response.user)
    await router.push('/dashboard')
  } catch (error) {
    errorMessage.value = toFriendlyMessage(error, 'We could not create your account right now.')
  } finally {
    isSubmitting.value = false
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
  <section class="page-shell auth-shell">
    <div class="page-card auth-card">
      <div class="auth-copy">
        <p class="eyebrow">Create Account</p>
        <h1>Join HoLink</h1>
        <p class="page-copy">
          Create a simple account to manage your link-in-bio profile with JWT-based auth.
        </p>
      </div>

      <ErrorMessage v-if="errorMessage" :message="errorMessage" />

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label class="auth-field">
          <span>Name</span>
          <input
            v-model="form.name"
            type="text"
            name="name"
            placeholder="Muhammad Fadlil"
            autocomplete="name"
          />
        </label>

        <label class="auth-field">
          <span>Email</span>
          <input
            v-model="form.email"
            type="email"
            name="email"
            placeholder="you@example.com"
            autocomplete="email"
          />
        </label>

        <label class="auth-field">
          <span>Password</span>
          <input
            v-model="form.password"
            type="password"
            name="password"
            placeholder="At least 8 characters"
            autocomplete="new-password"
          />
        </label>

        <label class="auth-field">
          <span>Confirm Password</span>
          <input
            v-model="form.confirmPassword"
            type="password"
            name="confirmPassword"
            placeholder="Repeat your password"
            autocomplete="new-password"
          />
        </label>

        <p v-if="passwordMismatch" class="auth-inline-error">
          Confirm password must match password.
        </p>

        <button class="auth-submit" type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? 'Creating account...' : 'Create Account' }}
        </button>
      </form>

      <p class="auth-footnote">
        Already have an account?
        <RouterLink to="/login">Login here</RouterLink>.
      </p>
    </div>
  </section>
</template>

<style scoped>
.auth-shell {
  display: grid;
  min-height: calc(100vh - 180px);
  place-items: center;
}

.auth-card {
  width: min(100%, 560px);
  display: grid;
  gap: 22px;
}

.auth-copy,
.auth-form {
  display: grid;
  gap: 16px;
}

.auth-field {
  display: grid;
  gap: 8px;
}

.auth-field span {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text);
}

.auth-field input {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--surface-strong);
  padding: 14px 15px;
  color: var(--text);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.auth-field input:focus {
  outline: none;
  border-color: rgba(182, 84, 45, 0.45);
  box-shadow: 0 0 0 4px rgba(182, 84, 45, 0.12);
}

.auth-inline-error {
  margin: -6px 0 0;
  color: var(--danger);
  font-weight: 700;
}

.auth-submit {
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff8f1;
  padding: 14px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.auth-submit:hover:enabled {
  transform: translateY(-1px);
}

.auth-submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

.auth-footnote {
  margin: 0;
  color: var(--muted);
}

.auth-footnote a {
  color: var(--accent);
  font-weight: 700;
}
</style>
