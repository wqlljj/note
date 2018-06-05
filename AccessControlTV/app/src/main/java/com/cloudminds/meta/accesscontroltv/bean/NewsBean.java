package com.cloudminds.meta.accesscontroltv.bean;

import java.util.List;

/**
 * Created by WQ on 2018/4/23.
 */

public class NewsBean {

    /**
     * data : [{"id":2,"title":"aaaaaa","pubtime":"2018-04-23T16:03:40+08:00"},{"id":3,"title":"bbbbb","pubtime":"2018-04-23T16:03:45+08:00"},{"id":4,"title":"cccccc","pubtime":"2018-04-23T16:03:47+08:00"},{"id":5,"title":"ddddd","pubtime":"2018-04-23T16:03:55+08:00"},{"id":7,"title":"ffffff","pubtime":"2018-04-23T16:03:57+08:00"}]
     * msg : 1-ok
     * status : true
     */

    private String msg;
    private boolean status;
    private List<DataBean> data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 2
         * title : aaaaaa
         * pubtime : 2018-04-23T16:03:40+08:00
         */

        private int id;
        private String title;
        private String pubtime;

        public DataBean() {
        }

        public DataBean(String title) {
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPubtime() {
            return pubtime;
        }

        public void setPubtime(String pubtime) {
            this.pubtime = pubtime;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", pubtime='" + pubtime + '\'' +
                    "}\n";
        }
    }

    @Override
    public String toString() {
        return "NewsBean{" +
                "msg='" + msg + '\'' +
                ", status=" + status +
                ", data=" + data +
                '}';
    }
}
