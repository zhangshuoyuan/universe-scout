import { defineStore } from 'pinia'

interface UserState {
  token: string
  username: string
  roleCode: string
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    roleCode: localStorage.getItem('roleCode') || '',
  }),
  actions: {
    setUser(token: string, username: string, roleCode: string) {
      this.token = token
      this.username = username
      this.roleCode = roleCode
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('roleCode', roleCode)
    },
    logout() {
      this.token = ''
      this.username = ''
      this.roleCode = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('roleCode')
    },
  },
})
