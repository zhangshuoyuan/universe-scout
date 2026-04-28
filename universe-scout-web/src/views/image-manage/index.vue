<template>
  <section class="image-page">
    <header class="page-head">
      <div>
        <h2>图片管理</h2>
        <p>集中查看已入库图片，支持预览、筛选、复制路径和删除。</p>
      </div>
      <button type="button" class="ghost-btn" @click="loadImages">刷新</button>
    </header>

    <section class="filter-panel">
      <label>
        <span>文件名</span>
        <input v-model="filters.keyword" placeholder="输入原始文件名关键词" @keyup.enter="search" />
      </label>
      <label>
        <span>文件类型</span>
        <select v-model="filters.fileType">
          <option value="">全部</option>
          <option value="jpg">JPG</option>
          <option value="jpeg">JPEG</option>
          <option value="png">PNG</option>
          <option value="webp">WEBP</option>
        </select>
      </label>
      <label>
        <span>批次 ID</span>
        <input v-model="filters.batchId" placeholder="可选" @keyup.enter="search" />
      </label>
      <div class="actions">
        <button type="button" @click="search">查询</button>
        <button type="button" class="ghost-btn" @click="reset">重置</button>
      </div>
    </section>

    <section class="summary">
      <article>
        <span>图片总数</span>
        <strong>{{ total }}</strong>
      </article>
      <article>
        <span>当前页</span>
        <strong>{{ images.length }}</strong>
      </article>
      <article>
        <span>删除状态</span>
        <strong>{{ deletingId ? '处理中' : '就绪' }}</strong>
      </article>
    </section>

    <section class="gallery">
      <article v-for="image in images" :key="image.fileId" class="image-card">
        <button type="button" class="preview-button" @click="openPreview(image)">
          <img :src="image.previewUrl" :alt="image.originalFilename" loading="lazy" />
        </button>
        <div class="image-info">
          <strong :title="image.originalFilename">{{ image.originalFilename }}</strong>
          <span>{{ image.fileType?.toUpperCase() }} · {{ formatSize(image.fileSize) }}</span>
          <span>{{ image.imageWidth || '-' }} × {{ image.imageHeight || '-' }}</span>
          <span class="batch">{{ image.batchNo || `批次 ${image.batchId}` }}</span>
        </div>
        <div class="card-actions">
          <button type="button" class="ghost-btn" @click="copyPath(image.filePath)">复制路径</button>
          <button
            type="button"
            class="danger-btn"
            :disabled="deletingId === image.fileId"
            @click="removeImage(image)"
          >
            {{ deletingId === image.fileId ? '删除中' : '删除' }}
          </button>
        </div>
      </article>

      <div v-if="images.length === 0" class="empty">
        暂无图片数据，请先到上传中心上传图片或 zip。
      </div>
    </section>

    <footer class="pager">
      <button type="button" class="ghost-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">上一页</button>
      <span>第 {{ pageNum }} / {{ pages || 1 }} 页</span>
      <button type="button" class="ghost-btn" :disabled="pageNum >= pages" @click="changePage(pageNum + 1)">下一页</button>
    </footer>

    <div v-if="previewImage" class="modal" @click.self="closePreview">
      <div class="modal-body">
        <header>
          <div>
            <strong>{{ previewImage.originalFilename }}</strong>
            <span>{{ previewImage.filePath }}</span>
          </div>
          <button type="button" class="ghost-btn" @click="closePreview">关闭</button>
        </header>
        <img :src="previewImage.previewUrl" :alt="previewImage.originalFilename" />
      </div>
    </div>

    <p v-if="message" class="message" :class="{ error: isError }">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { deleteImageFile, getImageFiles, type ImageFileItem } from '../../api/upload'

const pageNum = ref(1)
const pageSize = 12
const total = ref(0)
const pages = ref(0)
const images = ref<ImageFileItem[]>([])
const previewImage = ref<ImageFileItem | null>(null)
const deletingId = ref<string | null>(null)
const message = ref('')
const isError = ref(false)
const filters = reactive({
  keyword: '',
  fileType: '',
  batchId: '',
})

onMounted(() => {
  loadImages()
})

