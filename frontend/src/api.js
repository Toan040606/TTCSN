const BASE = import.meta.env.VITE_API_URL || ''

export async function api(path, options = {}) {
  const token = localStorage.getItem('bm_token') || sessionStorage.getItem('bm_token')
  const response = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  })
  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error(data.message || 'Không thể kết nối đến hệ thống')
  }
  if (response.status === 204) return null
  return response.json()
}

export const auth = {
  get: () => JSON.parse(localStorage.getItem('bm_user') || sessionStorage.getItem('bm_user') || 'null'),
  set: (data, remember = true) => {
    const storage = remember ? localStorage : sessionStorage
    localStorage.removeItem('bm_token'); localStorage.removeItem('bm_user')
    sessionStorage.removeItem('bm_token'); sessionStorage.removeItem('bm_user')
    storage.setItem('bm_token', data.token)
    storage.setItem('bm_user', JSON.stringify(data))
  },
  clear: () => {
    localStorage.removeItem('bm_token')
    localStorage.removeItem('bm_user')
    sessionStorage.removeItem('bm_token')
    sessionStorage.removeItem('bm_user')
  }
}
