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
      <button type="button" :disabled="busy" @click="$emit('toggle', link)">
        {{ link.isActive ? 'Deactivate' : 'Activate' }}
      </button>
      <button type="button" :disabled="busy" @click="$emit('edit', link)">Edit</button>
      <button type="button" class="link-item__danger" :disabled="busy" @click="$emit('delete', link)">
        Delete
      </button>
    </div>
  </article>
</template>

<style scoped>
.link-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
}

.link-item__main,
.link-item__meta {
  display: grid;
  gap: 12px;
}

.link-item__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.link-item__topline h3 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.2rem;
}

.link-item__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
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
  color: var(--muted);
  word-break: break-word;
}

.link-item__meta {
  margin: 0;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.link-item__meta div {
  display: grid;
  gap: 6px;
}

.link-item__meta dt {
  font-size: 0.8rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--accent);
}

.link-item__meta dd {
  margin: 0;
  color: var(--text);
  word-break: break-word;
}

.link-item__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.link-item__actions button {
  min-width: 118px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--surface-strong);
  color: var(--text);
  padding: 10px 14px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.link-item__actions button:hover:enabled {
  transform: translateY(-1px);
}

.link-item__actions button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.link-item__danger {
  color: var(--danger);
}

@media (max-width: 720px) {
  .link-item {
    grid-template-columns: 1fr;
  }

  .link-item__topline {
    align-items: flex-start;
    flex-direction: column;
  }

  .link-item__meta {
    grid-template-columns: 1fr;
  }

  .link-item__actions {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .link-item__actions button {
    flex: 1 1 140px;
  }
}
</style>
