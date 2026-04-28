<template>
  <section class="prompt-page">
    <header class="page-head">
      <div>
        <h2>提示词管理</h2>
        <p>管理解析模板的 Prompt 和 JSON Schema。保存后，新创建或重新启动的解析任务会使用最新提示词。</p>
      </div>
      <button type="button" :disabled="loading" @click="loadTemplates">
        {{ loading ? '刷新中...' : '刷新模板' }}
      </button>
    </header>

    <div class="workspace-grid">
      <aside class="template-list">
        <div class="section-title">
          <h3>模板列表</h3>
          <span>{{ templates.length }} 个模板</span>
        </div>

        <button
          v-for="template in templates"
          :key="template.id"
          type="button"
          class="template-item"
          :class="{ active: editing?.id === template.id }"
          @click="selectTemplate(template)"
        >
          <strong>{{ template.templateName }}</strong>
          <span>{{ template.templateCode }}</span>
          <em :class="{ disabled: template.status !== 1 }">{{ template.status === 1 ? '启用' : '停用' }}</em>
        </button>

        <div v-if="templates.length === 0 && !loading" class="empty">
          暂无解析模板
        </div>
      </aside>

      <form v-if="editing" class="editor-panel" @submit.prevent="saveTemplate">
        <div class="editor-head">
          <div>
            <h3>{{ editing.templateName || '未命名模板' }}</h3>
            <p>模板编码：{{ editing.templateCode }}</p>
          </div>
          <span class="status-tag" :class="{ disabled: form.status !== 1 }">
            {{ form.status === 1 ? '启用中' : '已停用' }}
          </span>
        </div>

        <div class="form-grid">
          <label>
            <span>模板名称</span>
            <input v-model.trim="form.templateName" required placeholder="例如：首发名单解析模板" />
          </label>

          <label>
            <span>状态</span>
            <select v-model.number="form.status">
              <option :value="1">启用</option>
              <option :value="0">停用</option>
            </select>
          </label>
        </div>

        <label class="field">
          <span>提示词 Prompt</span>
          <textarea
            v-model="form.prompt"
            required
            class="prompt-textarea"
            spellcheck="false"
            placeholder="请写入发送给视觉模型的完整提示词"
          />
        </label>

        <div class="assist-row">
          <button type="button" class="ghost-btn" @click="insertStrictJsonRules">追加严格 JSON 规则</button>
          <button type="button" class="ghost-btn" @click="copyText(form.prompt)">复制提示词</button>
          <span>{{ promptStats }}</span>
        </div>

        <label class="field">
          <span>JSON Schema / 期望结构</span>
          <textarea
            v-model="form.jsonSchema"
            class="schema-textarea"
            spellcheck="false"
            placeholder='例如：{"type":"LINEUP","season":null}'
          />
        </label>

        <div class="preview-panel">
          <div class="section-title">
            <h3>发送预览</h3>
            <button type="button" class="ghost-btn" @click="copyText(previewText)">复制预览</button>
          </div>
          <pre>{{ previewText }}</pre>
        </div>

        <footer class="editor-actions">
          <button type="button" class="ghost-btn" @click="resetForm">还原修改</button>
          <button type="submit" :disabled="saving || !hasChanges">
            {{ saving ? '保存中...' : '保存提示词' }}
          </button>
        </footer>
      </form>

      <section v-else class="editor-panel empty-editor">
        <h3>请选择一个模板</h3>
        <p>左侧选择 LINEUP 或后续新增的模板后，可以查看和修改它的提示词。</p>
      </section>
    </div>

    <p v-if="message" class="message" :class="{ error: isError }">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  getPromptTemplates,
  updatePromptTemplate,
  type PromptTemplate,
} from '../../api/promptTemplate'

const templates = ref<PromptTemplate[]>([])
const editing = ref<PromptTemplate | null>(null)
const loading = ref(false)
const saving = ref(false)
const message = ref('')
const isError = ref(false)

const form = reactive({
  templateName: '',
  prompt: '',
  jsonSchema: '',
  status: 1,
})

const original = reactive({
  templateName: '',
  prompt: '',
  jsonSchema: '',
  status: 1,
})

const strictRules = [
  '请只返回 JSON，不要输出 Markdown、解释文字或代码块。',
  '只提取图片中真实出现的数据，不要根据现实 NBA 阵容猜测。',
  '看不清的字段填 null。',
  '返回结果必须符合 JSON Schema / 期望结构。',
].join('\n')

const hasChanges = computed(() =>
  form.templateName !== original.templateName ||
  form.prompt !== original.prompt ||
  form.jsonSchema !== original.jsonSchema ||
  form.status !== original.status
)

const promptStats = computed(() => {
  const lines = form.prompt.split(/\r?\n/).filter(Boolean).length
  return `${form.prompt.length} 字符，${lines} 行`
})

const previewText = computed(() => {
  const schema = form.jsonSchema.trim()
  if (!schema) {
    return form.prompt
  }
  return `${form.prompt}\n\n期望 JSON 结构：\n${formatJson(schema)}`
})

onMounted(loadTemplates)

async function loadTemplates() {
  loading.value = true
  try {
    const { data } = await getPromptTemplates()
    templates.value = data.data || []
    if (!editing.value && templates.value.length > 0) {
      selectTemplate(templates.value[0])
    } else if (editing.value) {
      const latest = templates.value.find((item) => item.id === editing.value?.id)
      if (latest) {
        selectTemplate(latest)
      }
    }
    showMessage('模板已刷新')
  } catch (error) {
    showMessage('加载提示词模板失败，请确认后端服务已启动。', true)
  } finally {
    loading.value = false
  }
}

