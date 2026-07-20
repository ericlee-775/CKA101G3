package com.farmily.user.event;


// 會員註銷帳號事件類別
public class DeleteAccountEvent {

    private final String email;

    public DeleteAccountEvent(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }
}
