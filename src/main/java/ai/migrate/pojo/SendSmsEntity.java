package ai.migrate.pojo;

public class SendSmsEntity {
    private String phoneNumber;
    private String username;
    private String company;
    private String code;
    private Integer type;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "SendSmsEntity [phoneNumber=" + phoneNumber + ", username=" + username + ", company=" + company
                + ", code=" + code + ", type=" + type + "]";
    }
}
