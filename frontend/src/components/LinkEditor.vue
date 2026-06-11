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
        <p class="eyebrow">{{ mode === 'create' ? 'Your Links' : 'Editing Link' }}</p>
        <h2>{{ mode === 'create' ? 'Add a new destination' : heading }}</h2>
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

      <label class="link-editor__field link-editor__field--slim">
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
        <span>Active on your public profile</span>
      </label>
    </div>
  </form>
</template>

<style scoped>
.link-editor {
  display: grid;
  gap: 18px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-card-sm);
  padding: 22px;
  background: var(--surface-card);
  box-shadow: var(--shadow-soft);
}

.link-editor--compact {
  padding: 18px;
  border-radius: 20px;
  box-shadow: none;
}

.link-editor__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.link-editor__header h2 {
  margin: 0;
  font-family: "Segoe UI", "Trebuchet MS", sans-serif;
  font-size: 1.35rem;
  letter-spacing: -0.02em;
  color: #132642;
}

.link-editor__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.link-editor__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1.6fr) 130px auto;
  gap: 14px;
  align-items: end;
}

.link-editor__field,
.link-editor__toggle {
  display: grid;
  gap: 8px;
}

.link-editor__field span,
.link-editor__toggle span {
  font-size: 0.88rem;
  font-weight: 700;
  color: #263042;
}

.link-editor__field input {
  width: 100%;
  border: 1px solid var(--line-warm);
  border-radius: var(--radius-field);
  background: #fffdfb;
  padding: 14px 15px;
  color: var(--text-strong);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.link-editor__field input::placeholder {
  color: #99a4b5;
}

.link-editor__field input:focus {
  outline: none;
  border-color: rgba(184, 90, 22, 0.45);
  box-shadow: 0 0 0 4px rgba(184, 90, 22, 0.1);
}

.link-editor__toggle {
  grid-template-columns: auto 1fr;
  align-items: center;
  min-height: 52px;
  padding: 0 14px;
  border: 1px solid rgba(222, 191, 173, 0.72);
  border-radius: var(--radius-field);
  background: #fffdfb;
}

.link-editor__toggle input {
  width: 18px;
  height: 18px;
  accent-color: var(--accent);
}

.link-editor__submit,
.link-editor__secondary {
  border-radius: var(--radius-pill);
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease, box-shadow 0.18s ease;
}

.link-editor__submit {
  border: none;
  background: var(--accent-gradient);
  color: #fff8f1;
  box-shadow: 0 12px 24px rgba(184, 90, 22, 0.16);
}

.link-editor__secondary {
  border: 1px solid var(--line-soft);
  background: #fff;
  color: var(--text-strong);
}

.link-editor__submit:hover:enabled,
.link-editor__secondary:hover:enabled {
  transform: translateY(-1px);
}

.link-editor__submit:hover:enabled {
  box-shadow: 0 16px 28px rgba(184, 90, 22, 0.22);
}

.link-editor__submit:disabled,
.link-editor__secondary:disabled {
  opacity: 0.7;
  cursor: wait;
}

@media (max-width: 980px) {
  .link-editor__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: stretch;
  }
}

@media (max-width: 720px) {
  .link-editor {
    padding: 20px 18px;
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

  .link-editor__toggle {
    min-height: 48px;
  }
}
</style>
