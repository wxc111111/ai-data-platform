<template>
  <main class="login-page">
    <section class="login-brand">
      <div class="brand-mark">
        <div class="brand-cube">A</div>
        <div>
          <h1>AI Data Platform</h1>
          <p>统一认证与数据服务入口</p>
        </div>
      </div>

      <div class="brand-panel">
        <span class="panel-label">DATA SERVICE</span>
        <h2>连接业务系统，发布可调用的 Skill 能力。</h2>
        <div class="panel-grid">
          <div>业务系统</div>
          <div>Skill</div>
          <div>RAG</div>
          <div>agent</div>
        </div>
      </div>
    </section>

    <section class="login-card" aria-label="登录表单">
      <div class="login-title">
        <h2>登录</h2>
        <p>使用管理员账号进入数据服务中台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model.trim="form.username" :prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            placeholder="请输入密码"
            show-password
            type="password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button class="login-button" type="primary" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin'
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }

  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    await router.push('/home')
  } catch (error) {
    const message = error instanceof Error ? error.message : '登录失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>
