package com.farmily.user.dto;

import com.farmily.user.model.CityDistrict;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

//會員端 dto (/api/users)
public class UserUpdateRequest {

    @Size(max = 100, message = "姓名最多 100 字")
    private String userName;
    @Size(max = 100, message = "暱稱最多 100 字")
    private String userNickname;
    // 選填；有填才驗手機格式（09 開頭共 10 碼）
    @Pattern(regexp = "^(09\\d{8})?$", message = "手機號碼格式錯誤（需為 09 開頭共 10 碼）")
    private String userPhoneNum;
    private CityDistrict cityDistrict;
    @Size(max = 100, message = "地址最多 100 字")
    private String userAddress;
    private Integer districtId;
    @Past(message = "生日必須是過去日期")
    private LocalDate birthday;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public CityDistrict getCityDistrict() {
        return cityDistrict;
    }

    public void setCityDistrict(CityDistrict cityDistrict) {
        this.cityDistrict = cityDistrict;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
