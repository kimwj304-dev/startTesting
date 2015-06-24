package com.example.kimwj.smsimporter;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by ChonamDoo on 2015-04-03.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static SmsReceiver sInstance;

    public static SmsReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new SmsReceiver();
        }
        return sInstance;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        int error = intent.getIntExtra("errorCode", 0);
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        String format = intent.getStringExtra("format");
        Uri messageUri = insertMessage(context, msgs, error, format);
    }

    private Uri insertMessage(Context context, SmsMessage[] msgs, int error, String format) {
        // Build the helper classes to parse the messages.
        SmsMessage sms = msgs[0];
        return storeMessage(context, msgs, error);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Uri storeMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];

        // Store the message in the content provider.
        ContentValues values = extractContentValues(sms);
        values.put(Telephony.Sms.ERROR_CODE, error);
        int pduCount = msgs.length;

        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            values.put(Sms.Inbox.BODY, replaceFormFeeds(sms.getDisplayMessageBody()));
        } else {
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                if (!TextUtils.isEmpty(sms.getDisplayMessageBody())) {
                    body.append(sms.getDisplayMessageBody());
                }
            }
            values.put(Sms.Inbox.BODY, replaceFormFeeds(body.toString()));
        }

        Uri insertedUri = context.getContentResolver().insert(Sms.CONTENT_URI, values);
        // Now make sure we're not over the limit in stored messages
//        Recycler.getSmsRecycler().deleteOldMessagesByThreadId(context, threadId);
//        MmsWidgetProvider.notifyDatasetChanged(context);

//        LogUtils.v("KIMWJ", "sms.getOriginatingAddress() : " + sms.getOriginatingAddress() + "  msg : " + sms.getMessageBody());
//        if(Global.whisperActivity != null){
//
//            sendToWhisperPopup(context, sms.getOriginatingAddress(), sms.getMessageBody());
//        }

        return insertedUri;
    }

    private ContentValues extractContentValues(SmsMessage sms) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put(Telephony.Sms.Inbox.ADDRESS, sms.getDisplayOriginatingAddress());

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        // Check to make sure the system is giving us a non-bogus time.
        Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
        Calendar nowDate = new GregorianCalendar();
        long now = System.currentTimeMillis();
        nowDate.setTimeInMillis(now);

        if (nowDate.before(buildDate)) {
            // It looks like our system clock isn't set yet because the current time right now
            // is before an arbitrary time we made this build. Instead of inserting a bogus
            // receive time in this case, use the timestamp of when the message was sent.
            now = sms.getTimestampMillis();
        }

        values.put(Telephony.Sms.Inbox.DATE, new Long(now));
        values.put(Telephony.Sms.Inbox.DATE_SENT, Long.valueOf(sms.getTimestampMillis()));
        values.put(Telephony.Sms.Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Telephony.Sms.Inbox.READ, 0);
        values.put(Telephony.Sms.Inbox.SEEN, 0);
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Telephony.Sms.Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Telephony.Sms.Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Telephony.Sms.Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

    public static String replaceFormFeeds(String s) {
        // Some providers send formfeeds in their messages. Convert those formfeeds to newlines.
        return s == null ? "" : s.replace('\f', '\n');
    }


    private String sender_phone_num;

//    private void sendToWhisperPopup(Context context, String phoneNumber, String message){
//        String interPhoneNumber = Utils.replaceInternationalPhoneNumber(context, phoneNumber);
//        String interPopupPhoneNumber = Utils.replaceInternationalPhoneNumber(context, sender_phone_num);
//        if(!interPhoneNumber.equals(interPopupPhoneNumber)) {
//            return;
//        }
//
//        phoneNumber = Utils.replaceInternationalPhoneNumber(context, phoneNumber);
//        ContactInfo contact = ContactDBUtils.getInstance(context).getContactInfo(phoneNumber);
//
//        WhisperMessage whisperMessage = new WhisperMessage.Builder()
//                .setType(WhisperMessage.MESSAGE_TYPE_SMS_RECEIVE)
//                .setReceiveNumber(Global.getUserPhoneNumber())
//                .setSendNumber(phoneNumber)
//                .setMessageId(System.currentTimeMillis())
//                .setTime(System.currentTimeMillis())
//                .setMessage(message)
//                .setReceiveMuid(Global.getURID())
//                .setSendMuid(contact != null ? contact.getURID() : "")
//                .build();
//        WhisperMessageDataUtil.getInstance(context).insertWhisperMessage(whisperMessage);
//        EventBus.getDefault().post(whisperMessage);
//
//        new HistoryDBUtils(context).putWhisperMessageToHistoryDB(whisperMessage, true);
//    }
//
//    public void setSenderPhonenumber(String number) {
//        sender_phone_num = number;
//    }
}
