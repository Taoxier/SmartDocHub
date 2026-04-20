import { get, post, put, del } from "@/net";

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

// 分类管理
export function getAllCategories(success, failure) {
    get('/api/admin/category/list', success, failure);
}

export function getCategoryList(params, success, failure) {
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

export function getTagList(params, success, failure) {
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
