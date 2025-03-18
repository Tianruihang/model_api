package com.mjkj.chatgpt.model;

import lombok.Data;

@Data
public class WenDaBody {
    //        "title": "办理增加多人口阶梯水量业务？",
    //        "content": "新区居民用水阶梯是按照默认3人口用量标准设置，如您申请办理增加阶梯水量业务，每增加一人口，第一阶梯用量增加36吨，第二阶梯用量增加20吨。您可携带以下资料前往营业网点办理：居住地所属社区开具的居住证明、房屋产权证明、户口本、业主身份证、非业主本人办理还需代办人身份证和授权委托书。",
    //        "score": 134
    private String title; //标题
    private String content; //内容
    private int score; //分数
}
