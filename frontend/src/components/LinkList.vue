<script setup>
import EmptyState from './EmptyState.vue'
import LinkEditor from './LinkEditor.vue'
import LinkItem from './LinkItem.vue'

defineProps({
  links: {
    type: Array,
    default: () => [],
  },
  editingLinkId: {
    type: String,
    default: '',
  },
  activeActionLinkId: {
    type: String,
    default: '',
  },
})

defineEmits(['start-edit', 'cancel-edit', 'submit-edit', 'delete', 'toggle'])
</script>

<template>
  <section class="link-list">
    <div class="link-list__header">
      <div>
        <p class="eyebrow">Existing Links</p>
        <h2>Your current links</h2>
      </div>
      <p class="link-list__count">{{ links.length }} total</p>
    </div>

    <EmptyState
      v-if="links.length === 0"
      title="No links yet"
      description="Add your first destination above to start building your public profile."
    />

    <div v-else class="link-list__items">
      <div v-for="link in links" :key="link.id" class="link-list__row">
        <LinkItem
          v-if="editingLinkId !== link.id"
          :link="link"
          :busy="activeActionLinkId === link.id"
          @edit="$emit('start-edit', link)"
          @delete="$emit('delete', link)"
          @toggle="$emit('toggle', link)"
        />

        <LinkEditor
          v-else
          mode="edit"
          compact
          :initial-values="link"
          :saving="activeActionLinkId === link.id"
          @cancel="$emit('cancel-edit')"
          @submit="$emit('submit-edit', link.id, $event)"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.link-list {
  display: grid;
  gap: 18px;
}

.link-list__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.link-list__header h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.45rem;
}

.link-list__count {
  margin: 0;
  color: var(--muted);
  font-weight: 700;
}

.link-list__items {
  display: grid;
  gap: 16px;
}

.link-list__row {
  display: grid;
}

@media (max-width: 720px) {
  .link-list__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
