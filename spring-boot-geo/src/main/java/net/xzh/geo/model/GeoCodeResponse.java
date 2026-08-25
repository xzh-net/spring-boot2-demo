package net.xzh.geo.model;

import lombok.Data;

@Data
public class GeoCodeResponse {
    private String status;
    private String info;
    private String infocode;
    private Regeocode regeocode;

    @Data
    public static class Regeocode {
        private String formattedAddress;
        private AddressComponent addressComponent;
    }

    @Data
    public static class AddressComponent {
        private String country;
        private String province;
        private String city;
        private String district;
        private String township;
        private String neighborhood;
        private String building;
        private String adcode;
        private String citycode;
    }
}