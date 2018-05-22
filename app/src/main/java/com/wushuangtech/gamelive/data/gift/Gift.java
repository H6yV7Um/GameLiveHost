package com.wushuangtech.gamelive.data.gift;

/**
 * 礼物实体类，标记礼物的内容、图片等。
 */
public class Gift {
    /**
     * id : 100001
     * name : 守护之心
     * imgSrc : http://3tdoc.oss-cn-beijing.aliyuncs.com/wechat/avatar/1.jpg
     * price : 10
     * created : 1509605685
     * updated : 1509605685
     */

    private String id;
    private String name;
    private String imgSrc;
    private String price;
    private String created;
    private String updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }


//    private String id;
//    private String typeId;
//    @SerializedName("giftname")
//    private String displayName;
//    @SerializedName("gifticon")
//    private String imageUrl;
//    @SerializedName("needcoin")
//    private int price;   //use int instead of double
//    private int exp;
//    private String isred;
//    private String redId;
//
//    public String getRedId() {
//        return redId;
//    }
//
//    public void setRedId(String redId) {
//        this.redId = redId;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getTypeId() {
//        return typeId;
//    }
//
//    public void setTypeId(String typeId) {
//        this.typeId = typeId;
//    }
//
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
//    }
//
//    public String getImageUrl() {
//        return imageUrl;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }
//
//    public int getPrice() {
//        return price;
//    }
//
//    public void setPrice(int price) {
//        this.price = price;
//    }
//
//    public int getExp() {
//        return exp;
//    }
//
//    public void setExp(int exp) {
//        this.exp = exp;
//    }
//
//    public String getIsred() {
//        return isred;
//    }
//
//    public void setIsred(String isred) {
//        this.isred = isred;
//    }
}