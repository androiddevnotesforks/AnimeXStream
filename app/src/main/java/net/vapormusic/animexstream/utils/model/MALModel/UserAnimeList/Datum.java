
package net.vapormusic.animexstream.utils.model.MALModel.UserAnimeList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

    @SerializedName("node")
    @Expose
    private Node node;
    @SerializedName("list_status")
    @Expose
    private ListStatus listStatus;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public ListStatus getListStatus() {
        return listStatus;
    }

    public void setListStatus(ListStatus listStatus) {
        this.listStatus = listStatus;
    }

}
