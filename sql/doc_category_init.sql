-- ============================================
-- 文档分类初始化数据
-- ============================================

-- 清空现有分类（可选，如需保留可注释）
TRUNCATE TABLE doc_category;

-- 插入一级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(1, '技术文档', 0, 1, 1, NOW(), NOW()),
(2, '学术论文', 0, 2, 1, NOW(), NOW()),
(3, '工作报告', 0, 3, 1, NOW(), NOW()),
(4, '商业文档', 0, 4, 1, NOW(), NOW()),
(5, '教育资料', 0, 5, 1, NOW(), NOW()),
(6, '法律文档', 0, 6, 1, NOW(), NOW()),
(7, '医疗健康', 0, 7, 1, NOW(), NOW()),
(8, '金融财经', 0, 8, 1, NOW(), NOW()),
(9, '其他文档', 0, 99, 1, NOW(), NOW());

-- 插入技术文档的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(101, '编程开发', 1, 1, 1, NOW(), NOW()),
(102, '云计算与DevOps', 1, 2, 1, NOW(), NOW()),
(103, '人工智能与机器学习', 1, 3, 1, NOW(), NOW()),
(104, '网络安全', 1, 4, 1, NOW(), NOW()),
(105, '数据库', 1, 5, 1, NOW(), NOW()),
(106, '网络与通信', 1, 6, 1, NOW(), NOW()),
(107, '产品设计', 1, 7, 1, NOW(), NOW()),
(108, '软件测试', 1, 8, 1, NOW(), NOW());

-- 插入学术论文的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(201, '计算机科学', 2, 1, 1, NOW(), NOW()),
(202, '工程技术', 2, 2, 1, NOW(), NOW()),
(203, '自然科学', 2, 3, 1, NOW(), NOW()),
(204, '医学健康', 2, 4, 1, NOW(), NOW()),
(205, '社会科学', 2, 5, 1, NOW(), NOW()),
(206, '经济管理', 2, 6, 1, NOW(), NOW()),
(207, '人文艺术', 2, 7, 1, NOW(), NOW()),
(208, '教育学', 2, 8, 1, NOW(), NOW());

-- 插入工作报告的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(301, '项目总结', 3, 1, 1, NOW(), NOW()),
(302, '年度汇报', 3, 2, 1, NOW(), NOW()),
(303, '市场分析', 3, 3, 1, NOW(), NOW()),
(304, '运营报告', 3, 4, 1, NOW(), NOW()),
(305, '审计报告', 3, 5, 1, NOW(), NOW());

-- 插入商业文档的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(401, '商业计划书', 4, 1, 1, NOW(), NOW()),
(402, '合同协议', 4, 2, 1, NOW(), NOW()),
(403, '营销推广', 4, 3, 1, NOW(), NOW()),
(404, '人力资源', 4, 4, 1, NOW(), NOW()),
(405, '企业管理制度', 4, 5, 1, NOW(), NOW());

-- 插入教育资料的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(501, '课程教材', 5, 1, 1, NOW(), NOW()),
(502, '培训资料', 5, 2, 1, NOW(), NOW()),
(503, '考试试题', 5, 3, 1, NOW(), NOW()),
(504, '学术笔记', 5, 4, 1, NOW(), NOW());

-- 插入法律文档的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(601, '法律法规', 6, 1, 1, NOW(), NOW()),
(602, '合同范本', 6, 2, 1, NOW(), NOW()),
(603, '诉讼文书', 6, 3, 1, NOW(), NOW()),
(604, '知识产权', 6, 4, 1, NOW(), NOW());

-- 插入医疗健康的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(701, '医学文献', 7, 1, 1, NOW(), NOW()),
(702, '临床指南', 7, 2, 1, NOW(), NOW()),
(703, '健康养生', 7, 3, 1, NOW(), NOW()),
(704, '药品说明', 7, 4, 1, NOW(), NOW());

-- 插入金融财经的二级分类
INSERT INTO doc_category (id, name, parent_id, sort, status, create_time, update_time) VALUES
(801, '投资理财', 8, 1, 1, NOW(), NOW()),
(802, '财务报表', 8, 2, 1, NOW(), NOW()),
(803, '宏观经济', 8, 3, 1, NOW(), NOW()),
(804, '行业研究', 8, 4, 1, NOW(), NOW());
