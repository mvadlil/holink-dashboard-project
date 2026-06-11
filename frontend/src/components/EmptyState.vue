<script setup>
defineProps({
  title: {
    type: String,
    default: 'Nothing here yet',
  },
  description: {
    type: String,
    default: 'Content will appear here in a later phase.',
  },
})
</script>

<template>
  <div class="feedback-card feedback-card-empty">
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
  </div>
</template>
