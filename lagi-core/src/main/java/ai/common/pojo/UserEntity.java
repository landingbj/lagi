package ai.common.pojo;

import java.util.Date;

public class UserEntity {
    private Integer id;
    private String category;
    private Date categoryCreateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCategoryCreateTime() {
        return categoryCreateTime;
    }

    public void setCategoryCreateTime(Date categoryCreateTime) {
        this.categoryCreateTime = categoryCreateTime;
    }

    @Override
    public String toString() {
        return "UserEntity [id=" + id + ", category=" + category + ", categoryCreateTime=" + categoryCreateTime + "]";
    }
}
