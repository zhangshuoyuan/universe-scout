import request from './request'

export interface UploadFileItem {
  fileId: string
  originalFilename: string
  fileName: string
  filePath: string
  fileType: string
  fileSize: number
  imageWidth?: number
  imageHeight?: number
  status: string
  errorMessage?: string
  createdAt: string
}

export interface ImageFileItem extends UploadFileItem {
  batchId: string
  batchNo?: string
  sourceType?: string
  previewUrl: string
}

export interface UploadResult {
  batchId: string
  batchNo: string
  fileCount: number
  files: UploadFileItem[]
}

export interface UploadBatchItem {
  batchId: string
  batchNo: string
  batchName: string
  sourceType: string
  status: string
  totalFiles: number
  successCount: number
  failedCount: number
  createdAt: string
}

export interface UploadBatchDetail extends UploadBatchItem {
  remark?: string
  updatedAt: string
  files: UploadFileItem[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export interface ApiResult<T> {
  code: number | string
  message: string
  data: T
}

export function uploadImages(files: File[]) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return request.post<ApiResult<UploadResult>>('/files/upload/images', formData)
}

export function uploadZip(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResult<UploadResult>>('/files/upload/zip', formData)
}

export function getUploadBatches(params: {
  pageNum?: number
  pageSize?: number
  sourceType?: string
  status?: string
}) {
  return request.get<ApiResult<PageResult<UploadBatchItem>>>('/files/batches', { params })
}

export function getUploadBatchDetail(batchId: string) {
  return request.get<ApiResult<UploadBatchDetail>>(`/files/batches/${batchId}`)
}

export function getImageFiles(params: {
  pageNum?: number
  pageSize?: number
  batchId?: string
  fileType?: string
  keyword?: string
}) {
  return request.get<ApiResult<PageResult<ImageFileItem>>>('/files/images', { params })
}

export function deleteImageFile(fileId: string) {
  return request.delete<ApiResult<null>>(`/files/images/${fileId}`)
}
