package bg.sofia.uni.fmi.mjt.food.analyzer.server.dto;

public class SingleProductInfo {
    private int fdcId;

    private String description;

    private String gtinUpc;

    private String publicationDate;

    private double calories;

    public SingleProductInfo(int fdcId, String description, String gtinUpc, String publicationDate, double calories) {
        this.fdcId = fdcId;
        this.description = description;
        this.gtinUpc = gtinUpc;
        this.publicationDate = publicationDate;
        this.calories = calories;
    }

    public int getFdcId() {
        return fdcId;
    }

    public void setFdcId(int fdcId) {
        this.fdcId = fdcId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGtinUpc() {
        return gtinUpc;
    }

    public void setGtinUpc(String gtinUpc) {
        this.gtinUpc = gtinUpc;
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

    public String humanReadable() {
        return String.format("""
                {
                \tfdcId: %d
                \tdescription: %s
                \tgtinUpc: %s
                \tpublishedDate: %s
                \tscore: %.2f
                }
                """, fdcId, description, gtinUpc, publicationDate, calories);
    }
}
