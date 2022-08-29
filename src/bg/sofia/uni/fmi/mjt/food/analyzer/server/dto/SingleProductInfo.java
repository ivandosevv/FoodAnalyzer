package bg.sofia.uni.fmi.mjt.food.analyzer.server.dto;

public class SingleProductInfo {
    private int fdcId;

    private String publicationDate;

    private double calories;

    public SingleProductInfo(int fdcId, String publicationDate, double calories) {
        this.fdcId = fdcId;
        this.publicationDate = publicationDate;
        this.calories = calories;
    }

    public int getFdcId() {
        return fdcId;
    }

    public void setFdcId(int fdcId) {
        this.fdcId = fdcId;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }
}
