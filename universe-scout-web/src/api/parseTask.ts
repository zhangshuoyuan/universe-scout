import request from './request'
import type { ApiResult, PageResult, UploadFileItem } from './upload'

export interface ParseTemplate {
  id: string
  templateCode: string
  templateName: string
  status: number
  createdAt: string
}

export interface CreateParseTaskParams {
  batchId: string
  templateCode: string
}

export interface CreateParseTaskResult {
  taskId: string
  taskNo: string
  status: string
}

export interface ParseTaskItem {
  taskId: string
  taskNo: string
  batchId: string
  batchNo: string
  templateCode: string
  templateName: string
  status: string
  totalCount: number
  successCount: number
  failedCount: number
  createdAt: string
  updatedAt: string
}

export interface ParseTaskFileItem extends Pick<UploadFileItem, 'fileId' | 'originalFilename' | 'fileType' | 'fileSize' | 'status' | 'createdAt'> {}

export interface VisionParseResult {
  resultId: string
  fileId: string
  originalFilename: string
  modelName?: string
  responseRaw?: string
  responseJson?: string
  status: string
  errorMessage?: string
  createdAt: string
}

export interface ParseTaskDetail extends ParseTaskItem {
  errorMessage?: string
  sourceType: string
  totalFiles: number
  files: ParseTaskFileItem[]
  results: VisionParseResult[]
}

export function getParseTemplates() {
  return request.get<ApiResult<ParseTemplate[]>>('/parse/templates')
}

export function createParseTask(data: CreateParseTaskParams) {
  return request.post<ApiResult<CreateParseTaskResult>>('/parse/tasks', data)
}

export function getParseTasks(params: {
  pageNum?: number
  pageSize?: number
  templateCode?: string
  status?: string
}) {
  return request.get<ApiResult<PageResult<ParseTaskItem>>>('/parse/tasks', { params })
}

export function getParseTaskDetail(taskId: string) {
  return request.get<ApiResult<ParseTaskDetail>>(`/parse/tasks/${taskId}`)
}

export function startParseTask(taskId: string) {
  return request.post<ApiResult<null>>(`/parse/tasks/${taskId}/start`)
}
