<script setup>
defineProps({
  message: {
    type: String,
    default: 'Something went wrong.',
  },
})
</script>

<template>
  <div class="feedback-card feedback-card-error" role="alert">
    <p>{{ message }}</p>
  </div>
</template>
