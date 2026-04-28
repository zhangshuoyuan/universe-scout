import request from './request'
import type { ApiResult } from './upload'

export interface PromptTemplate {
  id: string
  templateCode: string
  templateName: string
  prompt: string
  jsonSchema?: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface UpdatePromptTemplateParams {
  templateName: string
  prompt: string
  jsonSchema?: string
  status: number
}

export function getPromptTemplates() {
  return request.get<ApiResult<PromptTemplate[]>>('/parse/prompt-templates')
}

export function getPromptTemplate(id: string) {
  return request.get<ApiResult<PromptTemplate>>(`/parse/prompt-templates/${id}`)
}

export function updatePromptTemplate(id: string, data: UpdatePromptTemplateParams) {
  return request.put<ApiResult<PromptTemplate>>(`/parse/prompt-templates/${id}`, data)
}
