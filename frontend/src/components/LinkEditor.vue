<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  initialValues: {
    type: Object,
    default: () => ({
      title: '',
      url: '',
      isActive: true,
      position: 1,
    }),
  },
  mode: {
    type: String,
    default: 'create',
  },
  saving: {
    type: Boolean,
    default: false,
  },
  compact: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  title: '',
  url: '',
  isActive: true,
  position: 1,
})

const heading = computed(() =>
  props.mode === 'create' ? 'Add a new link' : 'Edit this link',
)

const actionLabel = computed(() => {
  if (props.saving) {
    return props.mode === 'create' ? 'Saving...' : 'Updating...'
  }

  return props.mode === 'create' ? 'Add Link' : 'Save Link'
})

function applyValues(values) {
  form.title = values?.title ?? ''
  form.url = values?.url ?? ''
  form.isActive = values?.isActive ?? true
  form.position = values?.position ?? 1
}

watch(
  () => props.initialValues,
  (values) => {
    applyValues(values)
  },
  { immediate: true, deep: true },
)

function handleSubmit() {
  emit('submit', {
    title: form.title,
    url: form.url,
    isActive: form.isActive,
    position: Number.isFinite(form.position) ? form.position : Number(form.position),
  })
}
</script>

<template>
  <form class="link-editor" :class="{ 'link-editor--compact': compact }" @submit.prevent="handleSubmit">
    <div class="link-editor__header">
      <div>
        <p class="eyebrow">{{ mode === 'create' ? 'Link Management' : 'Editing Link' }}</p>
        <h2>{{ heading }}</h2>
      </div>

      <div class="link-editor__actions">
        <button
          v-if="mode === 'edit'"
          class="link-editor__secondary"
          type="button"
          :disabled="saving"
          @click="emit('cancel')"
        >
          Cancel
        </button>
        <button class="link-editor__submit" type="submit" :disabled="saving">
          {{ actionLabel }}
        </button>
      </div>
    </div>

    <div class="link-editor__grid">
      <label class="link-editor__field">
        <span>Title</span>
        <input
          v-model="form.title"
          type="text"
          name="title"
          placeholder="Instagram"
          autocomplete="off"
        />
      </label>

      <label class="link-editor__field">
        <span>URL</span>
        <input
          v-model="form.url"
          type="url"
          name="url"
          placeholder="https://instagram.com/example"
          inputmode="url"
        />
      </label>

      <label class="link-editor__field">
        <span>Position</span>
        <input
          v-model.number="form.position"
          type="number"
          name="position"
          min="1"
          step="1"
          placeholder="1"
        />
      </label>

      <label class="link-editor__toggle">
        <input v-model="form.isActive" type="checkbox" name="isActive" />
        <span>Link is active on the public page</span>
      </label>
    </div>
  </form>
</template>

<style scoped>
.link-editor {
  display: grid;
  gap: 22px;
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 24px;
  background: var(--surface-strong);
}

.link-editor--compact {
  padding: 20px;
}

.link-editor__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.link-editor__header h2 {
  margin: 0;
  font-family: var(--font-heading);
  font-size: 1.45rem;
}

.link-editor__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.link-editor__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.link-editor__field,
.link-editor__toggle {
  display: grid;
  gap: 8px;
}

.link-editor__field span,
.link-editor__toggle span {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text);
}

.link-editor__field input {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: #fff;
  padding: 14px 15px;
  color: var(--text);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.link-editor__field input:focus {
  outline: none;
  border-color: rgba(182, 84, 45, 0.45);
  box-shadow: 0 0 0 4px rgba(182, 84, 45, 0.12);
}

.link-editor__toggle {
  align-content: end;
  grid-template-columns: auto 1fr;
  align-items: center;
}

.link-editor__toggle input {
  width: 18px;
  height: 18px;
  accent-color: var(--accent);
}

.link-editor__submit,
.link-editor__secondary {
  border-radius: 999px;
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.link-editor__submit {
  border: none;
  background: var(--accent);
  color: #fff8f1;
}

.link-editor__secondary {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--text);
}

.link-editor__submit:hover:enabled,
.link-editor__secondary:hover:enabled {
  transform: translateY(-1px);
}

.link-editor__submit:disabled,
.link-editor__secondary:disabled {
  opacity: 0.7;
  cursor: wait;
}

@media (max-width: 720px) {
  .link-editor {
    padding: 20px;
  }

  .link-editor__header {
    flex-direction: column;
  }

  .link-editor__actions {
    width: 100%;
  }

  .link-editor__actions button {
    flex: 1 1 100%;
  }

  .link-editor__grid {
    grid-template-columns: 1fr;
  }
}
</style>
