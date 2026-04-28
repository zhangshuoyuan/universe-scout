<template>
  <section class="avatar-page">
    <header class="page-head">
      <div>
        <h2>球员头像管理</h2>
        <p>维护球员主表的中文名、英文名和头像，赛季球队数据会关联这里的球员。</p>
      </div>
      <button type="button" @click="loadProfiles">刷新</button>
    </header>

    <section class="filter-panel">
      <label>
        <span>球员搜索</span>
        <input v-model.trim="keyword" placeholder="输入中文名或英文名" @keyup.enter="search" />
      </label>
      <div class="actions">
        <button type="button" @click="search">查询</button>
        <button type="button" class="ghost-btn" @click="reset">重置</button>
      </div>
    </section>

    <section class="panel">
      <div class="section-title">
        <h3>球员主表</h3>
        <span>共 {{ total }} 条</span>
      </div>
      <div class="profile-grid">
        <article v-for="profile in profiles" :key="profile.profileId" class="profile-card">
          <div class="avatar">
            <img v-if="profile.avatarUrl" :src="profile.avatarUrl" :alt="profile.playerName" />
            <span v-else>{{ initials(profile.playerNameCn || profile.playerName) }}</span>
          </div>
          <div class="profile-info">
            <h3>{{ profile.playerNameCn || '中文名未填写' }}</h3>
            <p>{{ profile.playerName || '英文名未填写' }}</p>
            <p class="key">{{ profile.playerKey || '-' }}</p>
            <span class="position-tag">{{ profile.position || '未知位置' }}</span>
            <label class="avatar-upload">
              上传头像
              <input type="file" accept=".jpg,.jpeg,.png,.webp,image/*" @change="uploadAvatar(profile, $event)" />
            </label>
          </div>
        </article>
        <div v-if="profiles.length === 0" class="empty">暂无球员主表数据，请先整合解析数据。</div>
      </div>
    </section>

    <footer class="pager">
      <button type="button" class="ghost-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">上一页</button>
      <span>第 {{ pageNum }} / {{ pages || 1 }} 页</span>
      <button type="button" class="ghost-btn" :disabled="pageNum >= pages" @click="changePage(pageNum + 1)">下一页</button>
    </footer>

    <p v-if="message" class="message" :class="{ error: isError }">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getPlayerProfiles, uploadPlayerAvatar, type PlayerProfileItem } from '../../api/player'

const pageNum = ref(1)
const pageSize = 12
const pages = ref(0)
const total = ref(0)
const keyword = ref('')
const profiles = ref<PlayerProfileItem[]>([])
const message = ref('')
const isError = ref(false)

onMounted(loadProfiles)

async function loadProfiles() {
  const { data } = await getPlayerProfiles({ pageNum: pageNum.value, pageSize, keyword: keyword.value || undefined })
  profiles.value = data.data?.records || []
  total.value = data.data?.total || 0
  pages.value = data.data?.pages || 0
}

async function search() {
  pageNum.value = 1
  await loadProfiles()
}

async function reset() {
  keyword.value = ''
  pageNum.value = 1
  await loadProfiles()
}

async function changePage(nextPage: number) {
  if (nextPage < 1 || nextPage > pages.value) return
  pageNum.value = nextPage
  await loadProfiles()
}

async function uploadAvatar(profile: PlayerProfileItem, event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  try {
    const { data } = await uploadPlayerAvatar(profile.profileId, file)
    profile.avatarUrl = `${data.data}?t=${Date.now()}`
    showMessage('头像已上传')
  } catch (error: any) {
    showMessage(error?.response?.data?.message || '头像上传失败', true)
  }
}

function initials(name: string) {
  return (name || 'P').split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase()
}

function showMessage(text: string, error = false) {
  message.value = text
  isError.value = error
  window.setTimeout(() => {
    if (message.value === text) message.value = ''
  }, 2600)
}
</script>

<style scoped>
.avatar-page {
  display: grid;
  gap: 18px;
}

.page-head,
.filter-panel,
.panel {
  padding: 18px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 10px 22px rgb(15 23 42 / 5%);
}

.page-head,
.filter-panel,
.section-title,
.actions,
.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.filter-panel label {
  display: grid;
  flex: 1;
  gap: 7px;
}

.page-head h2,
.page-head p,
.section-title h3,
.section-title span,
.profile-card h3,
.profile-card p {
  margin: 0;
}

.page-head p,
.section-title span,
.profile-card p {
  color: var(--muted);
  font-size: 13px;
}

label span {
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

input {
  height: 40px;
  padding: 0 12px;
  border: 1px solid #ccd8e5;
  border-radius: 8px;
}

button,
.avatar-upload {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  justify-content: center;
  padding: 0 14px;
  color: #ffffff;
  background: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
}

.ghost-btn {
  color: #1d4f7a;
  background: #ffffff;
  border-color: #c8d7e6;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.profile-card {
  display: grid;
  grid-template-columns: 82px minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
}

.avatar {
  display: grid;
  width: 82px;
  height: 82px;
  place-items: center;
  overflow: hidden;
  color: #ffffff;
  background: #1d6fb8;
  border-radius: 8px;
  font-weight: 800;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.profile-info h3,
.profile-info p {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.position-tag {
  width: fit-content;
  padding: 3px 8px;
  color: #0f766e;
  background: #dff6ef;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.avatar-upload {
  position: relative;
  width: fit-content;
  overflow: hidden;
}

.avatar-upload input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.empty {
  grid-column: 1 / -1;
  padding: 36px;
  color: var(--muted);
  text-align: center;
}

.pager {
  justify-content: center;
}

.message {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 40;
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

@media (max-width: 680px) {
  .page-head,
  .filter-panel,
  .profile-card {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
