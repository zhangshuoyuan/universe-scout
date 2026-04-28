import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginData {
  token: string
  username: string
  roleCode: string
}

export interface ApiResult<T> {
  code: number | string
  message: string
  data: T
}

export function login(data: LoginParams) {
  return request.post<ApiResult<LoginData>>('/auth/login', data)
}
