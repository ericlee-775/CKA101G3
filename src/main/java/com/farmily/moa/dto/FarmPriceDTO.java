package com.farmily.moa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// 農業部「農產品批發市場交易行情」單筆資料（只保留顯示菜價會用到的欄位）
// 對應 API：https://data.moa.gov.tw/api/v1/AgriProductsTransType/
// @JsonProperty 把 API 欄位名(大寫/底線) 對到 Java 命名習慣；用不到的欄位由 @JsonIgnoreProperties 自動忽略
@JsonIgnoreProperties(ignoreUnknown = true)
public class FarmPriceDTO {

    @JsonProperty("TransDate")      private String transDate;      // 交易日期(民國年, 例 115.07.15)
    @JsonProperty("CropName")       private String cropName;       // 作物名稱(例 甘藍-初秋)
    @JsonProperty("MarketName")     private String marketName;     // 市場名稱(例 台北一)

    @JsonProperty("Upper_Price")    private Double upperPrice;     // 上價
    @JsonProperty("Middle_Price")   private Double middlePrice;    // 中價
    @JsonProperty("Lower_Price")    private Double lowerPrice;     // 下價
    @JsonProperty("Avg_Price")      private Double avgPrice;       // 平均價 ← 顯示主用
    @JsonProperty("Trans_Quantity") private Double transQuantity;  // 交易量

    // ===== getter / setter =====
    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public Double getUpperPrice() {
        return upperPrice;
    }

    public void setUpperPrice(Double upperPrice) {
        this.upperPrice = upperPrice;
    }

    public Double getMiddlePrice() {
        return middlePrice;
    }

    public void setMiddlePrice(Double middlePrice) {
        this.middlePrice = middlePrice;
    }

    public Double getLowerPrice() {
        return lowerPrice;
    }

    public void setLowerPrice(Double lowerPrice) {
        this.lowerPrice = lowerPrice;
    }

    public Double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public Double getTransQuantity() {
        return transQuantity;
    }

    public void setTransQuantity(Double transQuantity) {
        this.transQuantity = transQuantity;
    }
}
