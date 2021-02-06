
package net.xblacky.animexstream.utils.model.MALModel.UserAnimeList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListStatus {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("score")
    @Expose
    private Integer score;
    @SerializedName("num_episodes_watched")
    @Expose
    private Integer numEpisodesWatched;
    @SerializedName("is_rewatching")
    @Expose
    private Boolean isRewatching;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getNumEpisodesWatched() {
        return numEpisodesWatched;
    }

    public void setNumEpisodesWatched(Integer numEpisodesWatched) {
        this.numEpisodesWatched = numEpisodesWatched;
    }

    public Boolean getIsRewatching() {
        return isRewatching;
    }

    public void setIsRewatching(Boolean isRewatching) {
        this.isRewatching = isRewatching;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
