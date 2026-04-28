import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/index.vue'
import DashboardView from '../views/dashboard/index.vue'
import ImageManageView from '../views/image-manage/index.vue'
import LoginView from '../views/login/index.vue'
import ParseTaskView from '../views/parse-task/index.vue'
import UploadView from '../views/upload/index.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    {
      path: '/home',
      component: Layout,
      redirect: '/dashboard',
      children: [
        { path: '/dashboard', component: DashboardView },
        { path: '/upload', component: UploadView },
        { path: '/images', component: ImageManageView },
        { path: '/parse-task', component: ParseTaskView },
      ],
    },
  ],
})

export default router
