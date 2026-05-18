package com.example.productmanagement.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用户状态变更原因常量
 */
public final class UserStatusReasonConstants {

    private UserStatusReasonConstants() {
    }

    public static final String ACTION_FREEZE = "FREEZE";
    public static final String ACTION_UNFREEZE = "UNFREEZE";

    public static final Map<String, String> FREEZE_REASON_MAP = new LinkedHashMap<>();
    public static final Map<String, String> UNFREEZE_REASON_MAP = new LinkedHashMap<>();

    static {
        FREEZE_REASON_MAP.put("VIOLATION_REVIEW", "违规评论/违规内容");
        FREEZE_REASON_MAP.put("MALICIOUS_ORDER", "恶意下单/刷单");
        FREEZE_REASON_MAP.put("ACCOUNT_RISK", "账号风险异常");
        FREEZE_REASON_MAP.put("COMPLAINT_VERIFIED", "投诉核实成立");
        FREEZE_REASON_MAP.put("MANUAL_CONTROL", "人工风控处置");
        FREEZE_REASON_MAP.put("OTHER", "其他");

        UNFREEZE_REASON_MAP.put("APPEAL_APPROVED", "申诉通过");
        UNFREEZE_REASON_MAP.put("MANUAL_REVIEW_PASSED", "人工复核通过");
        UNFREEZE_REASON_MAP.put("MISJUDGMENT_CORRECTION", "误封修正");
        UNFREEZE_REASON_MAP.put("RISK_RELEASED", "风险解除");
        UNFREEZE_REASON_MAP.put("MANUAL_RECOVERY", "人工恢复");
        UNFREEZE_REASON_MAP.put("OTHER", "其他");
    }

    public static Set<String> getFreezeReasonTypes() {
        return FREEZE_REASON_MAP.keySet();
    }

    public static Set<String> getUnfreezeReasonTypes() {
        return UNFREEZE_REASON_MAP.keySet();
    }

    public static String getReasonName(String action, String reasonType) {
        if (ACTION_FREEZE.equals(action)) {
            return FREEZE_REASON_MAP.get(reasonType);
        }
        if (ACTION_UNFREEZE.equals(action)) {
            return UNFREEZE_REASON_MAP.get(reasonType);
        }
        return null;
    }
}
