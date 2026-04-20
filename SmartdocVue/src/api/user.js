import * as request from '@/net'

const { get, post, put, del } = request

// 获取个人中心用户信息
export function getUserProfile(success, failure) {
    get('/api/v1/users/profile', success, failure)
}

// 修改个人中心用户信息
export function updateUserProfile(data, success, failure) {
    put('/api/v1/users/profile', data, success, failure)
}

// 修改密码
export function changePassword(data, success, failure) {
    put('/api/v1/users/password', data, success, failure)
}

// 发送邮箱验证码
export function sendEmailCode(email, success, failure) {
    post('/api/v1/users/email/code', { email }, success, failure)
}

// 绑定或更换邮箱
export function bindOrChangeEmail(data, success, failure) {
    put('/api/v1/users/email', data, success, failure)
}

// 发送短信验证码
export function sendMobileCode(mobile, success, failure) {
    post('/api/v1/users/mobile/code', { mobile }, success, failure)
}

// 绑定或更换手机号
export function bindOrChangeMobile(data, success, failure) {
    put('/api/v1/users/mobile', data, success, failure)
}
