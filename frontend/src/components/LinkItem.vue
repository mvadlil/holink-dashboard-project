<script setup>
defineProps({
  link: {
    type: Object,
    required: true,
  },
  busy: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['edit', 'delete', 'toggle'])
</script>

<template>
  <article class="link-item">
    <div class="link-item__handle" aria-hidden="true">
      <span></span>
      <span></span>
      <span></span>
    </div>

    <div class="link-item__icon" aria-hidden="true">
      {{ link.title?.charAt(0)?.toUpperCase() || 'L' }}
    </div>

    <div class="link-item__main">
      <div class="link-item__topline">
        <h3>{{ link.title }}</h3>
        <span class="link-item__badge" :class="link.isActive ? 'link-item__badge--active' : 'link-item__badge--inactive'">
          {{ link.isActive ? 'Active' : 'Inactive' }}
        </span>
      </div>

      <p class="link-item__url">{{ link.url }}</p>

      <dl class="link-item__meta">
        <div>
          <dt>Position</dt>
          <dd>{{ link.position }}</dd>
        </div>
      </dl>
    </div>

    <div class="link-item__actions">
      <button type="button" class="link-item__action" :disabled="busy" @click="$emit('edit', link)">Edit</button>
      <button type="button" class="link-item__action" :disabled="busy" @click="$emit('toggle', link)">
        {{ link.isActive ? 'Deactivate' : 'Activate' }}
      </button>
      <button type="button" class="link-item__action link-item__danger" :disabled="busy" @click="$emit('delete', link)">
        Delete
      </button>
    </div>
  </article>
</template>

<style scoped>
.link-item {
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 18px 20px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-card-sm);
  background: var(--surface-card);
  box-shadow: var(--shadow-soft);
}

.link-item__handle {
  display: grid;
  gap: 4px;
  justify-items: center;
  width: 18px;
}

.link-item__handle span {
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: #c7d1e2;
}

.link-item__icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #eef4ff;
  color: #b85a16;
  font-weight: 800;
}

.link-item__main,
.link-item__meta {
  display: grid;
  gap: 8px;
}

.link-item__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.link-item__topline h3 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1rem;
  color: #132642;
}

.link-item__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 5px 10px;
  border-radius: var(--radius-pill);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.link-item__badge--active {
  background: rgba(74, 140, 88, 0.14);
  color: #2f6b3a;
}

.link-item__badge--inactive {
  background: rgba(107, 92, 78, 0.12);
  color: var(--muted);
}

.link-item__url {
  margin: 0;
  color: #69758a;
  font-size: 0.92rem;
  word-break: break-word;
}

.link-item__meta {
  margin: 0;
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.link-item__meta div {
  display: grid;
  gap: 6px;
}

.link-item__meta dt {
  font-size: 0.8rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #b85a16;
}

.link-item__meta dd {
  margin: 0;
  color: #132642;
  word-break: break-word;
}

.link-item__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.link-item__action {
  min-width: 92px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-pill);
  background: #ffffff;
  color: var(--text-strong);
  padding: 10px 14px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease, border-color 0.18s ease;
}

.link-item__action:hover:enabled {
  transform: translateY(-1px);
  border-color: rgba(184, 90, 22, 0.24);
}

.link-item__action:disabled {
  opacity: 0.7;
  cursor: wait;
}

.link-item__danger {
  color: var(--danger);
}

@media (max-width: 720px) {
  .link-item {
    grid-template-columns: auto 1fr;
    padding: 16px;
  }

  .link-item__topline {
    align-items: flex-start;
    flex-direction: column;
  }

  .link-item__handle {
    grid-row: 1 / span 2;
    align-self: start;
    margin-top: 4px;
  }

  .link-item__icon {
    grid-column: 2;
  }

  .link-item__main,
  .link-item__actions {
    grid-column: 1 / -1;
  }

  .link-item__meta {
    grid-template-columns: 1fr;
  }

  .link-item__actions {
    justify-content: flex-start;
    gap: 10px;
  }

  .link-item__action {
    flex: 1 1 140px;
  }
}
</style>
