package com.taoxier.smartdochub.document.service.impl;

import com.taoxier.smartdochub.document.model.entity.AsyncTask;
import com.taoxier.smartdochub.document.service.AsyncTaskService;
import com.taoxier.smartdochub.document.mapper.AsyncTaskMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskServiceImpl extends ServiceImpl<AsyncTaskMapper, AsyncTask> implements AsyncTaskService {
}
