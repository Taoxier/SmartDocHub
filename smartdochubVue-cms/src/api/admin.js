import axios from "axios";
import { get, post, put, del, accessHeader } from "@/net";
import { ElMessage } from "element-plus";

// ========== 用户管理 ==========

export function getUserPage(params, success, failure) {
    let url = `/api/v1/users/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.username) url += `&username=${encodeURIComponent(params.username)}`;
    if (params.nickname) url += `&nickname=${encodeURIComponent(params.nickname)}`;
    if (params.status) url += `&status=${params.status}`;
    get(url, success, failure);
}

export function getUserForm(userId, success, failure) {
    get(`/api/v1/users/${userId}/form`, success, failure);
}

export function updateUser(userId, data, success, failure) {
    put(`/api/v1/users/${userId}`, data, success, failure);
}

export function updateUserStatus(userId, status, success, failure) {
    patch(`/api/v1/users/${userId}/status?status=${status}`, success, failure);
}

export function resetUserPassword(userId, password, success, failure) {
    put(`/api/v1/users/${userId}/password/reset?password=${encodeURIComponent(password)}`, {}, success, failure);
}

export function deleteUsers(ids, success, failure) {
    del(`/api/v1/users/${ids}`, success, failure);
}

export function saveUser(data, success, failure) {
    post('/api/v1/users', data, success, failure);
}

export function importUsers(file, success, failure) {
    const formData = new FormData();
    formData.append('file', file);
    postForm('/api/v1/users/import', formData, success, failure);
}

export function exportUsers(params, success, failure) {
    let url = `/api/v1/users/export?`;
    if (params) {
        if (params.username) url += `&username=${encodeURIComponent(params.username)}`;
        if (params.status) url += `&status=${params.status}`;
    }
    window.location.href = url;
}

function patch(url, success, failure) {
    axios.patch(url, {}, { headers: accessHeader() }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else {
            failure(data.msg || data.message, data.code, url)
        }
    }).catch(err => defaultError(err))
}

function postForm(url, formData, success, failure) {
    axios.post(url, formData, { headers: accessHeader() }).then(({ data }) => {
        if (data.code === '00000') {
            success(data.data)
        } else {
            failure(data.msg || data.message, data.code, url)
        }
    }).catch(err => defaultError(err))
}

function defaultError(error) {
    console.error(error)
    ElMessage.error('发生了一些错误，请联系管理员')
}

// 文档管理
export function getAdminDocumentList(params, success, failure) {
    let url = `/api/admin/document/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`;
    if (params.fileType) url += `&fileType=${params.fileType}`;
    if (params.category) url += `&category=${encodeURIComponent(params.category)}`;
    if (params.tags) url += `&tags=${params.tags}`;
    if (params.sortBy) url += `&sortBy=${params.sortBy}`;
    if (params.sortOrder) url += `&sortOrder=${params.sortOrder}`;
    get(url, success, failure);
}

export function getAdminDocumentStats(success, failure) {
    get('/api/admin/document/stats', success, failure);
}

export function getUploadTrend(success, failure) {
    get('/api/admin/document/upload-trend', success, failure);
}

export function getDocumentTypeRatio(success, failure) {
    get('/api/admin/document/type-ratio', success, failure);
}

export function getVisitStats(success, failure) {
    get('/api/v1/logs/visit-stats', success, failure);
}

export function deleteAdminDocument(id, success, failure) {
    del(`/api/admin/document/${id}`, success, failure);
}

export function batchDeleteAdminDocuments(ids, success, failure) {
    post('/api/admin/document/batch-delete', ids, success, failure);
}

export function approveDocument(id, success, failure) {
    put(`/api/admin/document/${id}/approve`, null, success, failure);
}

export function rejectDocument(id, reason, success, failure) {
    put(`/api/admin/document/${id}/reject`, { reason }, success, failure);
}

export function updateAdminDocument(id, data, success, failure) {
    put(`/api/admin/document/${id}`, data, success, failure);
}

// 分类管理
export function getAllCategories(success, failure) {
    get('/api/admin/category/list', success, failure);
}

export function getCategoryList(success, failure) {
    get('/api/admin/category/list', success, failure);
}

export function getCategoryPage(params, success, failure) {
    let url = `/api/admin/category/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    get(url, success, failure);
}

