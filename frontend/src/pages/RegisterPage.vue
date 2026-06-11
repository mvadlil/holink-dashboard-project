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
  <section class="register-shell">
    <div class="register-card">
      <header class="register-header">
        <div class="register-brand">HoLink</div>
        <div class="register-copy">
          <h1>Create your HoLink account</h1>
          <p>Join the platform for modern digital creators.</p>
        </div>
      </header>

      <ErrorMessage v-if="errorMessage" :message="errorMessage" />

      <form class="register-form" @submit.prevent="handleSubmit">
        <label class="auth-field">
          <span>Full Name</span>
          <input
            v-model="form.name"
            type="text"
            name="name"
            placeholder="Jane Doe"
            autocomplete="name"
          />
        </label>

        <label class="auth-field">
          <span>Email Address</span>
          <input
            v-model="form.email"
            type="email"
            name="email"
            placeholder="jane@example.com"
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
        <RouterLink to="/login">Login</RouterLink>
      </p>
    </div>
  </section>
</template>

<style scoped>
.register-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px 20px;
  background:
    radial-gradient(circle at top, rgba(255, 236, 222, 0.75), transparent 28%),
    linear-gradient(180deg, #f9f6ef 0%, #f4efe5 100%);
}

.register-card {
  width: min(100%, 520px);
  padding: 36px 24px 28px;
  border: 1px solid rgba(213, 219, 232, 0.9);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-medium);
  display: grid;
  gap: 24px;
}

.register-header {
  display: grid;
  justify-items: center;
  gap: 16px;
  text-align: center;
}

.register-brand {
  color: var(--accent-strong);
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.04em;
}

.register-copy {
  display: grid;
  gap: 8px;
}

.register-copy h1 {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 2.3rem);
  line-height: 1.1;
  color: #0f2340;
  letter-spacing: -0.04em;
}

.register-copy p {
  margin: 0;
  color: var(--muted-soft);
}

.register-form {
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
  color: var(--text-strong);
}

.auth-field input {
  width: 100%;
  border: 1px solid rgba(166, 178, 196, 0.85);
  border-radius: var(--radius-field);
  background: #fffdfb;
  padding: 14px 15px;
  color: var(--text-strong);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.auth-field input:focus {
  outline: none;
  border-color: rgba(184, 90, 22, 0.72);
  box-shadow: 0 0 0 4px rgba(184, 90, 22, 0.12);
  transform: translateY(-1px);
}

.auth-field input::placeholder {
  color: #a0a9b7;
}

.auth-inline-error {
  margin: -4px 0 0;
  color: var(--danger);
  font-weight: 700;
  font-size: 0.92rem;
}

.auth-submit {
  border: none;
  border-radius: var(--radius-field);
  background: var(--accent-gradient);
  color: #fff8f1;
  padding: 15px 18px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 14px 24px rgba(184, 90, 22, 0.18);
  transition: transform 0.18s ease, opacity 0.18s ease, box-shadow 0.18s ease;
}

.auth-submit:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 18px 30px rgba(184, 90, 22, 0.22);
}

.auth-submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

.auth-footnote {
  margin: 0;
  padding-top: 18px;
  border-top: 1px solid rgba(227, 214, 205, 0.95);
  color: var(--muted-soft);
  text-align: center;
}

.auth-footnote a {
  color: var(--accent-strong);
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 640px) {
  .register-card {
    padding: 28px 18px 22px;
    border-radius: var(--radius-card-sm);
  }
}
</style>
