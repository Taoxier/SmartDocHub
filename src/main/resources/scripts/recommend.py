#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
文档推荐系统 - 基于物品的协同过滤
"""

import os
import sys
import json
import redis
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# 数据库连接信息
DB_HOST = 'localhost'
DB_PORT = 3306
DB_USER = 'root'
DB_PASSWORD = '123456'
DB_NAME = 'smartdochub'

# Redis连接信息
REDIS_HOST = 'localhost'
REDIS_PORT = 6379
REDIS_PASSWORD = ''
REDIS_DB = 0

# 推荐参数
SIMILARITY_THRESHOLD = 0.3
RECOMMENDATION_COUNT = 10


def get_user_behavior_data():
    """获取用户行为数据"""
    import pymysql
    
    # 连接数据库
    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        db=DB_NAME,
        charset='utf8mb4'
    )
    
    try:
        # 提取用户行为数据
        sql = """
        SELECT 
            user_id, 
            document_id, 
            behavior_type, 
            duration_seconds, 
            rating_value
        FROM 
            user_behavior
        WHERE 
            create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        """
        
        df = pd.read_sql(sql, conn)
        return df
    finally:
        conn.close()


def get_document_topics():
    """获取文档的主题信息"""
    import pymysql

    # 连接数据库
    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        db=DB_NAME,
        charset='utf8mb4'
    )

    try:
        # 检查doc_topic表是否存在
        cursor = conn.cursor()
        cursor.execute("SHOW TABLES LIKE 'doc_topic'")
        if not cursor.fetchone():
            print("doc_topic表不存在，跳过主题相似度计算")
            cursor.close()
            return None

        cursor.close()

        # 提取文档主题数据
        sql = """
        SELECT
            document_id,
            topic_value
        FROM
            doc_topic
        """

        df = pd.read_sql(sql, conn)
        return df
    finally:
        conn.close()


def build_user_item_matrix(df):
    """构建用户-物品矩阵"""
    # 计算用户对文档的偏好分数
    df['preference'] = 0
    
    # 浏览时长>30秒视为喜欢
    df.loc[df['duration_seconds'] > 30, 'preference'] = 1
    
    # 评分≥3视为喜欢
    df.loc[df['rating_value'] >= 3, 'preference'] = 1
    
    # 构建用户-物品矩阵
    user_item_matrix = df.pivot_table(
        index='user_id',
        columns='document_id',
        values='preference',
        fill_value=0
    )
    
    return user_item_matrix


def calculate_item_similarity(user_item_matrix):
    """计算物品之间的相似度"""
    # 转置矩阵，得到物品-用户矩阵
    item_user_matrix = user_item_matrix.T
    
    # 计算物品之间的余弦相似度
    n_items = len(item_user_matrix)
    similarity_matrix = np.zeros((n_items, n_items))
    
    for i in range(n_items):
        for j in range(i, n_items):
            # 获取两个物品的用户向量
            item_i = item_user_matrix.iloc[i].values
            item_j = item_user_matrix.iloc[j].values
            
            # 计算余弦相似度
            dot_product = np.dot(item_i, item_j)
            norm_i = np.linalg.norm(item_i)
            norm_j = np.linalg.norm(item_j)
            
            if norm_i > 0 and norm_j > 0:
                similarity = dot_product / (norm_i * norm_j)
            else:
                similarity = 0
            
            similarity_matrix[i, j] = similarity
            similarity_matrix[j, i] = similarity
    
    # 转换为DataFrame
    item_ids = item_user_matrix.index
    similarity_df = pd.DataFrame(
        similarity_matrix,
        index=item_ids,
        columns=item_ids
    )
    
    return similarity_df


def calculate_topic_similarity(document_topics):
    """基于主题计算文档之间的相似度"""
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.metrics.pairwise import cosine_similarity
    
    # 按文档分组，合并主题
    doc_topics = document_topics.groupby('document_id')['topic_value'].apply(lambda x: ' '.join(x)).reset_index()
    
    # 构建TF-IDF向量
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(doc_topics['topic_value'])
    
    # 计算余弦相似度
    similarity_matrix = cosine_similarity(tfidf_matrix)
    
    # 转换为DataFrame
    similarity_df = pd.DataFrame(
        similarity_matrix,
        index=doc_topics['document_id'],
        columns=doc_topics['document_id']
    )
    
    return similarity_df


def get_recommendations(user_id, user_item_matrix, item_similarity_df, topic_similarity_df):
    """获取用户的推荐列表"""
    # 检查用户是否存在
    if user_id not in user_item_matrix.index:
        return []

    # 获取用户喜欢的文档
    user_likes = user_item_matrix.loc[user_id]
    liked_docs = user_likes[user_likes > 0].index.tolist()

    if not liked_docs:
        return []

    # 计算推荐分数
    scores = {}

    for doc_id in liked_docs:
        # 获取与该文档相似的文档
        if doc_id not in item_similarity_df.index:
            continue

        similar_docs = item_similarity_df[doc_id]

        # 结合主题相似度
        for similar_doc, similarity in similar_docs.items():
            if similar_doc == doc_id:
                continue

            # 检查是否已经喜欢
            if user_likes.get(similar_doc, 0) > 0:
                continue

            # 计算综合相似度
            topic_sim = 0
            if topic_similarity_df is not None and similar_doc in topic_similarity_df.index and doc_id in topic_similarity_df.columns:
                topic_sim = topic_similarity_df.loc[similar_doc, doc_id]

            # 综合相似度 = 协同过滤相似度 * 0.7 + 主题相似度 * 0.3
            combined_sim = similarity * 0.7 + topic_sim * 0.3

            if combined_sim > SIMILARITY_THRESHOLD:
                scores[similar_doc] = scores.get(similar_doc, 0) + combined_sim

    # 排序并返回前N个推荐
    sorted_scores = sorted(scores.items(), key=lambda x: x[1], reverse=True)
    recommendations = [doc_id for doc_id, score in sorted_scores[:RECOMMENDATION_COUNT]]

    return recommendations


def save_recommendations_to_redis(recommendations_dict):
    """将推荐结果保存到Redis"""
    # 连接Redis
    r = redis.Redis(
        host=REDIS_HOST,
        port=REDIS_PORT,
        password=REDIS_PASSWORD,
        db=REDIS_DB
    )
    
    # 保存推荐结果
    for user_id, docs in recommendations_dict.items():
        key = f"recommend:user:{user_id}"
        # 清除旧的推荐
        r.delete(key)
        # 保存新的推荐
        if docs:
            r.lpush(key, *docs)
            # 设置过期时间为7天
            r.expire(key, 7 * 24 * 3600)


def main():
    """主函数"""
    print("开始生成推荐...")

    # 获取用户行为数据
    print("1. 获取用户行为数据...")
    user_behavior_df = get_user_behavior_data()

    if user_behavior_df.empty:
        print("没有足够的用户行为数据，跳过推荐")
        return

    # 获取文档主题数据
    print("2. 获取文档主题数据...")
    document_topics_df = get_document_topics()

    # 构建用户-物品矩阵
    print("3. 构建用户-物品矩阵...")
    user_item_matrix = build_user_item_matrix(user_behavior_df)

    # 计算物品相似度
    print("4. 计算物品相似度...")
    item_similarity_df = calculate_item_similarity(user_item_matrix)

    # 计算主题相似度（如果topic表存在）
    print("5. 计算主题相似度...")
    if document_topics_df is not None and not document_topics_df.empty:
        topic_similarity_df = calculate_topic_similarity(document_topics_df)
    else:
        topic_similarity_df = None
        print("跳过主题相似度计算")

    # 为每个用户生成推荐
    print("6. 为每个用户生成推荐...")
    recommendations_dict = {}

    for user_id in user_item_matrix.index:
        recommendations = get_recommendations(
            user_id,
            user_item_matrix,
            item_similarity_df,
            topic_similarity_df
        )
        recommendations_dict[user_id] = recommendations

    # 保存推荐结果到Redis
    print("7. 保存推荐结果到Redis...")
    save_recommendations_to_redis(recommendations_dict)

    print("推荐生成完成！")


if __name__ == "__main__":
    main()
