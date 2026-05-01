<template>
  <div class="document-detail" v-loading="loading">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="goBack">
        <template #default>
          <span class="button-text">返回列表</span>
        </template>
      </el-button>
      <div class="header-actions">
        <el-button type="primary" @click="handleDownload">下载</el-button>
        <el-button class="btn-rate" @click="showRateDialog = true">评分</el-button>
        <el-button :class="['btn-favorite', isFavorite ? 'btn-favorited' : '']" @click="handleFavorite">{{ isFavorite ?
          '取消收藏'
          : '收藏' }}</el-button>
        <el-button class="btn-share" @click="handleShare">分享</el-button>
        <!-- <el-button type="success" @click="showTranslateDialog = true">翻译</el-button> -->
        <el-button class="btn-convert" @click="openConvertDialog">格式转换</el-button>
        <el-button v-if="isOwner" @click="showVersionDialog = true">版本管理</el-button>
        <el-button v-if="isOwner" @click="showEditDialog = true">编辑</el-button>
        <el-button v-if="isOwner" class="btn-kg" @click="rebuildKnowledgeGraph">重新生成知识图谱</el-button>
        <el-button v-if="isOwner" type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <div class="detail-content" v-if="document">
      <div class="main-info">
        <div class="content-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="完整内容" name="full">
              <div class="full-content">
                <div class="content-actions">
                  <el-button type="primary" size="small" circle @click="toggleFullscreen('full')">
                    <el-icon>
                      <FullScreen />
                    </el-icon>
                  </el-button>
                </div>
                <div ref="fullContentRef" class="full-content-inner">
                  <v-md-preview :text="document.parsedContent || '暂无内容'"></v-md-preview>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="在线预览" name="preview">
              <div class="preview-container">
                <div class="content-actions">
                  <el-button type="primary" size="small" circle @click="toggleFullscreen('preview')">
                    <el-icon>
                      <FullScreen />
                    </el-icon>
                  </el-button>
                </div>
                <div v-if="!document.storagePath" class="preview-empty">
                  <el-icon>
                    <DocumentRemove />
                  </el-icon>
                  <span>暂无文件预览</span>
                </div>
                <div v-else-if="!document.previewUrl" class="preview-unsupported">
                  <el-icon>
                    <Warning />
                  </el-icon>
                  <span>该文件格式不支持在线预览，请下载后查看</span>
                  <el-button type="primary" @click="handleDownload">下载文档</el-button>
                </div>
                <div v-else ref="previewContentRef" class="preview-content">
                  <iframe :src="document.previewUrl" class="preview-iframe" frameborder="0" allowfullscreen></iframe>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="分块内容" name="chunks" v-if="document.chunks?.length">
              <div class="chunks-with-nav">
                <div class="chunk-nav-sidebar" v-if="chunkNavList.length">
                  <div class="chunk-nav-title">内容导航</div>
                  <div v-for="(chunk, idx) in chunkNavList" :key="chunk.id" class="chunk-nav-item"
                    :class="{ 'chunk-nav-active': activeChunkIndex === idx }" @click="scrollToChunk(idx)">
                    <div class="chunk-nav-label">{{ chunk.title || '分块 ' + (idx + 1) }}</div>
                    <div class="chunk-nav-preview">{{ chunk.preview }}...</div>
                  </div>
                </div>
                <div class="content-list chunks-content">
                  <div v-for="(chunk, idx) in document.chunks" :key="chunk.id" class="content-chunk"
                    :id="'chunk-' + idx" :ref="el => chunkRefs[idx] = el">
                    <div class="chunk-header">
                      <el-tag size="small" :type="getChunkType(chunk.contentType)">
                        {{ chunk.contentType }}
                      </el-tag>
                      <span class="chunk-index">第 {{ chunk.chunkIndex + 1 }} 块</span>
                    </div>
                    <div class="chunk-content">{{ chunk.contentText }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="文字" name="text" v-if="textChunks.length">
              <div class="content-list">
                <div v-for="chunk in textChunks" :key="chunk.id" class="content-chunk">
                  <div class="chunk-content">{{ chunk.contentText }}</div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="表格" name="table" v-if="tableChunks.length">
              <div class="content-list">
                <div v-for="chunk in tableChunks" :key="chunk.id" class="content-chunk">
                  <div class="chunk-content">{{ chunk.contentText }}</div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="知识图谱" name="kg">
              <div class="kg-container">
                <div class="content-actions">
                  <el-button type="primary" size="small" circle @click="toggleFullscreen('kg')">
                    <el-icon>
                      <FullScreen />
                    </el-icon>
                  </el-button>
                </div>
                <div v-if="loadingKg" class="kg-loading">
                  <el-icon class="is-loading">
                    <Loading />
                  </el-icon>
                  <span>加载知识图谱中...</span>
                </div>
                <div v-else-if="kgData.nodes && kgData.nodes.length" class="kg-content">
                  <div ref="kgContainer" class="kg-visualization"></div>
                </div>
                <div v-else class="kg-empty">
                  <el-icon>
                    <DocumentRemove />
                  </el-icon>
                  <span>暂无知识图谱数据</span>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="评论区" name="comments">
              <div class="comments-section">
                <div class="comment-input">
                  <div v-if="replyingToComment" class="reply-indicator">
                    <span>回复 {{ replyingToComment.userName || '用户' + replyingToComment.userId }}</span>
                    <el-button type="text" @click="cancelReply">取消</el-button>
                  </div>
                  <el-input v-model="newComment" type="textarea" :rows="3"
                    :placeholder="replyingToComment ? '写下你的回复...' : '发表评论...'" maxlength="500" show-word-limit />
                  <el-button type="primary" style="margin-top: 8px; background-color: #fa8c16; border-color: #fa8c16;"
                    @click="submitComment" :loading="submittingComment">
                    {{ replyingToComment ? '发表回复' : '发表评论' }}
                  </el-button>
                </div>
                <div class="comments-list" v-if="comments.length">
                  <div v-for="comment in comments" :key="comment.id" class="comment-item">
                    <div class="comment-header">
                      <span class="comment-user">{{ comment.userName || '用户' + comment.userId }}</span>
                      <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                      <el-tag v-if="comment.auditStatus" size="small" :type="getAuditStatusType(comment.auditStatus)">
                        {{ getAuditStatusText(comment.auditStatus) }}
                      </el-tag>
                    </div>
                    <div class="comment-body">{{ comment.content }}</div>
                    <div v-if="comment.auditStatus === 'REJECT'" class="comment-reject-reason">
                      <el-icon color="#f56c6c">
                        <Warning />
                      </el-icon>
                      <span>审核原因：{{ comment.auditReason || '内容违规' }}</span>
                    </div>
                    <div class="comment-actions">
                      <el-button size="small" text @click="replyToComment(comment)">
                        <el-icon>
                          <ChatDotRound />
                        </el-icon> 回复
                      </el-button>
                    </div>
                    <div v-if="comment.replies && comment.replies.length" class="comment-replies">
                      <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                        <div class="reply-content">
                          <span class="reply-user">{{ reply.userName || '用户' + reply.userId }}</span>
                          <span class="reply-arrow">回复</span>
                          <span class="reply-to">{{ reply.replyToUserName || '用户' + reply.replyToUserId }}</span>
                        </div>
                        <div class="reply-body">{{ reply.content }}</div>
                        <div class="reply-meta">
                          <span class="comment-time">{{ formatDate(reply.createTime) }}</span>
                          <el-button size="small" text @click="replyToComment(reply)">
                            <el-icon>
                              <ChatDotRound />
                            </el-icon> 回复
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无评论" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="版本历史" name="versions">
              <div class="version-history">
                <div class="version-actions-top">
                  <el-button type="primary" @click="showUploadVersionDialog = true"
                    style="background-color: #fa8c16; border-color: #fa8c16;" v-if="isOwner">
                    上传新版本
                  </el-button>
                  <el-button @click="openDefaultDiff">版本对比</el-button>
                </div>
                <el-timeline>
                  <el-timeline-item v-for="ver in versions" :key="ver.id" :timestamp="formatDate(ver.createTime)"
                    placement="top" :type="ver.versionNumber === currentVersion ? 'primary' : 'info'">
                    <el-card shadow="hover" class="version-card"
                      :class="{ 'version-current': ver.versionNumber === currentVersion }">
                      <div class="version-card-header">
                        <span class="version-number">v{{ ver.versionNumber }}</span>
                        <el-tag v-if="ver.versionNumber === currentVersion" type="success" size="small">当前版本</el-tag>
                        <el-tag v-else-if="ver.versionNumber === viewingVersion" type="info" size="small">当前查看</el-tag>
                      </div>
                      <div class="version-card-body">
                        <span>{{ ver.originalFilename }}</span>
                        <span class="version-size">{{ formatFileSize(ver.fileSize) }}</span>
                      </div>
                      <div class="version-card-actions">
                        <el-button size="small" type="primary" plain @click="switchVersion(ver.versionNumber)">
                          查看
                        </el-button>
                        <el-button size="small" plain @click="handleDownloadVersion(ver.versionNumber)">
                          下载
                        </el-button>
                        <el-button size="small" type="warning" plain @click="openDiffView(ver.versionNumber)"
                          v-if="ver.versionNumber !== currentVersion">
                          对比
                        </el-button>
                        <el-button size="small" type="danger" plain @click="handleRollback(ver.versionNumber)"
                          v-if="isOwner && ver.versionNumber !== currentVersion">
                          回滚
                        </el-button>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-tab-pane>
            <el-tab-pane label="相似文档" name="similar">
              <div class="similar-docs-section" v-if="similarDocuments.length">
                <div class="similar-docs-header">
                  <span class="similar-count">共找到 {{ similarDocuments.length }} 篇相似文档</span>
                </div>
                <div class="similar-docs-list">
                  <div v-for="doc in similarDocuments" :key="doc.targetDocumentId" class="similar-doc-card"
                    @click="goToSimilarDocument(doc.targetDocumentId)">
                    <div class="similar-doc-header">
                      <span class="similar-doc-title">{{ doc.targetTitle }}</span>
                      <el-tag type="danger" size="small" effect="plain">
                        {{ (doc.similarityScore * 100).toFixed(1) }}%
                      </el-tag>
                    </div>
                    <div class="similar-doc-meta">
                      <span class="similar-doc-category">{{ doc.category || '未分类' }}</span>
                      <span class="similar-doc-uploader">{{ doc.uploaderName }}</span>
                      <span class="similar-doc-date">{{ formatDate(doc.createTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else-if="loadingSimilar" class="similar-docs-loading">
                <el-icon class="is-loading">
                  <Loading />
                </el-icon>
                <span>加载中...</span>
              </div>
              <div v-else class="similar-docs-empty">
                <el-icon>
                  <DocumentRemove />
                </el-icon>
                <span>暂无相似文档</span>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <div class="side-info">
        <div class="info-section">
          <div class="title-section">
            <div class="file-icon" :style="{ backgroundColor: getFileIcon(document.fileType).color + '20' }">
              <el-icon :size="24" :color="getFileIcon(document.fileType).color">
                <Document />
              </el-icon>
            </div>
            <div class="title-info">
              <h1 class="doc-title">{{ document.title }}</h1>
            </div>
          </div>
          <div class="meta-row">
            <div class="meta-tags">
              <el-tag>{{ document.fileType?.toUpperCase() }}</el-tag>
              <el-tag type="info">{{ formatFileSize(document.fileSize) }}</el-tag>
              <el-tag :type="document.isPublic ? 'success' : 'warning'">
                {{ document.isPublic ? '公开' : '私有' }}
              </el-tag>
              <el-tag v-if="document.category" class="category-tag">{{ document.category }}</el-tag>
            </div>
          </div>

          <div class="stats-row">
            <div class="stat-item">
              <el-tooltip content="浏览" placement="top">
                <el-icon :size="16" color="#ff9800">
                  <View />
                </el-icon>
              </el-tooltip>
              <span class="stat-value">{{ document.viewCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-tooltip content="下载" placement="top">
                <el-icon :size="16" color="#52c41a">
                  <Download />
                </el-icon>
              </el-tooltip>
              <span class="stat-value">{{ document.downloadCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-tooltip content="评分" placement="top">
                <el-icon :size="16" color="#faad14">
                  <Star />
                </el-icon>
              </el-tooltip>
              <span class="stat-value">{{ document.avgRating?.toFixed(1) || '-' }}</span>
            </div>
            <div class="stat-item">
              <el-tooltip content="收藏" placement="top">
                <el-icon :size="16" color="#f56c6c">
                  <StarFilled />
                </el-icon>
              </el-tooltip>
              <span class="stat-value">{{ document.favoriteCount || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="document.topics?.length">
          <h4 class="side-title">关键词标签</h4>
          <div class="topic-tags">
            <el-tag v-for="topic in document.topics" :key="topic.id" size="small" class="topic-tag">
              {{ topic.topicValue }}
            </el-tag>
          </div>
        </div>

        <div class="info-section">
          <h4 class="side-title">文档信息</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">上传时间</span>
              <span class="info-value">{{ formatDate(document.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">上传者</span>
              <span class="info-value">{{ document.uploadUserName || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">原始文件名</span>
              <span class="info-value" :title="document.originalFilename">
                {{ document.originalFilename }}
              </span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h4 class="side-title">文档描述</h4>
          <p class="description-text">{{ document.description || '暂无描述' }}</p>
        </div>

        <div class="info-section" v-if="document.overallSimilarity !== undefined">
          <h4 class="side-title">相似度检测</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">总体相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.overallSimilarity)">
                {{ (document.overallSimilarity * 100).toFixed(2) }}%
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">文本相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.textSimilarity)">
                {{ (document.textSimilarity * 100).toFixed(2) }}%
              </span>
            </div>
            <div class="info-item" v-if="document.tableSimilarity !== undefined">
              <span class="info-label">表格相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.tableSimilarity)">
                {{ (document.tableSimilarity * 100).toFixed(2) }}%
              </span>
            </div>
            <div class="info-item" v-if="document.formulaSimilarity !== undefined">
              <span class="info-label">公式相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.formulaSimilarity)">
                {{ (document.formulaSimilarity * 100).toFixed(2) }}%
              </span>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="document.aiProbability !== undefined">
          <h4 class="side-title">AI生成检测</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">AI生成概率</span>
              <div class="info-value" :class="getAIProbabilityClass(document.aiProbability)">
                {{ formatFullDecimal(document.aiProbability) }}
              </div>
            </div>
            <div class="info-item" v-if="document.detectedAiModel">
              <span class="info-label">使用模型</span>
              <div class="info-value model-name">
                {{ document.detectedAiModel }}
              </div>
            </div>
            <!-- <div class="info-item" v-if="!viewingVersion">
              <span class="info-label">详细分析</span>
              <div class="info-value">
                <el-button size="small" @click="loadAiAnalysisResult">查看详细分析</el-button>
              </div>
            </div> -->
          </div>
        </div>

        <!-- AI分析详情（仅在查看最新版本且有详细分析时显示） -->
        <div v-if="aiAnalysisResult && !viewingVersion" class="info-section">
          <h4 class="side-title">AI分析详情</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">可信度</span>
              <div class="info-value" :class="getConfidenceClass(aiAnalysisResult.confidence)">
                {{ formatFullDecimal(aiAnalysisResult.confidence) }}
              </div>
            </div>
            <div v-if="aiAnalysisResult?.keyFeatures" class="info-item">
              <span class="info-label">分析指标</span>
              <div class="key-features">
                <div v-for="(feature, index) in JSON.parse(aiAnalysisResult.keyFeatures)" :key="index"
                  class="feature-item">
                  <span class="feature-text">{{ feature.text }}</span>
                  <div class="feature-score" :class="getConfidenceClass(feature.score)">
                    {{ formatFullDecimal(feature.score) }}
                  </div>
                </div>
              </div>
            </div>
            <div class="info-item">
              <span class="info-label">分析结果</span>
              <div class="info-value">
                <el-tag :type="getAiResultType(aiAnalysisResult.result)">
                  {{ getAiResultText(aiAnalysisResult.result) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="auditStatus">
          <h4 class="side-title">内容安全审核</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">审核状态</span>
              <el-tag :type="getAuditStatusType(auditStatus.status)" size="small">
                {{ getDocAuditStatusText(auditStatus.status) }}
              </el-tag>
            </div>
            <div class="info-item" v-if="auditStatus.auditType">
              <span class="info-label">审核类型</span>
              <span class="info-value">{{ auditStatus.auditType }}</span>
            </div>
            <div class="info-item" v-if="auditStatus.auditor">
              <span class="info-label">审核方</span>
              <span class="info-value">{{ auditStatus.auditor }}</span>
            </div>
            <div class="info-item" v-if="auditStatus.auditTime">
              <span class="info-label">审核时间</span>
              <span class="info-value">{{ formatDate(auditStatus.auditTime) }}</span>
            </div>
            <div class="info-item" v-if="auditStatus.remark">
              <span class="info-label">备注</span>
              <span class="info-value">{{ auditStatus.remark }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 所有对话框组件 -->
    <el-dialog v-model="showRateDialog" title="文档评分" width="450px">
      <div class="rate-content">
        <p class="rate-title">{{ document?.title }}</p>
        <div class="rate-item">
          <span class="rate-label">质量评分</span>
          <div class="rate-star-container">
            <el-rate v-model="qualityRating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text
              :texts="['极差', '失望', '一般', '满意', '惊喜']" size="large" />
          </div>
        </div>
        <div class="rate-item">
          <span class="rate-label">可读性评分</span>
          <div class="rate-star-container">
            <el-rate v-model="readabilityRating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text
              :texts="['极差', '失望', '一般', '满意', '惊喜']" size="large" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showRateDialog = false">取消</el-button>
        <el-button type="warning" @click="submitRating">确认评分</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑文档" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入文档描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" placeholder="请输入文档分类" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editForm.tags" multiple filterable allow-create default-first-option
            placeholder="请输入或选择标签" style="width: 100%">
            <el-option v-for="tag in editForm.existingTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="公开状态">
          <el-switch v-model="editForm.isPublic" active-text="公开" inactive-text="私有"
            style="--el-switch-on-color: #fa8c16" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="warning" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showVersionDialog" title="版本管理" width="1000px">
      <div class="version-management">
        <div class="version-actions" style="margin-bottom: 20px;">
          <el-button type="primary" @click="showUploadVersionDialog = true"
            style="background-color: #fa8c16; border-color: #fa8c16;">
            上传新版本
          </el-button>
        </div>
        <el-table :data="versions" style="width: 100%">
          <el-table-column prop="versionNumber" label="版本号" width="100" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="originalFilename" label="文件名" />
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="上传时间" width="200">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" type="warning" plain @click="switchVersion(scope.row.versionNumber)">
                查看
              </el-button>
              <el-button size="small" type="warning" plain @click="handleDownloadVersion(scope.row.versionNumber)">
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="showUploadVersionDialog" title="上传新版本" width="500px">
      <div class="upload-version">
        <el-upload class="upload-demo" :auto-upload="false" :on-change="handleFileChange" :show-file-list="false"
          accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt">
          <el-button type="primary" style="width: 100%; background-color: #fa8c16; border-color: #fa8c16;">
            选择文件
          </el-button>
        </el-upload>
        <div v-if="uploadFile" class="file-info" style="margin-top: 16px;">
          <p>{{ uploadFile.name }}</p>
          <p>{{ formatFileSize(uploadFile.size) }}</p>
        </div>
        <el-progress :percentage="uploadProgress" :status="uploadProgress === 100 ? 'success' : ''"
          style="margin-top: 16px;" />
      </div>
      <template #footer>
        <el-button @click="showUploadVersionDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadVersion"
          style="background-color: #fa8c16; border-color: #fa8c16;">
          上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTranslateDialog" title="文档翻译" width="500px">
      <el-form :model="translateForm" label-width="80px">
        <el-form-item label="源语言">
          <el-select v-model="translateForm.sourceLanguage" placeholder="请选择源语言">
            <el-option label="中文" value="zh" />
            <el-option label="英文" value="en" />
            <el-option label="日文" value="ja" />
            <el-option label="韩文" value="ko" />
            <el-option label="法文" value="fr" />
            <el-option label="德文" value="de" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标语言">
          <el-select v-model="translateForm.targetLanguage" placeholder="请选择目标语言">
            <el-option label="中文" value="zh" />
            <el-option label="英文" value="en" />
            <el-option label="日文" value="ja" />
            <el-option label="韩文" value="ko" />
            <el-option label="法文" value="fr" />
            <el-option label="德文" value="de" />
          </el-select>
        </el-form-item>
      </el-form>
      <div v-if="translateTask" class="task-status">
        <el-tag :type="getTaskStatusType(translateTask.status)">
          {{ getTaskStatusText(translateTask.status) }}
        </el-tag>
        <el-progress v-if="translateTask.status === 'PROCESSING'" :percentage="translateTask.progress"
          style="margin-top: 8px;" />
        <div v-if="translateTask.status === 'COMPLETED'" style="margin-top: 8px;">
          <el-button type="success" @click="downloadTranslationResult">下载翻译结果</el-button>
        </div>
        <div v-if="translateTask.errorMessage" class="task-error">{{ translateTask.errorMessage }}</div>
      </div>
      <template #footer>
        <el-button @click="showTranslateDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleTranslate" :loading="translating"
          style="background-color: #fa8c16; border-color: #fa8c16;">
          开始翻译
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showConvertDialog" title="格式转换" width="400px">
      <el-form :model="convertForm" label-width="80px">
        <el-form-item label="源格式">
          <el-select v-model="convertForm.sourceFormat" disabled>
            <el-option :label="getFileFormatLabel(convertForm.sourceFormat)" :value="convertForm.sourceFormat" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标格式">
          <el-select v-model="convertForm.targetFormat" placeholder="请选择目标格式">
            <el-option v-for="format in availableTargetFormats" :key="format.value" :label="format.label"
              :value="format.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <div v-if="convertTask" class="task-status">
        <el-tag :type="getTaskStatusType(convertTask.status)">
          {{ getTaskStatusText(convertTask.status) }}
        </el-tag>
        <el-progress v-if="convertTask.status === 'PROCESSING'" :percentage="convertTask.progress"
          style="margin-top: 8px;" />
        <div v-if="convertTask.status === 'COMPLETED'" style="margin-top: 8px;">
          <el-button type="success" @click="downloadConversionResult">查看转换结果</el-button>
        </div>
        <div v-if="convertTask.errorMessage" class="task-error">{{ convertTask.errorMessage }}</div>
      </div>
      <template #footer>
        <el-button @click="showConvertDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleConvert" :loading="converting" :disabled="!convertForm.targetFormat"
          style="background-color: #fa8c16; border-color: #fa8c16;">
          开始转换
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDiffDialog" title="版本差异对比" width="90%" top="5vh" style="max-height: 90vh; height: 90vh;">
      <div class="diff-container">
        <div class="diff-selector">
          <el-select v-model="diffVersion1" placeholder="选择版本1" style="width: 150px;">
            <el-option v-for="ver in versions" :key="ver.id" :label="'v' + ver.versionNumber"
              :value="ver.versionNumber" />
          </el-select>
          <span style="margin: 0 12px; font-weight: bold;">VS</span>
          <el-select v-model="diffVersion2" placeholder="选择版本2" style="width: 150px;">
            <el-option v-for="ver in versions" :key="ver.id" :label="'v' + ver.versionNumber"
              :value="ver.versionNumber" />
          </el-select>
          <el-button type="primary" @click="loadVersionDiff" style="margin-left: 12px;">对比</el-button>
        </div>
        <div v-if="loadingDiff" style="text-align: center; padding: 40px;">
          <el-icon class="is-loading" :size="32">
            <Loading />
          </el-icon>
          <p>加载差异对比中...</p>
        </div>
        <template v-else-if="diffData">
          <div class="diff-stats"
            style="margin-bottom: 12px; padding: 8px 12px; background: #f5f7fa; border-radius: 4px;">
            <span style="margin-right: 16px;"><span style="color: #67c23a;">●</span> 新增 {{ diffData.stats?.addedCount ||
              0 }} 行</span>
            <span style="margin-right: 16px;"><span style="color: #f56c6c;">●</span> 删除 {{ diffData.stats?.deletedCount
              || 0 }} 行</span>
            <span style="margin-right: 16px;"><span style="color: #909399;">●</span> 未变 {{
              diffData.stats?.unchangedCount || 0 }} 行</span>
          </div>
          <div class="diff-content">
            <div class="diff-panel">
              <div class="diff-panel-header">版本 v{{ diffVersion1 }} <span
                  style="font-weight: normal; color: #909399;">(旧版本)</span></div>
              <div class="diff-panel-body">
                <div v-for="(line, index) in diffData.lines1" :key="index" class="diff-line"
                  :class="getDiffLineClass(line.diffType)">
                  <span class="line-number">{{ line.lineNumber }}</span>
                  <span class="line-content">{{ line.content }}</span>
                </div>
              </div>
            </div>
            <div class="diff-panel">
              <div class="diff-panel-header">版本 v{{ diffVersion2 }} <span
                  style="font-weight: normal; color: #909399;">(新版本)</span></div>
              <div class="diff-panel-body">
                <div v-for="(line, index) in diffData.lines2" :key="index" class="diff-line"
                  :class="getDiffLineClass(line.diffType)">
                  <span class="line-number">{{ line.lineNumber }}</span>
                  <span class="line-content">{{ line.content }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="请选择两个版本进行对比" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Download, Star, Edit, Delete, Document, View, StarFilled, Share, Loading, DocumentRemove, Warning, FullScreen, SwitchButton, Promotion, Switch, ChatDotRound
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDocumentKnowledgeGraph, rebuildDocumentKnowledgeGraph } from '@/api/document'
import {
  getDocumentDetail, downloadDocument, rateDocument, updateDocumentMetadata, deleteDocument,
  favoriteDocument, unfavoriteDocument, shareDocument, getAllCategories, getDocumentCategories, setDocumentCategories,
  uploadNewVersion, getDocumentVersions, getDocumentVersionDetail, downloadDocumentVersion, getAiAnalysisResult,
  getComments, addComment, getDocumentAuditStatus,
  submitTranslationTask, getTranslationTaskStatus,
  submitConversionTask, getConversionTaskStatus,
  getDocumentChunks, getVersionDiff, rollbackVersion,
  formatFileSize, formatDate, getFileIcon, getSimilarDocuments
} from '@/api/document'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const document = ref(null)
const showRateDialog = ref(false)
const showEditDialog = ref(false)
const showVersionDialog = ref(false)
const showUploadVersionDialog = ref(false)
const qualityRating = ref(5)
const readabilityRating = ref(5)
const activeTab = ref('full')
const isFavorite = ref(false)
const categories = ref([])
const selectedCategories = ref([])
const versions = ref([])
const currentVersion = ref(1)
const viewingVersion = ref(null) // 当前查看的历史版本
const uploadProgress = ref(0)
const uploadFile = ref(null)
const loadingKg = ref(false)
const kgData = ref({ nodes: [], links: [] })
const kgChart = ref(null)
const kgContainer = ref(null)
const fullContentRef = ref(null)
const kgPollingTimer = ref(null)
const previewContentRef = ref(null)
const isFullscreen = ref(false)
const currentFullscreenType = ref(null)
const aiAnalysisResult = ref(null)
const loadingAiAnalysis = ref(false)
const comments = ref([])
const newComment = ref('')
const submittingComment = ref(false)
const replyingToComment = ref(null)
const auditStatus = ref(null)
const showTranslateDialog = ref(false)
const showConvertDialog = ref(false)
const showDiffDialog = ref(false)
const translating = ref(false)
const converting = ref(false)
const translateTask = ref(null)
const convertTask = ref(null)
const translateForm = reactive({ sourceLanguage: 'zh', targetLanguage: 'en' })
const convertForm = reactive({ sourceFormat: '', targetFormat: '' })
const diffVersion1 = ref(null)
const diffVersion2 = ref(null)
const diffData = ref(null)
const loadingDiff = ref(false)
const chunkNavList = ref([])
const activeChunkIndex = ref(-1)
const chunkRefs = ref([])
const similarDocuments = ref([])
const loadingSimilar = ref(false)

// 格式选项
const formatOptions = [
  { label: 'PDF', value: 'pdf' },
  { label: 'Word', value: 'docx' },
  { label: 'TXT', value: 'txt' },
  { label: 'PNG', value: 'png' },
  { label: 'JPG', value: 'jpg' }
]

// 计算可用的目标格式
const availableTargetFormats = computed(() => {
  return formatOptions.filter(option => option.value !== convertForm.sourceFormat)
})

// 获取文件格式标签
function getFileFormatLabel(format) {
  const option = formatOptions.find(opt => opt.value === format)
  return option ? option.label : format.toUpperCase()
}

const editForm = reactive({
  title: '',
  description: '',
  isPublic: true,
  category: '',
  tags: [],
  existingTags: []
})

const isOwner = computed(() => {
  const authInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!authInfo || !document.value) return false
  const auth = JSON.parse(authInfo)
  return auth.userId === document.value.uploadUserId
})

const textChunks = computed(() => {
  return document.value?.chunks?.filter(c => c.contentType === 'text') || []
})

const tableChunks = computed(() => {
  return document.value?.chunks?.filter(c => c.contentType === 'table') || []
})

const isPreviewSupported = computed(() => {
  if (!document.value?.fileType) return false
  const supportedTypes = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
  return supportedTypes.includes(document.value.fileType.toLowerCase())
})

// 监听tab切换，加载知识图谱
watch(activeTab, (newTab) => {
  if (newTab === 'kg' && document.value) {
    loadKnowledgeGraph()
  }
  if (newTab === 'chunks' && document.value) {
    nextTick(() => {
      setupChunkObserver()
      window.document.querySelectorAll('[id^="chunk-"]').forEach(el => {
        if (chunkObserver) chunkObserver.observe(el)
      })
    })
  }
  if (newTab === 'comments' && document.value) {
    loadComments(document.value.id)
  }
})

onMounted(() => {
  loadDocument()
})

function loadDocument() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  getDocumentDetail(id, (data) => {
    document.value = data
    editForm.title = data.title
    editForm.description = data.description
    editForm.isPublic = data.isPublic === 1
    editForm.category = data.category || ''
    editForm.tags = data.topics ? data.topics.map(t => t.topicValue) : []
    editForm.existingTags = data.topics ? data.topics.map(t => t.topicValue) : []
    currentVersion.value = data.currentVersion || 1
    isFavorite.value = data.isFavorite || false
    loadCategories()
    loadVersions()
    loadAiAnalysisResult(id)
    loadComments(id)
    loadAuditStatus(id)
    loadChunkNav(id)
    loadSimilarDocuments(id)
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载失败')
    loading.value = false
  })
}

function loadAiAnalysisResult(documentId) {
  loadingAiAnalysis.value = true
  getAiAnalysisResult(documentId, (data) => {
    aiAnalysisResult.value = data
    console.log('AI分析结果:', aiAnalysisResult.value)
    loadingAiAnalysis.value = false
  }, (msg) => {
    console.error('加载AI分析结果失败:', msg)
    aiAnalysisResult.value = null
    loadingAiAnalysis.value = false
  })
}

function loadComments(docId) {
  getComments(docId, (data) => {
    const allComments = data || []
    // 主评论（parentId 为 null 或 0）
    const mainComments = allComments.filter(c => !c.parentId && c.status !== 1)
    // 所有回复
    const replies = allComments.filter(c => c.parentId && c.status !== 1)

    comments.value = mainComments.map(c => ({
      ...c,
      replies: replies.filter(r => r.parentId === c.id)
    }))
  }, (msg) => {
    console.error('加载评论失败:', msg)
  })
}

function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  const authInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!authInfo) {
    ElMessage.warning('请先登录')
    return
  }
  const auth = JSON.parse(authInfo)
  submittingComment.value = true
  addComment({
    documentId: document.value.id,
    userId: auth.userId,
    content: newComment.value.trim(),
    parentId: replyingToComment.value?.id || null
  }, (data) => {
    ElMessage.success('评论发表成功，AI正在审核中')
    newComment.value = ''
    replyingToComment.value = null
    submittingComment.value = false
    loadComments(document.value.id)
  }, (msg) => {
    ElMessage.error(msg || '评论发表失败')
    submittingComment.value = false
  })
}

function replyToComment(comment) {
  replyingToComment.value = comment
  newComment.value = `回复${comment.userName || '用户' + comment.userId} `
}

function cancelReply() {
  replyingToComment.value = null
  newComment.value = ''
}

function loadAuditStatus(docId) {
  getDocumentAuditStatus(docId, (data) => {
    auditStatus.value = data
  }, () => {
    auditStatus.value = null
  })
}

function loadSimilarDocuments(docId) {
  loadingSimilar.value = true
  getSimilarDocuments(docId, 0, 10, (data) => {
    similarDocuments.value = data || []
    loadingSimilar.value = false
  }, () => {
    similarDocuments.value = []
    loadingSimilar.value = false
  })
}

function goToSimilarDocument(docId) {
  router.push(`/document/detail/${docId}`)
}

function loadChunkNav(docId) {
  getDocumentChunks(docId, (data) => {
    chunkNavList.value = (data || []).map(chunk => ({
      id: chunk.id,
      title: chunk.contentType,
      preview: (chunk.contentText || '').substring(0, 80),
      chunkIndex: chunk.chunkIndex
    }))
  }, () => {
    chunkNavList.value = []
  })
}

function handleTranslate() {
  if (translateForm.sourceLanguage === translateForm.targetLanguage) {
    ElMessage.warning('源语言和目标语言不能相同')
    return
  }
  const authInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!authInfo) {
    ElMessage.warning('请先登录')
    return
  }
  const auth = JSON.parse(authInfo)
  translating.value = true
  translateTask.value = null
  submitTranslationTask(document.value.id, auth.userId, translateForm.sourceLanguage, translateForm.targetLanguage,
    (taskId) => {
      ElMessage.success('翻译任务已提交')
      translating.value = false
      pollTaskStatus(taskId, 'translate')
    }, (msg) => {
      ElMessage.error(msg || '翻译任务提交失败')
      translating.value = false
    })
}

function handleConvert() {
  if (!convertForm.targetFormat) {
    ElMessage.warning('请选择目标格式')
    return
  }
  const authInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!authInfo) {
    ElMessage.warning('请先登录')
    return
  }
  const auth = JSON.parse(authInfo)
  converting.value = true
  convertTask.value = null
  submitConversionTask(document.value.id, auth.userId, convertForm.sourceFormat, convertForm.targetFormat,
    (taskId) => {
      ElMessage.success('格式转换任务已提交')
      converting.value = false
      pollTaskStatus(taskId, 'convert')
    }, (msg) => {
      ElMessage.error(msg || '格式转换任务提交失败')
      converting.value = false
    })
}

function pollTaskStatus(taskId, type) {
  const getStatus = type === 'translate' ? getTranslationTaskStatus : getConversionTaskStatus
  const setTask = type === 'translate' ? (v) => translateTask.value = v : (v) => convertTask.value = v
  const maxPolls = 60
  let count = 0
  const timer = setInterval(() => {
    count++
    getStatus(taskId, (data) => {
      setTask(data)
      if (data.status === 'COMPLETED' || data.status === 'FAILED' || count >= maxPolls) {
        clearInterval(timer)
      }
    }, () => {
      if (count >= maxPolls) clearInterval(timer)
    })
  }, 3000)
}

function downloadTranslationResult() {
  if (translateTask.value?.resultPath) {
    window.open(translateTask.value.resultPath, '_blank')
  }
}

function downloadConversionResult() {
  if (convertTask.value?.resultPath) {
    window.open(convertTask.value.resultPath, '_blank')
  }
}

function openDiffView(versionNumber) {
  diffVersion1.value = versionNumber
  diffVersion2.value = currentVersion.value
  showDiffDialog.value = true
  // 自动加载差异数据
  setTimeout(() => {
    loadVersionDiff()
  }, 100)
}

function openDefaultDiff() {
  // 按版本号降序排序
  const sortedVersions = [...versions.value].sort((a, b) => b.versionNumber - a.versionNumber)
  if (sortedVersions.length >= 2) {
    // 默认选择当前版本和上一版本
    diffVersion2.value = sortedVersions[0].versionNumber // 当前版本
    diffVersion1.value = sortedVersions[1].versionNumber // 上一版本
  } else if (sortedVersions.length === 1) {
    // 只有一个版本时，无法对比
    ElMessage.warning('只有一个版本，无法对比')
    return
  } else {
    // 没有版本时
    ElMessage.warning('没有版本数据')
    return
  }
  showDiffDialog.value = true
  // 自动加载差异数据
  setTimeout(() => {
    loadVersionDiff()
  }, 100)
}

function loadVersionDiff() {
  if (!diffVersion1.value || !diffVersion2.value) {
    ElMessage.warning('请选择两个版本')
    return
  }
  if (diffVersion1.value === diffVersion2.value) {
    ElMessage.warning('请选择不同的版本')
    return
  }
  loadingDiff.value = true
  diffData.value = null
  getVersionDiff(document.value.id, diffVersion1.value, diffVersion2.value, (data) => {
    diffData.value = {
      lines1: data.chunksA,
      lines2: data.chunksB,
      stats: data.stats
    }
    loadingDiff.value = false
  }, (msg) => {
    ElMessage.error(msg || '获取版本差异失败')
    loadingDiff.value = false
  })
}

function handleRollback(versionNumber) {
  ElMessageBox.confirm(`确定要回滚到 v${versionNumber} 吗？`, '版本回滚', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    rollbackVersion(document.value.id, versionNumber, () => {
      ElMessage.success('版本回滚成功')
      loadDocument()
    }, (msg) => {
      ElMessage.error(msg || '版本回滚失败')
    })
  }).catch(() => { })
}

function getAuditStatusType(status) {
  const map = { 'PASS': 'success', 'APPROVED': 'success', 'REJECT': 'danger', 'REJECTED': 'danger', 'WARN': 'warning', 'PENDING': 'info', 'PROCESSING': '' }
  return map[status] || 'info'
}

function getAuditStatusText(status) {
  const map = { 'PASS': '通过', 'APPROVED': '已通过', 'REJECT': '拒绝', 'REJECTED': '已拒绝', 'WARN': '待人工审核', 'PENDING': '审核中', 'PROCESSING': '处理中' }
  return map[status] || status
}

function getDocAuditStatusText(status) {
  const map = { 'PASS': '审核通过', 'REJECT': '审核拒绝', 'WARN': '疑似违规', 'PENDING': '待审核', 'PROCESSING': '审核中' }
  return map[status] || status
}

function getTaskStatusType(status) {
  const map = { 'PENDING': 'info', 'PROCESSING': '', 'COMPLETED': 'success', 'FAILED': 'danger' }
  return map[status] || 'info'
}

function getTaskStatusText(status) {
  const map = { 'PENDING': '排队中', 'PROCESSING': '处理中', 'COMPLETED': '已完成', 'FAILED': '失败' }
  return map[status] || status
}

function getDiffLineClass(diffType) {
  const map = {
    'ADDED': 'diff-add',
    'DELETED': 'diff-delete',
    'MODIFIED': 'diff-modify',
    'UNCHANGED': ''
  }
  return map[diffType] || ''
}

function scrollToChunk(idx) {
  activeChunkIndex.value = idx
  const el = window.document.getElementById('chunk-' + idx)
  if (el) {
    // 找到滚动容器
    const scrollContainer = el.closest('.content-list')
    if (scrollContainer) {
      const containerTop = scrollContainer.getBoundingClientRect().top
      const elTop = el.getBoundingClientRect().top
      const scrollTop = scrollContainer.scrollTop
      const newScrollTop = scrollTop + (elTop - containerTop) - (scrollContainer.clientHeight / 2) + (el.clientHeight / 2)
      scrollContainer.scrollTo({ top: newScrollTop, behavior: 'smooth' })
    } else {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }
}

let chunkObserver = null

function setupChunkObserver() {
  if (chunkObserver) chunkObserver.disconnect()
  chunkObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const id = entry.target.id
        const idx = parseInt(id.replace('chunk-', ''))
        if (!isNaN(idx)) activeChunkIndex.value = idx
      }
    })
  }, { threshold: 0.5 })
}

function loadVersions() {
  const id = route.params.id
  if (!id) return

  getDocumentVersions(id, (data) => {
    // 按版本号降序排序，确保最新版本在最上面
    versions.value = data.sort((a, b) => b.versionNumber - a.versionNumber)
  }, (msg) => {
    console.error('加载版本列表失败:', msg)
  })
}

function switchVersion(versionNumber) {
  const id = route.params.id
  if (!id) return

  loading.value = true
  getDocumentVersionDetail(id, versionNumber, (data) => {
    document.value = data
    viewingVersion.value = versionNumber
    loading.value = false
    // 跳转到完整内容板块
    activeTab.value = 'full'
  }, (msg) => {
    ElMessage.error(msg || '加载版本失败')
    loading.value = false
  })
}

function handleUploadVersion() {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  const id = route.params.id
  if (!id) return

  loading.value = true
  uploadProgress.value = 0
  uploadNewVersion(id, uploadFile.value, (progress) => {
    uploadProgress.value = progress
  }, (data) => {
    ElMessage.success('版本上传成功')
    showUploadVersionDialog.value = false
    uploadFile.value = null
    loading.value = false
    loadDocument()
  }, (msg) => {
    ElMessage.error(msg || '版本上传失败')
    loading.value = false
  })
}

function handleDownloadVersion(versionNumber) {
  const id = route.params.id
  if (!id) return
  downloadDocumentVersion(id, versionNumber)
}

function handleFileChange(file) {
  // el-upload的on-change返回的是UploadFile对象，需要取raw属性获取原始文件
  uploadFile.value = file.raw
}

function loadCategories() {
  // 加载所有分类
  getAllCategories((data) => {
    categories.value = data
    // 加载文档的分类
    getDocumentCategories(document.value.id, (categoryIds) => {
      selectedCategories.value = categoryIds
    })
  })
}

function saveCategories() {
  setDocumentCategories(document.value.id, selectedCategories.value, () => {
    ElMessage.success('分类设置成功')
  }, (msg) => {
    ElMessage.error(msg || '分类设置失败')
  })
}

function goBack() {
  // 直接导航到文档列表页，避免浏览器历史记录问题
  // router.push('/doc/list')
  router.back()
}

function handleDownload() {
  downloadDocument(document.value.id)
}

function submitRating() {
  if (qualityRating.value === 0 && readabilityRating.value === 0) {
    ElMessage.warning('请至少选择一个评分')
    return
  }
  rateDocument(document.value.id, qualityRating.value, readabilityRating.value, () => {
    ElMessage.success('评分成功')
    showRateDialog.value = false
    loadDocument()
  }, (msg) => {
    ElMessage.error(msg || '评分失败')
  })
}

function handleEdit() {
  updateDocumentMetadata({
    id: document.value.id,
    title: editForm.title,
    description: editForm.description,
    isPublic: editForm.isPublic ? 1 : 0,
    category: editForm.category,
    tags: editForm.tags
  }, () => {
    ElMessage.success('更新成功')
    showEditDialog.value = false
    loadDocument()
  }, (msg) => {
    ElMessage.error(msg || '更新失败')
  })
}

function handleDelete() {
  ElMessageBox.confirm('确定要删除这个文档吗？删除后无法恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteDocument(document.value.id, () => {
      ElMessage.success('删除成功')
      router.push('/doc/list')
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleFavorite() {
  if (isFavorite.value) {
    // 取消收藏
    unfavoriteDocument(document.value.id, () => {
      ElMessage.success('取消收藏成功')
      isFavorite.value = false
      loadDocument()
    }, (msg) => {
      ElMessage.error(msg || '取消收藏失败')
    })
  } else {
    // 收藏
    favoriteDocument(document.value.id, () => {
      ElMessage.success('收藏成功')
      isFavorite.value = true
      loadDocument()
    }, (msg) => {
      ElMessage.error(msg || '收藏失败')
    })
  }
}

function handleShare() {
  shareDocument(document.value.id, (shareUrl) => {
    // 复制分享链接到剪贴板
    navigator.clipboard.writeText(shareUrl).then(() => {
      ElMessage.success('分享链接已复制到剪贴板')
    }).catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
  }, (msg) => {
    ElMessage.error(msg || '生成分享链接失败')
  })
}

function loadKnowledgeGraph() {
  const id = route.params.id
  if (!id) return

  loadingKg.value = true
  // 清除之前的轮询
  if (kgPollingTimer.value) {
    clearInterval(kgPollingTimer.value)
    kgPollingTimer.value = null
  }

  // 从后端API获取知识图谱数据
  getDocumentKnowledgeGraph(id,
    (data) => {
      kgData.value = data
      loadingKg.value = false

      // 如果数据为空，启动轮询检测知识图谱是否生成
      if (!data.nodes || data.nodes.length === 0) {
        let pollingCount = 0
        const maxPollingCount = 30 // 最多轮询30次（约30秒）

        kgPollingTimer.value = setInterval(() => {
          pollingCount++
          getDocumentKnowledgeGraph(id,
            (pollingData) => {
              if (pollingData.nodes && pollingData.nodes.length > 0) {
                // 知识图谱已生成，停止轮询
                clearInterval(kgPollingTimer.value)
                kgPollingTimer.value = null
                kgData.value = pollingData
                nextTick(() => {
                  renderKnowledgeGraph()
                })
              } else if (pollingCount >= maxPollingCount) {
                // 达到最大轮询次数，停止轮询
                clearInterval(kgPollingTimer.value)
                kgPollingTimer.value = null
              }
            },
            () => {
              // 轮询失败，继续轮询
            }
          )
        }, 1000)
      } else {
        // 确保DOM更新后再渲染知识图谱
        nextTick(() => {
          renderKnowledgeGraph()
        })
      }
    },
    (error) => {
      console.error('获取知识图谱失败:', error)
      loadingKg.value = false
      ElMessage.error('获取知识图谱失败，请重试')
      // 使用默认数据作为回退
      kgData.value = {
        nodes: [
          { id: 1, label: '人工智能', type: 'CONCEPT' },
          { id: 2, label: '机器学习', type: 'CONCEPT' },
          { id: 3, label: '深度学习', type: 'CONCEPT' },
          { id: 4, label: '自然语言处理', type: 'CONCEPT' },
          { id: 5, label: 'AI', type: 'TECHNOLOGY' }
        ],
        links: [
          { source: 1, target: 2, label: '包含' },
          { source: 2, target: 3, label: '包含' },
          { source: 1, target: 4, label: '包含' },
          { source: 1, target: 5, label: '简称' }
        ]
      }
      nextTick(() => {
        renderKnowledgeGraph()
      })
    }
  )
}

function rebuildKnowledgeGraph() {
  const id = route.params.id
  if (!id) return

  ElMessageBox.confirm('确定要重新构建知识图谱吗？这将会覆盖现有的知识图谱数据。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    loadingKg.value = true
    rebuildDocumentKnowledgeGraph(id,
      () => {
        ElMessage.success('知识图谱重新构建成功')
        // 重新加载知识图谱
        loadKnowledgeGraph()
      },
      (error) => {
        console.error('重新构建知识图谱失败:', error)
        loadingKg.value = false
        ElMessage.error('重新构建知识图谱失败，请重试')
      }
    )
  }).catch(() => { })
}

function renderKnowledgeGraph() {
  const container = kgContainer.value
  if (!container) return

  // 销毁旧的图表实例
  if (kgChart.value) {
    kgChart.value.dispose()
  }

  // 创建新的图表实例
  kgChart.value = echarts.init(container)

  const nodes = kgData.value.nodes
  const links = kgData.value.links

  // 准备ECharts数据
  const echartsNodes = nodes.map(node => ({
    id: node.id.toString(),
    name: node.label,
    symbolSize: 50,
    value: node.label,
    category: node.type === 'CONCEPT' ? 0 : 1,
    itemStyle: {
      // 使用系统主题色
      color: node.type === 'CONCEPT' ? '#fa8c16' : '#f56c6c'
    }
  }))

  const echartsLinks = links.map(link => ({
    source: link.source.toString(),
    target: link.target.toString(),
    label: {
      show: true,
      formatter: link.label
    }
  }))

  const option = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'bottom',
      data: [
        { name: '概念', itemStyle: { color: '#fa8c16' } },
        { name: '技术', itemStyle: { color: '#f56c6c' } }
      ]
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: echartsNodes,
        links: echartsLinks,
        categories: [
          {
            name: '概念'
          },
          {
            name: '技术'
          }
        ],
        roam: true,
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}',
          color: '#fff'
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        },
        force: {
          repulsion: 1000,
          edgeLength: [80, 120]
        }
      }
    ]
  }

  // 渲染图表
  kgChart.value.setOption(option)

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (kgChart.value) {
    kgChart.value.resize()
  }
}

onUnmounted(() => {
  if (kgChart.value) {
    kgChart.value.dispose()
  }
  if (kgPollingTimer.value) {
    clearInterval(kgPollingTimer.value)
    kgPollingTimer.value = null
  }
  if (chunkObserver) {
    chunkObserver.disconnect()
    chunkObserver = null
  }
  window.removeEventListener('resize', handleResize)
})

function getChunkType(type) {
  const typeMap = {
    'text': '',
    'table': 'success',
    'formula': 'warning',
    'image': 'info'
  }
  return typeMap[type] || ''
}

function getSimilarityClass(similarity) {
  if (similarity >= 0.8) return 'similarity-high'
  if (similarity >= 0.5) return 'similarity-medium'
  return 'similarity-low'
}

function getAIProbabilityClass(aiProbability) {
  if (aiProbability >= 0.8) return 'ai-high'
  if (aiProbability >= 0.5) return 'ai-medium'
  return 'ai-low'
}

function getConfidenceClass(confidence) {
  if (confidence >= 0.8) return 'ai-low'
  if (confidence >= 0.5) return 'ai-medium'
  return 'ai-high'
}

function getAiResultType(result) {
  const typeMap = {
    'AI_GENERATED': 'danger',
    'HUMAN_WRITTEN': 'success',
    'MIXED': 'warning'
  }
  return typeMap[result] || 'info'
}

function getAiResultText(result) {
  const textMap = {
    'AI_GENERATED': 'AI生成',
    'HUMAN_WRITTEN': '人工撰写',
    'MIXED': '混合内容'
  }
  return textMap[result] || result
}

function formatFullDecimal(value) {
  if (value === undefined || value === null) return '-'
  const result = (value * 100).toPrecision(12)
  return Number(result) + '%'
}

function toggleFullscreen(type) {
  if (isFullscreen.value && currentFullscreenType.value === type) {
    // 退出全屏
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
    isFullscreen.value = false
    currentFullscreenType.value = null
  } else {
    // 进入全屏
    let element = null
    if (type === 'full' && fullContentRef.value) {
      element = fullContentRef.value
    } else if (type === 'preview' && document.value?.previewUrl && previewContentRef.value) {
      element = previewContentRef.value
    } else if (type === 'kg' && kgContainer.value) {
      element = kgContainer.value
    }

    if (element) {
      if (element.requestFullscreen) {
        element.requestFullscreen()
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen()
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen()
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen()
      }
      isFullscreen.value = true
      currentFullscreenType.value = type
    }
  }
}

function openConvertDialog() {
  // 自动设置源格式为当前文档的格式
  convertForm.sourceFormat = document.value.fileType?.toLowerCase() || 'pdf'
  convertForm.targetFormat = ''
  convertTask.value = null
  showConvertDialog.value = true
}
</script>

<style scoped>
.document-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.button-text {
  margin-left: 8px;
}

.detail-content {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.main-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.title-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.title-info {
  flex: 1;
}

.doc-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}

.meta-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.meta-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.category-tag {
  background-color: #fa8c16;
  color: white;
  border-color: #fa8c16;
}

.topic-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.topic-tag {
  background-color: #fff7e6;
  color: #fa8c16;
  border-color: #ffd591;
  font-size: 12px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding-top: 16px;
  margin-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px;
  background: #fafafa;
  border-radius: 8px;
}

.stat-value {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.description-card,
.content-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.content-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.full-content-inner {
  position: relative;
  max-height: 600px;
  overflow-y: auto;
}

.content-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.content-actions .el-button {
  margin-left: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 全屏样式 */
.full-content-inner:-webkit-full-screen {
  max-height: 100vh;
  width: 100vw;
  background: white;
  z-index: 9999;
  position: fixed;
  top: 0;
  left: 0;
  padding: 20px;
}

.preview-content:-webkit-full-screen {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: white;
  z-index: 9999;
  padding: 20px;
}

.preview-content:-webkit-full-screen .preview-iframe {
  width: 100%;
  height: 100%;
}

.comments-section {
  padding: 16px;
}

.comment-input {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-body {
  color: #333;
  line-height: 1.6;
  font-size: 14px;
}

.comment-reject-reason {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
}

.comment-actions {
  margin-top: 8px;
}

.comment-replies {
  margin-top: 12px;
  padding-left: 20px;
  border-left: 2px solid #fa8c16;
}

.reply-item {
  padding: 8px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 4px;
}

.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 8px;
  background: #fff0e6;
  border-radius: 4px;
  border-left: 3px solid #fa8c16;
}

.reply-indicator span {
  color: #fa8c16;
  font-size: 13px;
}

.reply-content {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.reply-user {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.reply-arrow {
  color: #999;
  font-size: 12px;
}

.reply-to {
  color: #fa8c16;
  font-size: 13px;
}

.reply-body {
  color: #333;
  line-height: 1.5;
  font-size: 13px;
  margin-bottom: 4px;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-history {
  padding: 16px;
}

.version-actions-top {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.version-card {
  margin-bottom: 0;
}

.version-current {
  border-color: #fa8c16;
}

.version-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.version-number {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.version-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
}

.version-size {
  color: #999;
}

.version-card-actions {
  display: flex;
  gap: 4px;
}

.task-status {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.task-error {
  color: #f56c6c;
  margin-top: 8px;
  font-size: 13px;
}

.diff-container {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.diff-content {
  flex: 1;
  display: flex;
  gap: 16px;
  overflow: hidden;
}

.diff-selector {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.diff-content {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.diff-panel {
  flex: 1;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.diff-panel-header {
  padding: 8px 16px;
  background: #f5f7fa;
  font-weight: 600;
  border-bottom: 1px solid #e8e8e8;
}

.diff-panel-body {
  flex: 1;
  overflow-y: auto;
  font-family: monospace;
  font-size: 13px;
  background: white;
  padding: 8px 0;
  max-height: 100%;
}

.diff-line {
  display: flex;
  align-items: flex-start;
  padding: 0 16px;
  min-height: 20px;
}

.line-number {
  width: 60px;
  text-align: right;
  padding-right: 16px;
  color: #999;
  border-right: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.line-content {
  flex: 1;
  padding-left: 16px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.diff-add {
  background-color: #f6ffed;
}

.diff-delete {
  background-color: #fff1f0;
}

.diff-modify {
  background-color: #fffbe6;
}



.chunks-with-nav {
  display: flex;
  gap: 16px;
}

.chunk-nav-sidebar {
  width: 220px;
  flex-shrink: 0;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow-y: auto;
  max-height: 600px;
}

.chunk-nav-title {
  padding: 10px 12px;
  font-weight: 600;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #e8e8e8;
  background: #f5f7fa;
}

.chunk-nav-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.chunk-nav-item:hover {
  background: #fff0e6;
}

.chunk-nav-active {
  background: #fff0e6;
  border-left: 3px solid #fa8c16;
}

.chunk-nav-label {
  font-weight: 600;
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
}

.chunk-nav-preview {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.chunks-content {
  flex: 1;
}

.kg-visualization:-webkit-full-screen {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: white;
  z-index: 9999;
  padding: 20px;
}

:-webkit-full-screen {
  background: white;
  z-index: 9999;
}

:-ms-fullscreen {
  background: white;
  z-index: 9999;
}

:fullscreen {
  background: white;
  z-index: 9999;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.description-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.content-list {
  min-height: 70vh;
  max-height: 80vh;
  overflow-y: auto;
}

.content-actions {
  margin-bottom: 16px;
  margin-right: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-action-buttons {
  display: flex;
  gap: 8px;
}

.full-content {
  min-height: 600px;
  max-height: 800px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.content-text {
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.content-chunk {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.chunk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chunk-index {
  font-size: 12px;
  color: #999;
}

.chunk-content {
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.side-info {
  width: 200px;
  flex-shrink: 0;
  overflow-y: auto;
  max-height: calc(100vh - 140px);
}

.info-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.side-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-name {
  white-space: normal;
  text-overflow: unset;
  overflow: visible;
  line-height: 1.4;
}

.key-features {
  margin-top: 4px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 12px;
}

.feature-item:last-child {
  margin-bottom: 0;
}

.feature-text {
  color: #666;
}

.feature-score {
  font-weight: 500;
  color: #fa8c16;
  font-size: 14px;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-tag {
  background: linear-gradient(135deg, #fff0e6, #ffe4cc);
  border-color: #ffcc80;
  color: #e65100;
}

.rate-content {
  text-align: center;
  padding: 20px 0;
}

.rate-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 20px;
}

.rate-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rate-item:last-child {
  margin-bottom: 0;
}

.rate-label {
  font-size: 14px;
  color: #666;
}

.rate-star-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 全局样式：覆盖 Element Plus 默认样式 */
:deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #fa8c16 inset;
}

:deep(.el-textarea__inner:focus) {
  border-color: #fa8c16;
  box-shadow: 0 0 0 2px rgba(250, 140, 22, 0.2);
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #fa8c16 inset;
}

:deep(.el-tabs__item.is-active) {
  color: #fa8c16;
}

:deep(.el-tabs__active-bar) {
  background-color: #fa8c16;
}

:deep(.el-tabs__item:hover) {
  color: #fa8c16;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #fa8c16;
  border-color: #fa8c16;
}

.similarity-high {
  color: #f5222d;
  font-weight: 600;
}

.similarity-medium {
  color: #faad14;
  font-weight: 600;
}

.similarity-low {
  color: #52c41a;
  font-weight: 600;
}

.ai-high {
  color: #f5222d;
  font-weight: 600;
}

.ai-medium {
  color: #faad14;
  font-weight: 600;
}

.ai-low {
  color: #52c41a;
  font-weight: 600;
}

.kg-container {
  position: relative;
  min-height: 70vh;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.kg-container .content-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.kg-container .content-actions .el-button {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.kg-loading {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.kg-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.kg-empty .el-icon {
  font-size: 48px;
  color: #d9d9d9;
}

.kg-content {
  width: 100%;
  height: 560px;
  position: relative;
}

.kg-visualization {
  width: 100%;
  height: 100%;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background-color: #fafafa;
}

.preview-container {
  position: relative;
  width: 100%;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.preview-container .content-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.preview-container .content-actions .el-button {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.preview-empty,
.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.preview-empty .el-icon,
.preview-unsupported .el-icon {
  font-size: 48px;
  color: #d9d9d9;
}

.preview-unsupported {
  gap: 16px;
}

.preview-content {
  width: 100%;
  height: 800px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
}

/* 文档详情页顶部按钮莫兰迪色系样式 */
.btn-rate {
  background-color: #FFF3D6 !important;
  border-color: #E6D0A8 !important;
  color: #8B7355 !important;
}

.btn-rate:hover {
  background-color: #FFE8C2 !important;
  border-color: #D4C094 !important;
  color: #6B5344 !important;
}

.btn-favorite {
  background-color: #FFE4D6 !important;
  border-color: #F0D0C0 !important;
  color: #8B6355 !important;
}

.btn-favorite:hover {
  background-color: #FFDAC4 !important;
  border-color: #E8C4B0 !important;
  color: #6B4A3A !important;
}

.btn-favorited {
  background-color: #E07848 !important;
  border-color: #D06838 !important;
  color: #FFFFFF !important;
}

.btn-favorited:hover {
  background-color: #F08858 !important;
  border-color: #E07848 !important;
  color: #FFFFFF !important;
}

.btn-share {
  background-color: #FFE4EC !important;
  border-color: #F0D0E0 !important;
  color: #8B5A6B !important;
}

.btn-share:hover {
  background-color: #FFD6E0 !important;
  border-color: #E8C0D0 !important;
  color: #6B4555 !important;
}

.btn-convert {
  background-color: #E8E0FF !important;
  border-color: #D0C8F0 !important;
  color: #6B5A8B !important;
}

.btn-convert:hover {
  background-color: #DCD4F8 !important;
  border-color: #C0B8E8 !important;
  color: #554470 !important;
}

.btn-kg {
  background-color: #D6E0FF !important;
  border-color: #C0D0E8 !important;
  color: #4A5A8B !important;
}

.btn-kg:hover {
  background-color: #C4D4F8 !important;
  border-color: #B0C0E0 !important;
  color: #3A486B !important;
}

.similar-docs-section {
  padding: 16px;
}

.similar-docs-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.similar-count {
  font-size: 14px;
  color: #666;
}

.similar-docs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.similar-doc-card {
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.similar-doc-card:hover {
  border-color: #fa8c16;
  background: #fff0e6;
}

.similar-doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.similar-doc-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.similar-doc-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.similar-docs-loading,
.similar-docs-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  gap: 12px;
}

.similar-docs-empty .el-icon {
  font-size: 48px;
  color: #dcdfe6;
}
</style>
