/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.cloudminds.hc.hariservice.webrtc;

import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.StatsReport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class CallStatsHelper {

    final private static String SSRC_DIRECTION_RECV="recv";
    final private static String SSRC_DIRECTION_SEND="send";
    final private static String STATS_TYPE_SSRC="ssrc";
    final private static String STATS_ITEM_KEY_PACKETS_LOST="packetsLost";
    final private static String STATS_ITEM_KEY_PACKET_RECEIVED="packetsReceived";
    final private static String STATS_ITEM_KEY_PACKET_SENT="packetsSent";
    final private static String STATS_ITEM_KEY_GOOG_CURRENT_DELAY_MS="googCurrentDelayMs";
    final private static String STATS_ITEM_KEY_GOOG_JITTER_BUFFER_MS="googJitterBufferMs";
    final private static String STATS_ITEM_KEY_GOOG_JITTER_RECEIVED="googJitterReceived";
    final private static String STATS_ITEM_KEY_GOOG_RTT="googRtt";

    final private static String STATS_ITEM_KEY_GOOG_FRAMERATE_SENT="googFrameRateSent";
    final private static String STATS_ITEM_KEY_GOOG_CODEC_NAME="googCodecName";

    final private static String STATS_ITEM_KEY_GOOG_FRAMERATE_RECEIVED="googFrameRateReceived";

    final private static String STATS_ITEM_KEY_GOOG_ACTUAL_BITRATE = "googActualEncBitrate";
    final private static String STATS_ID_BWEVIDEO = "bweforvideo";

//    final private static String STATS_ITEM_KEY_GOOG_TRACKID="googTrackId";
//    final private static String STATS_AUDIO_TRACK_ID="ARDAMSa0";
//    final private static String STATS_VIDEO_TRACK_ID="ARDAMSv0";
    final private static String STATS_ITEM_KEY_GOOG_TRACKID="mediaType";
    final private static String STATS_AUDIO_TRACK_ID="audio";
    final private static String STATS_VIDEO_TRACK_ID="video";

    final public static int AUDIO_TRACK_ID=0;
    final public static int VIDEO_TRACK_ID=1;

    final private static String UNKNOWN_VALUE="N/A";


    public static class NetworkQualityReport{
        String direction;
        String packetLost;
        String packets;
        String currentDelay;
        String jitterBufferMs;
        String jitter;
        String rtt;
        String codecName;
        String fps;
        String actualBitrate;

        public String getCurrentDelay(){
            return currentDelay;
        }

        public String getCurrentBitrate(){
            return actualBitrate;
        }

        public NetworkQualityReport(){
            fps = UNKNOWN_VALUE;
            codecName = UNKNOWN_VALUE;
            rtt = UNKNOWN_VALUE;
            jitter =UNKNOWN_VALUE;
            jitterBufferMs = UNKNOWN_VALUE;
            currentDelay = UNKNOWN_VALUE;
        }

        public String toString(){
            StringBuilder sb = new StringBuilder();
            //sb.append("NetStat[");
            //sb.append("\ndirection:"+direction);
            sb.append("\npacketLost:"+packetLost);
            sb.append("\npackets:"+packets);
            sb.append("\ncurrentDelay:"+currentDelay);
            sb.append("\njitterBufferMs:"+jitterBufferMs);
            sb.append("\njitter:"+jitter);
            //sb.append("\nrtt:"+rtt);
            //sb.append("\nfps:"+fps);
            //sb.append("\ncodecName:"+codecName);
            //sb.append("]");
            return sb.toString();
        }

        private String toJsonString(){
            JSONObject object = new JSONObject();
            try {
                object.put("network","wifi");
                object.put("packetLost",packetLost);
                object.put("packets",packets);
                object.put("currentDelay",currentDelay);
                object.put("jitterBufferMs",jitterBufferMs);
                object.put("actualBitrate",actualBitrate);
                object.put("jitter",jitter);
            }catch (JSONException e){
                e.printStackTrace();
                return "";
            }

            String string  = object.toString();
            return object.toString();
        }

        public static  String getFormatedHeaderString(){
            StringBuilder sb = new StringBuilder();

            sb.append("TimeInMs,");
            sb.append("\tDirection,");
            sb.append("\tpacketLost,");
            sb.append("\tpackets,");
            sb.append("\tcurrentDelay,");
            sb.append("\tjitterBufferMs,");
            sb.append("\tjitter,");
            sb.append("\trtt");
            sb.append("\tfps");
            sb.append("\tcodecName");
            sb.append("\r\n");
            return sb.toString();
        }

        public String getFormatedString(){
            StringBuilder sb = new StringBuilder();
            long dateTime = System.currentTimeMillis();
            sb.append(dateTime+",");
            sb.append(direction+",");
            sb.append(packetLost+",");
            sb.append(packets+",");
            sb.append(currentDelay+",");
            sb.append(jitterBufferMs+",");
            sb.append(jitter+",");
            sb.append(rtt+",");
            sb.append(fps+",");
            sb.append(codecName+"");
            sb.append("\r\n");
            return sb.toString();
        }
    }

    public static String formatStatsReport(List<NetworkQualityReport> reports){
        String ret = "network:wifi";
        if(reports!=null) {
            for (NetworkQualityReport report: reports){
                ret = report.toJsonString();
            }
        }
        return  ret;
    }


    public static String formatStatsReport(int track, StatsReport[] reports){
        String ret = "";
        List<NetworkQualityReport> qualityReports = parseStatsReport(track, reports);
        ret = formatStatsReport(qualityReports);
        return  ret;
    }

    public static  List<NetworkQualityReport> parseStatsReport(int track, StatsReport[] reports) {
        String actualBitrate = "";
        List<NetworkQualityReport> qualityReports = new ArrayList<NetworkQualityReport>();
        for(StatsReport report:reports){
            if (report.id.equals(STATS_ID_BWEVIDEO)){
                for(StatsReport.Value kv: report.values){
                    if (STATS_ITEM_KEY_GOOG_ACTUAL_BITRATE.equals(kv.name)){
                        actualBitrate = kv.value;
                        break;
                    }
                }
                continue;
            }
            if(STATS_TYPE_SSRC.equals(report.type)){
                NetworkQualityReport netReport = new NetworkQualityReport();
                String direction = SSRC_DIRECTION_RECV;
                int packets = 0;
                if(report.id.endsWith(SSRC_DIRECTION_SEND)){
                    direction = SSRC_DIRECTION_SEND;
                    continue;
                }
                netReport.direction = direction;
                netReport.codecName = UNKNOWN_VALUE;
                netReport.fps = UNKNOWN_VALUE;
                String trackId = UNKNOWN_VALUE;

                for(StatsReport.Value kv: report.values){
                    if(STATS_ITEM_KEY_PACKETS_LOST.equals(kv.name)){
                        netReport.packetLost = kv.value;
                    } else if(STATS_ITEM_KEY_PACKET_RECEIVED.equals(kv.name)){
                        netReport.packets = kv.value;
                    } else if(STATS_ITEM_KEY_PACKET_SENT.equals(kv.name)){
                        netReport.packets = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_CURRENT_DELAY_MS.equals(kv.name)){
                        netReport.currentDelay = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_JITTER_BUFFER_MS.equals(kv.name)){
                        netReport.jitterBufferMs = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_JITTER_RECEIVED.equals(kv.name)){
                        netReport.jitter = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_RTT.equals(kv.name)){
                        netReport.rtt = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_CODEC_NAME.equals(kv.name)){
                        netReport.codecName = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_FRAMERATE_SENT.equals(kv.name)){
                        netReport.fps = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_FRAMERATE_RECEIVED.equals(kv.name)){
                        netReport.fps = kv.value;
                    } else if(STATS_ITEM_KEY_GOOG_TRACKID.equals(kv.name)){
                        trackId = kv.value;
                    }
                }
                if(isExpectedTrack(track,trackId))
                    qualityReports.add(netReport);
            }
        }

        for (NetworkQualityReport report: qualityReports){
            report.actualBitrate = actualBitrate;
        }

        return qualityReports;
    }

    public static int getTrackId(boolean isVideoEnabled){
        int ret = AUDIO_TRACK_ID;

        if(isVideoEnabled) ret = VIDEO_TRACK_ID;

        return ret;
    }

    private static boolean isExpectedTrack(int track, String trackId){
        boolean ret = false;

        if(track==AUDIO_TRACK_ID){
            if(STATS_AUDIO_TRACK_ID.equalsIgnoreCase(trackId))
                ret = true;
        }
        if(track==VIDEO_TRACK_ID){
            if(STATS_VIDEO_TRACK_ID.equalsIgnoreCase(trackId))
                ret = true;
        }

        return ret;
    }
}
