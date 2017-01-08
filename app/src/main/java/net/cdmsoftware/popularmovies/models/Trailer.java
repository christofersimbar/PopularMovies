package net.cdmsoftware.popularmovies.models;

public class Trailer {
    private String mTitle;
    private String mKey;
    private String mLink;
    private String mImagePath;

    public Trailer(String name,String key) {
        this.mTitle = name;
        this.mKey = key;
        this.mLink = "https://www.youtube.com/embed/" + key;
        this.mImagePath = "http://img.youtube.com/vi/" + key + "/0.jpg";
    }

    public String getKey(){
        return mKey;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getLink(){
        return mLink;
    }

    public String getImagePath(){
        return mImagePath;
    }
}
