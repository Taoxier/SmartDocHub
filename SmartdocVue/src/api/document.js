import { get, post, put, del, accessHeader } from '@/net'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export function getDocumentList(params, success, failure) {
    const queryString = new URLSearchParams(params).toString()
    get(`/api/document/page?${queryString}`, success, failure)
}

export function getDocumentDetail(id, success, failure) {
    get(`/api/document/${id}`, success, failure)
}

export function updateDocumentMetadata(data, success, failure) {
    put('/api/document', data, success, failure)
}

export function uploadNewVersion(documentId, file, onProgress, success, failure) {
    const formData = new FormData()
    formData.append('file', file)
    
    const headers = {
        'Content-Type': 'multipart/form-data',
        ...accessHeader()
    }
    
    axios.post(`/api/document/versions/${documentId}`, formData, {
        headers: headers,
        onUploadProgress: (progressEvent) => {
            if (onProgress) {
                const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                onProgress(percent)
            }
        }
    }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else {
            failure(data.msg || data.message, data.code)
        }
    }).catch(err => {
        console.error(err)
        ElMessage.error('版本更新失败，请重试')
    })
}

export function getDocumentVersions(documentId, success, failure) {
    get(`/api/document/versions/${documentId}`, success, failure)
}

export function getDocumentVersionDetail(documentId, versionNumber, success, failure) {
    get(`/api/document/version/${documentId}/${versionNumber}`, success, failure)
}

export function deleteDocument(id, success, failure) {
    del(`/api/document/${id}`, success, failure)
}

export function rateDocument(id, ratingValue, success, failure) {
    post(`/api/document/rate/${id}?ratingValue=${ratingValue}`, {}, success, failure)
}

export function uploadDocument(file, onProgress, success, failure) {
    const formData = new FormData()
    formData.append('file', file)
    
    const headers = {
        'Content-Type': 'multipart/form-data',
        ...accessHeader()
    }
    
    axios.post('/api/document/upload', formData, {
        headers: headers,
        onUploadProgress: (progressEvent) => {
            if (onProgress) {
                const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                onProgress(percent)
            }
        }
    }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else {
            failure(data.msg || data.message, data.code)
        }
    }).catch(err => {
        console.error(err)
        ElMessage.error('上传失败，请重试')
    })
}

export function uploadMultipleDocuments(files, onProgress, success, failure) {
    const formData = new FormData()
    for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i])
    }
    
    const headers = {
        'Content-Type': 'multipart/form-data',
        ...accessHeader()
    }
    
    axios.post('/api/document/upload-multiple', formData, {
        headers: headers,
        onUploadProgress: (progressEvent) => {
            if (onProgress) {
                const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                onProgress(percent)
            }
        }
    }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else {
            failure(data.msg || data.message, data.code)
        }
    }).catch(err => {
        console.error(err)
        ElMessage.error('批量上传失败，请重试')
    })
}

export function downloadDocument(id) {
    const url = `/api/document/download/${id}`
    const headers = accessHeader()
    console.log('下载请求头:', headers)
    
    axios({
        url: url,
        method: 'GET',
        headers: headers,
        responseType: 'blob'
    }).then(response => {
        // 从响应头中获取文件名
        const contentDisposition = response.headers['content-disposition']
        let fileName = 'download'
        if (contentDisposition) {
            const match = contentDisposition.match(/filename="([^"]+)"/)
            if (match && match[1]) {
                fileName = decodeURIComponent(match[1])
            }
        }
        
        // 创建Blob对象并下载
        const blob = new Blob([response.data])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(link.href)
    }).catch(error => {
        console.error('下载失败:', error)
        ElMessage.error('下载失败，请重试')
    })
}

export function downloadDocumentVersion(documentId, versionNumber) {
    // 使用axios发送带有Authorization头的GET请求
    const url = `/api/document/version/download/${documentId}/${versionNumber}`
    const headers = accessHeader()
    
    // 创建临时链接进行下载
    axios({
        url: url,
        method: 'GET',
        headers: headers,
        responseType: 'blob' // 重要：设置响应类型为blob
    }).then(response => {
        // 从响应头中获取文件名
        const contentDisposition = response.headers['content-disposition']
        let fileName = 'download'
        if (contentDisposition) {
            const match = contentDisposition.match(/filename="([^"]+)"/)
            if (match && match[1]) {
                fileName = decodeURIComponent(match[1])
            }
        }
        
        // 创建Blob对象并下载
        const blob = new Blob([response.data])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(link.href)
    }).catch(error => {
        console.error('下载失败:', error)
        ElMessage.error('下载失败，请重试')
    })
}

