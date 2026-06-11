<script setup>
import axios from 'axios'
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { login } from '../api/authApi'
import ErrorMessage from '../components/ErrorMessage.vue'
import { normalizeRedirectTarget } from '../utils/authRoute'
import { setStoredUser, setToken } from '../utils/authStorage'

const router = useRouter()
const route = useRoute()

const isSubmitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: '',
})

async function handleSubmit() {
  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const response = await login(form.email, form.password)
    setToken(response.token)
    setStoredUser(response.user)
    const redirectTarget = normalizeRedirectTarget(route.query.redirect)
    await router.push(redirectTarget)
  } catch (error) {
    errorMessage.value = toFriendlyMessage(error, 'We could not log you in right now.')
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
  <section class="login-shell">
    <div class="login-layout">
      <div class="login-panel">
        <div class="login-brand">
          <span class="login-brand__icon">Ho</span>
          <span class="login-brand__text">HoLink</span>
        </div>

        <div class="login-copy">
          <p class="login-kicker">Creator dashboard access</p>
          <h1>Welcome back</h1>
          <p class="login-copy__text">
            Log in to manage your creator profile, active links, and audience analytics from one polished dashboard.
          </p>
        </div>

        <ErrorMessage v-if="errorMessage" :message="errorMessage" />

        <form class="login-form" @submit.prevent="handleSubmit">
          <label class="auth-field">
            <span>Email address</span>
            <div class="auth-input-wrap">
              <span class="auth-input-icon">@</span>
              <input
                v-model="form.email"
                type="email"
                name="email"
                placeholder="you@example.com"
                autocomplete="email"
              />
            </div>
          </label>

          <label class="auth-field">
            <div class="auth-field__row">
              <span>Password</span>
              <span class="auth-field__hint">Secure sign in</span>
            </div>
            <div class="auth-input-wrap">
              <span class="auth-input-icon">*</span>
              <input
                v-model="form.password"
                type="password"
                name="password"
                placeholder="password123"
                autocomplete="current-password"
              />
            </div>
          </label>

          <button class="auth-submit" type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? 'Logging in...' : 'Login' }}
          </button>
        </form>

        <p class="auth-footnote">
          Don't have an account?
          <RouterLink to="/register">Create Account</RouterLink>
        </p>
      </div>

      <aside class="login-preview" aria-hidden="true">
        <div class="preview-card">
          <div class="preview-card__header">
            <div class="preview-profile">
              <div class="preview-avatar">ER</div>
              <div>
                <p class="preview-name">Elena Rodriguez</p>
                <p class="preview-handle">@elenacreates</p>
              </div>
            </div>
            <div class="preview-share">-></div>
          </div>

          <div class="preview-feature">
            <div class="preview-feature__icon">GO</div>
            <div>
              <p class="preview-feature__title">New Video Course</p>
              <p class="preview-feature__meta">Mastering Minimalism</p>
            </div>
            <span class="preview-feature__arrow">-></span>
          </div>

          <div class="preview-grid">
            <div class="preview-tile">
              <span class="preview-tile__icon">[]</span>
              <span>My Store</span>
            </div>
            <div class="preview-tile">
              <span class="preview-tile__icon">@</span>
              <span>Newsletter</span>
            </div>
          </div>

          <div class="preview-pill">+12% views this week</div>
        </div>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  background:
    linear-gradient(90deg, #fbf7ef 0%, #fbf7ef 54%, #dfe9ff 54%, #dfe9ff 100%);
}

.login-layout {
  min-height: 100vh;
  max-width: 1600px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.login-panel {
  display: grid;
  align-content: center;
  gap: 28px;
  padding: 48px clamp(32px, 6vw, 96px) 48px clamp(48px, 8vw, 120px);
  max-width: 560px;
  width: min(100%, 560px);
  margin: 0 auto;
}

.login-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--accent-strong);
  font-weight: 700;
}

.login-brand__icon {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: inline-grid;
  place-items: center;
  background: var(--accent-soft);
  font-size: 0.95rem;
}

.login-brand__text {
  font-size: 1.75rem;
  letter-spacing: -0.03em;
}

.login-copy {
  display: grid;
  gap: 12px;
}

.login-kicker {
  margin: 0;
  color: var(--accent-strong);
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.login-copy h1 {
  margin: 0;
  font-size: clamp(2.8rem, 6vw, 4.25rem);
  line-height: 0.98;
  letter-spacing: -0.05em;
  color: #0f2340;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
}

.login-copy__text {
  margin: 0;
  color: var(--muted-soft);
  font-size: 1.05rem;
  line-height: 1.7;
  max-width: 34rem;
}

.login-form {
  display: grid;
  gap: 18px;
}

.auth-field {
  display: grid;
  gap: 9px;
}

.auth-field span {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-strong);
}

