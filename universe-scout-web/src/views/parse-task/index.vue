<template>
  <section class="parse-page">
    <header class="page-head">
      <div>
        <h2>解析任务</h2>
        <p>选择上传批次和解析模板，创建一个等待后续视觉模型处理的任务。</p>
      </div>
      <button type="button" @click="openCreateModal">创建解析任务</button>
    </header>

    <section class="filter-panel">
      <label>
        <span>解析模板</span>
        <select v-model="filters.templateCode">
          <option value="">全部模板</option>
          <option v-for="template in templates" :key="template.templateCode" :value="template.templateCode">
            {{ template.templateName }}
          </option>
        </select>
      </label>
      <label>
        <span>任务状态</span>
        <select v-model="filters.status">
          <option value="">全部状态</option>
          <option value="PENDING">待解析</option>
          <option value="RUNNING">解析中</option>
          <option value="SUCCESS">成功</option>
          <option value="PARTIAL_SUCCESS">部分成功</option>
          <option value="FAILED">失败</option>
        </select>
      </label>
      <div class="actions">
        <button type="button" @click="search">查询</button>
        <button type="button" class="ghost-btn" @click="reset">重置</button>
      </div>
    </section>

    <section class="panel">
      <div class="section-title">
        <h3>任务列表</h3>
        <span>共 {{ total }} 条</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>任务编号</th>
              <th>上传批次</th>
              <th>解析模板</th>
              <th>状态</th>
              <th>总数</th>
              <th>成功</th>
              <th>失败</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in tasks" :key="task.taskId">
              <td class="strong">{{ task.taskNo }}</td>
              <td>{{ task.batchNo || task.batchId }}</td>
              <td>{{ task.templateName || task.templateCode }}</td>
              <td><span class="tag">{{ statusText(task.status) }}</span></td>
              <td>{{ task.totalCount }}</td>
              <td>{{ task.successCount }}</td>
              <td>{{ task.failedCount }}</td>
              <td>{{ formatTime(task.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button
                    v-if="canStart(task.status)"
                    type="button"
                    class="link-button"
                    :disabled="startingId === task.taskId"
                    @click="startTask(task)"
                  >
                    {{ startText(task.status, task.taskId) }}
                  </button>
                  <button type="button" class="link-button" @click="openDetail(task.taskId)">查看详情</button>
                </div>
              </td>
            </tr>
            <tr v-if="tasks.length === 0">
              <td colspan="9" class="empty">暂无解析任务</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <footer class="pager">
      <button type="button" class="ghost-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">上一页</button>
      <span>第 {{ pageNum }} / {{ pages || 1 }} 页</span>
      <button type="button" class="ghost-btn" :disabled="pageNum >= pages" @click="changePage(pageNum + 1)">下一页</button>
    </footer>

    <div v-if="showCreate" class="modal" @click.self="closeCreateModal">
      <form class="modal-card create-card" @submit.prevent="submitCreate">
        <header>
          <div>
            <h3>创建解析任务</h3>
            <p>任务创建后会进入 PENDING 状态，等待后续模型解析。</p>
          </div>
          <button type="button" class="ghost-btn" @click="closeCreateModal">关闭</button>
        </header>

        <label>
          <span>上传批次</span>
          <select v-model="createForm.batchId" required>
            <option value="">请选择上传批次</option>
            <option v-for="batch in uploadBatches" :key="batch.batchId" :value="batch.batchId">
              {{ batch.batchNo }} · {{ sourceText(batch.sourceType) }} · {{ batch.totalFiles }} 个文件
            </option>
          </select>
        </label>

        <label>
          <span>解析模板</span>
          <select v-model="createForm.templateCode" required>
            <option value="">请选择解析模板</option>
            <option v-for="template in templates" :key="template.templateCode" :value="template.templateCode">
              {{ template.templateName }}（{{ template.templateCode }}）
            </option>
          </select>
        </label>

        <button type="submit" :disabled="creating">
          {{ creating ? '创建中...' : '确认创建' }}
        </button>
      </form>
    </div>

    <div v-if="detail" class="modal" @click.self="detail = null">
      <section class="modal-card detail-card">
        <header>
          <div>
            <h3>任务详情</h3>
            <p>{{ detail.taskNo }}</p>
          </div>
          <button type="button" class="ghost-btn" @click="detail = null">关闭</button>
        </header>

        <div class="meta">
          <span>任务状态</span><strong>{{ statusText(detail.status) }}</strong>
          <span>解析模板</span><strong>{{ detail.templateName || detail.templateCode }}</strong>
          <span>关联批次</span><strong>{{ detail.batchNo || detail.batchId }}</strong>
          <span>文件总数</span><strong>{{ detail.totalCount }}</strong>
          <span>成功数量</span><strong>{{ detail.successCount }}</strong>
          <span>失败数量</span><strong>{{ detail.failedCount }}</strong>
          <span>创建时间</span><strong>{{ formatTime(detail.createdAt) }}</strong>
          <span>更新时间</span><strong>{{ formatTime(detail.updatedAt) }}</strong>
        </div>

        <div class="table-wrap">
          <h4>关联文件</h4>
          <table>
            <thead>
              <tr>
                <th>原始文件名</th>
                <th>类型</th>
                <th>大小</th>
                <th>状态</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="file in detail.files" :key="file.fileId">
                <td>{{ file.originalFilename }}</td>
                <td>{{ file.fileType }}</td>
                <td>{{ formatSize(file.fileSize) }}</td>
                <td>{{ fileStatusText(file.status) }}</td>
                <td>{{ formatTime(file.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="table-wrap">
          <h4>解析结果</h4>
          <table>
            <thead>
              <tr>
                <th>图片文件名</th>
                <th>模型</th>
                <th>状态</th>
                <th>错误信息</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="result in detail.results" :key="result.resultId">
                <td>{{ result.originalFilename || result.fileId }}</td>
                <td>{{ result.modelName || '-' }}</td>
                <td><span class="tag">{{ resultStatusText(result.status) }}</span></td>
                <td>{{ result.errorMessage || '-' }}</td>
                <td>{{ formatTime(result.createdAt) }}</td>
                <td>
                  <div class="row-actions">
                    <button type="button" class="link-button" @click="openTextModal('原始响应', result.responseRaw)">原始响应</button>
                    <button type="button" class="link-button" @click="openTextModal('结构化 JSON', formatJson(result.responseJson))">JSON</button>
                  </div>
                </td>
              </tr>
              <tr v-if="detail.results.length === 0">
                <td colspan="6" class="empty">暂无解析结果，请先启动解析任务</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div v-if="textModal.visible" class="modal" @click.self="closeTextModal">
      <section class="modal-card text-card">
        <header>
          <div>
            <h3>{{ textModal.title }}</h3>
            <p>模型返回内容</p>
          </div>
          <button type="button" class="ghost-btn" @click="closeTextModal">关闭</button>
        </header>
        <pre>{{ textModal.content || '暂无数据' }}</pre>
        <button type="button" @click="copyText(textModal.content)">复制内容</button>
      </section>
    </div>

    <p v-if="message" class="message" :class="{ error: isError }">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getUploadBatches, type UploadBatchItem } from '../../api/upload'
import {
  createParseTask,
  getParseTaskDetail,
  getParseTasks,
  getParseTemplates,
  startParseTask,
  type ParseTaskDetail,
  type ParseTaskItem,
  type ParseTemplate,
} from '../../api/parseTask'

const pageNum = ref(1)
const pageSize = 10
const pages = ref(0)
const total = ref(0)
const tasks = ref<ParseTaskItem[]>([])
const templates = ref<ParseTemplate[]>([])
const uploadBatches = ref<UploadBatchItem[]>([])
const detail = ref<ParseTaskDetail | null>(null)
const showCreate = ref(false)
const creating = ref(false)
const startingId = ref<string | null>(null)
const message = ref('')
const isError = ref(false)
const textModal = reactive({
  visible: false,
  title: '',
  content: '',
})
const filters = reactive({
  templateCode: '',
  status: '',
})
const createForm = reactive({
  batchId: '',
  templateCode: '',
})

onMounted(async () => {
  await Promise.all([loadTemplates(), loadTasks()])
})

async function loadTemplates() {
  const { data } = await getParseTemplates()
  templates.value = data.data || []
}

async function loadTasks() {
  message.value = ''
  isError.value = false
  try {
    const { data } = await getParseTasks({
      pageNum: pageNum.value,
      pageSize,
      templateCode: filters.templateCode || undefined,
      status: filters.status || undefined,
    })
    if (data.code !== 0 || !data.data) {
      throw new Error(data.message)
    }
    tasks.value = data.data.records
    total.value = data.data.total
    pages.value = data.data.pages
  } catch {
    tasks.value = []
    total.value = 0
    pages.value = 0
    isError.value = true
    message.value = '解析任务加载失败，请确认后端服务已启动到最新版本。'
  }
}

async function openCreateModal() {
  createForm.batchId = ''
  createForm.templateCode = templates.value[0]?.templateCode || ''
  const { data } = await getUploadBatches({ pageNum: 1, pageSize: 100, status: 'UPLOADED' })
  uploadBatches.value = (data.data?.records || []).filter((batch) => batch.successCount > 0)
  showCreate.value = true
}

function closeCreateModal() {
  showCreate.value = false
}

async function submitCreate() {
  creating.value = true
  message.value = ''
  isError.value = false
  try {
    const { data } = await createParseTask({
      batchId: createForm.batchId,
      templateCode: createForm.templateCode,
    })
    if (data.code !== 0) {
      throw new Error(data.message)
    }
    message.value = `解析任务已创建：${data.data.taskNo}`
    showCreate.value = false
    pageNum.value = 1
    await loadTasks()
  } catch {
    isError.value = true
    message.value = '创建解析任务失败，请确认批次和模板可用。'
  } finally {
    creating.value = false
  }
}

async function openDetail(taskId: string) {
  const { data } = await getParseTaskDetail(taskId)
  detail.value = data.data
}

async function startTask(task: ParseTaskItem) {
  const confirmed = window.confirm(`确认开始解析任务“${task.taskNo}”吗？`)
  if (!confirmed) return

  startingId.value = task.taskId
  message.value = ''
  isError.value = false
  try {
    const { data } = await startParseTask(task.taskId)
    if (data.code !== 0) {
      throw new Error(data.message)
    }
    message.value = '解析任务已执行完成。'
    await loadTasks()
    if (detail.value?.taskId === task.taskId) {
      await openDetail(task.taskId)
    }
  } catch {
    isError.value = true
    message.value = '启动解析失败，请确认任务状态和后端服务。'
  } finally {
    startingId.value = null
  }
}

function search() {
  pageNum.value = 1
  loadTasks()
}

function reset() {
  filters.templateCode = ''
  filters.status = ''
  search()
}

function changePage(target: number) {
  pageNum.value = target
  loadTasks()
}

function formatTime(value: string) {
  return value ? value.replace('T', ' ') : '-'
}

function formatSize(size: number) {
  if (!size) return '0 B'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function statusText(value: string) {
  const map: Record<string, string> = {
    PENDING: '待解析',
    RUNNING: '解析中',
    SUCCESS: '成功',
    PARTIAL_SUCCESS: '部分成功',
    FAILED: '失败',
  }
  return map[value] || value
}

function fileStatusText(value: string) {
  if (value === 'UPLOADED') return '已上传'
  if (value === 'FAILED') return '失败'
  return value
}

function sourceText(value: string) {
  if (value === 'IMAGE_BATCH') return '图片批量'
  if (value === 'ZIP') return 'ZIP 包'
  return value
}

function resultStatusText(value: string) {
  if (value === 'SUCCESS') return '成功'
  if (value === 'FAILED') return '失败'
  if (value === 'NEED_REVIEW') return '待复核'
  return value
}

function canStart(status: string) {
  return status === 'PENDING' || status === 'FAILED'
}

function startText(status: string, taskId: string) {
  if (startingId.value === taskId) return '解析中'
  if (status === 'FAILED') return '重新解析'
  return '开始解析'
}

function openTextModal(title: string, content?: string) {
  textModal.visible = true
  textModal.title = title
  textModal.content = content || ''
}

function closeTextModal() {
  textModal.visible = false
}

function formatJson(value?: string) {
  if (!value) return ''
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

async function copyText(value: string) {
  if (!value) return
  await navigator.clipboard.writeText(value)
  message.value = '内容已复制。'
  isError.value = false
}
</script>

<style scoped>
.parse-page {
  display: grid;
  gap: 18px;
}

.page-head,
.filter-panel {
  display: grid;
  gap: 16px;
}

.page-head {
  grid-template-columns: 1fr auto;
  align-items: center;
}

h2,
h3,
h4,
p {
  margin: 0;
}

.page-head h2 {
  font-size: 26px;
}

.page-head p,
.section-title span,
.modal-card p {
  margin-top: 6px;
  color: var(--muted);
}

.filter-panel,
.panel,
.modal-card {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 12px 32px rgb(15 23 42 / 5%);
}

.filter-panel {
  grid-template-columns: 220px 180px auto;
  align-items: end;
  padding: 18px;
}

label {
  display: grid;
  gap: 7px;
  color: #425466;
  font-size: 14px;
  font-weight: 700;
}

input,
select {
  height: 38px;
  padding: 0 10px;
  color: #172033;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  outline: none;
}

input:focus,
select:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgb(29 111 184 / 14%);
}

.actions {
  display: flex;
  gap: 10px;
}

button {
  width: fit-content;
  min-height: 36px;
  padding: 0 14px;
  color: #ffffff;
  background: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 6px;
  cursor: pointer;
  font-weight: 700;
}

button:hover {
  background: var(--primary-dark);
  border-color: var(--primary-dark);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.ghost-btn {
  color: var(--primary);
  background: #ffffff;
}

.link-button {
  min-height: auto;
  padding: 0;
  color: var(--primary);
  background: transparent;
  border: 0;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.panel {
  display: grid;
  gap: 14px;
  padding: 18px;
}

.section-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

th,
td {
  padding: 11px 10px;
  border-bottom: 1px solid var(--line);
  text-align: left;
  vertical-align: top;
}

th {
  color: #425466;
  background: #f5f8fb;
  font-weight: 800;
  white-space: nowrap;
}

.strong {
  color: #102a43;
  font-weight: 800;
}

.tag {
  display: inline-flex;
  padding: 3px 8px;
  color: #0f766e;
  background: #dff6ef;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.empty {
  color: var(--muted);
  text-align: center;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: var(--muted);
}

.modal {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  padding: 28px;
  background: rgb(15 23 42 / 72%);
}

.modal-card {
  display: grid;
  gap: 18px;
  width: min(820px, 96vw);
  max-height: 92vh;
  overflow: auto;
  padding: 20px;
}

.text-card {
  width: min(900px, 96vw);
}

pre {
  max-height: 58vh;
  overflow: auto;
  margin: 0;
  padding: 14px;
  color: #d9e6f2;
  background: #0f172a;
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.create-card {
  width: min(560px, 96vw);
}

.modal-card header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.meta {
  display: grid;
  grid-template-columns: 100px 1fr 100px 1fr;
  gap: 10px 14px;
  padding: 14px;
  background: #f6f9fc;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 14px;
}

.meta span {
  color: var(--muted);
}

.meta strong {
  color: #102a43;
}

.message {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 30;
  max-width: min(420px, calc(100vw - 44px));
  padding: 12px 14px;
  color: #0f766e;
  background: #dff6ef;
  border: 1px solid #b7ead8;
  border-radius: 6px;
  box-shadow: 0 12px 36px rgb(15 23 42 / 18%);
  font-weight: 700;
}

.message.error {
  color: #b42318;
  background: #ffe8e5;
  border-color: #ffc9c1;
}

@media (max-width: 980px) {
  .page-head,
  .filter-panel,
  .meta {
    grid-template-columns: 1fr;
  }
}
</style>
