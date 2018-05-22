package com.wushuangtech.gamelive.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mrliu on 2018/1/16.
 * 此类用于:
 */

public class PublishRoomIdBean {

    /**
     * liveId : 1000083
     * pushRtmp : rtmp://video-center-bj.alivecdn.com/3ttechlive/1000083?vhost=ali.push.cdn.3ttechlive.3ttech.cn&auth_key=1523178006-0-0-2f75077587cfe0fd8dfb27e98d1034cb
     * wsServer : {"code":0,"msg":"success","data":{"roomServer":{"host":"118.25.15.37","port":"9505"},"roomServer-wss":{"host":"118.25.15.37","port":"9506"}}}
     * imgSrc :
     */

    private int liveId;
    private String pushRtmp;
    private WsServerBean wsServer;
    private String imgSrc;

    @Override
    public String toString() {
        return "PublishRoomIdBean{" +
                "liveId=" + liveId +
                ", pushRtmp='" + pushRtmp + '\'' +
                ", wsServer=" + wsServer +
                ", imgSrc='" + imgSrc + '\'' +
                '}';
    }

    public int getLiveId() {
        return liveId;
    }

    public void setLiveId(int liveId) {
        this.liveId = liveId;
    }

    public String getPushRtmp() {
        return pushRtmp;
    }

    public void setPushRtmp(String pushRtmp) {
        this.pushRtmp = pushRtmp;
    }

    public WsServerBean getWsServer() {
        return wsServer;
    }

    public void setWsServer(WsServerBean wsServer) {
        this.wsServer = wsServer;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public static class WsServerBean {
        /**
         * code : 0
         * msg : success
         * data : {"roomServer":{"host":"118.25.15.37","port":"9505"},"roomServer-wss":{"host":"118.25.15.37","port":"9506"}}
         */

        private int code;
        private String msg;
        private DataBean data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public DataBean getData() {
            return data;
        }

        public void setData(DataBean data) {
            this.data = data;
        }


        @Override
        public String toString() {
            return "WsServerBean{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", data=" + data +
                    '}';
        }

        public static class DataBean {
            /**
             * roomServer : {"host":"118.25.15.37","port":"9505"}
             * roomServer-wss : {"host":"118.25.15.37","port":"9506"}
             */

            private RoomServerBean roomServer;
            @SerializedName("roomServer-wss")
            private RoomServerwssBean roomServerwss;

            public RoomServerBean getRoomServer() {
                return roomServer;
            }

            public void setRoomServer(RoomServerBean roomServer) {
                this.roomServer = roomServer;
            }

            public RoomServerwssBean getRoomServerwss() {
                return roomServerwss;
            }

            public void setRoomServerwss(RoomServerwssBean roomServerwss) {
                this.roomServerwss = roomServerwss;
            }

            @Override
            public String toString() {
                return "DataBean{" +
                        "roomServer=" + roomServer +
                        ", roomServerwss=" + roomServerwss +
                        '}';
            }

            public static class RoomServerBean {
                /**
                 * host : 118.25.15.37
                 * port : 9505
                 */

                private String host;
                private String port;

                public String getHost() {
                    return host;
                }

                public void setHost(String host) {
                    this.host = host;
                }

                public String getPort() {
                    return port;
                }

                public void setPort(String port) {
                    this.port = port;
                }

                @Override
                public String toString() {
                    return "RoomServerBean{" +
                            "host='" + host + '\'' +
                            ", port='" + port + '\'' +
                            '}';
                }
            }

            public static class RoomServerwssBean {
                /**
                 * host : 118.25.15.37
                 * port : 9506
                 */

                private String host;
                private String port;

                public String getHost() {
                    return host;
                }

                public void setHost(String host) {
                    this.host = host;
                }

                public String getPort() {
                    return port;
                }

                public void setPort(String port) {
                    this.port = port;
                }


                @Override
                public String toString() {
                    return "RoomServerwssBean{" +
                            "host='" + host + '\'' +
                            ", port='" + port + '\'' +
                            '}';
                }
            }
        }
    }
}
