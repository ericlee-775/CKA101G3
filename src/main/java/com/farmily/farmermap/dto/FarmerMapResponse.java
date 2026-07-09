package com.farmily.farmermap.dto;

import com.farmily.user.model.CityDistrict;
import com.farmily.user.model.Farmer;

import java.math.BigDecimal;

public class FarmerMapResponse {

    private Integer farmerId;
    private String farmName;
    private String farmAddress;
    private String farmDesc;
    private String farmerPhoneNum;
    private BigDecimal locLat;
    private BigDecimal locLong;
    private Integer districtId;
    private String cityName;
    private String distName;
    private String mapQuery;

    public static FarmerMapResponse from(Farmer farmer) {
        FarmerMapResponse response = new FarmerMapResponse();

        response.farmerId = farmer.getFarmerId();
        response.farmName = farmer.getFarmName();
        response.farmAddress = farmer.getFarmAddress();
        response.farmDesc = farmer.getFarmDesc();
        response.farmerPhoneNum = farmer.getFarmerPhoneNum();
        response.locLat = farmer.getLocLat();
        response.locLong = farmer.getLocLong();

        CityDistrict cityDistrict = farmer.getCityDistrict();
        if (cityDistrict != null) {
            response.districtId = cityDistrict.getDistrictId();
            response.cityName = cityDistrict.getCityName();
            response.distName = cityDistrict.getDistName();
        }

        response.mapQuery = buildMapQuery(response);
        return response;
    }

    // 清單版：不含電話。/api/farms 是公開端點，避免一次把所有小農電話外洩（資料最小化）
    public static FarmerMapResponse fromListItem(Farmer farmer) {
        FarmerMapResponse response = from(farmer);
        response.farmerPhoneNum = null;
        return response;
    }

    private static String buildMapQuery(FarmerMapResponse response) {
        if (response.locLat != null && response.locLong != null) {
            return response.locLat + "," + response.locLong;
        }

        StringBuilder query = new StringBuilder();
        append(query, response.cityName);
        append(query, response.distName);
        append(query, response.farmAddress);
        append(query, response.farmName);
        return query.toString();
    }

    private static void append(StringBuilder query, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!query.isEmpty()) {
            query.append(' ');
        }
        query.append(value);
    }

    public Integer getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Integer farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getFarmAddress() {
        return farmAddress;
    }

    public void setFarmAddress(String farmAddress) {
        this.farmAddress = farmAddress;
    }

    public String getFarmDesc() {
        return farmDesc;
    }

    public void setFarmDesc(String farmDesc) {
        this.farmDesc = farmDesc;
    }

    public String getFarmerPhoneNum() {
        return farmerPhoneNum;
    }

    public void setFarmerPhoneNum(String farmerPhoneNum) {
        this.farmerPhoneNum = farmerPhoneNum;
    }

    public BigDecimal getLocLat() {
        return locLat;
    }

    public void setLocLat(BigDecimal locLat) {
        this.locLat = locLat;
    }

    public BigDecimal getLocLong() {
        return locLong;
    }

    public void setLocLong(BigDecimal locLong) {
        this.locLong = locLong;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public String getMapQuery() {
        return mapQuery;
    }

    public void setMapQuery(String mapQuery) {
        this.mapQuery = mapQuery;
    }
}
