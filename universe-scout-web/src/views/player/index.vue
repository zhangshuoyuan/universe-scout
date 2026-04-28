<template>
  <section class="player-page">
    <header class="page-head">
      <div>
        <h2>球员管理</h2>
        <p>按赛季和当前队伍筛选球员，查看中文名、英文名、球队、位置、能力属性和解析数据。</p>
      </div>
      <button type="button" @click="openIntegrate">整合解析数据</button>
    </header>

    <section class="filter-panel">
      <label>
        <span>赛季</span>
        <select v-model="filters.seasonId" @change="onSeasonChange">
          <option value="">全部赛季</option>
          <option v-for="season in seasons" :key="season.id" :value="season.id">
            {{ season.seasonName }}
          </option>
        </select>
      </label>

      <label>
        <span>当前队伍</span>
        <select v-model="filters.teamId">
          <option value="">全部队伍</option>
          <option v-for="team in teams" :key="team.id" :value="team.id">
            {{ teamLabel(team) }}
          </option>
        </select>
      </label>

      <label>
        <span>名单状态</span>
        <select v-model="filters.rosterStatus">
          <option value="">全部状态</option>
          <option value="CURRENT">当前</option>
          <option value="HISTORY">历史</option>
        </select>
      </label>

      <label>
        <span>位置</span>
        <select v-model="filters.position">
          <option value="">全部位置</option>
          <option value="PG">PG</option>
          <option value="SG">SG</option>
          <option value="SF">SF</option>
          <option value="PF">PF</option>
          <option value="C">C</option>
        </select>
      </label>

      <label>
        <span>球员搜索</span>
        <input v-model.trim="filters.keyword" placeholder="输入中文名或英文名" @keyup.enter="search" />
      </label>

      <div class="actions">
        <button type="button" @click="search">查询</button>
        <button type="button" class="ghost-btn" @click="reset">重置</button>
      </div>
    </section>

    <section class="panel">
      <div class="section-title">
        <h3>球员列表</h3>
        <span>共 {{ total }} 条</span>
      </div>

      <div class="player-grid">
        <article v-for="player in players" :key="player.playerId" class="player-card">
          <div class="avatar">
            <img v-if="player.avatarUrl" :src="player.avatarUrl" :alt="player.playerName" />
            <span v-else>{{ initials(player.playerNameCn || player.playerName) }}</span>
          </div>

          <div class="player-info">
            <div class="name-row">
              <div class="names">
                <h3>{{ player.playerNameCn || '中文名未填写' }}</h3>
                <p>{{ player.playerName || '英文名未填写' }}</p>
              </div>
              <em :class="{ history: player.rosterStatus === 'HISTORY' }">
                {{ statusText(player.rosterStatus) }}
              </em>
            </div>

            <p class="team-line">
              {{ player.teamNameCn || player.teamName || '未分配球队' }}
              <span v-if="player.teamNameEn">/ {{ player.teamNameEn }}</span>
              · {{ player.seasonName || '未分配赛季' }}
            </p>

            <dl>
              <div><dt>首发位置</dt><dd>{{ player.lineupPosition || '-' }}</dd></div>
              <div><dt>主位置</dt><dd>{{ player.position || '-' }}</dd></div>
              <div><dt>能力</dt><dd>{{ player.overallRating ?? '-' }}</dd></div>
              <div><dt>潜力</dt><dd>{{ player.potentialRating ?? '-' }}</dd></div>
            </dl>

            <div class="card-actions">
              <button type="button" class="ghost-btn" @click="openData(player)">查看数据</button>
            </div>
          </div>
        </article>

        <div v-if="players.length === 0" class="empty">
          暂无球员数据，可以先完成解析任务，再点击右上角整合解析数据。
        </div>
      </div>
    </section>

    <footer class="pager">
      <button type="button" class="ghost-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">上一页</button>
      <span>第 {{ pageNum }} / {{ pages || 1 }} 页</span>
      <button type="button" class="ghost-btn" :disabled="pageNum >= pages" @click="changePage(pageNum + 1)">下一页</button>
    </footer>

    <div v-if="showIntegrate" class="modal" @click.self="showIntegrate = false">
      <form class="modal-card" @submit.prevent="submitIntegrate">
        <header>
          <div>
            <h3>整合解析数据</h3>
            <p>从 vision_parse_result 的成功 LINEUP 结果中读取 JSON，并写入球员表。</p>
          </div>
          <button type="button" class="ghost-btn" @click="showIntegrate = false">关闭</button>
        </header>

        <label>
          <span>赛季</span>
          <input v-model.trim="integrateForm.seasonName" required placeholder="例如：2026-27" />
        </label>

        <label>
          <span>模型类型</span>
          <select v-model="integrateForm.templateCode" required>
            <option value="LINEUP">LINEUP 首发名单</option>
          </select>
        </label>

        <button type="submit" :disabled="integrating">
          {{ integrating ? '整合中...' : '开始整合' }}
        </button>

        <div v-if="integrateResult" class="result-box">
          <strong>整合完成</strong>
          <span>解析结果：{{ integrateResult.resultCount }}</span>
          <span>新增：{{ integrateResult.createdCount }}</span>
          <span>更新：{{ integrateResult.updatedCount }}</span>
          <span>旧队标记历史：{{ integrateResult.historyCount }}</span>
          <span>跳过：{{ integrateResult.skippedCount }}</span>
        </div>
      </form>
    </div>

    <div v-if="dataModal.visible" class="modal" @click.self="closeData">
      <section class="modal-card data-card">
        <header>
          <div>
            <h3>{{ dataModal.title }}</h3>
            <p>解析写入 player.data_json 的原始字段</p>
          </div>
          <button type="button" class="ghost-btn" @click="closeData">关闭</button>
        </header>
        <pre>{{ dataModal.content || '暂无数据' }}</pre>
      </section>
    </div>

    <p v-if="message" class="message" :class="{ error: isError }">{{ message }}</p>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  getPlayers,
  getPlayerSeasons,
  getPlayerTeams,
  integratePlayers,
  type IntegratePlayersResult,
  type PlayerItem,
  type SeasonItem,
  type TeamItem,
} from '../../api/player'