function selectTemplate(template: PromptTemplate) {
  editing.value = template
  form.templateName = template.templateName || ''
  form.prompt = template.prompt || ''
  form.jsonSchema = formatJson(template.jsonSchema || '')
  form.status = template.status ?? 1
  syncOriginal()
}

function syncOriginal() {
  original.templateName = form.templateName
  original.prompt = form.prompt
  original.jsonSchema = form.jsonSchema
  original.status = form.status
}

function resetForm() {
  form.templateName = original.templateName
  form.prompt = original.prompt
  form.jsonSchema = original.jsonSchema
  form.status = original.status
  showMessage('已还原到上次保存的内容')
}

function insertStrictJsonRules() {
  if (form.prompt.includes(strictRules)) {
    showMessage('严格 JSON 规则已经在提示词里了')
    return
  }
  form.prompt = `${form.prompt.trim()}\n\n${strictRules}`.trim()
}

async function saveTemplate() {
  if (!editing.value) return
  saving.value = true
  try {
    const { data } = await updatePromptTemplate(editing.value.id, {
      templateName: form.templateName,
      prompt: form.prompt,
      jsonSchema: form.jsonSchema.trim() || undefined,
      status: form.status,
    })
    const saved = data.data
    if (saved) {
      const index = templates.value.findIndex((item) => item.id === saved.id)
      if (index >= 0) {
        templates.value[index] = saved
      }
      selectTemplate(saved)
    }
    showMessage('提示词已保存')
  } catch (error: any) {
    const backendMessage = error?.response?.data?.message
    showMessage(backendMessage || '保存失败，请检查 JSON Schema 是否为合法 JSON。', true)
  } finally {
    saving.value = false
  }
}

function formatJson(value: string) {
  if (!value) return ''
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

async function copyText(text: string) {
  if (!text) {
    showMessage('暂无可复制内容', true)
    return
  }
  await navigator.clipboard.writeText(text)
  showMessage('已复制到剪贴板')
}

function showMessage(text: string, error = false) {
  message.value = text
  isError.value = error
  window.setTimeout(() => {
    if (message.value === text) {
      message.value = ''
    }
  }, 2400)
}
</script>

<style scoped>
.prompt-page {
  display: grid;
  gap: 18px;
}

.page-head,
.template-list,
.editor-panel {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 10px 22px rgb(15 23 42 / 5%);
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
}

.page-head h2,
.page-head p,
.section-title h3,
.section-title span,
.editor-head h3,
.editor-head p {
  margin: 0;
}

.page-head p,
.editor-head p,
.section-title span,
.assist-row span {
  color: var(--muted);
  font-size: 13px;
}

.workspace-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.template-list {
  display: grid;
  gap: 10px;
  padding: 16px;
}

.section-title,
.editor-head,
.editor-actions,
.assist-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.template-item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 5px 10px;
  width: 100%;
  padding: 12px;
  text-align: left;
  background: #f8fafc;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  color: var(--ink);
  cursor: pointer;
}

.template-item strong,
.template-item span {
  min-width: 0;
}

.template-item span {
  color: var(--muted);
  font-size: 12px;
}

.template-item em {
  grid-row: 1 / span 2;
  align-self: center;
  padding: 4px 8px;
  color: #0f766e;
  background: #dff6ef;
  border-radius: 999px;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.template-item em.disabled,
.status-tag.disabled {
  color: #9a3412;
  background: #ffedd5;
}

.template-item.active {
  background: #eaf4ff;
  border-color: #1d6fb8;
}

.editor-panel {
  display: grid;
  gap: 16px;
  padding: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px;
  gap: 14px;
}

label,
.field {
  display: grid;
  gap: 7px;
}

label span,
.field span {
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

input,
select,
textarea {
  width: 100%;
  color: var(--ink);
  background: #ffffff;
  border: 1px solid #ccd8e5;
  border-radius: 8px;
  font: inherit;
  outline: none;
}

input,
select {
  height: 40px;
  padding: 0 12px;
}

textarea {
  padding: 12px;
  line-height: 1.65;
  resize: vertical;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #1d6fb8;
  box-shadow: 0 0 0 3px rgb(29 111 184 / 12%);
}

.prompt-textarea {
  min-height: 260px;
}

.schema-textarea {
  min-height: 150px;
  font-family: Consolas, "Microsoft YaHei", monospace;
}

.status-tag {
  padding: 6px 10px;
  color: #0f766e;
  background: #dff6ef;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

button {
  min-height: 38px;
  padding: 0 14px;
  color: #ffffff;
  background: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
}

button:hover:not(:disabled) {
  background: var(--primary-dark);
  border-color: var(--primary-dark);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.ghost-btn {
  color: #1d4f7a;
  background: #ffffff;
  border-color: #c8d7e6;
}

.ghost-btn:hover:not(:disabled) {
  color: #ffffff;
  background: #1d6fb8;
}

.preview-panel {
  display: grid;
  gap: 10px;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid var(--line);
  border-radius: 8px;
}

pre {
  max-height: 260px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  color: #1f2937;
  background: #ffffff;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  line-height: 1.65;
}

.empty,
.empty-editor {
  color: var(--muted);
  text-align: center;
}

.empty-editor {
  min-height: 360px;
  place-content: center;
}

.message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 20;
  margin: 0;
  padding: 12px 16px;
  color: #0f766e;
  background: #dff6ef;
  border: 1px solid #b7ead8;
  border-radius: 8px;
  box-shadow: 0 12px 28px rgb(15 23 42 / 16%);
  font-weight: 700;
}

.message.error {
  color: #991b1b;
  background: #fee2e2;
  border-color: #fecaca;
}

@media (max-width: 980px) {
  .workspace-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .page-head,
  .editor-head,
  .editor-actions,
  .assist-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
