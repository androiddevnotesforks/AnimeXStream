
package net.vapormusic.animexstream.utils.model.MALModel.UserAnimeList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainPicture {

    @SerializedName("medium")
    @Expose
    private String medium;
    @SerializedName("large")
    @Expose
    private String large;

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

}
