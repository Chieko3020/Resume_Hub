import { createRouter, createWebHistory } from 'vue-router'

const Home = () => import('../views/Home.vue')
const Login = () => import('../views/Login.vue')
const UserCenter = () => import('../views/UserCenter.vue')
const AdminCenter = () => import('../views/AdminCenter.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/login', name: 'login', component: Login },
    { path: '/user', name: 'user', component: UserCenter },
    { path: '/admin', name: 'admin', component: AdminCenter },
  ],
})


export default router