.auth-field__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.auth-field__hint {
  color: var(--accent-strong);
  font-size: 0.86rem;
  font-weight: 700;
}

.auth-input-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 58px;
  padding: 0 16px;
  border: 1px solid rgba(193, 152, 127, 0.52);
  border-radius: var(--radius-field);
  background: rgba(255, 255, 255, 0.78);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.auth-input-wrap:focus-within {
  border-color: rgba(184, 90, 22, 0.72);
  box-shadow: 0 0 0 4px rgba(184, 90, 22, 0.12);
  transform: translateY(-1px);
}

.auth-input-icon {
  width: 24px;
  color: #7b8799;
  font-weight: 700;
  text-align: center;
}

.auth-field input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-strong);
}

.auth-field input::placeholder {
  color: #94a0af;
}

.auth-submit {
  margin-top: 4px;
  border: none;
  border-radius: var(--radius-field);
  background: var(--accent-gradient);
  color: #fffaf4;
  padding: 16px 20px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 16px 30px rgba(184, 90, 22, 0.22);
  transition: transform 0.18s ease, opacity 0.18s ease, box-shadow 0.18s ease;
}

.auth-submit:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 20px 34px rgba(184, 90, 22, 0.28);
}

.auth-submit:disabled {
  opacity: 0.72;
  cursor: wait;
}

.auth-footnote {
  margin: 0;
  color: var(--muted-soft);
}

.auth-footnote a {
  color: var(--accent-strong);
  font-weight: 700;
  text-decoration: none;
}

.login-preview {
  display: grid;
  place-items: center;
  padding: 48px clamp(56px, 7vw, 112px) 48px clamp(32px, 5vw, 72px);
}

.preview-card {
  position: relative;
  width: min(100%, 500px);
  padding: 24px;
  border: 1px solid rgba(191, 168, 154, 0.7);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 28px 60px rgba(74, 95, 140, 0.16);
}

.preview-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(157, 181, 219, 0.35);
}

.preview-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #f3d8c5 0%, #ffffff 100%);
  color: #8d4f2a;
  font-weight: 800;
}

.preview-name,
.preview-handle,
.preview-feature__title,
.preview-feature__meta {
  margin: 0;
}

.preview-name {
  color: #0f2340;
  font-weight: 700;
}

.preview-handle,
.preview-feature__meta {
  color: #6d7686;
  font-size: 0.92rem;
}

.preview-share {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #c7671a;
  color: white;
  font-weight: 800;
}

.preview-feature {
  margin-top: 18px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 14px;
  background: #dfe9ff;
  border: 1px solid rgba(157, 181, 219, 0.55);
}

.preview-feature__icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: #173152;
  color: #f3f7ff;
}

.preview-feature__title {
  color: #14304f;
  font-weight: 700;
}

.preview-feature__arrow {
  color: #566f96;
  font-size: 1.2rem;
}

.preview-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.preview-tile {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 24px 12px;
  border-radius: 14px;
  border: 1px solid rgba(191, 168, 154, 0.44);
  background: #fffdfb;
  color: #14304f;
  font-weight: 600;
}

.preview-tile__icon {
  font-size: 1.1rem;
  color: #6d7686;
}

.preview-pill {
  position: absolute;
  right: -10px;
  bottom: -18px;
  padding: 12px 18px;
  border-radius: 999px;
  background: #fffdfb;
  border: 1px solid rgba(191, 168, 154, 0.55);
  box-shadow: 0 12px 28px rgba(86, 111, 150, 0.14);
  color: #14304f;
  font-size: 0.92rem;
  font-weight: 700;
}

@media (max-width: 980px) {
  .login-shell {
    background:
      radial-gradient(circle at top, rgba(212, 228, 255, 0.9), transparent 36%),
      linear-gradient(180deg, #fbf7ef 0%, #eef4ff 100%);
  }

  .login-layout {
    grid-template-columns: 1fr;
  }

  .login-panel {
    max-width: 560px;
    padding: 40px 24px 24px;
  }

  .login-preview {
    padding: 0 24px 36px;
  }
}

@media (max-width: 640px) {
  .login-layout {
    min-height: auto;
  }

  .login-panel {
    padding: 28px 20px 18px;
    gap: 24px;
  }

  .login-copy h1 {
    font-size: 2.65rem;
  }

  .login-preview {
    padding: 0 20px 32px;
  }

  .preview-card {
    padding: 20px;
  }

  .preview-pill {
    position: static;
    margin-top: 16px;
    width: fit-content;
  }

  .preview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
