import request from './request'
import type { ApiResult, PageResult } from './upload'

export interface SeasonItem {
  id: string
  seasonName: string
  startYear: number
  endYear: number
  status: number
}

export interface TeamItem {
  id: string
  teamCode: string
  teamNameEn: string
  teamNameCn?: string
}

export interface PlayerItem {
  playerId: string
  playerName: string
  playerNameCn?: string
  avatarUrl?: string
  seasonId?: string
  seasonName?: string
  teamId?: string
  teamName?: string
  teamNameEn?: string
  teamNameCn?: string
  lineupPosition?: string
  position?: string
  overallRating?: number
  potentialRating?: number
  rosterStatus: string
  dataJson?: string
  remark?: string
  updatedAt?: string
}

export interface IntegratePlayersParams {
  seasonName: string
  templateCode: string
}

export interface IntegratePlayersResult {
  seasonId: string
  seasonName: string
  templateCode: string
  resultCount: number
  createdCount: number
  updatedCount: number
  historyCount: number
  skippedCount: number
}

export function getPlayerSeasons() {
  return request.get<ApiResult<SeasonItem[]>>('/players/seasons')
}

export function getPlayerTeams(seasonId?: string) {
  return request.get<ApiResult<TeamItem[]>>('/players/teams', { params: { seasonId } })
}

export function getPlayers(params: {
  pageNum?: number
  pageSize?: number
  seasonId?: string
  teamId?: string
  keyword?: string
  rosterStatus?: string
}) {
  return request.get<ApiResult<PageResult<PlayerItem>>>('/players', { params })
}

export function integratePlayers(data: IntegratePlayersParams) {
  return request.post<ApiResult<IntegratePlayersResult>>('/players/integrate', data)
}

export function uploadPlayerAvatar(playerId: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResult<string>>(`/players/${playerId}/avatar`, formData)
}
