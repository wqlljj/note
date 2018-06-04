package com.example.wangqi.mvvm.model.bean;

/**
 * Created by SX on 2017/4/13.
 */

public class UserInfo extends Response {

    /**
     * data : {"name":"String","phoneNumber":"String","address":"String","emergencyContact":{"name":"String","phoneNumber":"String","address":"String"}}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * name : String
         * phoneNumber : String
         * address : String
         * emergencyContact : {"name":"String","phoneNumber":"String","address":"String"}
         */

        private String name;
        private String phoneNumber;
        private String address;
        private String loginId;
        private EmergencyContactBean emergencyContact;

        public String getloginId() {
            return loginId;
        }

        public void setloginId(String loginId) {
            this.loginId = loginId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public EmergencyContactBean getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(EmergencyContactBean emergencyContact) {
            this.emergencyContact = emergencyContact;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "name='" + name + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", address='" + address + '\'' +
                    ", loginId='" + loginId + '\'' +
                    ", emergencyContact=" + emergencyContact +
                    '}';
        }

        public static class EmergencyContactBean {
            /**
             * name : String
             * phoneNumber : String
             * address : String
             */

            private String name;
            private String phoneNumber;
            private String address;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPhoneNumber() {
                return phoneNumber;
            }

            public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            @Override
            public String toString() {
                return "EmergencyContactBean{" +
                        "name='" + name + '\'' +
                        ", phoneNumber='" + phoneNumber + '\'' +
                        ", address='" + address + '\'' +
                        '}';
            }
        }
    }

    @Override
    public String toString() {
        return "UserInfo{" +super.toString()+
                "data=" + data +
                '}';
    }
}
