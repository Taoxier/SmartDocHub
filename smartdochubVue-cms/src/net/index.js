import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";

const authItemName = "authorize"

const accessHeader = () => {
    const token = takeAccessToken()
    return token ? { 'Authorization': `Bearer ${token}` } : {}
}

const defaultError = (error) => {
    console.error(error)
    const status = error.response?.status
    if (status === 429) {
        ElMessage.error(error.response.data.message)
    } else if (status === 401) {
        ElMessage.error('登录状态已过期，请重新登录')
        deleteAccessToken(true)
    } else {
        ElMessage.error('发生了一些错误，请联系管理员')
    }
}

const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

function takeAccessToken() {
    // 先从 sessionStorage 获取，再从 localStorage 获取
    let str = sessionStorage.getItem(authItemName);
    if (!str) {
        str = localStorage.getItem(authItemName);
    }
    if (!str || str === '{}') {
        console.log('没有找到认证信息或认证信息为空');
        return null;
    }
    try {
        const authObj = JSON.parse(str)
        if (!authObj.token) {
            console.log('认证信息中没有token字段');
            return null;
        }
        if (new Date(authObj.expire) <= new Date()) {
            console.log('认证信息已过期');
            deleteAccessToken()
            ElMessage.warning("登录状态已过期，请重新登录！")
            return null
        }
        return authObj.token
    } catch (e) {
        console.error('解析认证信息失败:', e);
        return null;
    }
}

// 获取用户信息
export function getUserInfo() {
    let str = sessionStorage.getItem(authItemName);
    if (!str) {
        str = localStorage.getItem(authItemName);
    }
    if (!str || str === '{}') {
        return null;
    }
    try {
        const authObj = JSON.parse(str);
        return {
            userId: authObj.userId,
            username: authObj.username,
            roles: authObj.roles || []
        };
    } catch (e) {
        console.error('解析用户信息失败:', e);
        return null;
    }
}

// 检查用户是否有指定角色
export function hasRole(role) {
    const userInfo = getUserInfo();
    if (!userInfo || !userInfo.roles) {
        return false;
    }
    return userInfo.roles.includes(role);
}

// 检查用户是否为管理员
export function isAdmin() {
    const userInfo = getUserInfo();
    if (!userInfo || !userInfo.roles) {
        return false;
    }
    // 检查角色是否包含管理员角色（支持带ROLE_前缀和不带前缀的情况）
    return userInfo.roles.some(role => 
        role === 'ROOT' || role === 'ADMIN' || 
        role === 'ROLE_ROOT' || role === 'ROLE_ADMIN'
    );
}

function storeAccessToken(remember, token, expire, userId, username, roles) {
    const authObj = { 
        token: token, 
        expire: expire,
        userId: userId,
        username: username,
        roles: roles
    }
    const str = JSON.stringify(authObj)
    if (remember) {
        localStorage.setItem(authItemName, str)
    } else {
        sessionStorage.setItem(authItemName, str)
    }
}

function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
    if (redirect) {
        router.push({ name: 'welcome-login' })
    }
}

function internalPost(url, data, headers, success, failure, error = defaultError) {
    axios.post(url, data, { headers: headers }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else if (data.code === 'A00003' || data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.msg || data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalGet(url, headers, success, failure, error = defaultError) {
    axios.get(url, { headers: headers }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else if (data.code === 'A00003' || data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.msg || data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalPut(url, data, headers, success, failure, error = defaultError) {
    axios.put(url, data, { headers: headers }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else if (data.code === 'A00003' || data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.msg || data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalDelete(url, headers, success, failure, error = defaultError) {
    axios.delete(url, { headers: headers }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else if (data.code === 'A00003' || data.code === 401) {
            failure('登录状态已过期，请重新登录！')
            deleteAccessToken(true)
        } else {
            failure(data.msg || data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function login(username, password, remember, success, failure = defaultFailure) {
    // 构建表单数据
    const formData = new URLSearchParams();
    formData.append('username', username);
    formData.append('password', password);
    
    internalPost('/api/v1/auth/login', formData, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        // 确保 accessToken 和 expiresIn 存在
        if (!data.accessToken) {
            ElMessage.error('登录失败：缺少访问令牌');
            return;
        }
        
        if (!data.expiresIn) {
            ElMessage.error('登录失败：缺少过期时间');
            return;
        }
        
        storeAccessToken(remember, data.accessToken, Date.now() + data.expiresIn * 1000, data.userId, data.username, data.roles)
        ElMessage.success(`登录成功，欢迎您，${username} `)
        success(data)
    }, failure)
}

function post(url, data, success, failure = defaultFailure, withAuth = true) {
    internalPost(url, data, withAuth ? accessHeader() : {}, success, failure)
}

function get(url, success, failure = defaultFailure, withAuth = true) {
    internalGet(url, withAuth ? accessHeader() : {}, success, failure)
}

function put(url, data, success, failure = defaultFailure, withAuth = true) {
    internalPut(url, data, withAuth ? accessHeader() : {}, success, failure)
}

function del(url, success, failure = defaultFailure, withAuth = true) {
    internalDelete(url, withAuth ? accessHeader() : {}, success, failure)
}

function logout(success, failure = defaultFailure) {
    get('/api/v1/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, failure)
}

function unauthorized() {
    const token = takeAccessToken();
    return !token
}

export { post, get, put, del, login, logout, unauthorized, accessHeader }