export function formatFileSize(bytes) {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export function formatDate(dateStr) {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

export function getFileIcon(fileType) {
    const iconMap = {
        'pdf': { icon: 'Document', color: '#ff4d4f' },
        'doc': { icon: 'Document', color: '#1890ff' },
        'docx': { icon: 'Document', color: '#1890ff' },
        'xls': { icon: 'Document', color: '#52c41a' },
        'xlsx': { icon: 'Document', color: '#52c41a' },
        'ppt': { icon: 'Document', color: '#fa8c16' },
        'pptx': { icon: 'Document', color: '#fa8c16' },
        'txt': { icon: 'Document', color: '#8c8c8c' }
    }
    return iconMap[fileType?.toLowerCase()] || { icon: 'Document', color: '#8c8c8c' }
}

// 获取文档统计数据
export function getDocumentStats(success, failure) {
    get('/api/document/stats', success, failure)
}

// 收藏文档
export function favoriteDocument(documentId, success, failure) {
    post(`/api/document/favorite/${documentId}`, {}, success, failure)
}

// 取消收藏文档
export function unfavoriteDocument(documentId, success, failure) {
    del(`/api/document/favorite/${documentId}`, success, failure)
}

// 分享文档
export function shareDocument(documentId, success, failure) {
    post(`/api/document/share/${documentId}`, {}, success, failure)
}

// 获取所有分类
export function getAllCategories(success, failure) {
    get('/api/category', success, failure)
}

// 获取热门标签
export function getHotTopics(limit = 30, success, failure) {
    get(`/api/topic/hot?limit=${limit}`, success, failure)
}

// 获取文档的分类
export function getDocumentCategories(documentId, success, failure) {
    get(`/api/category/document/${documentId}`, success, failure)
}

// 设置文档的分类
export function setDocumentCategories(documentId, categoryIds, success, failure) {
    post(`/api/category/document/${documentId}`, categoryIds, success, failure)
}

// 获取我的上传文档
export function getMyUploadedDocuments(params, success, failure) {
    const queryString = new URLSearchParams(params).toString()
    get(`/api/document/my/uploaded?${queryString}`, success, failure)
}

// 获取我的收藏文档
export function getMyFavoriteDocuments(params, success, failure) {
    const queryString = new URLSearchParams(params).toString()
    get(`/api/document/my/favorites?${queryString}`, success, failure)
}

// 获取我的浏览历史
export function getMyHistoryDocuments(params, success, failure) {
    const queryString = new URLSearchParams(params).toString()
    get(`/api/document/my/history?${queryString}`, success, failure)
}

// 批量取消收藏
export function batchRemoveFavorite(documentIds, success, failure) {
    post('/api/document/favorite/batch-remove', documentIds, success, failure)
}

// 批量删除历史记录
export function batchRemoveHistory(documentIds, success, failure) {
    post('/api/document/history/batch-remove', documentIds, success, failure)
}

// 清空历史记录
export function clearHistory(success, failure) {
    post('/api/document/history/clear', {}, success, failure)
}

// 获取文档知识图谱
export function getDocumentKnowledgeGraph(documentId, success, failure) {
    get(`/api/document/kg/${documentId}`, success, failure)
}

// 重新构建文档知识图谱
export function rebuildDocumentKnowledgeGraph(documentId, success, failure) {
    post(`/api/document/kg/${documentId}/rebuild`, {}, success, failure)
}

// 获取推荐文档
export function getRecommendedDocuments(userId, limit, offset = 0, refresh = false, success, failure) {
    get(`/api/recommend/user?userId=${userId}&limit=${limit}&offset=${offset}&refresh=${refresh}`, success, failure)
}

// 根据文档ID列表获取文档详情
export function getDocumentsByIds(documentIds, success, failure) {
    post('/api/recommend/documents', documentIds, success, failure)
}
