import { createRouter, createWebHistory } from 'vue-router'

import DashboardPage from '../pages/DashboardPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import PublicProfilePage from '../pages/PublicProfilePage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
import { getToken } from '../utils/authStorage'
import { buildLoginRedirect, normalizeRedirectTarget } from '../utils/authRoute'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardPage,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: {
        guestOnly: true,
      },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterPage,
      meta: {
        guestOnly: true,
      },
    },
    {
      path: '/u/:username',
      name: 'public-profile',
      component: PublicProfilePage,
    },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(getToken())

  if (to.meta.requiresAuth && !hasToken) {
    return buildLoginRedirect(to.fullPath)
  }

  if (to.meta.guestOnly && hasToken) {
    return normalizeRedirectTarget('/dashboard')
  }

  return true
})

export default router
