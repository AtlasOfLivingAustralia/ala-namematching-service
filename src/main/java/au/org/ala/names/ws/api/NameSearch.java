package au.org.ala.names.ws.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NameSearch {

    private boolean success;
    private String cleanName;
    private String matchType;
    private String guid;
    private String left;
    private String right;
    private String acceptedGuid;
    private String rank;

    public NameSearch() {}

    public NameSearch(boolean success, String cleanName, String matchType, String guid, String acceptedGuid, String rank, String left, String right) {
        this.success = success;
        this.cleanName = cleanName;
        this.matchType = matchType;
        this.guid = guid;
        this.acceptedGuid = acceptedGuid;
        this.rank = rank;
        this.left = left;
        this.right = right;
    }

    public NameSearch(boolean success){
        this.success = success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @JsonProperty
    public String getCleanName() {
        return cleanName;
    }

    public void setCleanName(String cleanName) {
        this.cleanName = cleanName;
    }

    @JsonProperty
    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setRight(String right) {
        this.right = right;
    }

    @JsonProperty
    public String getAcceptedGuid() {
        return acceptedGuid;
    }

    public void setAcceptedGuid(String acceptedGuid) {
        this.acceptedGuid = acceptedGuid;
    }

    @JsonProperty
    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }


    @JsonProperty
    public boolean getSuccess() {
        return success;
    }

    @JsonProperty
    public String getGuid() {
        return guid;
    }

    public boolean isSuccess() {
        return success;
    }

    @JsonProperty
    public String getLeft() {
        return left;
    }

    @JsonProperty
    public String getRight() {
        return right;
    }
}
