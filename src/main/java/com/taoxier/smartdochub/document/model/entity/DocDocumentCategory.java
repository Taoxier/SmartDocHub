package com.taoxier.smartdochub.document.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doc_document_category")
public class DocDocumentCategory {

    private Long documentId;

    private Long categoryId;

}
