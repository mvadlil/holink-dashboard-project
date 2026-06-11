<script setup>
import { computed } from 'vue'

import EmptyState from './EmptyState.vue'
import ErrorMessage from './ErrorMessage.vue'
import LoadingState from './LoadingState.vue'

const props = defineProps({
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

const maxClicks = computed(() => {
  if (props.items.length === 0) {
    return 1
  }

  return Math.max(...props.items.map((item) => item.totalClicks || 0), 1)
})

function barWidth(clicks) {
  return `${Math.max((clicks / maxClicks.value) * 100, 8)}%`
}
</script>

<template>
  <section class="analytics-summary">
    <div class="analytics-summary__header">
      <div>
        <p class="eyebrow">Analytics</p>
        <h2>Link Performance</h2>
        <p class="analytics-summary__copy">
          Click counts for the last 30 days.
        </p>
      </div>

      <div class="analytics-summary__actions">
        <button
          class="analytics-summary__refresh"
          type="button"
          :disabled="isLoading || isRefreshing"
          @click="$emit('refresh')"
        >
          {{ isLoading || isRefreshing ? 'Refreshing...' : 'Refresh' }}
        </button>
        <span class="analytics-summary__view">View full analytics</span>
      </div>
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
            </div>

            <p>{{ item.url }}</p>
            <div class="analytics-card__bar-track">
              <span class="analytics-card__bar-fill" :style="{ width: barWidth(item.totalClicks) }"></span>
            </div>
          </div>

          <div class="analytics-card__stat">
            <span
              class="analytics-card__badge"
              :class="item.isActive ? 'analytics-card__badge--active' : 'analytics-card__badge--inactive'"
            >
              {{ item.isActive ? 'Active' : 'Inactive' }}
            </span>
            <strong>{{ item.totalClicks }} clicks</strong>
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
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-card);
  padding: 24px;
  background: var(--surface-card);
  box-shadow: var(--shadow-soft);
}

.analytics-summary__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.analytics-summary__header h2 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1.5rem;
  color: #132642;
}

.analytics-summary__copy {
  margin: 10px 0 0;
  color: var(--muted-soft);
}

.analytics-summary__body,
.analytics-summary__list {
  display: grid;
  gap: 16px;
}

.analytics-summary__actions {
  display: grid;
  justify-items: end;
  gap: 10px;
}

.analytics-summary__refresh {
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-pill);
  background: #fff;
  color: var(--text-strong);
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease, border-color 0.18s ease;
}

.analytics-summary__refresh:hover:enabled {
  transform: translateY(-1px);
  border-color: rgba(184, 90, 22, 0.24);
}

.analytics-summary__refresh:disabled {
  opacity: 0.7;
  cursor: wait;
}

.analytics-summary__view {
  color: var(--accent-strong);
  font-weight: 700;
}

.analytics-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(235, 239, 245, 0.95);
  border-radius: 18px;
  background: #fffdfb;
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
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1rem;
  color: #132642;
}

.analytics-card__main p {
  margin: 0;
  color: var(--muted-soft);
  font-size: 0.92rem;
  word-break: break-word;
}

.analytics-card__bar-track {
  margin-top: 14px;
  height: 8px;
  border-radius: 999px;
  background: #e6eefb;
  overflow: hidden;
}

.analytics-card__bar-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #bfd5ff 0%, #8eb1f6 100%);
}

.analytics-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
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
  gap: 10px;
}

.analytics-card__stat strong {
  font-size: 1rem;
  line-height: 1;
  color: #132642;
}

@media (max-width: 720px) {
  .analytics-summary {
    padding: 20px 18px;
  }

  .analytics-summary__header {
    flex-direction: column;
  }

  .analytics-summary__actions {
    width: 100%;
    justify-items: stretch;
  }

  .analytics-summary__refresh {
    width: 100%;
  }

  .analytics-card {
    grid-template-columns: 1fr;
    padding: 16px;
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
