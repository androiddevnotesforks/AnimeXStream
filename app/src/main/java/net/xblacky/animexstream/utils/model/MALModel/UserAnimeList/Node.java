
package net.xblacky.animexstream.utils.model.MALModel.UserAnimeList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Node {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("main_picture")
    @Expose
    private MainPicture mainPicture;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MainPicture getMainPicture() {
        return mainPicture;
    }

    public void setMainPicture(MainPicture mainPicture) {
        this.mainPicture = mainPicture;
    }

}
