<script setup>
import EmptyState from './EmptyState.vue'
import ErrorMessage from './ErrorMessage.vue'
import LoadingState from './LoadingState.vue'

defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
  isRefreshing: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['refresh'])
</script>

<template>
  <section class="analytics-summary">
    <div class="analytics-summary__header">
      <div>
        <p class="eyebrow">Analytics</p>
        <h2>Total clicks by link</h2>
        <p class="analytics-summary__copy">
          See how many visits each destination has received so far.
        </p>
      </div>

      <button
        class="analytics-summary__refresh"
        type="button"
        :disabled="isLoading || isRefreshing"
        @click="$emit('refresh')"
      >
        {{ isLoading || isRefreshing ? 'Refreshing...' : 'Refresh' }}
      </button>
    </div>

    <LoadingState v-if="isLoading" />

    <div v-else class="analytics-summary__body">
      <ErrorMessage v-if="errorMessage" :message="errorMessage" />

      <EmptyState
        v-else-if="items.length === 0"
        title="No analytics yet"
        description="Add links and start clicking them from the public page to see totals here."
      />

      <div v-else class="analytics-summary__list">
        <article
          v-for="item in items"
          :key="item.linkId"
          class="analytics-card"
        >
          <div class="analytics-card__main">
            <div class="analytics-card__title-row">
              <h3>{{ item.title }}</h3>
              <span
                class="analytics-card__badge"
                :class="item.isActive ? 'analytics-card__badge--active' : 'analytics-card__badge--inactive'"
              >
                {{ item.isActive ? 'Active' : 'Inactive' }}
              </span>
            </div>

            <p>{{ item.url }}</p>
          </div>

          <div class="analytics-card__stat">
            <span class="analytics-card__label">Total Clicks</span>
            <strong>{{ item.totalClicks }}</strong>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.analytics-summary {
  display: grid;
  gap: 18px;
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 24px;
  background: var(--surface-strong);
}

.analytics-summary__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.analytics-summary__header h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.5rem;
}

.analytics-summary__copy {
  margin: 10px 0 0;
  color: var(--muted);
}

.analytics-summary__body,
.analytics-summary__list {
  display: grid;
  gap: 16px;
}

.analytics-summary__refresh {
  border: 1px solid var(--line);
  border-radius: 999px;
  background: transparent;
  color: var(--text);
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.analytics-summary__refresh:hover:enabled {
  transform: translateY(-1px);
}

.analytics-summary__refresh:disabled {
  opacity: 0.7;
  cursor: wait;
}

.analytics-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.62);
}

.analytics-card__main {
  min-width: 0;
}

.analytics-card__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.analytics-card__title-row h3 {
  margin: 0 0 6px;
  font-family: var(--font-heading);
  font-size: 1.18rem;
}

.analytics-card__main p {
  margin: 0;
  color: var(--muted);
  word-break: break-word;
}

.analytics-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
  white-space: nowrap;
}

.analytics-card__badge--active {
  background: rgba(74, 140, 88, 0.14);
  color: #2f6b3a;
}

.analytics-card__badge--inactive {
  background: rgba(107, 92, 78, 0.12);
  color: var(--muted);
}

.analytics-card__stat {
  display: grid;
  align-content: center;
  justify-items: end;
  gap: 6px;
}

.analytics-card__label {
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--accent);
}

.analytics-card__stat strong {
  font-size: 2rem;
  line-height: 1;
  color: var(--text);
}

@media (max-width: 720px) {
  .analytics-summary {
    padding: 20px;
  }

  .analytics-summary__header {
    flex-direction: column;
  }

  .analytics-summary__refresh {
    width: 100%;
  }

  .analytics-card {
    grid-template-columns: 1fr;
  }

  .analytics-card__title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .analytics-card__stat {
    justify-items: start;
  }
}
</style>
