<template>
  <section class="upload-page">
    <header class="page-head">
      <div>
        <h2>上传中心</h2>
        <p>上传图片或 zip 图片包，系统会保存文件并生成批次记录。</p>
      </div>
      <button type="button" class="ghost-btn" @click="loadBatches">刷新列表</button>
    </header>

    <div class="upload-grid">
      <form class="upload-card" @submit.prevent="handleImageUpload">
        <div class="card-title">
          <strong>多图片上传</strong>
          <span>支持 jpg / jpeg / png / webp</span>
        </div>
        <label class="file-zone">
          <input type="file" multiple accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" @change="onImageChange" />
          <strong>选择图片</strong>
          <span>已选择 {{ imageFiles.length }} 张</span>
        </label>
        <button type="submit" :disabled="uploadingImages || imageFiles.length === 0">
          {{ uploadingImages ? '上传中...' : '上传图片' }}
        </button>
      </form>

      <form class="upload-card" @submit.prevent="handleZipUpload">
        <div class="card-title">
          <strong>ZIP 图片包</strong>
          <span>系统会自动解压并忽略非图片文件</span>
        </div>
        <label class="file-zone">
          <input type="file" accept=".zip,application/zip" @change="onZipChange" />
          <strong>选择 zip</strong>
          <span>{{ zipFile ? zipFile.name : '尚未选择文件' }}</span>
        </label>
        <button type="submit" :disabled="uploadingZip || !zipFile">
          {{ uploadingZip ? '上传中...' : '上传 ZIP' }}
        </button>
      </form>
    </div>

    <p v-if="message" class="message" :class="{ error: isError }">{{ message }}</p>

    <div class="content-grid">
      <section class="panel">
        <div class="section-title">
          <h3>上传批次</h3>
          <span>最近 20 条</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>批次编号</th>
                <th>来源</th>
                <th>状态</th>
                <th>总数</th>
                <th>成功</th>
                <th>失败</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="batch in batches" :key="batch.batchId">
                <td class="batch-no">{{ batch.batchNo }}</td>
                <td>{{ sourceText(batch.sourceType) }}</td>
                <td><span class="tag">{{ statusText(batch.status) }}</span></td>
                <td>{{ batch.totalFiles }}</td>
                <td>{{ batch.successCount }}</td>
                <td>{{ batch.failedCount }}</td>
                <td>{{ formatTime(batch.createdAt) }}</td>
                <td>
                  <button type="button" class="link-button" @click="loadDetail(batch.batchId)">查看</button>
                </td>
              </tr>
              <tr v-if="batches.length === 0">
                <td colspan="8" class="empty">暂无上传批次</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel detail-panel">
        <div class="section-title">
          <h3>批次详情</h3>
          <span>{{ detail ? `${detail.files.length} 个文件` : '未选择' }}</span>
        </div>
        <div v-if="detail" class="detail">
          <div class="meta">
            <span>批次编号</span>
            <strong>{{ detail.batchNo }}</strong>
            <span>来源类型</span>
            <strong>{{ sourceText(detail.sourceType) }}</strong>
            <span>批次状态</span>
            <strong>{{ statusText(detail.status) }}</strong>
            <span>文件总数</span>
            <strong>{{ detail.totalFiles }}</strong>
          </div>

          <div class="table-wrap compact">
            <table>
              <thead>
                <tr>
                  <th>原始文件名</th>
                  <th>类型</th>
                  <th>大小</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="file in detail.files" :key="file.fileId">
                  <td>{{ file.originalFilename }}</td>
                  <td>{{ file.fileType }}</td>
                  <td>{{ formatSize(file.fileSize) }}</td>
                  <td>{{ statusText(file.status) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <p v-else class="empty detail-empty">点击批次列表中的“查看”展示文件明细。</p>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  getUploadBatchDetail,
  getUploadBatches,
  uploadImages,
  uploadZip,
  type UploadBatchDetail,
  type UploadBatchItem,
} from '../../api/upload'

const imageFiles = ref<File[]>([])
const zipFile = ref<File | null>(null)
const uploadingImages = ref(false)
const uploadingZip = ref(false)
const message = ref('')
const isError = ref(false)
const batches = ref<UploadBatchItem[]>([])
const detail = ref<UploadBatchDetail | null>(null)

onMounted(() => {
  loadBatches()
})

function onImageChange(event: Event) {
  const target = event.target as HTMLInputElement
  imageFiles.value = Array.from(target.files || [])
}

function onZipChange(event: Event) {
  const target = event.target as HTMLInputElement
  zipFile.value = target.files?.[0] || null
}

async function handleImageUpload() {
  uploadingImages.value = true
  await runUpload(async () => {
    const { data } = await uploadImages(imageFiles.value)
    message.value = `图片上传成功：${data.data.batchNo}，共 ${data.data.fileCount} 个文件。`
    imageFiles.value = []
  })
  uploadingImages.value = false
}

async function handleZipUpload() {
  if (!zipFile.value) return
  uploadingZip.value = true
  await runUpload(async () => {
    const { data } = await uploadZip(zipFile.value as File)
    message.value = `ZIP 上传成功：${data.data.batchNo}，解压 ${data.data.fileCount} 张图片。`
    zipFile.value = null
  })
  uploadingZip.value = false
}

async function runUpload(action: () => Promise<void>) {
  message.value = ''
  isError.value = false
  try {
    await action()
    await loadBatches()
  } catch {
    isError.value = true
    message.value = '上传失败，请检查后端服务、文件类型或本地目录权限。'
  }
}

async function loadBatches() {
  const { data } = await getUploadBatches({ pageNum: 1, pageSize: 20 })
  batches.value = data.data.records
}

async function loadDetail(batchId: number) {
  const { data } = await getUploadBatchDetail(batchId)
  detail.value = data.data
}

function formatSize(size: number) {
  if (!size) return '0 B'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function formatTime(value: string) {
  return value ? value.replace('T', ' ') : '-'
}

function sourceText(value: string) {
  if (value === 'IMAGE_BATCH') return '图片批量'
  if (value === 'ZIP') return 'ZIP 包'
  return value
}

function statusText(value: string) {
  if (value === 'UPLOADED') return '已上传'
  if (value === 'FAILED') return '失败'
  return value
}
</script>

<style scoped>
.upload-page {
  display: grid;
  gap: 18px;
}

.page-head,
.upload-grid,
.content-grid {
  display: grid;
  gap: 16px;
}

.page-head {
  grid-template-columns: 1fr auto;
  align-items: center;
}

h2,
h3,
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

.upload-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.content-grid {
  grid-template-columns: minmax(0, 1.2fr) minmax(380px, 0.8fr);
  align-items: start;
}

.upload-card,
.panel {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 12px 32px rgb(15 23 42 / 5%);
}

.upload-card {
  display: grid;
  gap: 16px;
  padding: 18px;
}

.card-title strong,
.card-title span,
.section-title span {
  display: block;
}

.card-title strong,
.section-title h3 {
  color: #102a43;
  font-size: 18px;
}

.card-title span,
.section-title span {
  margin-top: 4px;
  color: var(--muted);
  font-size: 13px;
}

.file-zone {
  display: grid;
  gap: 8px;
  padding: 18px;
  background: #f6f9fc;
  border: 1px dashed #9fc3df;
  border-radius: 8px;
  cursor: pointer;
}

.file-zone input {
  width: 100%;
}

.file-zone strong {
  color: var(--primary);
}

.file-zone span {
  color: var(--muted);
  font-size: 13px;
}

button {
  width: fit-content;
  min-height: 36px;
  padding: 0 16px;
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
  opacity: 0.65;
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

.message {
  padding: 12px 14px;
  color: #0f766e;
  background: #dff6ef;
  border: 1px solid #b7ead8;
  border-radius: 6px;
  font-weight: 700;
}

.message.error {
  color: #b42318;
  background: #ffe8e5;
  border-color: #ffc9c1;
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

td {
  color: #243b53;
}

.batch-no {
  color: #102a43;
  font-weight: 700;
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

.detail {
  display: grid;
  gap: 16px;
}

.meta {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 8px 12px;
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

.detail-empty {
  padding: 40px 10px;
}

@media (max-width: 1100px) {
  .upload-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
