package cn.vika.keycloak.social.util;

import com.thoughtworks.xstream.XStream;
import me.chanjar.weixin.mp.bean.message.*;

/**
 * <p>
 * 微信xml转对象映射
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/09/13 16:58
 */
public class WxMpXStreamUtil {
    public static void setXStreamAlias(XStream xs) {
        xs.alias("xml", WxMpXmlMessage.class);
        xs.aliasField("ToUserName", WxMpXmlMessage.class, "toUser");
        xs.aliasField("FromUserName", WxMpXmlMessage.class, "fromUser");
        xs.aliasField("CreateTime", WxMpXmlMessage.class, "createTime");
        xs.aliasField("MsgType", WxMpXmlMessage.class, "msgType");
        xs.aliasField("Content", WxMpXmlMessage.class, "content");
        xs.aliasField("MenuId", WxMpXmlMessage.class, "menuId");
        xs.aliasField("MsgId", WxMpXmlMessage.class, "msgId");
        xs.aliasField("MsgID", WxMpXmlMessage.class, "msgId");
        xs.aliasField("PicUrl", WxMpXmlMessage.class, "picUrl");
        xs.aliasField("MediaId", WxMpXmlMessage.class, "mediaId");
        xs.aliasField("Format", WxMpXmlMessage.class, "format");
        xs.aliasField("ThumbMediaId", WxMpXmlMessage.class, "thumbMediaId");
        xs.aliasField("Location_X", WxMpXmlMessage.class, "locationX");
        xs.aliasField("Location_Y", WxMpXmlMessage.class, "locationY");
        xs.aliasField("Scale", WxMpXmlMessage.class, "scale");
        xs.aliasField("Label", WxMpXmlMessage.class, "label");
        xs.aliasField("Title", WxMpXmlMessage.class, "title");
        xs.aliasField("Description", WxMpXmlMessage.class, "description");
        xs.aliasField("Url", WxMpXmlMessage.class, "url");
        xs.aliasField("Event", WxMpXmlMessage.class, "event");
        xs.aliasField("EventKey", WxMpXmlMessage.class, "eventKey");
        xs.aliasField("Ticket", WxMpXmlMessage.class, "ticket");
        xs.aliasField("Latitude", WxMpXmlMessage.class, "latitude");
        xs.aliasField("Longitude", WxMpXmlMessage.class, "longitude");
        xs.aliasField("Precision", WxMpXmlMessage.class, "precision");
        xs.aliasField("Recognition", WxMpXmlMessage.class, "recognition");
        xs.aliasField("UnionId", WxMpXmlMessage.class, "unionId");
        xs.aliasField("Status", WxMpXmlMessage.class, "status");
        xs.aliasField("TotalCount", WxMpXmlMessage.class, "totalCount");
        xs.aliasField("FilterCount", WxMpXmlMessage.class, "filterCount");
        xs.aliasField("SentCount", WxMpXmlMessage.class, "sentCount");
        xs.aliasField("ErrorCount", WxMpXmlMessage.class, "errorCount");
        xs.aliasField("KfAccount", WxMpXmlMessage.class, "kfAccount");
        xs.aliasField("ToKfAccount", WxMpXmlMessage.class, "toKfAccount");
        xs.aliasField("FromKfAccount", WxMpXmlMessage.class, "fromKfAccount");
        xs.aliasField("CardId", WxMpXmlMessage.class, "cardId");
        xs.aliasField("FriendUserName", WxMpXmlMessage.class, "friendUserName");
        xs.aliasField("IsGiveByFriend", WxMpXmlMessage.class, "isGiveByFriend");
        xs.aliasField("UserCardCode", WxMpXmlMessage.class, "userCardCode");
        xs.aliasField("OldUserCardCode", WxMpXmlMessage.class, "oldUserCardCode");
        xs.aliasField("OuterId", WxMpXmlMessage.class, "outerId");
        xs.aliasField("IsRestoreMemberCard", WxMpXmlMessage.class, "isRestoreMemberCard");
        xs.aliasField("OuterStr", WxMpXmlMessage.class, "outerStr");
        xs.aliasField("IsReturnBack", WxMpXmlMessage.class, "isReturnBack");
        xs.aliasField("IsChatRoom", WxMpXmlMessage.class, "isChatRoom");
        xs.aliasField("ConsumeSource", WxMpXmlMessage.class, "consumeSource");
        xs.aliasField("LocationName", WxMpXmlMessage.class, "locationName");
        xs.aliasField("StaffOpenId", WxMpXmlMessage.class, "staffOpenId");
        xs.aliasField("VerifyCode", WxMpXmlMessage.class, "verifyCode");
        xs.aliasField("RemarkAmount", WxMpXmlMessage.class, "remarkAmount");
        xs.aliasField("Detail", WxMpXmlMessage.class, "detail");
        xs.aliasField("ModifyBonus", WxMpXmlMessage.class, "modifyBonus");
        xs.aliasField("ModifyBalance", WxMpXmlMessage.class, "modifyBalance");
        xs.aliasField("TransId", WxMpXmlMessage.class, "transId");
        xs.aliasField("LocationId", WxMpXmlMessage.class, "locationId");
        xs.aliasField("Fee", WxMpXmlMessage.class, "fee");
        xs.aliasField("OriginalFee", WxMpXmlMessage.class, "originalFee");

        xs.alias("ScanCodeInfo", ScanCodeInfo.class);
        xs.aliasField("ScanType", ScanCodeInfo.class, "scanType");
        xs.aliasField("ScanResult", ScanCodeInfo.class, "scanResult");
        xs.aliasField("ScanCodeInfo", WxMpXmlMessage.class, "scanCodeInfo");

        xs.alias("SendPicsInfo", SendPicsInfo.class);
        xs.aliasField("PicList", SendPicsInfo.class, "picList");
        xs.aliasField("Count", SendPicsInfo.class, "count");
        xs.aliasField("SendPicsInfo", WxMpXmlMessage.class, "sendPicsInfo");

        xs.alias("SendLocationInfo", SendLocationInfo.class);
        xs.aliasField("Location_X", SendLocationInfo.class, "locationX");
        xs.aliasField("Location_Y", SendLocationInfo.class, "locationY");
        xs.aliasField("Scale", SendLocationInfo.class, "scale");
        xs.aliasField("Label", SendLocationInfo.class, "label");
        xs.aliasField("Poiname", SendLocationInfo.class, "poiName");
        xs.aliasField("SendLocationInfo", WxMpXmlMessage.class, "sendLocationInfo");

        xs.aliasField("RefuseReason", WxMpXmlMessage.class, "refuseReason");
        xs.aliasField("IsRecommendByFriend", WxMpXmlMessage.class, "isRecommendByFriend");
        xs.aliasField("PayFinishTime", WxMpXmlMessage.class, "payFinishTime");
        xs.aliasField("CreateOrderTime", WxMpXmlMessage.class, "createOrderTime");
        xs.aliasField("Desc", WxMpXmlMessage.class, "desc");
        xs.aliasField("FreeCoinCount", WxMpXmlMessage.class, "freeCoinCount");
        xs.aliasField("PayCoinCount", WxMpXmlMessage.class, "payCoinCount");
        xs.aliasField("RefundFreeCoinCount", WxMpXmlMessage.class, "refundFreeCoinCount");
        xs.aliasField("RefundPayCoinCount", WxMpXmlMessage.class, "refundPayCoinCount");
        xs.aliasField("OrderType", WxMpXmlMessage.class, "orderType");
        xs.aliasField("Memo", WxMpXmlMessage.class, "memo");
        xs.aliasField("ReceiptInfo", WxMpXmlMessage.class, "receiptInfo");
        xs.aliasField("UniqId", WxMpXmlMessage.class, "storeUniqId");
        xs.aliasField("PoiId", WxMpXmlMessage.class, "poiId");
        xs.aliasField("Result", WxMpXmlMessage.class, "result");
        xs.aliasField("msg", WxMpXmlMessage.class, "msg");
        xs.aliasField("ExpiredTime", WxMpXmlMessage.class, "expiredTime");
        xs.aliasField("FailTime", WxMpXmlMessage.class, "failTime");
        xs.aliasField("FailReason", WxMpXmlMessage.class, "failReason");
        xs.aliasField("OrderId", WxMpXmlMessage.class, "orderId");
        xs.aliasField("OrderStatus", WxMpXmlMessage.class, "orderStatus");
        xs.aliasField("ProductId", WxMpXmlMessage.class, "productId");
        xs.aliasField("SkuInfo", WxMpXmlMessage.class, "skuInfo");
        xs.aliasField("DeviceType", WxMpXmlMessage.class, "deviceType");
        xs.aliasField("DeviceID", WxMpXmlMessage.class, "deviceId");
        xs.aliasField("SessionID", WxMpXmlMessage.class, "sessionId");
        xs.aliasField("OpenID", WxMpXmlMessage.class, "openId");

        xs.alias("HardWare", HardWare.class);
        xs.aliasField("MessageView", HardWare.class, "messageView");
        xs.aliasField("MessageAction", HardWare.class, "messageAction");
        xs.aliasField("HardWare", WxMpXmlMessage.class, "hardWare");

        xs.aliasField("OpType", WxMpXmlMessage.class, "opType");
        xs.aliasField("DeviceStatus", WxMpXmlMessage.class, "deviceStatus");
        xs.aliasField("SuccTime", WxMpXmlMessage.class, "successTime");
        xs.aliasField("Reason", WxMpXmlMessage.class, "reason");
        xs.aliasField("KeyStandard", WxMpXmlMessage.class, "keyStandard");
        xs.aliasField("KeyStr", WxMpXmlMessage.class, "keyStr");
        xs.aliasField("Country", WxMpXmlMessage.class, "country");
        xs.aliasField("Province", WxMpXmlMessage.class, "province");
        xs.aliasField("City", WxMpXmlMessage.class, "city");
        xs.aliasField("Sex", WxMpXmlMessage.class, "sex");
        xs.aliasField("Scene", WxMpXmlMessage.class, "scene");
        xs.aliasField("ExtInfo", WxMpXmlMessage.class, "extInfo");
        xs.aliasField("RegionCode", WxMpXmlMessage.class, "regionCode");
        xs.aliasField("ReasonMsg", WxMpXmlMessage.class, "reasonMsg");
        xs.aliasField("bizmsgmenuid", WxMpXmlMessage.class, "bizMsgMenuId");
        xs.aliasField("SuccOrderId", WxMpXmlMessage.class, "succOrderId");
        xs.aliasField("FailOrderId", WxMpXmlMessage.class, "failOrderId");
        xs.aliasField("AuthorizeAppId", WxMpXmlMessage.class, "authorizeAppId");
        xs.aliasField("source", WxMpXmlMessage.class, "source");
        xs.aliasField("fpqqlsh", WxMpXmlMessage.class, "fpqqlsh");
        xs.aliasField("nsrsbh", WxMpXmlMessage.class, "nsrsbh");
    }
}