const pageNum = ref(1)
const pageSize = 12
const pages = ref(0)
const total = ref(0)
const seasons = ref<SeasonItem[]>([])
const teams = ref<TeamItem[]>([])
const players = ref<PlayerItem[]>([])
const message = ref('')
const isError = ref(false)
const showIntegrate = ref(false)
const integrating = ref(false)
const integrateResult = ref<IntegratePlayersResult | null>(null)

const filters = reactive({
  seasonId: '',
  teamId: '',
  keyword: '',
  rosterStatus: 'CURRENT',
  position: '',
})

const integrateForm = reactive({
  seasonName: '',
  templateCode: 'LINEUP',
})

const dataModal = reactive({
  visible: false,
  title: '',
  content: '',
})

onMounted(async () => {
  await loadSeasons()
  await loadTeams()
  await loadPlayers()
})

async function loadSeasons() {
  const { data } = await getPlayerSeasons()
  seasons.value = data.data || []
  if (!filters.seasonId && seasons.value.length > 0) {
    filters.seasonId = seasons.value[0].id
    integrateForm.seasonName = seasons.value[0].seasonName
  }
}

async function loadTeams() {
  const { data } = await getPlayerTeams(filters.seasonId || undefined)
  teams.value = data.data || []
}

async function loadPlayers() {
  const { data } = await getPlayers({
    pageNum: pageNum.value,
    pageSize,
    seasonId: filters.seasonId || undefined,
    teamId: filters.teamId || undefined,
    keyword: filters.keyword || undefined,
    rosterStatus: filters.rosterStatus || undefined,
    position: filters.position || undefined,
  })
  players.value = data.data?.records || []
  total.value = data.data?.total || 0
  pages.value = data.data?.pages || 0
}

async function onSeasonChange() {
  filters.teamId = ''
  const season = seasons.value.find((item) => item.id === filters.seasonId)
  if (season) {
    integrateForm.seasonName = season.seasonName
  }
  pageNum.value = 1
  await loadTeams()
  await loadPlayers()
}

async function search() {
  pageNum.value = 1
  await loadPlayers()
}

async function reset() {
  filters.seasonId = seasons.value[0]?.id || ''
  filters.teamId = ''
  filters.keyword = ''
  filters.rosterStatus = 'CURRENT'
  filters.position = ''
  pageNum.value = 1
  await loadTeams()
  await loadPlayers()
}

async function changePage(nextPage: number) {
  if (nextPage < 1 || nextPage > pages.value) return
  pageNum.value = nextPage
  await loadPlayers()
}

function openIntegrate() {
  const season = seasons.value.find((item) => item.id === filters.seasonId)
  integrateForm.seasonName = season?.seasonName || integrateForm.seasonName
  integrateResult.value = null
  showIntegrate.value = true
}

