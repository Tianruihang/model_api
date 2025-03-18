package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.model.WenDaBody;
import com.mjkj.chatgpt.model.WenDaParam;

public interface WenDaService {
    //调用闻达接口
    WenDaBody getWenDaContent(WenDaParam wenDaParam);
}
