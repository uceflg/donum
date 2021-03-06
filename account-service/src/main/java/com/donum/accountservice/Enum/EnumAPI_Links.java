package com.donum.accountservice.Enum;

public enum EnumAPI_Links {

    GOOGLE_API("http://localhost:8110/"),
    POSTCODE_API("https://api.postcodes.io/"),
    API_KEYS("http://18.130.137.35:5333/get-key/");

    private String txt;

    EnumAPI_Links(String txt) {
        this.txt = txt;
    }

    @Override
    public String toString() {
        return txt;
    }
}
