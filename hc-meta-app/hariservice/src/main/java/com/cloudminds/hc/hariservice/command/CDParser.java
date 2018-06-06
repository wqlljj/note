package com.cloudminds.hc.hariservice.command;

import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPData;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPEntity;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPEntityBodyHeader;
import com.cloudminds.hc.hariservice.bean.dcProtocol.CDPHeader;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoey on 17/5/5.
 */

public class CDParser {

    public static final String TAG = "HS/CDParser";
    public static CDPData parseData(ByteBuffer dataBuffer){

        CDPData cdpData = new CDPData();

        try {
            //解析header
//            CDPHeader header =  new CDPHeader();
//            short dataType = dataBuffer.getShort();
//            short reverse = dataBuffer.getShort();
//            header.setDatatype(dataType);
//            header.setReserve(reverse);
//            cdpData.setHeader(header);
//            LogUtils.d(TAG,"Data Header dataType:"+dataType+" reverse:"+reverse);

            ArrayList<CDPEntity> enList = new ArrayList<CDPEntity>();
            //读取body
            //while (dataBuffer.position() != dataBuffer.limit()){

                CDPEntity entity =  new CDPEntity();

//                CDPEntityBodyHeader bodyHeader = new CDPEntityBodyHeader();
//                int len = dataBuffer.getInt();
//                short contentType = dataBuffer.getShort();
//                bodyHeader.setContentType(contentType);
//                bodyHeader.setLen(len);
//                entity.setBodyHeader(bodyHeader);
//                LogUtils.d(TAG,"Body Header len:"+len+" contentType:"+contentType);

                //body
                byte[] jsonBytes = new byte[dataBuffer.remaining()];
                dataBuffer.get(jsonBytes);
                String jsonString = new String(jsonBytes);
                entity.setBody(jsonString);
               // LogUtils.d(TAG,"Body content:"+jsonString);

                JSONObject bodyJSON = new JSONObject(jsonString);
                entity.setType(bodyJSON.getString("type"));

                enList.add(entity);
            //}

            cdpData.setBodyList(enList);

        }catch (Exception e){

        }finally {
            return cdpData;
        }
    }

    public static CDPData parseJSON(JSONObject  dataJson){

        CDPData cdpData = new CDPData();

        try {

            ArrayList<CDPEntity> enList = new ArrayList<CDPEntity>();

            CDPEntity entity =  new CDPEntity();

            entity.setBody(dataJson.toString());

            entity.setType(dataJson.getString("type"));

            enList.add(entity);

            cdpData.setBodyList(enList);

        }catch (Exception e){

        }finally {
            return cdpData;
        }
    }
}
