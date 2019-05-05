package com.ydhnwb.comodity.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class PopulateImageModel implements Parcelable {

    private String fileName;
    private Uri filePath;
    private String status;

    public PopulateImageModel() {
    }

    public PopulateImageModel(String fileName, Uri filePath, String status) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Uri getFilePath() {
        return filePath;
    }

    public void setFilePath(Uri filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    protected PopulateImageModel(Parcel in) {
        fileName = in.readString();
        filePath = (Uri) in.readValue(Uri.class.getClassLoader());
        status = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeValue(filePath);
        dest.writeString(status);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PopulateImageModel> CREATOR = new Parcelable.Creator<PopulateImageModel>() {
        @Override
        public PopulateImageModel createFromParcel(Parcel in) {
            return new PopulateImageModel(in);
        }

        @Override
        public PopulateImageModel[] newArray(int size) {
            return new PopulateImageModel[size];
        }
    };
}

