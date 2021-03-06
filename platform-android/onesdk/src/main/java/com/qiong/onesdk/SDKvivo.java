package com.qiong.onesdk;

import android.content.Intent;
import android.os.Bundle;

import com.vivo.sdkplugin.aidl.VivoUnionManager;
import com.vivo.sdkplugin.accounts.OnVivoAccountChangedListener;
import com.bbk.payment.payment.OnVivoPayResultListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sric0880 on 2016/11/22.
 */

public class SDKvivo extends SDK {
    private static final String appid = "";
    private VivoUnionManager  mVivoUnionManager;

    public SDKvivo(OneSDK oneSDK){
        super(oneSDK);
    }

    OnVivoAccountChangedListener  accountListener = new OnVivoAccountChangedListener() {
        //通过该方法获取用户信息
        @Override
        public void onAccountLogin(String name, String openid, String authtoken) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("openid", openid);
                jsonObject.put("token", authtoken);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //TODO
//            mLoginCb.succeed();
        }
        //第三方游戏不需要使用此回调方法
        @Override
        public void onAccountRemove(boolean  isRemoved) {
        }
        @Override
        //取消登录的回调方法
        public void onAccountLoginCancled() {
            mLoginCb.cancelled();
        }
    };

    OnVivoPayResultListener  payResultListener = new  OnVivoPayResultListener() {
        //通过该回调方法获取支付结果
        @Override
        public void payResult (String transNo, boolean pay_result,
                               String result_code, String pay_msg) {
            // transNo: 交易编号
            // pay_result:交易结果
            // result_code：状态码（参考附录“状态码(res_code)及描述”）
            // pay_msg:结果描述
            if (result_code == "9000"){
                mPayCb.succeed(mPayOrder, "");
            }
            else{
                mPayCb.failed(mPayOrder, pay_msg);
            }
        }
        //第三方游戏不需要使用此回调方法
        @Override
        public  void rechargeResult (String openid, boolean pay_result,
                                     String result_code, String pay_msg) {
        }
    };

    @Override
    public void onCreate() {
        mVivoUnionManager = new VivoUnionManager(this.mActivity);
        mVivoUnionManager.registVivoAccountChangeListener(accountListener);
        mVivoUnionManager.bindUnionService();
        mVivoUnionManager.initVivoPaymentAndRecharge(this.mActivity,  payResultListener);
    }

    @Override
    public void onResume() {
        mVivoUnionManager.showVivoAssitView(this.mActivity);
    }

    @Override
    public void onDestroy() {
        mVivoUnionManager.unRegistVivoAccountChangeListener(accountListener);
        mVivoUnionManager.hideVivoAssitView(this.mActivity);
        mVivoUnionManager.cancelVivoPaymentAndRecharge(payResultListener);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void login() {
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVivoUnionManager.startLogin(appid);
            }
        });
    }

    @Override
    public void pay(String data) {
        try {
            if (data != null && data.length() > 0) {
//                    Toast.makeText(UnityPlayer.currentActivity, data, Toast.LENGTH_SHORT).show();
                JSONObject json = new JSONObject(data);
                if (null != json) {
                    String name = json.getString("productName");
                    String description = json.getString("productDes");
                    long moneyInFen = json.getLong("moneyInFen");
                    String gameTradeNo = json.getString("tradeNo");
                    String accessKey = json.getString("accessKey");
                    Bundle localBundle = new Bundle();
                    localBundle.putString("transNo", gameTradeNo); //订单推送接口返回的vivo订单号
                    localBundle.putString("accessKey", accessKey); //订单推送接口返回的accessKey
                    localBundle.putString("appId", appid); //在vivo开发者平台注册应用后获取到的appId
                    localBundle.putString("productName", name); //商品名称
                    localBundle.putString("productDes", description);//商品描述
                    localBundle.putLong("price", moneyInFen);//商品价格，单位为分（1000即10.00元）
                    // 以下为可选参数，能收集到务必填写，如未填写，掉单、用户密码找回等问题可能无法解决。
//                    localBundle.putString("blance", "100元宝");
//                    localBundle.putString("party", "工会");
//                    localBundle.putString("roleId", "角色id");
//                    localBundle.putString("roleName", "角色名称角色名称角色名称");
                    localBundle.putBoolean("logOnOff", true); // CP在接入过程请传true值,接入完成后在改为false, 传true会在支付SDK打印大量日志信息
                    //调用支付接口进行支付
                    mVivoUnionManager.payment(this.mActivity, localBundle);
                    mPayOrder = gameTradeNo;
                } else {
                    mPayCb.failed(mPayOrder, "返回订单数据格式错误");
                }
            } else {
                mPayCb.failed(mPayOrder, "返回订单数据格式错误");
            }
        }
        catch (Exception e)
        {
            mPayCb.failed(mPayOrder, "返回订单数据格式错误");
        }
    }

    @Override
    public String getChannelStr()
    {
        return "vivo";
    }
}
