import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
  scenarios: {
    peak: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 100 },
        { duration: '60s', target: 1000 },
        { duration: '60s', target: 1000 },
        { duration: '30s', target: 0 }
      ]
    }
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.01']
  }
}

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080'

export default function () {
  const response = http.get(`${baseUrl}/api/public/facilities`)
  check(response, { 'HTTP 200': r => r.status === 200, 'dưới 2 giây': r => r.timings.duration < 2000 })
  sleep(1)
}
