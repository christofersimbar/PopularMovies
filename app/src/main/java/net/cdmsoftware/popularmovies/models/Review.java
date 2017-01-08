package net.cdmsoftware.popularmovies.models;

public class Review {
    private String mAuthor;
    private String mContent;

    public Review(String author,String content) {
        this.mAuthor = author;
        this.mContent = content;
    }

    public String getAuthor(){
        return mAuthor;
    }

    public String getContent(){
        return mContent;
    }
}