async function loadImages() {
  message.value = ''
  isError.value = false
  try {
    const { data } = await getImageFiles({
      pageNum: pageNum.value,
      pageSize,
      keyword: filters.keyword || undefined,
      fileType: filters.fileType || undefined,
      batchId: filters.batchId || undefined,
    })
    if (data.code !== 0 || !data.data) {
      throw new Error(data.message || 'load failed')
    }
    images.value = data.data.records
    total.value = data.data.total
    pages.value = data.data.pages
  } catch {
    images.value = []
    total.value = 0
    pages.value = 0
    isError.value = true
    message.value = '图片列表加载失败，请确认后端已重启到最新版本。'
  }
}

function search() {
  pageNum.value = 1
  loadImages()
}

function reset() {
  filters.keyword = ''
  filters.fileType = ''
  filters.batchId = ''
  search()
}

function changePage(target: number) {
  pageNum.value = target
  loadImages()
}

function openPreview(image: ImageFileItem) {
  previewImage.value = image
}

function closePreview() {
  previewImage.value = null
}

async function removeImage(image: ImageFileItem) {
  const confirmed = window.confirm(`确认删除图片“${image.originalFilename}”吗？`)
  if (!confirmed) return

  deletingId.value = image.fileId
  message.value = ''
  isError.value = false
  try {
    const { data } = await deleteImageFile(image.fileId)
    if (data.code !== 0) {
      throw new Error(data.message || 'delete failed')
    }
    images.value = images.value.filter((item) => item.fileId !== image.fileId)
    total.value = Math.max(0, total.value - 1)
    if (previewImage.value?.fileId === image.fileId) {
      previewImage.value = null
    }
    message.value = '图片已删除。'
    await loadImages()
  } catch {
    isError.value = true
    message.value = '删除失败，请确认后端服务已重启到最新版本。'
  } finally {
    deletingId.value = null
  }
}

async function copyPath(path: string) {
  try {
    await navigator.clipboard.writeText(path)
    message.value = '本地路径已复制。'
    isError.value = false
  } catch {
    message.value = path
    isError.value = false
  }
}

function formatSize(size: number) {
  if (!size) return '0 B'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>

<style scoped>
.image-page {
  display: grid;
  gap: 18px;
}

.page-head,
.filter-panel,
.summary,
.gallery {
  display: grid;
  gap: 16px;
}

.page-head {
  grid-template-columns: 1fr auto;
  align-items: center;
}

h2,
p {
  margin: 0;
}

.page-head h2 {
  font-size: 26px;
}

.page-head p {
  margin-top: 6px;
  color: var(--muted);
}

.filter-panel {
  grid-template-columns: minmax(220px, 1fr) 160px 160px auto;
  align-items: end;
  padding: 18px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 12px 32px rgb(15 23 42 / 5%);
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
  opacity: 0.55;
}

.ghost-btn {
  color: var(--primary);
  background: #ffffff;
}

.danger-btn {
  color: #ffffff;
  background: var(--danger);
  border-color: var(--danger);
}

.summary {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary article {
  display: grid;
  gap: 8px;
  padding: 16px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
}

.summary span {
  color: var(--muted);
  font-weight: 700;
}

.summary strong {
  color: #102a43;
  font-size: 24px;
}

.gallery {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.image-card {
  overflow: hidden;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 12px 32px rgb(15 23 42 / 5%);
}

.preview-button {
  display: block;
  width: 100%;
  height: 178px;
  padding: 0;
  background: #102a43;
  border: 0;
  border-radius: 0;
}

.preview-button img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #0f172a;
}

.image-info {
  display: grid;
  gap: 5px;
  padding: 12px;
}

.image-info strong {
  overflow: hidden;
  color: #102a43;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-info span {
  color: var(--muted);
  font-size: 13px;
}

.batch {
  color: var(--primary) !important;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 0 12px 12px;
}

.card-actions button {
  flex: 1;
}

.empty {
  grid-column: 1 / -1;
  padding: 50px;
  color: var(--muted);
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
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

.modal-body {
  display: grid;
  gap: 14px;
  width: min(980px, 96vw);
  max-height: 92vh;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
}

.modal-body header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.modal-body strong,
.modal-body span {
  display: block;
}

.modal-body span {
  max-width: 760px;
  overflow: hidden;
  margin-top: 5px;
  color: var(--muted);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.modal-body img {
  max-width: 100%;
  max-height: calc(92vh - 96px);
  object-fit: contain;
  background: #0f172a;
  border-radius: 6px;
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

@media (max-width: 1280px) {
  .gallery {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .filter-panel,
  .summary,
  .gallery {
    grid-template-columns: 1fr;
  }
}
</style>
