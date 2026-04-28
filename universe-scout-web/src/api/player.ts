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
  playerProfileId: string
  playerKey?: string
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

export interface PlayerProfileItem {
  profileId: string
  playerKey?: string
  playerName: string
  playerNameCn?: string
  avatarUrl?: string
  position?: string
  updatedAt?: string
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
  position?: string
}) {
  return request.get<ApiResult<PageResult<PlayerItem>>>('/players', { params })
}

export function getPlayerProfiles(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
}) {
  return request.get<ApiResult<PageResult<PlayerProfileItem>>>('/players/profiles', { params })
}

export function integratePlayers(data: IntegratePlayersParams) {
  return request.post<ApiResult<IntegratePlayersResult>>('/players/integrate', data)
}

export function uploadPlayerAvatar(profileId: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResult<string>>(`/players/profiles/${profileId}/avatar`, formData)
}