export function getCategoryDetail(id, success, failure) {
    get(`/api/admin/category/${id}`, success, failure);
}

export function createCategory(data, success, failure) {
    post('/api/admin/category', data, success, failure);
}

export function updateCategory(data, success, failure) {
    put('/api/admin/category', data, success, failure);
}

export function deleteCategory(id, success, failure) {
    del(`/api/admin/category/${id}`, success, failure);
}

export function batchDeleteCategories(ids, success, failure) {
    post('/api/admin/category/batch-delete', ids, success, failure);
}

// 标签管理
export function getAllTags(success, failure) {
    get('/api/admin/tag/list', success, failure);
}

export function getHotTags(limit, success, failure) {
    get(`/api/admin/tag/hot?limit=${limit || 30}`, success, failure);
}

export function getTagList(success, failure) {
    get('/api/admin/tag/list', success, failure);
}

export function getTagPage(params, success, failure) {
    let url = `/api/admin/tag/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`;
    get(url, success, failure);
}

export function getTagDetail(id, success, failure) {
    get(`/api/admin/tag/${id}`, success, failure);
}

export function createTag(data, success, failure) {
    post('/api/admin/tag', data, success, failure);
}

export function updateTag(data, success, failure) {
    put('/api/admin/tag', data, success, failure);
}

export function deleteTag(id, success, failure) {
    del(`/api/admin/tag/${id}`, success, failure);
}

export function batchDeleteTags(ids, success, failure) {
    post('/api/admin/tag/batch-delete', ids, success, failure);
}

// 任务管理
export function getTaskList(params, success, failure) {
    let url = `/api/admin/task/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.taskType) url += `&taskType=${params.taskType}`;
    if (params.status) url += `&status=${params.status}`;
    get(url, success, failure);
}

export function getTaskDetail(id, success, failure) {
    get(`/api/admin/task/${id}`, success, failure);
}

export function getTaskStats(success, failure) {
    get('/api/admin/task/stats', success, failure);
}

export function deleteTask(id, success, failure) {
    del(`/api/admin/task/${id}`, success, failure);
}

export function batchDeleteTasks(ids, success, failure) {
    post('/api/admin/task/batch-delete', ids, success, failure);
}

// ========== 评论管理 ==========

export function getAdminCommentPage(params, success, failure) {
    let url = `/api/admin/comment/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.documentId) url += `&documentId=${params.documentId}`;
    if (params.auditStatus) url += `&auditStatus=${params.auditStatus}`;
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`;
    get(url, success, failure);
}

export function getAdminCommentDetail(id, success, failure) {
    get(`/api/admin/comment/${id}`, success, failure);
}

export function auditComment(id, result, reason, success, failure) {
    let url = `/api/admin/comment/${id}/audit?result=${result}`;
    if (reason) url += `&reason=${encodeURIComponent(reason)}`;
    put(url, {}, success, failure);
}

export function batchAuditComments(ids, result, reason, success, failure) {
    post('/api/admin/comment/batch-audit', { ids, result, reason }, success, failure);
}

export function deleteComment(id, success, failure) {
    del(`/api/admin/comment/${id}`, success, failure);
}

export function getCommentList(docId, success, failure) {
    get(`/api/comment/document/${docId}`, success, failure);
}

export function getCommentAuditResult(commentId, success, failure) {
    get(`/api/comment/audit/result/${commentId}`, success, failure);
}

// ========== 文档审核管理 ==========

export function getDocumentAuditStatus(docId, success, failure) {
    get(`/api/document/audit/status/${docId}`, success, failure);
}

export function getDocumentAuditList(docId, success, failure) {
    get(`/api/document/audit/list/${docId}`, success, failure);
}

export function submitDocumentAudit(documentId, auditType, success, failure) {
    post(`/api/document/audit/content?documentId=${documentId}&auditType=${auditType}`, {}, success, failure);
}

export function getDocumentPreviewUrl(id, success, failure) {
    get(`/api/admin/document/${id}/preview-url`, success, failure);
}

// ========== 仪表盘AI分析 ==========

export function getAiAnalysisSummary(success, failure) {
    get('/api/dashboard/ai-analysis/summary', success, failure);
}

export function getAiAnalysisTrend(success, failure) {
    get('/api/dashboard/ai-analysis/trend', success, failure);
}

export function getAiAnalysisAnomaly(success, failure) {
    get('/api/dashboard/ai-analysis/anomaly', success, failure);
}

// ========== 系统配置 ==========

export function getConfigPage(params, success, failure) {
    let url = `/api/v1/config/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 100}`;
    if (params.keywords) url += `&keywords=${encodeURIComponent(params.keywords)}`;
    get(url, success, failure);
}

export function getConfigForm(id, success, failure) {
    get(`/api/v1/config/${id}/form`, success, failure);
}

export function saveConfig(data, success, failure) {
    post('/api/v1/config', data, success, failure);
}

export function updateConfig(id, data, success, failure) {
    put(`/api/v1/config/${id}`, data, success, failure);
}

export function deleteConfig(id, success, failure) {
    del(`/api/v1/config/${id}`, success, failure);
}

export function refreshConfigCache(success, failure) {
    put('/api/v1/config/refresh', null, success, failure);
}

// ========== 角色管理 ==========

export function getRolePage(params, success, failure) {
    let url = `/api/v1/roles/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.keywords) url += `&keywords=${encodeURIComponent(params.keywords)}`;
    if (params.startDate) url += `&startDate=${params.startDate}`;
    if (params.endDate) url += `&endDate=${params.endDate}`;
    get(url, success, failure);
}

