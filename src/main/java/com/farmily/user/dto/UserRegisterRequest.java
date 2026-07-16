package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

//會員請求端 dto (/api/member)
public class UserRegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 60, message = "密碼長度需 8~60 字")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密碼需同時包含英文字母與數字")
    private String password;        // 本地註冊一定要填

    @NotBlank
    @Size(max = 100, message = "姓名最多 100 字")
    private String userName;

    @Size(max = 100, message = "暱稱最多 100 字")
    private String userNickname;

    private Integer districtId;       // 只收 id,不收整個 CityDistrict

    @Size(max = 100, message = "地址最多 100 字")
    private String userAddress;

    // 選填；有填才驗手機格式（09 開頭共 10 碼）
    @Pattern(regexp = "^(09\\d{8})?$", message = "手機號碼格式錯誤（需為 09 開頭共 10 碼）")
    private String userPhoneNum;

    @Past(message = "生日必須是過去日期")
    private LocalDate birthday;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
