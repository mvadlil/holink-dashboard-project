<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand-block">
        <p class="brand-kicker">HoLink Prototype</p>
        <p class="brand-title">Vue Frontend Base</p>
      </div>
      <nav class="app-nav" aria-label="Primary">
        <RouterLink to="/dashboard">Dashboard</RouterLink>
        <RouterLink to="/u/kevincreator">Public Profile</RouterLink>
      </nav>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