export function getRoleOptions(success, failure) {
    get('/api/v1/roles/options', success, failure);
}

export function addRole(data, success, failure) {
    post('/api/v1/roles', data, success, failure);
}

export function getRoleForm(roleId, success, failure) {
    get(`/api/v1/roles/${roleId}/form`, success, failure);
}

export function updateRole(data, success, failure) {
    put(`/api/v1/roles/${data.id}`, data, success, failure);
}

export function deleteRoles(ids, success, failure) {
    del(`/api/v1/roles/${ids}`, success, failure);
}

export function updateRoleStatus(roleId, status, success, failure) {
    put(`/api/v1/roles/${roleId}/status?status=${status}`, null, success, failure);
}

export function getRoleMenuIds(roleId, success, failure) {
    get(`/api/v1/roles/${roleId}/menuIds`, success, failure);
}

export function assignMenusToRole(roleId, menuIds, success, failure) {
    put(`/api/v1/roles/${roleId}/menus`, menuIds, success, failure);
}

// ========== 菜单管理 ==========

export function getMenuList(success, failure) {
    get('/api/v1/menus', success, failure);
}

// ========== 日志管理 ==========

export function getLogPage(params, success, failure) {
    let url = `/api/v1/logs/page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    if (params.keywords) url += `&keywords=${encodeURIComponent(params.keywords)}`;
    if (params.createTime && params.createTime.length === 2) {
        url += `&createTime=${encodeURIComponent(params.createTime[0])}&createTime=${encodeURIComponent(params.createTime[1])}`;
    }
    get(url, success, failure);
}

// ========== 个人中心 ==========

export function getUserProfile(success, failure) {
    get('/api/v1/users/profile', success, failure);
}

export function updateUserProfile(data, success, failure) {
    put('/api/v1/users/profile', data, success, failure);
}

// ========== 通知公告 ==========

export function getMyNotices(params, success, failure) {
    let url = `/api/v1/notices/my-page?pageNum=${params.pageNum || 1}&pageSize=${params.pageSize || 10}`;
    get(url, success, failure);
}

export function getNoticeDetail(id, success, failure) {
    get(`/api/v1/notices/${id}/detail`, success, failure);
}

export function readAllNotices(success, failure) {
    put('/api/v1/notices/read-all', null, success, failure);
}