async function submitIntegrate() {
  integrating.value = true
  try {
    const { data } = await integratePlayers({
      seasonName: integrateForm.seasonName,
      templateCode: integrateForm.templateCode,
    })
    integrateResult.value = data.data
    showMessage('解析数据已整合到球员表')
    await loadSeasons()
    filters.seasonId = data.data.seasonId
    await loadTeams()
    await loadPlayers()
  } catch (error: any) {
    showMessage(error?.response?.data?.message || '整合失败，请检查解析结果是否存在。', true)
  } finally {
    integrating.value = false
  }
}

function openData(player: PlayerItem) {
  dataModal.title = player.playerNameCn
    ? `${player.playerNameCn} / ${player.playerName}`
    : player.playerName
  dataModal.content = formatJson(player.dataJson || '')
  dataModal.visible = true
}

function closeData() {
  dataModal.visible = false
}

function teamLabel(team: TeamItem) {
  if (team.teamNameCn && team.teamNameEn) {
    return `${team.teamNameCn} / ${team.teamNameEn}`
  }
  return team.teamNameCn || team.teamNameEn || team.teamCode
}

function formatJson(value: string) {
  if (!value) return ''
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function initials(name: string) {
  return (name || 'P')
    .split(/\s+/)
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

function statusText(status: string) {
  return status === 'HISTORY' ? '历史' : '当前'
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
.player-page {
  display: grid;
  gap: 18px;
}

.page-head,
.filter-panel,
.panel {
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
.player-card h3,
.player-card p {
  margin: 0;
}

.page-head p,
.section-title span,
.player-card p {
  color: var(--muted);
  font-size: 13px;
}

.filter-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(140px, 1fr)) auto;
  gap: 14px;
  align-items: end;
  padding: 16px;
}

label {
  display: grid;
  gap: 7px;
}

label span {
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

input,
select {
  height: 40px;
  padding: 0 12px;
  color: var(--ink);
  background: #ffffff;
  border: 1px solid #ccd8e5;
  border-radius: 8px;
  outline: none;
}

input:focus,
select:focus {
  border-color: #1d6fb8;
  box-shadow: 0 0 0 3px rgb(29 111 184 / 12%);
}

.actions,
.card-actions,
.pager {
  display: flex;
  gap: 10px;
  align-items: center;
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

button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.ghost-btn {
  color: #1d4f7a;
  background: #ffffff;
  border-color: #c8d7e6;
}

.panel {
  padding: 18px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.player-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 14px;
}

.player-card {
  display: grid;
  grid-template-columns: 74px minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
}

.avatar {
  display: grid;
  width: 74px;
  height: 74px;
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

.player-info {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.names {
  min-width: 0;
}

.names h3,
.names p,
.team-line {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.names p {
  margin-top: 3px;
}

.team-line span {
  color: var(--muted);
}

.name-row em {
  flex: 0 0 auto;
  padding: 4px 8px;
  color: #0f766e;
  background: #dff6ef;
  border-radius: 999px;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.name-row em.history {
  color: #9a3412;
  background: #ffedd5;
}

dl {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin: 0;
}

dt {
  color: var(--muted);
  font-size: 12px;
}

dd {
  margin: 2px 0 0;
  font-weight: 800;
}

.avatar-upload {
  position: relative;
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

.modal {
  position: fixed;
  inset: 0;
  z-index: 30;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgb(15 23 42 / 46%);
}

.modal-card {
  display: grid;
  gap: 16px;
  width: min(620px, 100%);
  max-height: 90vh;
  padding: 18px;
  overflow: auto;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 24px 60px rgb(15 23 42 / 28%);
}

.modal-card header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.modal-card h3,
.modal-card p {
  margin: 0;
}

.modal-card p {
  margin-top: 4px;
  color: var(--muted);
  font-size: 13px;
}

.result-box {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid var(--line);
  border-radius: 8px;
}

.result-box strong {
  grid-column: 1 / -1;
}

.data-card {
  width: min(760px, 100%);
}

pre {
  max-height: 460px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8fafc;
  border: 1px solid var(--line);
  border-radius: 8px;
  line-height: 1.6;
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

@media (max-width: 1060px) {
  .filter-panel {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 680px) {
  .page-head,
  .modal-card header {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-panel,
  .player-card,
  dl,
  .result-box {
    grid-template-columns: 1fr;
  }
}
</style>
